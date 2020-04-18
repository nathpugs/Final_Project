package com.onecoder.device.hubconfig;

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
import com.onecoder.devicelib.hubconfig.api.HubConfigManager;
import com.onecoder.devicelib.utils.BleConstanst;
import com.onecoder.devicelib.utils.BluetoothUtils;
import com.onecoder.devicelib.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class HubConfigScanActivity extends BaseActivity implements BleScanCallBack,
        BlueToothShowAdapter.OnItemClickListner, View.OnClickListener {
    public static final String KEY_IS_FULL_FUNCTION = HubConfigMainActivity.KEY_IS_FULL_FUNCTION;
    public static final int CMD_DEAL_DATA = 0;

    private ListView deviceLists;
    private TextView deviceConnectStatus;
    private boolean isFullFunction;

    private BleScanner scanner;

    private List<BluetoothBean> bluetoothBeenList = new ArrayList<>();
    private List<String> bluetoothDevAddrList = new ArrayList<>();
    private BlueToothShowAdapter showAdapter;
    private Button scanDevice;
    private String TAG = HubConfigScanActivity.class.getCanonicalName();
    private ProgressDialog dialog;

    private HubConfigManager hubConfigManager;
    private BaseDevice baseDevice;

    private boolean hasStartDataPage;
    private int protocolVersion;

    @Override
    protected BleScanner getBleScanner() {
        if (scanner != null) {
            return scanner;
        }
        scanner = new BleScanner();
        //添加需要扫描出来的手环，不添加默认扫描所有的设备
        //scanner.addNameFilter(DeviceType.HubConfig, new String[]{"RC"});
        return scanner;
    }

    @Override
    protected Manager getManager(Bundle savedInstanceState) {
        if (hubConfigManager != null) {
            return hubConfigManager;
        }
        hubConfigManager = HubConfigManager.getInstance();
        //注册状态回调
        hubConfigManager.registerStateChangeCallback(stateChangeCallback);
        //注册获取设备版本号回调
        hubConfigManager.registerGetDeviceVersionCallback(getDeviceVersionCallback);
        return hubConfigManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_scan_act);
        hasStartDataPage = false;
        isFullFunction = getIntent().getBooleanExtra(KEY_IS_FULL_FUNCTION, false);

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
        LogUtils.e(TAG, BleConstanst.BLE_FLOW_KEY_SCAN, "    " + bluetoothBean.toString()
                + " " + Thread.currentThread().getId() + " " + Thread.currentThread().getName());
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
        baseDevice.setDeviceType(DeviceType.HubConfig);
        baseDevice.setMacAddress(bleDevice.getAddress());
        baseDevice.setName(bleDevice.getName());
        connectDev();
    }

    private void connectDev() {
        scanEnableDevice(false);
        hubConfigManager.connectDevice(baseDevice);
    }

    /**
     * ble被系统打开/关闭
     *
     * @param switchOn true:开启,即蓝牙由关闭状态切换至开启状态，此时需用户自己连接设备 false:关闭
     */
    @Override
    public void onBleSwitchedBySystem(final boolean switchOn) {
        super.onBleSwitchedBySystem(switchOn);
        if (switchOn) {
            bluetoothBeenList.clear();
            bluetoothDevAddrList.clear();
            showAdapter.notifyDataSetChanged();
            Log.i(TAG, "onBleSwitchedBySystem to scanEnableDevice(true)");
            scanEnableDevice(true);
        } else {
            Log.i(TAG, "onBleSwitchedBySystem to scanEnableDevice(false)");
            scanEnableDevice(false);
//            hubConfigManager.disconnect(false);
//            hubConfigManager.closeDevice();
//            hubConfigManager.refreshDeviceCache();
        }
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
     * 手环的连接状态回调
     */
    private DeviceStateChangeCallback stateChangeCallback = new DeviceStateChangeCallback() {
        /**
         * 手环的连接状态变化回调
         * @param mac ：mac地址
         * @param status ：状态值
         */
        @Override
        public void onStateChange(String mac, int status) {
            updateConnectStatus(status);
            //连接成功，通道已打开进行页面跳转，
            if (!hasStartDataPage && status == BleDevice.STATE_SERVICES_OPENCHANNELSUCCESS) {
                Intent intent = new Intent(HubConfigScanActivity.this, HubConfigMainActivity.class);
                intent.putExtra(KEY_BASE_DEVICE, baseDevice);
                intent.putExtra(HubConfigMainActivity.KEY_PROTOCOL_VERSION, protocolVersion);
                intent.putExtra(HubConfigMainActivity.KEY_IS_FULL_FUNCTION, isFullFunction);
                startActivityForResult(intent, CMD_DEAL_DATA);
                hasStartDataPage = true;
            }
        }

        /**
         * 手环可以下发数据的回调，新协议称可以在此时设置用户信息、闹钟、健康开关等
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
            Log.i(TAG, "onGetDeviceVersion protocolVersion:" + deviceVersion);
            HubConfigScanActivity.this.protocolVersion = deviceVersion;
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
        hubConfigManager.unregistStateChangeCallback(stateChangeCallback);
        hubConfigManager.unregisterGetDeviceVersionCallback(getDeviceVersionCallback);
        scanEnableDevice(false);
        super.onDestroy();
    }
}
