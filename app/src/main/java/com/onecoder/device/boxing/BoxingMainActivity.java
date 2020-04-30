package com.onecoder.device.boxing;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.onecoder.device.R;
import com.onecoder.device.adpater.FraPagerAdapter;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.device.base.MyHandler;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.control.entity.BleDevice;
import com.onecoder.devicelib.base.control.interfaces.DeviceStateChangeCallback;
import com.onecoder.devicelib.base.entity.BaseDevice;
import com.onecoder.devicelib.boxing.api.BoxingManager;
import com.onecoder.devicelib.boxing.api.interfaces.RealTimeDataListener;
import com.onecoder.devicelib.boxing.api.interfaces.SynchHistoryDataCallBack;
import com.onecoder.devicelib.boxing.protocol.entity.FistInfoBase;
import com.onecoder.devicelib.boxing.protocol.entity.FistType;
import com.onecoder.devicelib.boxing.protocol.entity.RealTimeFistInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/9/16.
 */
public class BoxingMainActivity extends BaseActivity implements View.OnClickListener,
        BoxingFragment.OnClickListener, Runnable, MyHandler.OnHandleMessageListener {
    private static final String TAG = BoxingMainActivity.class.getSimpleName();


    @BindView(R.id.nathTestId10)
    TextView nathTestId10;

    @BindView(R.id.punchSpeedId)
    TextView punchSpeedId;

    @BindView(R.id.punchPowerId)
    TextView punchPowerId;

    @BindView(R.id.punchCountId)
    TextView punchCountId;

    private FraPagerAdapter fraPagerAdapter;
    private List<BoxingFragment> fragmentList = new ArrayList<>();

    private BaseDevice baseDevice;

    private ProgressDialog dialog;

    private BoxingManager boxingManager;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final boolean DEBUG_REAL_TIME_DATA_FUNCTION = false;
    private static final long RECV_DATA_TIMEOUT_MS = 2000;
    private Map<String, Long> macTimeStampMap = new HashMap<>();
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;//= new ScheduledThreadPoolExecutor(2);
    private MyHandler handler = null;
    private RingtoneManager ringtoneManager = null;
    private Ringtone ringtone = null;

    // Timer Code - Variables

    private EditText mEditTextInput;
    private TextView mTextViewCountDown;
    private Button mButtonSet;
    private Button mButtonStartPause;
    private Button mButtonReset;

    private CountDownTimer mCountDownTimer;

    private boolean mTimerRunning;

    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private long mEndTime;



    @Override
    protected Manager getManager(Bundle savedInstanceState) {
        if (boxingManager != null) {
            return boxingManager;
        }
//        boxingManager = BoxingManager.getInstance();
//        //
//        //Registration status callback
//        boxingManager.registerStateChangeCallback(stateChangeCallback);
//        // Register real-time data callback
//        boxingManager.registerRealTimeDataListener(realTimeDataListener);

        List<BoxingManager> managerList = BoxingManagerContainer.getInstance().getManagerList();
        if (managerList == null || managerList.size() == 0) {
            return null;
        }
        BoxingFragment boxingFragment = null;
        for (BoxingManager boxingManager : managerList) {
            if (boxingManager == null || boxingManager.getBaseDevice() == null
                    || boxingManager.getBaseDevice().getMacAddress() == null) {
            }
            // NP - This is where they split the data?
            boxingFragment = new BoxingFragment();
            boxingFragment.setMac(boxingManager.getBaseDevice().getMacAddress());
            boxingFragment.setOnClickListener(this);
            fragmentList.add(boxingFragment);

            // Registration status callback
            boxingManager.registerStateChangeCallback(stateChangeCallback);
            // Register real-time data callback
            boxingManager.registerRealTimeDataListener(realTimeDataListener);
            boxingManager.registerSynchHistoryDataCallBack(synchHistoryDataCallBack);
            this.boxingManager = boxingManager;
        }
        return boxingManager;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boxing_main_act);
        ButterKnife.bind(this);

        baseDevice = (BaseDevice) getIntent().getSerializableExtra(KEY_BASE_DEVICE);
        dialog = new ProgressDialog(this);
        Log.i(TAG, "onCreate fragmentList:" + fragmentList);
        fraPagerAdapter = new FraPagerAdapter(getSupportFragmentManager(), fragmentList);


        if (DEBUG_REAL_TIME_DATA_FUNCTION) {
            handler = new MyHandler(this);
            scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);
            scheduledThreadPoolExecutor.scheduleAtFixedRate(this, RECV_DATA_TIMEOUT_MS, RECV_DATA_TIMEOUT_MS, TimeUnit.SECONDS);
        }

        nathTestId10.setText("success");

        // Timer Code [ START ]

        mEditTextInput = findViewById(R.id.edit_text_input);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);

        mButtonSet = findViewById(R.id.button_set);
        mButtonStartPause = findViewById(R.id.button_start_pause);
        mButtonReset = findViewById(R.id.button_reset);

        mButtonSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String input = mEditTextInput.getText().toString();
                    if (input.length() == 0) {
                        Toast.makeText(BoxingMainActivity.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long millisInput = Long.parseLong(input) * 60000;
                    if (millisInput == 0) {
                        Toast.makeText(BoxingMainActivity.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    setTime(millisInput);
                    mEditTextInput.setText("");

                }
        });

        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        // Timer Code - [ END ]
    }

    // Timer Code - Methods [ START ]

    private void setTime(long milliseconds) {
        mStartTimeInMillis = milliseconds;
        resetTimer();
    }

    private void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                updateWatchInterface();
            }
        }.start();

        mTimerRunning = true;
        updateWatchInterface();

        // Registration status callback
        boxingManager.registerStateChangeCallback(stateChangeCallback);
        // Register real-time data callback
        boxingManager.registerRealTimeDataListener(realTimeDataListener);
        boxingManager.registerSynchHistoryDataCallBack(synchHistoryDataCallBack);
    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        updateWatchInterface();

        // Registration status callback
        boxingManager.unregistStateChangeCallback(stateChangeCallback);
        // Register real-time data callback
        boxingManager.unregisterRealTimeDataListener(realTimeDataListener);
        boxingManager.unregisterSynchHistoryDataCallBack(synchHistoryDataCallBack);
    }

    private void resetTimer() {
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
        updateWatchInterface();
    }

    private void updateCountDownText() {
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d,%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }



        mTextViewCountDown.setText(timeLeftFormatted);
    }

    private void updateWatchInterface() {
        if (mTimerRunning) {
            mEditTextInput.setVisibility(View.INVISIBLE);
            mButtonSet.setVisibility(View.INVISIBLE);
            mButtonReset.setVisibility(View.INVISIBLE);
            mButtonStartPause.setText("Pause");
        } else {
            mEditTextInput.setVisibility(View.VISIBLE);
            mButtonSet.setVisibility(View.VISIBLE);
            mButtonStartPause.setText("Start");

            if (mTimeLeftInMillis < 1000) {
                mButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                mButtonStartPause.setVisibility(View.VISIBLE);
            }

            if (mTimeLeftInMillis < mStartTimeInMillis) {
                mButtonReset.setVisibility(View.VISIBLE);
            } else {
                mButtonReset.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("startTimeInMillis", mStartTimeInMillis);
        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);

        editor.apply();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        mStartTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
        mTimeLeftInMillis = prefs.getLong("millisLeft", mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean("timerRunning", false);

        updateCountDownText();
        updateWatchInterface();

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                updateCountDownText();
                updateWatchInterface();
            } else {
                startTimer();
            }
        }
    }



    // Timer Code - Methods [ END ]

    public void punchSpeed(String info) {
        punchSpeedId.setText(info);
    }

    public void punchPower(String info) {
        punchPowerId.setText(info);
    }

    public void punchCount(String info) {
        punchCountId.setText(info);
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
        //getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (boxingManager == null) {
            return super.onOptionsItemSelected(item);
        }
        switch (id) {
            case R.id.main_menu_disconnect:
                if (boxingManager.getConnectState()
                        >= BleDevice.STATE_CONNECTED) {
                    boxingManager.disconnect(false);
                }
                break;

            case R.id.main_menu_connect:
                if (boxingManager.getConnectState()
                        < BleDevice.STATE_CONNECTED) {
                    boxingManager.connectDevice(baseDevice);
                }
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (boxingManager == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.get_battery_level_btn:
                boxingManager.getBatteryLevel();
                break;

            case R.id.setting_utc:
                boxingManager.setUTC();
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(String mac, View v) {
        Manager manager = BoxingManagerContainer.getInstance().getManager(mac);
        if (!(manager instanceof BoxingManager)) {
            return;
        }
        BoxingManager boxingManager = (BoxingManager) manager;
        switch (v.getId()) {
            case R.id.get_battery_level_btn:
                boxingManager.getBatteryLevel();
                break;

            case R.id.setting_utc:
                boxingManager.setUTC();
                break;

            case R.id.get_history_data_btn:
                if (SynchHistoryDataCallBack.SYNCH_STATUS_SYNCHING == boxingManager.synchHistoryData()) {
                    showDialog(getString(R.string.synch_history), true);
                }
                break;

            default:
                break;
        }
    }

    private BoxingFragment getBoxingFragment(String mac) {
        if (mac == null) {
            return null;
        }

        for (BoxingFragment boxingFragment : fragmentList) {
            if (boxingFragment == null || !mac.equalsIgnoreCase(boxingFragment.getMac())) {
                continue;
            }
            return boxingFragment;
        }
        return null;
    }

    /**
     *
     * Armband connection status callback
     */
    private DeviceStateChangeCallback stateChangeCallback = new DeviceStateChangeCallback() {
        /**
         *
         * Armband connection status change callback
         * @param mac ：mac地址
         * @param status ：状态值
         */
        @Override
        public void onStateChange(String mac, int status) {
            Log.i(TAG, "DeviceStateChangeCallback onStateChange mac:" + mac + " status:" + status);
            BoxingFragment boxingFragment = getBoxingFragment(mac);
            if (boxingFragment == null) {
                return;
            }
            boxingFragment.updateConnectStatus(getConnectStatusStr(status));
        }

        /**
         *
         * Armband can send callback data
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
     * Real time data callback
     */


    private RealTimeDataListener realTimeDataListener = new RealTimeDataListener() {

        /**
         * Got the battery level.
         * @param mac MAC of device
         * @param batteryLevel battery level.. Range: 0-100
         */
        @Override
        public void onGotBatteryLevel(String mac, int batteryLevel) {
            nathTestId10.setText("" + batteryLevel);
        }

        /**
         * received real time boxing data
         *
         * @param mac              MAC of device
         * @param realTimeFistInfo   real time boxing data
         */

        @Override
        public void onRealTimeFistData(String mac, RealTimeFistInfo realTimeFistInfo) {
            /** punchSpeedId.setText("Hardware version:" + boxingManager.getProtocolVersion ()); **/
            if (realTimeFistInfo == null) {
                Log.i(TAG, "onRealTimeFistData realTimeFistInfo is null");
                return;
            }
            StringBuffer stringBuffer = new StringBuffer();
            FistType fistType = realTimeFistInfo.getFistType();
            if (fistType == null) {
                Log.i(TAG, "onRealTimeFistData fistType is null");
                return;
            }
            String fistTypeStr = fistType.name();
            switch (fistType) {
                case Hook:
                    fistTypeStr = " Hook ";
                    break;

                case Punch:
                    fistTypeStr = " Punch ";
                    break;

                case StraightPunch:
                    fistTypeStr = " Straight punch ";
                    break;

                default:
                    break;
            }
            stringBuffer.append("Number of punches:" + realTimeFistInfo.getFistNum());
            stringBuffer.append("\n Boxing type:" + fistTypeStr);
            stringBuffer.append("\n Fist time:" + realTimeFistInfo.getFistOutTime() + " ms");
            stringBuffer.append("\n Closing time:" + realTimeFistInfo.getFistInTime() + " ms");
            stringBuffer.append("\n Boxing intensity:" + realTimeFistInfo.getFistPower() + " G");
            stringBuffer.append("\n The speed of boxing:" + realTimeFistInfo.getFistSpeed() + " km/h");
            stringBuffer.append("\n Distance of Boxing:" + realTimeFistInfo.getFistDistance() + " m");

            punchSpeedId.setText("" + realTimeFistInfo.getFistSpeed());
            punchPowerId.setText("" + realTimeFistInfo.getFistPower());
            punchCountId.setText("" + realTimeFistInfo.getFistNum());

        }

    };

    /**
     * 历史数据回调
     */
    private SynchHistoryDataCallBack synchHistoryDataCallBack = new SynchHistoryDataCallBack() {

        /**
         *
         * @param mac 设备MAC
         *
         * int SYNCH_STATUS_NOT_SYNCH = -1; //未同步或同步已完成
         * int SYNCH_STATUS_START = 7; //开始同步
         * int SYNCH_STATUS_SYNCHING = 8; //同步中
         * int SYNCH_STATUS_SUCCESS = 9; //同步成功
         * int SYNCH_STATUS_FEILURE = 10; //同步失败
         * @param status
         */
        @Override
        public void onSynchStateChange(String mac, int status) {
            BoxingFragment boxingFragment = getBoxingFragment(mac);
            if (boxingFragment == null) {
                return;
            }
            switch (status) {
                case SynchHistoryDataCallBack.SYNCH_STATUS_SUCCESS:
                    showDialog(null, false);
                    break;
                case SynchHistoryDataCallBack.SYNCH_STATUS_FEILURE:
                    showDialog(null, false);
                    break;
            }
        }

        /**
         *
         * @param mac 设备MAC
         * @param fistInfoBaseList 历史数据
         */
        @Override
        public void onSynchAllHistoryData(final String mac, final List<FistInfoBase> fistInfoBaseList) {
            if (mac == null || fistInfoBaseList == null) {
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BoxingFragment boxingFragment = getBoxingFragment(mac);
                    if (boxingFragment == null) {
                        return;
                    }
                    StringBuffer stringBuffer = new StringBuffer();
                    int cnt = 0;
                    for (FistInfoBase fistInfoBase : fistInfoBaseList) {
                        if (fistInfoBase == null) {
                            continue;
                        }
                        cnt++;
                        stringBuffer.append(getDisplayStr(false, fistInfoBase));
                    }
                    stringBuffer.insert(0, "历史数据数目:" + cnt + "\n");
                    boxingFragment.setHistoryDataInfo(stringBuffer.toString());
                }
            });
        }
    };

    public String getConnectStatusStr(int status) {
        int resId = R.string.un_stpes_walk;
        switch (status) {
            case BleDevice.STATE_DISCONNECTED:  //断开连接
                break;
            case BleDevice.STATE_CONNECTING:  //正在连接
                resId = R.string.device_connecting;
                break;
            case BleDevice.STATE_CONNECTED:  //已连接
                resId = R.string.stpes_walk;
                break;
            case BleDevice.STATE_SERVICES_DISCOVERED:  //发现服务
            case BleDevice.STATE_SERVICES_OPENCHANNELSUCCESS:  //打开通道
                resId = R.string.stpes_walk;
                break;

            default:
                return "";
        }
        return getString(resId);
    }

    private String getDisplayStr(boolean realtimeData, FistInfoBase fistInfoBase) {
        StringBuffer ret = new StringBuffer();
        if (fistInfoBase == null || fistInfoBase.getFistType() == null) {
            return ret.toString();
        }
        FistType fistType = fistInfoBase.getFistType();
        String fistTypeStr = fistType.name();
        switch (fistType) {
            case Hook:
                fistTypeStr = "Uppercut";
                break;

            case Punch:
                fistTypeStr = "Hook";
                break;

            case StraightPunch:
                fistTypeStr = "Straight";
                break;

            default:
                break;
        }
        if (realtimeData && fistInfoBase instanceof RealTimeFistInfo) {
            RealTimeFistInfo realTimeFistInfo = (RealTimeFistInfo) fistInfoBase;
            ret.append("Punches thrown: " + realTimeFistInfo.getFistNum());
        }
        ret.append("\n Boxing Type: " + fistTypeStr);
        ret.append("\n Fist time: " + fistInfoBase.getFistOutTime() + " ms");
        ret.append("\n Closing time: " + fistInfoBase.getFistInTime() + " ms");
        ret.append("\n Fist power: " + fistInfoBase.getFistPower() + " G");
        ret.append("\n Fist speed: " + fistInfoBase.getFistSpeed() + " km/h");
        ret.append("\n Fist distance: " + fistInfoBase.getFistDistance() + " m");
        ret.append("\n Boxing time: " + simpleDateFormat.format(fistInfoBase.getUtc()));
        ret.append("\n");
        return ret.toString();
    }


    @Override
    public void run() {
        if (handler != null) {
            handler.sendEmptyMessage(0);
        }
    }

    @Override
    public void onHandleMessage(Message msg) {
        List<String> devList = getTimeoutDev();
        if (devList == null || devList.size() == 0) {
            //没有设备接收数据超时
            return;
        }
        Log.e(TAG, "onHandleMessage 以下设备接收数据超时：\n" + devList);
        if (ringtoneManager == null) {
            ringtoneManager = new RingtoneManager(this);
            ringtoneManager.setType(RingtoneManager.TYPE_ALARM);
            ringtoneManager.setStopPreviousRingtone(true);
            ringtone = ringtoneManager.getRingtone(0);
        }
        if (ringtone != null && !ringtone.isPlaying()) {
            ringtone.play();
        }
    }

    private List<String> getTimeoutDev() {
        List<String> macList = new ArrayList<>();

        long timeStamp = System.currentTimeMillis();
        String mac;
        Long preTimeStamp;
        for (BoxingFragment boxingFragment : fragmentList) {
            if (boxingFragment == null || boxingFragment.getMac() == null) {
                continue;
            }
            mac = boxingFragment.getMac();
            preTimeStamp = macTimeStampMap.get(mac);
            if (preTimeStamp == null) {
                continue;
            }
            if (timeStamp - preTimeStamp > RECV_DATA_TIMEOUT_MS) {
                macList.add(mac);
            }
        }
        return macList;
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_OK);
        //注销各种回调
//        boxingManager.unregistStateChangeCallback(stateChangeCallback);
//        boxingManager.unregisterRealTimeDataListener(realTimeDataListener);
//        boxingManager.disconnect(false);
//        boxingManager.closeDevice();

        BoxingManagerContainer boxingManagerContainer = BoxingManagerContainer.getInstance();
        if (boxingManagerContainer != null) {
            List<BoxingManager> managerList = boxingManagerContainer.getManagerList();
            if (managerList != null) {
                for (BoxingManager boxingManager : managerList) {
                    if (boxingManager == null) {
                        continue;
                    }
                    boxingManager.closeDevice();
                    boxingManager.unregistStateChangeCallback(stateChangeCallback);
                    boxingManager.unregisterRealTimeDataListener(realTimeDataListener);
                    //注册历史数据回调
                    boxingManager.unregisterSynchHistoryDataCallBack(synchHistoryDataCallBack);
                }
            }
        }
        if (DEBUG_REAL_TIME_DATA_FUNCTION) {
            handler.removeCallbacksAndMessages(null);
            handler.setOnHandleMessageListener(null);
            scheduledThreadPoolExecutor.shutdownNow();
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }
        }
        super.onDestroy();
    }
}
