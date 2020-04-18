package com.onecoder.device.scale;

import android.Manifest;
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
import com.onecoder.devicelib.scale.api.ScaleManager;
import com.onecoder.devicelib.scale.api.entity.ScaleUser;
import com.onecoder.devicelib.utils.BleConstanst;
import com.onecoder.devicelib.utils.BluetoothUtils;
import com.onecoder.devicelib.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ScaleMainActivity extends BaseActivity implements BleScanCallBack,
        BlueToothShowAdapter.OnItemClickListner, View.OnClickListener {
    public static final int CMD_DEAL_DATA = 0;

    // 所需的全部权限
    private String[] needPermission = new String[]{
            //位置权限
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private ListView deviceLists;

    private TextView deviceConnectStatus;
    private BleScanner scanner;

    private boolean isUpdateList = false;

    private List<BluetoothBean> bluetoothBeenList = new ArrayList<>();
    private List<String> bluetoothDevAddrList = new ArrayList<>();
    private BlueToothShowAdapter showAdapter;
    private Button scanDevice;
    private String TAG = ScaleMainActivity.class.getCanonicalName();
    private Timer mUpdateDeviceListTimer;
    private MyTimerTask updateDeviceListTask;
    private ProgressDialog dialog;

    private ScaleManager scaleManager;
    private int protocolType = CommonProtocol.NEW_PROTOCOL;
    private BaseDevice scaleDevice;

    private boolean hasStartDataPage;

    @Override
    protected BleScanner getBleScanner() {
        if (scanner != null) {
            return scanner;
        }
        scanner = new BleScanner();
        //scanner.addNameFilter(DeviceType.Scale, new String[]{"ACS-903", "JJ Scale", "WS"});
        return scanner;
    }

    @Override
    protected Manager getManager(Bundle savedInstanceState) {
        if (scaleManager != null) {
            return scaleManager;
        }
        scaleManager = ScaleManager.getInstance();
        //注册状态回调
        scaleManager.registerStateChangeCallback(stateChangeCallback);
        return scaleManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_scan_act);
        hasStartDataPage = false;
        Intent intent = getIntent();
        if (intent != null) {
            protocolType = intent.getIntExtra(KEY_PROTOCOL_TYPE, CommonProtocol.NEW_PROTOCOL);
        }
        //protocolType = CommonProtocol.NEW_PROTOCOL;//CommonProtocol.NEW_PROTOCOL  OLD_PROTOCOL
        //连接前设置称的协议(分新旧两种)
        scaleManager.setProtocolType(protocolType);
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
    protected void onResume() {
        super.onResume();
//        scanner.scanLeDevice(true, this);
    }

    @Override
    protected void onStop() {
        showDialog(null, false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scaleManager.unregistStateChangeCallback(stateChangeCallback);
        scanner.stopScan();
        scaleManager.disconnect(false);
        scaleManager.closeDevice();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_device:
                showDialog("搜索设备", true);
                bluetoothBeenList.clear();
                scanEnableDevice(true);
//                scanner.scanLeDevice(true, this);
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
        if (bluetoothBeenList.isEmpty()) {
            showDialog(null, false);
        }
        LogUtils.e(TAG, BleConstanst.BLE_FLOW_KEY_SCAN,
                " name: " + bluetoothBean.getBleDevice().getName()
                        + " rssi:" + bluetoothBean.getRssi()
                        + " thred:" + Thread.currentThread().getId());
        if (deviceIsExist(bluetoothBean)) {
            return;
        }
        bluetoothBeenList.add(bluetoothBean);
        bluetoothDevAddrList.add(bluetoothBean.getBleDevice().getAddress());
        showAdapter.notifyDataSetChanged();
        if (scaleDevice != null && bluetoothBean != null && bluetoothBean.getBleDevice() != null
                && bluetoothBean.getBleDevice().getAddress().equalsIgnoreCase(scaleDevice.getMacAddress())) {
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

    /**
     * 获取连接脂肪秤所需用户信息
     *
     * @return
     */
    private ScaleUser getUserEntity() {
        ScaleUser bUserInfo = new ScaleUser();
        //设置默认用户参数
        bUserInfo.setAge(22);
        bUserInfo.setHeight(170);
        bUserInfo.setSex(0);  //用户性别  0.女  1.男
        bUserInfo.setId(2);  //秤分配的用户ID ，默认255,使用后保存秤分配的ID，再次连接时使用秤分配的ID
        bUserInfo.setUserScaleAddress(null);  //用户已绑定的称的Mac地址，若无设置为空
        bUserInfo.setUnit(0);  //單位 （0--KG   1---lb  2---ST  3----公斤）

        return bUserInfo;
    }

    @Override
    public void onItemClick(BluetoothBean device) {
        showDialog("连接设备", true);
        scanEnableDevice(false);
        BluetoothDevice bleDevice = device.getBleDevice();

        //BaseDevice scaleDevice = new BaseDevice();
        scaleDevice = new BaseDevice();
        scaleDevice.setDeviceType(DeviceType.Scale);
        scaleDevice.setMacAddress(bleDevice.getAddress());
        scaleDevice.setName(bleDevice.getName());
        connectDev();
    }

    private void connectDev() {
        scanEnableDevice(false);
        if (protocolType == CommonProtocol.OLD_PROTOCOL) {
            ScaleUser connectEntity = getUserEntity();
            //连接秤
            scaleManager.connectDevice(scaleDevice, connectEntity);
        } else if (protocolType == CommonProtocol.NEW_PROTOCOL) {
            //连接秤
            scaleManager.connectDevice(scaleDevice);
        }
    }

    public void updateConnectStatus(int status) {
        if (status >= BleDevice.STATE_CONNECTED) {
            scanEnableDevice(false);
        }

        int connectSate = R.string.un_stpes_walk;
        switch (status) {
            case BleDevice.STATE_DISCONNECTED:  //断开连接
                showDialog("连接设备", false);
                break;
            case BleDevice.STATE_CONNECTING:  //正在连接
                connectSate = R.string.device_connecting;
                break;
            case BleDevice.STATE_CONNECTED:  //已连接
                connectSate = R.string.stpes_walk;
                break;
            case BleDevice.STATE_SERVICES_DISCOVERED:  //发现服务
            case BleDevice.STATE_SERVICES_OPENCHANNELSUCCESS:  //打开通道
                showDialog("连接设备", false);
                connectSate = R.string.stpes_walk;
                break;
        }
        deviceConnectStatus.setText(getString(connectSate));

    }


    /**
     * 秤的连接状态回调
     */
    private DeviceStateChangeCallback stateChangeCallback = new DeviceStateChangeCallback() {

        /**
         * 秤的连接状态变化回调
         * @param mac ：mac地址
         * @param status ：状态值
         */
        @Override
        public void onStateChange(String mac, int status) {
            updateConnectStatus(status);
            //连接成功，通道已打开进行页面跳转，
            if (!hasStartDataPage && status == BleDevice.STATE_SERVICES_OPENCHANNELSUCCESS) {
                Intent intent = new Intent(ScaleMainActivity.this, ScaleDataActivity.class);
                ScaleMainActivity.this.startActivityForResult(intent, CMD_DEAL_DATA);
                hasStartDataPage = true;
            }
        }

        @Override
        public void onEnableWriteToDevice(String mac, boolean isNeedSetParam) {
            Log.i(TAG, "onEnableWriteToDevice mac:" + mac + " isNeedSetParam:" + isNeedSetParam);
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
        if (switchOn) {
            bluetoothBeenList.clear();
            bluetoothDevAddrList.clear();
            showAdapter.notifyDataSetChanged();
            scanEnableDevice(true);
        } else {
            scanEnableDevice(false);
//            scaleManager.disconnect(false);
//            scaleManager.closeDevice();
//            scaleManager.refreshDeviceCache();
        }
    }
}
