package com.onecoder.device.tracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.onecoder.device.R;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.device.utils.GToast;
import com.onecoder.device.utils.Utils;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.control.entity.BleDevice;
import com.onecoder.devicelib.base.control.interfaces.DeviceStateChangeCallback;
import com.onecoder.devicelib.base.entity.BaseDevice;
import com.onecoder.devicelib.base.protocol.CommonProtocol;
import com.onecoder.devicelib.base.protocol.entity.HeartRateData;
import com.onecoder.devicelib.base.protocol.entity.SleepData;
import com.onecoder.devicelib.base.protocol.entity.StepData;
import com.onecoder.devicelib.tracker.api.TrackerManager;
import com.onecoder.devicelib.tracker.api.entity.HistoryDataEntity;
import com.onecoder.devicelib.tracker.api.entity.RealTimeData;
import com.onecoder.devicelib.tracker.api.entity.UserSleepTime;
import com.onecoder.devicelib.tracker.api.interfaces.MusicListner;
import com.onecoder.devicelib.tracker.api.interfaces.RealTimeDataListner;
import com.onecoder.devicelib.tracker.api.interfaces.RingPhoneListner;
import com.onecoder.devicelib.tracker.api.interfaces.SynchHistoryDataCallBack;
import com.onecoder.devicelib.tracker.api.interfaces.TakePictureListner;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/9/16.
 */
public class TrackerMainActivity extends BaseActivity implements View.OnClickListener {
    public static final String KEY_PROTOCOL_TYPE = "protocolType";
    private static final String TAG = TrackerMainActivity.class.getSimpleName();
    @BindView(R.id.tv_battery_level)
    TextView tvBatteryLevel;
    @BindView(R.id.get_battery_level_btn)
    Button getPowerBtn;
    private BaseDevice baseDevice;

    private ProgressDialog dialog;
    private TextView tvCurrentStep;
    // 协议类别 （新协议、旧协议）
    //protocol type(new protocol or old protocol)
    private int protocolType;
    private TrackerManager trackerManager;
    private TextView historyData;

    @Override
    protected Manager getManager(Bundle savedInstanceState) {
        if (trackerManager != null) {
            return trackerManager;
        }
        trackerManager = TrackerManager.getInstance();
        //注册状态回调
        //Register connection status call-back
        trackerManager.registerStateChangeCallback(stateChangeCallback);
        trackerManager.registerRealTimeDataListner(realTimeDataListner);
        // 设置音乐控制监听器
        // Set up music control listener.
        trackerManager.setMusicListner(musicListner);
        // 设置拍照控制监听器
        // Set up a camera control listener.
        trackerManager.setTakePictureListner(takePictureListner);
        // 设置找手机监听器
        // Set up a find mobile phone listener.
        trackerManager.setRingPhoneListner(ringPhoneListner);
        return trackerManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker_main_act);
        ButterKnife.bind(this);

        protocolType = getIntent().getIntExtra(KEY_PROTOCOL_TYPE, CommonProtocol.NEW_PROTOCOL);

        //requestPermisson();
        baseDevice = (BaseDevice) getIntent().getSerializableExtra(KEY_BASE_DEVICE);

        tvCurrentStep = (TextView) findViewById(R.id.tv_current_step);
        historyData = (TextView) findViewById(R.id.history_data);

        dialog = new ProgressDialog(this);

        updateConnectStatus(trackerManager.getConnectState());
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                if (trackerManager.getConnectState()
                        >= BleDevice.STATE_CONNECTED) {
                    trackerManager.disconnect(false);
                }
                break;
            case R.id.main_menu_connect:
                if (trackerManager.getConnectState()
                        < BleDevice.STATE_CONNECTED) {
                    trackerManager.connectDevice(baseDevice);
                }
                break;
            case R.id.main_menu_unbind:
               /* MainService.getInstance(this).unBind(baseDevice);
                Intent intent = new Intent(this, DevicesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();*/
                break;
            case R.id.main_menu_history:
                //Intent intent1 = new Intent(this, HistoryActivity.class);
                //startActivity(intent1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.get_battery_level_btn:
                trackerManager.getBatteryLevel();
                break;

            case R.id.btn_sync_data:
                showDialog(getString(R.string.synch_history), true);
                trackerManager.synchHistoryData(historyDataCallBack);
                break;
            case R.id.btn_history_data:
                break;
            case R.id.set_sleep_time:
                UserSleepTime sleepTime = new UserSleepTime();
                // 晚上开始入睡时间
                // The time to start sleeping at night.
                sleepTime.setStartTime("23:00");
                // 工作日起床时间
                // Wake-up time in work day.
                sleepTime.setNormalGetUpTime("07:30");
                // 周末起床时间(周六、周日)
                // Wake-up time on weekends (Saturdays and Sundays).
                sleepTime.setWeekGetUpTime("09:00");
                trackerManager.setUserSleep(sleepTime);
                break;
            case R.id.btn_startHeart:
                if (trackerManager.getConnectState() >= BleDevice.STATE_CONNECTED) {
                    Intent intent1 = new Intent(this, TrackerHeartActivity.class);
                    startActivity(intent1);
                }
                break;

