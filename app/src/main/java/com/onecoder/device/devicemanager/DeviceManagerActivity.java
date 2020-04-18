package com.onecoder.device.devicemanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.onecoder.device.ManagerContainer;
import com.onecoder.device.R;
import com.onecoder.device.adpater.DevManagerAdapter;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.device.base.BleDev;
import com.onecoder.device.boxing.BoxingMainActivity;
import com.onecoder.device.boxing.BoxingManagerContainer;
import com.onecoder.device.kettlebell.KettleBellMainActivity;
import com.onecoder.device.kettlebell.KettleBellManagerContainer;
import com.onecoder.device.utils.Utils;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.control.entity.BleDevice;
import com.onecoder.devicelib.base.control.entity.BluetoothBean;
import com.onecoder.devicelib.base.control.interfaces.BleScanCallBack;
import com.onecoder.devicelib.base.control.interfaces.DeviceStateChangeCallback;
import com.onecoder.devicelib.base.control.manage.BleScanner;
import com.onecoder.devicelib.base.entity.BaseDevice;
import com.onecoder.devicelib.base.entity.DeviceType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceManagerActivity extends BaseActivity implements View.OnClickListener, DialogInterface.OnClickListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, DeviceStateChangeCallback, BleScanCallBack {
    public static final String KEY_DEVICE_TYPE = "deviceType";
    private static final String TAG = DeviceManagerActivity.class.getSimpleName();
    private static final int REQUEST_CODE_ADD_DEV = 100;

    @BindView(R.id.dev_list_view)
    ListView devListView;
    @BindView(R.id.control_btn)
    Button controlBtn;
    private DevManagerAdapter devManagerAdapter;
    private List<BleDev> bleDevList;

    private DeviceType deviceType;
    private ManagerContainer managerContainer;

    private BleScanner scanner;

    private int selectedPosition;
    protected AlertDialog alertDialog;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manager);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Serializable serializable = intent.getSerializableExtra(KEY_DEVICE_TYPE);
        if (serializable instanceof DeviceType) {
            deviceType = (DeviceType) serializable;
        } else {
            finish();
        }

        alertDialog = new AlertDialog.Builder(this).setTitle("删除设备?").create();
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.common_confirm), this);
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.common_cancel), this);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setCancelable(false);

        switch (deviceType) {
            case KettleBell:
                managerContainer = KettleBellManagerContainer.getInstance();
                break;

            case Boxing:
                managerContainer = BoxingManagerContainer.getInstance();
                break;

            default:
                break;
        }
        if (managerContainer == null) {
            finish();
        }
        //setTitle(deviceType.getName());
        setTitle(Utils.getDeviceTypeName(this, deviceType));

        bleDevList = new ArrayList<>();
        devManagerAdapter = new DevManagerAdapter(this);
        devListView.setAdapter(devManagerAdapter);
        devListView.setOnItemClickListener(this);
        devListView.setOnItemLongClickListener(this);
        controlBtn.setOnClickListener(this);
        syncList(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "+");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                startScanDevPage();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * @param isScan true 扫描  false ：停止扫描
     */
    private void scanEnableDevice(boolean isScan) {
        if (isScan) {
            scanner.startScan(this);
        } else {
            scanner.stopScan();
        }
    }

    private void syncList(boolean init) {
        List<Manager> managerList = managerContainer.getManagerList();
        if (managerList == null) {
            return;
        }
        bleDevList.clear();
        BaseDevice baseDevice;
        boolean hasDevNotConnected = false;
        int connectState;
        for (Manager manager : managerList) {
            if (manager == null) {
                continue;
            }
            baseDevice = manager.getBaseDevice();
            connectState = manager.getConnectState();
            bleDevList.add(new BleDev(baseDevice, connectState));
            if (init) {
                manager.registerCheckSystemBleCallback(this);
                manager.registerStateChangeCallback(this);
                //manager.connectDevice(baseDevice);
                if (connectState < BleDevice.STATE_CONNECTING) {
                    hasDevNotConnected = true;
                }
            }
        }
        if (init && hasDevNotConnected) {
            scanEnableDevice(true);
        }
        devManagerAdapter.setBaseDeviceList(bleDevList);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.control_btn:
                List<Manager> managerList = managerContainer.getManagerList();
                if (managerList == null || managerList.size() == 0) {
                    Toast.makeText(this, "请先添加设备！", Toast.LENGTH_LONG).show();
                    break;
                }
                startControlPage();
                break;

            default:
                break;
        }
    }

    private void startScanDevPage() {
        Intent intent = new Intent(this, ScanConnectDevActivity.class);
        intent.putExtra(ScanConnectDevActivity.KEY_DEVICE_TYPE, deviceType);
        startActivityForResult(intent, REQUEST_CODE_ADD_DEV);
    }

    private void startControlPage() {
        Class cls = null;
        switch (deviceType) {
            // 手环
            case Tracker:
                //cls = TrackerMainActivity.class;
                break;

            // 秤
            case Scale:
                //cls = ScaleDataActivity.class;
                break;

            // 心率带
            case HRMonitor:
                //cls = OtherDevcieDataActivity.class;
                break;

            // 踏频
            case Cadence:
                //cls = OtherDevcieDataActivity.class;
                break;

            // 跳绳
            case Jump:
                //cls = OtherDevcieDataActivity.class;
                break;

            // 臂带
            case ArmBand:
                //cls = ArmBandMainActivity.class;
                break;

            // 壶铃
            case KettleBell:
                cls = KettleBellMainActivity.class;
                break;

            // 码表
            case BikeComputer:
                //cls = BikeComputerMainActivity.class;
                break;

            // Hub配置
            case HubConfig:
                //cls = HubConfigMainActivity.class;
                break;

            // 拳击
            case Boxing:
                cls = BoxingMainActivity.class;
                break;

            default:
                break;
        }
        if (cls == null) {
            return;
        }
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        switch (requestCode) {
            case REQUEST_CODE_ADD_DEV:
                if (resultCode == RESULT_OK) {
                    syncList(true);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object object = devManagerAdapter.getItem(position);
        if (!(object instanceof BaseDevice)) {
            return;
        }
        BaseDevice baseDevice = (BaseDevice) object;
        Manager manager = managerContainer.getManager(baseDevice.getMacAddress());
        if (manager != null) {
            if (manager.getConnectState() >= BleDevice.STATE_CONNECTING) {
                Toast.makeText(this, "连接中或已连接", Toast.LENGTH_LONG).show();
                return;
            }
            manager.connectDevice(baseDevice);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Object object = devManagerAdapter.getItem(position);
        if (!(object instanceof BaseDevice)) {
            return false;
        }
        BaseDevice baseDevice = (BaseDevice) object;
        Manager manager = managerContainer.getManager(baseDevice.getMacAddress());
        if (manager == null) {
            return false;
        }
        selectedPosition = position;
        alertDialog.show();
        return false;
    }

    @Override
    public void onStateChange(String macAddress, int status) {
        Log.i(TAG, "onStateChange macAddress:" + macAddress + " status:" + status);
        if (bleDevList == null || bleDevList.size() == 0 || macAddress == null) {
            return;
        }
        boolean needUpdateUi = false;
        for (BleDev bleDev : bleDevList) {
            if (bleDev == null || !macAddress.equalsIgnoreCase(bleDev.getMacAddress())) {
                continue;
            }
            bleDev.setConnectState(status);
            needUpdateUi = true;
        }
        if (needUpdateUi) {
            Log.i(TAG, "onStateChange needUpdateUi bleDevList:" + bleDevList);
            devManagerAdapter.setBaseDeviceList(bleDevList);
        }
    }

    @Override
    public void onEnableWriteToDevice(String mac, boolean isNeedSetaram) {
        if (bleDevList == null || bleDevList.size() == 0) {
            return;
        }
    }

    @Override
    public void findDevice(BluetoothBean bluetoothBean) {
        if (bluetoothBean == null || bluetoothBean.getBleDevice() == null || bluetoothBean.getBleDevice().getAddress() == null) {
            return;
        }
        Manager manager = managerContainer.getManager(bluetoothBean.getBleDevice().getAddress());
        if (manager == null || manager.getConnectState() >= BleDevice.STATE_CONNECTING) {
            return;
        }
        manager.connectDevice(manager.getBaseDevice());
        boolean hasDevNotConnected = false;
        for (BleDev bleDev : bleDevList) {
            if (bleDev == null) {
                continue;
            }
            if (bleDev.getConnectState() < BleDevice.STATE_CONNECTED) {
                hasDevNotConnected = true;
                break;
            }
        }
        if (!hasDevNotConnected) {
            scanEnableDevice(false);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                Object object = devManagerAdapter.getItem(selectedPosition);
                if (!(object instanceof BaseDevice)) {
                    break;
                }
                BaseDevice baseDevice = (BaseDevice) object;
                Manager manager = managerContainer.getManager(baseDevice.getMacAddress());
                if (manager == null) {
                    break;
                }
                manager.disconnect(false);
                managerContainer.getManagerList().remove(manager);
                syncList(false);
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                break;

            default:
                break;
        }
    }

    @Override
    public void onBleSwitchedBySystem(boolean switchOn) {
        super.onBleSwitchedBySystem(switchOn);
        if (switchOn) {
            syncList(true);
        } else {
            scanEnableDevice(false);
            syncList(false);
        }
    }

    @Override
    protected void onDestroy() {
        if (managerContainer != null) {
            List<Manager> managerList = managerContainer.getManagerList();
            if (managerList != null) {
                for (Manager manager : managerList) {
                    if (manager == null) {
                        continue;
                    }
                    Log.i(TAG, "onDestroy to unregisterCheckSystemBleCallback");
                    manager.unregisterCheckSystemBleCallback(this);
                    manager.unregistStateChangeCallback(this);
                    manager.disconnect(false);
                }
            }
        }
        super.onDestroy();
    }

}
