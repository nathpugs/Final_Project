package com.onecoder.device.tracker;

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
import com.onecoder.devicelib.base.protocol.CommonProtocol;
import com.onecoder.devicelib.tracker.api.TrackerManager;
import com.onecoder.devicelib.tracker.api.entity.TrackerUser;
import com.onecoder.devicelib.utils.BleConstanst;
import com.onecoder.devicelib.utils.BluetoothUtils;
import com.onecoder.devicelib.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TrackerScanActivity extends BaseActivity implements BleScanCallBack,
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
    private String TAG = TrackerScanActivity.class.getCanonicalName();
    private Timer mUpdateDeviceListTimer;
    private MyTimerTask updateDeviceListTask;
    private ProgressDialog dialog;

    /**
     * 手环协议类型
     * Tracker protocol type
     */
    private int protocolType = CommonProtocol.NEW_PROTOCOL;

    private TrackerManager trackerManager;
    private BaseDevice trackerDevice;

    private boolean hasStartDataPage;

    @Override
    protected BleScanner getBleScanner() {
        if (scanner != null) {
            return scanner;
        }
        scanner = new BleScanner();
        //添加需要扫描出来的手环，不添加默认扫描所有的设备
        //Add mapping relation between device type and device name, or BleScanner will list every ble device it scanned as default option.
        //scanner.addNameFilter(DeviceType.Tracker, new String[]{"MOVE", "Track-ST", "ACS-605", "HW", "PE841B"});
        return scanner;
    }

    @Override
    protected Manager getManager(Bundle savedInstanceState) {
        if (trackerManager != null) {
            return trackerManager;
        }
        trackerManager = TrackerManager.getInstance();
        //注册状态回调
        //Register connection status call-back
        trackerManager.registerStateChangeCallback(stateChangeCallback);
        return trackerManager;
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

        Intent intent = getIntent();
        if (intent != null) {
            protocolType = intent.getIntExtra(KEY_PROTOCOL_TYPE, CommonProtocol.NEW_PROTOCOL);
        }
        //连接前设置称的协议(分新旧两种),这里以新协议称为例进行说明
        //Set the protocol type(new or old protocol type) before connect to device.
        //Here, take the new protocol type as an example.
        //protocolType = CommonProtocol.OLD_PROTOCOL;
        //protocolType = CommonProtocol.NEW_PROTOCOL;
        trackerManager.setProtocolType(protocolType);
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
        trackerManager.unregistStateChangeCallback(stateChangeCallback);
        scanEnableDevice(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_device:
                bluetoothBeenList.clear();
                //scan device
                showDialog(getString(R.string.scan_dev), true);
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
     * 扫描到设备后的回调
     * Callback after scanned to the device
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
        if (trackerDevice != null && bluetoothBean != null && bluetoothBean.getBleDevice() != null
                && bluetoothBean.getBleDevice().getAddress().equalsIgnoreCase(trackerDevice.getMacAddress())) {
            connectDev();
        }
    }

    /**
     * @param isScan true 扫描 start scan  false ：stop scan
     */
    private void scanEnableDevice(boolean isScan) {
        Log.i(TAG, "scanEnableDevice isScan:" + isScan);
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
     * Determine whether the device has been scanned
     *
     * @param bluetoothBean
     * @return
     */
    private boolean deviceIsExist(BluetoothBean bluetoothBean) {
        return bluetoothBean != null && bluetoothDevAddrList != null
                && bluetoothDevAddrList.contains(bluetoothBean.getBleDevice().getAddress());
    }

    /**
     * 获取连接脂肪秤所需用户信息
     * Get the user information needed to connect the fat scale.
     *
     * @return
     */
    private TrackerUser getUserEntity() {
        TrackerUser bUserInfo = new TrackerUser();
        //设置默认用户参数
        //Set default user parameters.
        bUserInfo.setAge(22);
        bUserInfo.setHeight(170);
        bUserInfo.setSex(0);  //用户性别  0.女  1.男  sex 0:male a:female
        bUserInfo.setTarget(1000); //设置用户的步行目标 Set the user's walking goal.
        return bUserInfo;
    }

    @Override
    public void onItemClick(BluetoothBean device) {
        //Connecting the device...
        showDialog(getString(R.string.connecting), true);
        BluetoothDevice bleDevice = device.getBleDevice();
        trackerDevice = new BaseDevice();
        //设置设备类型 Set device type.
        trackerDevice.setDeviceType(DeviceType.Tracker);
        trackerDevice.setMacAddress(bleDevice.getAddress());
        trackerDevice.setName(bleDevice.getName());
        connectDev();
    }

    /**
     * ble被系统打开/关闭
     * Ble is turned on/off by the system.
     *
     * @param switchOn true:开启,即蓝牙由关闭状态切换至开启状态，此时需用户自己连接设备
     *                 Turn on, that is, Bluetooth is switched from off state to on state, at which time users need to connect device themselves.
     *                 false:关闭  Turn off
     */
    @Override
    public void onBleSwitchedBySystem(boolean switchOn) {
        super.onBleSwitchedBySystem(switchOn);
        Log.i(TAG, "onBleSwitchedBySystem switchOn:" + switchOn + " needSysBlePaired:" + trackerManager.getNeedSysBlePaired());
        if (switchOn) {
            bluetoothBeenList.clear();
            bluetoothDevAddrList.clear();
            showAdapter.notifyDataSetChanged();
            if (trackerManager.getNeedSysBlePaired() != null && trackerManager.getNeedSysBlePaired()) {
                // 旧手环需要与系统配对。
                // 重连与系统配对的设备时可以直连成功，且系统原生的蓝牙扫描API无法扫描到配对设备，所以需要直连。
                //Old tracker need to be paired with the system.
                //When reconnecting the device paired with the system, it can be directly connected successfully,
                //and the original Bluetooth scanning API of the system can not scan the paired device,
                //so direct connection is needed.
                connectDev();
            } else {
                scanEnableDevice(true);
            }
        } else {
            scanEnableDevice(false);
//            trackerManager.disconnect(false);
//            trackerManager.closeDevice();
//            trackerManager.refreshDeviceCache();
        }
    }

    private void connectDev() {
        Log.i(TAG, "connectDev");
        scanEnableDevice(false);
        if (protocolType == CommonProtocol.OLD_PROTOCOL) {
            TrackerUser connectEntity = getUserEntity();
            //连接旧协议手环
            //Connect the old protocol tracker.
            trackerManager.connectDevice(trackerDevice, connectEntity);
        } else if (protocolType == CommonProtocol.NEW_PROTOCOL) {
            //连接新协议手环
            //Connect the new protocol tracker.
            trackerManager.connectDevice(trackerDevice);
        }
    }

    public void updateConnectStatus(int status) {
        if (status >= BleDevice.STATE_CONNECTED) {
            scanEnableDevice(false);
        }

        int connectSate = R.string.un_stpes_walk;
        switch (status) {
            case BleDevice.STATE_DISCONNECTED:  //断开连接 Disconnect the connection.
                break;
            case BleDevice.STATE_CONNECTING:  //正在连接 connecting
                connectSate = R.string.device_connecting;
                break;
            case BleDevice.STATE_CONNECTED:  //已连接 connected
                connectSate = R.string.stpes_walk;
                break;
            case BleDevice.STATE_SERVICES_DISCOVERED:  //发现服务  services was found.
                showDialog(null, false);
                connectSate = R.string.stpes_walk;
                break;

            case BleDevice.STATE_SERVICES_OPENCHANNELSUCCESS://通道打开成功 The channel was opened successfully.
                connectSate = R.string.stpes_walk;
                break;
        }
        deviceConnectStatus.setText(connectSate);
    }


    /**
     * 手环的连接状态回调
     * device connection status call-back
     */
    private DeviceStateChangeCallback stateChangeCallback = new DeviceStateChangeCallback() {
        /**
         * 手环的连接状态变化回调
         * device connection state change call-back
         * @param mac ：mac地址 mac address
         * @param status ：状态值 Status value
         */
        @Override
        public void onStateChange(String mac, int status) {
            updateConnectStatus(status);
            //连接成功，通道已打开进行页面跳转
            //Connect successfully and channel opened, then could get into main page.
            if (!hasStartDataPage && status == BleDevice.STATE_SERVICES_OPENCHANNELSUCCESS) {
                Intent intent = new Intent(TrackerScanActivity.this, TrackerMainActivity.class);
                intent.putExtra(KEY_BASE_DEVICE, trackerDevice);
                intent.putExtra(TrackerMainActivity.KEY_PROTOCOL_TYPE, protocolType);
                startActivityForResult(intent, CMD_DEAL_DATA);
                hasStartDataPage = true;
            }
        }

        /**
         * 手环可以下发数据的回调，新协议手环可以在此时设置用户信息、闹钟、健康开关等
         * Tracker can be used to send data callback. The new protocol tracker can be set user information, alarm clock, health switch and so on at this time.
         * @param mac
         * @param isNeedSetParam
         */
        @Override
        public void onEnableWriteToDevice(String mac, boolean isNeedSetParam) {
            Log.i(TAG, "onEnableWriteToDevice mac:" + mac + " isNeedSetParam:" + isNeedSetParam);
            // 设置用户信息
            //set user info
            //  trackerManager.setUserBodyInfo();
            // 设置闹钟
            //set alarm clock
            //  trackerManager.setAlarmPlan();
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

}
