package com.onecoder.device.armband;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.onecoder.device.R;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.devicelib.armband.api.ArmBandManager;
import com.onecoder.devicelib.armband.api.entity.HistoryDataEntity;
import com.onecoder.devicelib.armband.api.entity.StepFrequencyEntity;
import com.onecoder.devicelib.armband.api.interfaces.RealTimeDataListener;
import com.onecoder.devicelib.armband.api.interfaces.SynchHistoryDataCallBack;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.control.entity.BleDevice;
import com.onecoder.devicelib.base.control.interfaces.DeviceStateChangeCallback;
import com.onecoder.devicelib.base.entity.BaseDevice;
import com.onecoder.devicelib.base.protocol.entity.HeartRateData;
import com.onecoder.devicelib.base.protocol.entity.PureStepHistoryData;
import com.onecoder.devicelib.base.protocol.entity.RTHeartRate;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/9/16.
 */
public class ArmBandMainActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = ArmBandMainActivity.class.getSimpleName();
    @BindView(R.id.tv_battery_level)
    TextView tvBatteryLevel;
    @BindView(R.id.get_battery_level_btn)
    Button getBatteryLevelBtn;
    private BaseDevice baseDevice;

    private ProgressDialog dialog;
    private TextView tvHardwareVersion;
    private TextView tvCurrentHeartRate;
    private TextView historyData;
    private TextView tvCurrentStepFrequency;
    private Button btnSyncData;
    private Button btnSetUtc;

    private ArmBandManager armBandManager;

    @Override
    protected Manager getManager(Bundle savedInstanceState) {
        if (armBandManager != null) {
            return armBandManager;
        }
        armBandManager = ArmBandManager.getInstance();
        //注册状态回调
        armBandManager.registerStateChangeCallback(stateChangeCallback);
        // 注册实时数据回调
        armBandManager.registerRealTimeDataListner(realTimeDataListener);
        return armBandManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.armband_main_act);
        ButterKnife.bind(this);

        //requestPermisson();
        baseDevice = (BaseDevice) getIntent().getSerializableExtra(KEY_BASE_DEVICE);

        tvHardwareVersion = (TextView) findViewById(R.id.tv_hardware_version);
        tvCurrentHeartRate = (TextView) findViewById(R.id.tv_current_heart_rate);
        tvCurrentStepFrequency = (TextView) findViewById(R.id.tv_current_step_frequency);
        historyData = (TextView) findViewById(R.id.history_data);
        btnSyncData = (Button) findViewById(R.id.btn_sync_data);
        btnSetUtc = (Button) findViewById(R.id.setting_utc);

        tvHardwareVersion.setText("");

        btnSyncData.setOnClickListener(this);
        btnSetUtc.setOnClickListener(this);

        dialog = new ProgressDialog(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isAllow = false;
        int count = 0;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                count++;
            }
        }
        if (count != grantResults.length) {
            Log.e("mainactivity", "Some Permission is Denied");
        }

    }

    /**
     * if you want you receive sms , call remind,save sport data and bluetooth,you must check permission,and request permission below
     */
    public void requestPermisson() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean readCallLog = (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED);
            boolean writeCallLog = (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED);
            boolean phoneState = (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
            boolean readSms = (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED);
            boolean readCall = (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED);
            boolean storage = (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            boolean contact = (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
            boolean location = (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
            if (!readCall || !readSms || !storage || !contact || !location || !phoneState || !readCallLog || !writeCallLog) {
                requestPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.CALL_PHONE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG}, 1);
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.main_menu_disconnect:
                if (armBandManager.getConnectState()
                        >= BleDevice.STATE_CONNECTED) {
                    armBandManager.disconnect(false);
                }
                break;

            case R.id.main_menu_connect:
                if (armBandManager.getConnectState()
                        < BleDevice.STATE_CONNECTED) {
                    armBandManager.connectDevice(baseDevice);
                }
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_battery_level_btn:
                armBandManager.getBatteryLevel();
                break;

            case R.id.btn_sync_data:
                showDialog(getString(R.string.synch_history), true);
                armBandManager.synchHistoryData(historyDataCallBack);
                break;

            case R.id.setting_utc:
                armBandManager.setUTC();
                break;

            default:
                break;
        }
    }

    public void updateConnectStatus(int status) {

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
            case BleDevice.STATE_SERVICES_OPENCHANNELSUCCESS:  //打开通道
                connectSate = R.string.stpes_walk;
                break;
        }
        setTitle("当前设备状态： " + getString(connectSate));

    }


    /**
     * 臂带的连接状态回调
     */
    private DeviceStateChangeCallback stateChangeCallback = new DeviceStateChangeCallback() {
        /**
         * 臂带的连接状态变化回调
         * @param mac ：mac地址
         * @param status ：状态值
         */
        @Override
        public void onStateChange(String mac, int status) {
            Log.i(TAG, "DeviceStateChangeCallback onStateChange mac:" + mac + " status:" + status);
            updateConnectStatus(status);
        }

        /**
         * 臂带可以下发数据的回调
         * @param mac
         * @param isNeedSetParam
         */
        @Override
        public void onEnableWriteToDevice(String mac, boolean isNeedSetParam) {
            Log.i(TAG, "DeviceStateChangeCallback onEnableWriteToDevice mac:"
                    + mac + " isNeedSetParam:" + isNeedSetParam);
        }
    };

    /**
     * 实时数据回调接口
     */
    private RealTimeDataListener realTimeDataListener = new RealTimeDataListener() {
        /**
         * 获取到电池电量
         *
         * @param batteryLevel 电池电量。取值范围:0-100
         */
        @Override
        public void onGotBatteryLevel(int batteryLevel) {
            tvBatteryLevel.setText("" + batteryLevel);
        }

        @Override
        public void onRealTimeHeartRateData(RTHeartRate value) {
            Log.i(TAG, "onRealTimeHeartRateData value:" + value);
            if (TextUtils.isEmpty(tvHardwareVersion.getText()) && armBandManager.getHardwareVersion() != null) {
                tvHardwareVersion.setText("硬件版本:" + armBandManager.getHardwareVersion());
            }
            tvCurrentHeartRate.setText("实时心率:" + value.getHeartRate());
        }

        @Override
        public void onRealTimeStepFrequencyData(StepFrequencyEntity value) {
            Log.i(TAG, "onRealTimeStepFrequencyData value:" + value);
            tvCurrentStepFrequency.setText("实时步频数据:\n"
                    + "当前总步数:" + value.getCurrentTotalSteps()
                    + "步频:" + value.getStepFrequency());
        }
    };

    /**
     * 历史数据回调接口
     */
    private SynchHistoryDataCallBack historyDataCallBack = new SynchHistoryDataCallBack() {
        /**
         * 同步状态
         * @param status ： SYNCH_STATUS_START = 7; //开始同步
        SYNCH_STATUS_SYNCHING = 8; //同步中
        SYNCH_STATUS_SUCCESS = 9; //同步成功
        SYNCH_STATUS_FEILURE = 10; //同步失败
         */
        @Override
        public void onSynchStateChange(int status) {
            switch (status) {
                case SynchHistoryDataCallBack.SYNCH_STATUS_SUCCESS:
                    showDialog(null, false);
                    // dialog.dismiss();
                    break;
                case SynchHistoryDataCallBack.SYNCH_STATUS_FEILURE:
                    showDialog(null, false);
                    // dialog.dismiss();
                    break;
            }
        }

        /**
         * 历史数据 : 分步数数据、心率数据
         * @param historyDataEntity
         */
        @Override
        public void onSynchAllHistoryData(final HistoryDataEntity historyDataEntity) {
            Log.i(TAG, "onSynchAllHistoryData start historyDataEntity:" + historyDataEntity);
            //历史步数数据
            List<PureStepHistoryData> liststep = historyDataEntity.getPureStepHistoryDataList();
            // 历史心率数据
            List<HeartRateData> listHeartRateData = historyDataEntity.getListHeartRateData();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    historyData.setText("历史数据：" + historyDataEntity);
                }
            });

            Log.i(TAG, "onSynchAllHistoryData end");
        }
    };

    @Override
    protected void onStop() {
        showDialog(null, false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_OK);
        //注销各种回调
        armBandManager.unregistStateChangeCallback(stateChangeCallback);
        armBandManager.unregistRealTimeDataListner(realTimeDataListener);
        armBandManager.unregistStateChangeCallback(stateChangeCallback);
        armBandManager.disconnect(false);
        armBandManager.closeDevice();
        super.onDestroy();
    }
}