            case R.id.btn_device_setting:
                // 旧协议手环无设置
                // The old protocol tracker has no setup function.
                if (trackerManager.getProtocolType() == CommonProtocol.NEW_PROTOCOL) {
                    intent = new Intent(this, TrackerSettingActivity.class);
                    startActivity(intent);
                } else {
                    //The old protocol trackers do not need to set parameters.
                    GToast.show(this, getString(R.string.old_protocol_tracker_has_no_need_to_set_param));
                }
                break;
            case R.id.send_call:
                // 发送来电提醒
                // 可以是姓名，也可以使手机号码
                // Send a call reminder.
                // It can be a name or a cell phone number.
                String callName = "张三";

                int ctrlcode = 1; // 1：来电 0：挂断  1:Call  0:hang up
                trackerManager.sendAndroidCallinfo(callName, ctrlcode);
                break;
            case R.id.setting_utc:
                trackerManager.setUTC();
                break;
            default:
                break;
        }
    }

    public void updateConnectStatus(int status) {
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
        // The current connection status of the device.
        setTitle(connectSate);
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
            Log.i(TAG, "DeviceStateChangeCallback onStateChange mac:" + mac + " status:" + status);
            updateConnectStatus(status);
        }

        /**
         * 手环可以下发数据的回调，新协议称可以在此时设置用户信息、闹钟、健康开关等
         * Tracker can be used to send data callback. The new protocol tracker can be set user information, alarm clock, health switch and so on at this time.
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
     * Real-time data callback
     */
    private RealTimeDataListner realTimeDataListner = new RealTimeDataListner() {
        /**
         * 获取到电池电量
         * Got the battery power.
         * @param batteryLevel 电池电量。取值范围:0-100  Battery power. Value range: 0-100.
         */
        @Override
        public void onGotBatteryLevel(int batteryLevel) {
            Log.i(TAG, "onGotBatteryLevel value:" + batteryLevel);
            tvBatteryLevel.setText("" + batteryLevel);
        }

        @Override
        public void onRealTimeData(RealTimeData value) {
            Log.i(TAG, "onRealTimeData value:" + value);
            //Current real-time data
            tvCurrentStep.setText(getString(R.string.current_real_time_data) + "：" + value.toString());
        }
    };

    /**
     * 历史数据回调接口
     * Historical data callback
     */
    private SynchHistoryDataCallBack historyDataCallBack = new SynchHistoryDataCallBack() {
        /**
         * 同步状态
         * Synchronization state
         * @param status ： SYNCH_STATUS_START = 7; //开始同步 Start synchronization.
        SYNCH_STATUS_SYNCHING = 8; //同步中 Synchronizing...
        SYNCH_STATUS_SUCCESS = 9; //同步成功 Synchronization succeeded.
        SYNCH_STATUS_FEILURE = 10; //同步失败 Synchronization failed.
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
         * 历史数据 : 分步行数据、睡眠数据、心率数据（部分设备有）
         * History data: Walking data, sleep data, heart rate data (some devices have).
         * @param historyDataEntity
         */
        @Override
        public void onSynchAllHistoryData(final HistoryDataEntity historyDataEntity) {
            Log.i(TAG, "onSynchAllHistoryData start historyDataEntity:" + historyDataEntity);

            //历史步数数据
            //History step data
            List<StepData> liststep = historyDataEntity.getListstep();
            Utils.sortStepData(liststep);
            //历史睡眠数据
            //History sleep data
            List<SleepData> listsleep = historyDataEntity.getListsleep();
            // 心率历史数据
            //Heart rate history data
            List<HeartRateData> listHeartRateData = historyDataEntity.getListHeartRateData();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //history data
                    historyData.setText(getString(R.string.history_data) + "：" + historyDataEntity);
                }
            });

            Log.i(TAG, "onSynchAllHistoryData end");
        }
    };

    /**
     * 手环控制音乐的回调
     * Tracker controls music callback
     */
    private MusicListner musicListner = new MusicListner() {
        /**
         * 音乐控制
         *
         * @param operate 0：关闭音乐
         *                1；打开音乐
         *                3：上一首
         *                :5：下一首
         *                :9：音量--
         *                :11：音量+
         *                21：暂停
         */
        @Override
        public void onPlayMusic(int operate) {
            Log.i(TAG, "onPlayMusic operate:" + operate);
        }
    };

    /**
     * 手环控制拍照的回调
     * Tracker controls photo callback
     */
    private TakePictureListner takePictureListner = new TakePictureListner() {
        /**
         * 拍照
         *
         * @param mode ：模式  1 为拍一张，2为拍两张
         */
        @Override
        public void onTakePicture(int mode) {
            Log.i(TAG, "onTakePicture mode:" + mode);
        }
    };

    /**
     * 手环找手机事件接口
     * Tracker looking for phone callback
     */
    private RingPhoneListner ringPhoneListner = new RingPhoneListner() {
        /**
         * 响铃
         *
         * @param switch_on ：1 开  0 关
         */
        @Override
        public void onNeedRingPhone(int switch_on) {
            Log.i(TAG, "onNeedRingPhone switch_on:" + switch_on);
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
        trackerManager.unregistStateChangeCallback(stateChangeCallback);
        trackerManager.disconnect(false);
        trackerManager.closeDevice();
        super.onDestroy();
    }
}
