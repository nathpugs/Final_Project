package com.onecoder.device.bikecomputer;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.onecoder.device.R;
import com.onecoder.device.adpater.BlueToothShowAdapter;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.control.entity.BleDevice;
import com.onecoder.devicelib.base.control.entity.BluetoothBean;
import com.onecoder.devicelib.base.control.interfaces.BleScanCallBack;
import com.onecoder.devicelib.base.control.interfaces.DeviceStateChangeCallback;
import com.onecoder.devicelib.base.control.manage.BleScanner;
import com.onecoder.devicelib.base.entity.BaseDevice;
import com.onecoder.devicelib.base.entity.DeviceType;
import com.onecoder.devicelib.base.protocol.interfaces.GetDeviceVersionCallback;
import com.onecoder.devicelib.bikecomputer.api.BikeComputerManager;
import com.onecoder.devicelib.utils.BleConstanst;
import com.onecoder.devicelib.utils.BluetoothUtils;
import com.onecoder.devicelib.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class BikeComputerScanActivity extends BaseActivity implements BleScanCallBack,
        BlueToothShowAdapter.OnItemClickListner, View.OnClickListener {
    public static final int CMD_DEAL_DATA = 0;

    private ListView deviceLists;

    private TextView deviceConnectStatus;
    private BleScanner scanner;

    private boolean isUpdateList = false;

    private List<BluetoothBean> bluetoothBeenList = new ArrayList<>();
    private List<String> bluetoothDevAddrList = new ArrayList<>();
    private BlueToothShowAdapter showAdapter;
    private Button scanDevice;
    private String TAG = BikeComputerScanActivity.class.getCanonicalName();
    private ProgressDialog dialog;

    private BikeComputerManager bikeComputerManager;
    private BaseDevice baseDevice;

    private boolean hasStartDataPage;
    private int deviceVersion;

    @Override
    protected BleScanner getBleScanner() {
        if (scanner != null) {
            return scanner;
        }
        scanner = new BleScanner();
        //添加需要扫描出来的设备，不添加默认扫描所有的设备
        //scanner.addNameFilter(DeviceType.BikeComputer, new String[]{"BC"});
        return scanner;
    }

    @Override
    protected Manager getManager(Bundle savedInstanceState) {
        if (bikeComputerManager != null) {
            return bikeComputerManager;
        }
        bikeComputerManager = BikeComputerManager.getInstance();
        //注册状态回调
        bikeComputerManager.registerStateChangeCallback(stateChangeCallback);
        bikeComputerManager.registerGetDeviceVersionCallback(getDeviceVersionCallback);

        return bikeComputerManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_scan_act);
        hasStartDataPage = false;

        deviceLists = (ListView) findViewById(R.id.showBlueTooth);
        deviceConnectStatus = (TextView) findViewById(R.id.device_connect_status);
        scanDevice = (Button) findViewById(R.id.scan_device);
        scanDevice.setOnClickListener(this);

        dialog = new ProgressDialog(this);
        showAdapter = new BlueToothShowAdapter(this);
        showAdapter.setBluetoothList(bluetoothBeenList);
        showAdapter.setItemClickListner(this);
        deviceLists.setAdapter(showAdapter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_device:
                bluetoothBeenList.clear();
                showDialog("搜索设备", true);
                scanEnableDevice(true);
                break;
        }
    }

    private void showDialog(String meassge, boolean isShow) {
        if (isShow && !dialog.isShowing()) {
            dialog.setMessage(meassge);
            dialog.show();
            return;
        }
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * 扫描到设备后回调
     *
     * @param bluetoothBean
     */
    @Override
    public void findDevice(BluetoothBean bluetoothBean) {
        LogUtils.e(TAG, BleConstanst.BLE_FLOW_KEY_SCAN, "    " + bluetoothBean.toString());
        if (bluetoothBeenList.isEmpty()) {
            showDialog(null, false);
        }
        if (deviceIsExist(bluetoothBean)) {
            return;
        }
        bluetoothBeenList.add(bluetoothBean);
        bluetoothDevAddrList.add(bluetoothBean.getBleDevice().getAddress());
        showAdapter.notifyDataSetChanged();
        if (baseDevice != null && bluetoothBean != null && bluetoothBean.getBleDevice() != null
                && bluetoothBean.getBleDevice().getAddress().equalsIgnoreCase(baseDevice.getMacAddress())) {
            connectDev();
        }
    }

    /**
     * @param isScan true 扫描  false ：停止扫描
     */
    private void scanEnableDevice(boolean isScan) {
        if (scanner == null) {
            return;
        }
        if (isScan) {
            scanner.startScan(this);
        } else {
            scanner.stopScan();
        }
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            isUpdateList = true;
        }
    }


    /**
     * 判断设备是否已扫描到
     *
     * @param bluetoothBean
     * @return
     */
    private boolean deviceIsExist(BluetoothBean bluetoothBean) {
        return bluetoothBean != null && bluetoothDevAddrList != null
                && bluetoothDevAddrList.contains(bluetoothBean.getBleDevice().getAddress());
    }

    @Override
    public void onItemClick(BluetoothBean device) {
        showDialog("设备连接", true);
        BluetoothDevice bleDevice = device.getBleDevice();
        baseDevice = new BaseDevice();
        //设置设备类型
        baseDevice.setDeviceType(DeviceType.BikeComputer);
        baseDevice.setMacAddress(bleDevice.getAddress());
        baseDevice.setName(bleDevice.getName());
        connectDev();
    }

    /**
     * ble被系统打开/关闭
     *
     * @param switchOn true:开启,即蓝牙由关闭状态切换至开启状态，此时需用户自己连接设备 false:关闭
     */
    @Override
    public void onBleSwitchedBySystem(boolean switchOn) {
        super.onBleSwitchedBySystem(switchOn);
        if (switchOn) {
            bluetoothBeenList.clear();
            bluetoothDevAddrList.clear();
            showAdapter.notifyDataSetChanged();
            scanEnableDevice(true);
        } else {
            scanEnableDevice(false);
//            bikeComputerManager.disconnect(false);
//            bikeComputerManager.closeDevice();
//            bikeComputerManager.refreshDeviceCache();
        }
    }

    private void connectDev() {
        scanEnableDevice(false);
        bikeComputerManager.connectDevice(baseDevice);
    }

    public void updateConnectStatus(int status) {
        if (status >= BleDevice.STATE_CONNECTED) {
            scanEnableDevice(false);
        }

        int connectSate = R.string.un_stpes_walk;
        switch (status) {
            case BleDevice.STATE_DISCONNECTED:  //断开连接
                break;
            case BleDevice.STATE_CONNECTING:  //正在连接
                connectSate = R.string.device_connecting;
                break;
            case BleDevice.STATE_CONNECTED:  //已连接
                connectSate = R.string.stpes_walk;
                break;
            case BleDevice.STATE_SERVICES_DISCOVERED:  //发现服务
                showDialog(null, false);
                connectSate = R.string.stpes_walk;
                break;

            case BleDevice.STATE_SERVICES_OPENCHANNELSUCCESS:
                connectSate = R.string.stpes_walk;
                break;
        }
        deviceConnectStatus.setText(getString(connectSate));
    }


    /**
     * 设备的连接状态回调
     */
    private DeviceStateChangeCallback stateChangeCallback = new DeviceStateChangeCallback() {
        /**
         * 设备的连接状态变化回调
         * @param mac ：mac地址
         * @param status ：状态值
         */
        @Override
        public void onStateChange(String mac, int status) {
            updateConnectStatus(status);
            //连接成功，通道已打开进行页面跳转，
            if (!hasStartDataPage && status == BleDevice.STATE_SERVICES_OPENCHANNELSUCCESS) {
                Intent intent = new Intent(BikeComputerScanActivity.this, BikeComputerMainActivity.class);
                intent.putExtra(KEY_BASE_DEVICE, baseDevice);
                intent.putExtra(BikeComputerMainActivity.KEY_DEVICE_VERSION, deviceVersion);
                startActivityForResult(intent, CMD_DEAL_DATA);
                hasStartDataPage = true;
            }
        }

        /**
         * 设备可以下发数据的回调
         * @param mac
         * @param isNeedSetParam
         */
        @Override
        public void onEnableWriteToDevice(String mac, boolean isNeedSetParam) {
            Log.i(TAG, "onEnableWriteToDevice mac:" + mac + " isNeedSetParam:" + isNeedSetParam);
        }
    };


    /**
     * 获取硬件版本回调
     */
    private GetDeviceVersionCallback getDeviceVersionCallback = new GetDeviceVersionCallback() {

        /**
         * 获取到硬件版本
         *
         * @param deviceVersion 硬件版本
         */
        @Override
        public void onGetDeviceVersion(String mac, int deviceVersion) {
            Log.i(TAG, "onGetDeviceVersion deviceVersion:" + deviceVersion);
            BikeComputerScanActivity.this.deviceVersion = deviceVersion;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CMD_REQUEST_ENABLE_BLE:
                if (!BluetoothUtils.isBluetoothEnabled()) {
                    showDialog(null, false);
                }
                break;

            case CMD_DEAL_DATA:
                hasStartDataPage = false;
                break;

            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        showDialog(null, false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bikeComputerManager.unregistStateChangeCallback(stateChangeCallback);
        bikeComputerManager.unregisterGetDeviceVersionCallback(getDeviceVersionCallback);
        scanEnableDevice(false);
    }
}
