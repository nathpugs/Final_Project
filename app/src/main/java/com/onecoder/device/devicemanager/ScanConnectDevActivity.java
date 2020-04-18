package com.onecoder.device.devicemanager;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.onecoder.device.ManagerContainer;
import com.onecoder.device.R;
import com.onecoder.device.adpater.BlueToothShowAdapter;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.device.boxing.BoxingManagerContainer;
import com.onecoder.device.kettlebell.KettleBellManagerContainer;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.control.entity.BleDevice;
import com.onecoder.devicelib.base.control.entity.BluetoothBean;
import com.onecoder.devicelib.base.control.interfaces.BleScanCallBack;
import com.onecoder.devicelib.base.control.interfaces.DeviceStateChangeCallback;
import com.onecoder.devicelib.base.control.manage.BleScanner;
import com.onecoder.devicelib.base.entity.BaseDevice;
import com.onecoder.devicelib.base.entity.DeviceType;
import com.onecoder.devicelib.utils.BleConstanst;
import com.onecoder.devicelib.utils.LogUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScanConnectDevActivity extends BaseActivity implements BleScanCallBack,
        BlueToothShowAdapter.OnItemClickListner, View.OnClickListener {
    public static final int CMD_DEAL_DATA = 0;
    public static final String KEY_DEVICE_TYPE = "deviceType";
    private static final String TAG = ScanConnectDevActivity.class.getSimpleName();

    private ListView deviceLists;

    private TextView deviceConnectStatus;
    private BleScanner scanner;

    private List<BluetoothBean> bluetoothBeenList = new ArrayList<>();
    private List<String> bluetoothDevAddrList = new ArrayList<>();
    private BlueToothShowAdapter showAdapter;
    private Button scanDevice;
    private ProgressDialog dialog;

    private DeviceType deviceType;
    private Manager manager;
    private BaseDevice baseDevice;

    private boolean connecting;
    private boolean channelOpened;

    @Override
    protected BleScanner getBleScanner() {
        if (scanner != null) {
            return scanner;
        }
        scanner = new BleScanner();
        //添加需要扫描出来的手环，不添加默认扫描所有的设备
        //scanner.addNameFilter(DeviceType.ArmBand, new String[]{"HW"});
        return scanner;
    }

    @Override
    protected Manager getManager(Bundle savedInstanceState) {
        if (manager != null) {
            return manager;
        }
        manager = initManager(deviceType);
        //注册状态回调
        manager.registerStateChangeCallback(stateChangeCallback);
        return manager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        Serializable serializable = intent.getSerializableExtra(KEY_DEVICE_TYPE);
        if (serializable instanceof DeviceType) {
            deviceType = (DeviceType) serializable;
        } else {
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_scan_act);
        setTitle("扫描及连接");
        channelOpened = false;

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

    private Manager initManager(DeviceType deviceType) {
        return ManagerContainer.instanceManager(deviceType);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        if (connecting) {
            Toast.makeText(this, "连接中，请等待！", Toast.LENGTH_LONG).show();
            return;
        }
        if (channelOpened) {
            Toast.makeText(this, "设备已连接！", Toast.LENGTH_LONG).show();
            return;
        }

        showDialog("设备连接", true);
        BluetoothDevice bleDevice = device.getBleDevice();
        baseDevice = new BaseDevice();
        //设置设备类型
        baseDevice.setDeviceType(DeviceType.ArmBand);
        baseDevice.setMacAddress(bleDevice.getAddress());
        baseDevice.setName(bleDevice.getName());
        connectDev();
        connecting = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.connect_disconnect_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (baseDevice == null) {
            Toast.makeText(this, "请选择设备！", Toast.LENGTH_LONG).show();
            return super.onOptionsItemSelected(item);
        }

        int id = item.getItemId();
        switch (id) {
            case R.id.main_menu_disconnect:
                if (manager.getConnectState() >= BleDevice.STATE_CONNECTED) {
                    manager.disconnect(false);
                    connecting = false;
                }
                break;
            case R.id.main_menu_connect:
                if (manager.getConnectState() < BleDevice.STATE_CONNECTED) {
                    manager.connectDevice(baseDevice);
                    connecting = true;
                }
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
        }
    }

    private void connectDev() {
        scanEnableDevice(false);
        manager.connectDevice(baseDevice);
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
            if (mac == null || baseDevice == null || !mac.equalsIgnoreCase(baseDevice.getMacAddress())) {
                return;
            }
            if (status >= BleDevice.STATE_CONNECTING) {
                connecting = false;
            }
            updateConnectStatus(status);
            //连接成功，通道已打开进行页面跳转
            if (!channelOpened && status == BleDevice.STATE_SERVICES_OPENCHANNELSUCCESS) {
                showDialog(null, false);
                ManagerContainer managerContainer;
                switch (deviceType) {
                    case KettleBell:
                        managerContainer = KettleBellManagerContainer.getInstance();
                        break;

                    case Boxing:
                        managerContainer = BoxingManagerContainer.getInstance();
                        break;

                    default:
                        return;
                }
                channelOpened = true;
                if (managerContainer != null && !managerContainer.contains(manager)) {
                    managerContainer.getManagerList().add(manager);
                    setResult(RESULT_OK);
                    finish();
                }
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

    @Override
    protected void onStop() {
        showDialog(null, false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        manager.unregistStateChangeCallback(stateChangeCallback);
        scanEnableDevice(false);
        super.onDestroy();
    }

}
