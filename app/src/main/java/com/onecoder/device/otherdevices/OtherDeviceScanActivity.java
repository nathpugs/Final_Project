package com.onecoder.device.otherdevices;

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
import com.onecoder.device.broadcastingscale.BroadcastingActivity;
import com.onecoder.device.utils.GToast;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.control.entity.BleDevice;
import com.onecoder.devicelib.base.control.entity.BluetoothBean;
import com.onecoder.devicelib.base.control.interfaces.BleScanCallBack;
import com.onecoder.devicelib.base.control.interfaces.DeviceStateChangeCallback;
import com.onecoder.devicelib.base.control.manage.BleScanner;
import com.onecoder.devicelib.base.entity.BaseDevice;
import com.onecoder.devicelib.base.entity.DeviceType;
import com.onecoder.devicelib.cadence.api.CadenceManager;
import com.onecoder.devicelib.heartrate.api.HeartRateMonitorManager;
import com.onecoder.devicelib.jump.api.JumpManager;
import com.onecoder.devicelib.utils.BleConstanst;
import com.onecoder.devicelib.utils.BluetoothUtils;
import com.onecoder.devicelib.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class OtherDeviceScanActivity extends BaseActivity implements BleScanCallBack,
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
    private String TAG = OtherDeviceScanActivity.class.getCanonicalName();
    private Timer mUpdateDeviceListTimer;
    private MyTimerTask updateDeviceListTask;
    private ProgressDialog dialog;

    /**
     * 手环协议
     */
    private int protocolType;

    private Manager manager;
    private BaseDevice device;

    private boolean hasStartDataPage;

    @Override
    protected BleScanner getBleScanner() {
        if (scanner == null) {
            scanner = new BleScanner();
        }
        return scanner;
    }

    @Override
    protected Manager getManager(Bundle savedInstanceState) {
        if (manager != null) {
            return manager;
        }
        device = (BaseDevice) getIntent().getSerializableExtra(KEY_BASE_DEVICE);
        if (device == null || device.getDeviceType() == null) {
            GToast.show(this, "Device type is invalid");
            finish();
        }
        switch (device.getDeviceType()) {
            case HRMonitor:
                manager = HeartRateMonitorManager.getInstance();
                break;
            case Cadence:
                manager = CadenceManager.getInstance();
                break;
            case Jump:
                manager = JumpManager.getInstance();
                break;
            case BroadcastingScale:
                break;

            default:
                GToast.show(this, "Device type is invalid");
                finish();
                break;
        }
        return manager;
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

        switch (device.getDeviceType()) {
            case Jump:
                //添加需要扫描出来的跳绳
                //scanner.addNameFilter(DeviceType.Jump, new String[]{"SKIPPING ROPE"});
                break;

            case Cadence:
                //添加需要扫描出来的踏频
                //scanner.addNameFilter(DeviceType.Cadence, new String[]{"BK"});
                break;

            case HRMonitor:
                //添加需要扫描出来的心率带
                //scanner.addNameFilter(DeviceType.HRMonitor, new String[]{"ACCURO HRM", "JJ HRM"});
                break;

            case BroadcastingScale://蓝牙广播秤
                break;

            default:
                GToast.show(this, "Device type is invalid");
                finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        showDialog(null, false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.unregistStateChangeCallback(stateChangeCallback);
            manager.disconnect(false);
            manager.closeDevice();
        }
        scanEnableDevice(false);
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
        if (device != null && bluetoothBean != null && bluetoothBean.getBleDevice() != null
                && bluetoothBean.getBleDevice().getAddress().equalsIgnoreCase(device.getMacAddress()) && manager != null) {
            scanEnableDevice(false);
            manager.connectDevice(device);
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
    public void onItemClick(BluetoothBean bluetoothBean) {
        if (bluetoothBean == null) {
            return;
        }
        showDialog("设备连接", true);
        scanEnableDevice(false);
        BluetoothDevice bleDevice = bluetoothBean.getBleDevice();
        //device = new BaseDevice();
        //设置设备类型
        //device.setDeviceType(device.getDeviceType());
        device.setMacAddress(bleDevice.getAddress());
        device.setName(bleDevice.getName());
        Log.i(TAG, "onItemClick DeviceType:" + device.getDeviceType());
        if (manager != null) {
            // 注册状态回调接口
            manager.registerStateChangeCallback(stateChangeCallback);
            manager.connectDevice(device);
        } else if (device.getDeviceType() == DeviceType.BroadcastingScale) {
            Intent intent = new Intent(this, BroadcastingActivity.class);
            intent.putExtra(BroadcastingActivity.KEY_MAC, device.getMacAddress());
            startActivity(intent);
            hasStartDataPage = true;
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
            case BleDevice.STATE_SERVICES_OPENCHANNELSUCCESS:  //打开通道
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
            if (!hasStartDataPage && (device.getDeviceType() == DeviceType.HRMonitor ?
                    status == BleDevice.STATE_CONNECTED : status == BleDevice.STATE_SERVICES_OPENCHANNELSUCCESS)) {
                Intent intent = new Intent(OtherDeviceScanActivity.this, OtherDevcieDataActivity.class);
                intent.putExtra(KEY_BASE_DEVICE, device);
                startActivityForResult(intent, CMD_DEAL_DATA);
                hasStartDataPage = true;
            }
        }

        @Override
        public void onEnableWriteToDevice(String mac, boolean isNeedSetParam) {
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

    /**
     * ble被系统打开/关闭
     *
     * @param switchOn true:开启,即蓝牙由关闭状态切换至开启状态，此时需用户自己连接设备 false:关闭
     */
    @Override
    public void onBleSwitchedBySystem(boolean switchOn) {
        super.onBleSwitchedBySystem(switchOn);
        if (device.getDeviceType() == DeviceType.BroadcastingScale && hasStartDataPage) {
            return;
        }
        if (switchOn) {
            bluetoothBeenList.clear();
            bluetoothDevAddrList.clear();
            showAdapter.notifyDataSetChanged();
            scanEnableDevice(true);
        } else {
            scanEnableDevice(false);
//            manager.disconnect(false);
//            manager.closeDevice();
//            manager.refreshDeviceCache();
        }
    }
}
