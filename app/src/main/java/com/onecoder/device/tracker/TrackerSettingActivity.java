package com.onecoder.device.tracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.onecoder.device.R;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.device.utils.GToast;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.control.entity.BleDevice;
import com.onecoder.devicelib.base.control.interfaces.DeviceStateChangeCallback;
import com.onecoder.devicelib.base.entity.BaseDevice;
import com.onecoder.devicelib.base.entity.BaseUserInfo;
import com.onecoder.devicelib.base.protocol.protocol.ProtocolMarco;
import com.onecoder.devicelib.tracker.api.TrackerManager;
import com.onecoder.devicelib.tracker.api.entity.AlarmPlanEntity;
import com.onecoder.devicelib.tracker.api.entity.HealthSetEntity;
import com.onecoder.devicelib.tracker.api.entity.RemindEntity;
import com.onecoder.devicelib.tracker.api.entity.TrackerUser;
import com.onecoder.devicelib.tracker.api.entity.UserSleepTime;
import com.onecoder.devicelib.utils.TimeUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2016/9/26.
 */
public class TrackerSettingActivity extends BaseActivity implements View.OnClickListener {

    private BaseDevice baseDevice;
    private TrackerManager trackerManager;

    private Spinner spinner;
    private EditText androidMsgEditText;
    private int androidMsgType;

    private int pairPwd;
    private Dialog dialog = null;
    private EditText pairPwdEditText;

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
        setContentView(R.layout.tracker_setting_act);
        spinner = (Spinner) findViewById(R.id.android_msg_type_spinner);
        androidMsgEditText = (EditText) findViewById(R.id.android_msg_edit_text);

        List<String> andfroidMsgTypeList = new ArrayList<String>();
        andfroidMsgTypeList.add("MISSEDCALL");
        andfroidMsgTypeList.add("EMAIL");
        andfroidMsgTypeList.add("SMS");
        andfroidMsgTypeList.add("WECHAT");
        andfroidMsgTypeList.add("QQ");
        andfroidMsgTypeList.add("SYKPE");
        andfroidMsgTypeList.add("WHATSAPP");
        andfroidMsgTypeList.add("FACEBOOK");
        andfroidMsgTypeList.add("OTHER");
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.android_msg_type_item_layout, andfroidMsgTypeList);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                androidMsgType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        androidMsgType = ProtocolMarco.MsgType.MISSEDCALL_TYPE;

        setAndroidRemindSw(androidMsgSw);

        pairPwd = new Random().nextInt(999999);
    }

    boolean rtDataSw = true;
    boolean alarmClockSw = true;
    boolean androidMsgSw = true;
    boolean healthRemindSw = true;
    boolean cameraModeSw = false;
    boolean heartRateModeSw = false;

    @Override
    public void onClick(View v) {
        if (trackerManager.getConnectState() < BleDevice.STATE_SERVICES_DISCOVERED) {
            //The tracker is not connected and can not be operated.
            GToast.show(this, getString(R.string.device_not_connected_can_not_operate));
            return;
        }
        Intent intent = null;
        switch (v.getId()) {
            //设置闹钟
            //Set the alarm clock.
            case R.id.setting_alarm:
                List<AlarmPlanEntity> alarmList = new ArrayList<>();
                AlarmPlanEntity planEntity = new AlarmPlanEntity();
                planEntity.setNumbers(0);
                planEntity.setOpenStatus(AlarmPlanEntity.STATE_OPEN); //闹钟开启  Turn on the alarm clock
                planEntity.setPlanName("MyAlarmOne");// 闹钟名字 Name of alarm clock

                //提醒时间
                //重复闹钟格式：hh：mm
                //单次闹钟格式：yyyy-MM-dd HH：mm
                //Reminder time
                //Repeat alarm clock format: hh:mm
                //Single alarm format: yyyy-MM-dd HH:mm
                planEntity.setRemindTime("15:30");

                //重复星期
                //周一到周日:1-7
                //如:周一、周二重复，数组为repeatDay ={1,2},或周六、周日重复，数组为repeatDay ={6,7}
                //若为单次提醒时数组长度为1，repeatDay ={0};
                //Repeat
                //Monday to Sunday: 1-7
                //Such as: repeated on Monday, Tuesday, the array is repeatDay = {1, 2},
                //or repeated on Saturday and Sunday, the array is repeatDay = {6, 7}.
                //If the array length is 1 for a single reminder, repeatDay = {0}.
                int[] repeatDay = {1, 2};
                // 每周一和周二，15:30 闹钟提醒
                //Alarm reminders every Monday and Tuesday at 15:30.
                planEntity.setRepeatDay(repeatDay);
                alarmList.add(planEntity);
                //设置闹钟,最多可添加8个闹钟
                //Set clocks. You can add up to 8 alarm clocks.
                trackerManager.setAlarmPlan(alarmList);
                break;

            //设置睡眠时间
            //Set the sleep time.
            case R.id.setting_auto_sleep:
                UserSleepTime sleepTime = new UserSleepTime();
                // 晚上开始入睡时间
                // The time to start sleeping at night.
                sleepTime.setStartTime("12:00");
                // 工作日起床时间
                // Wake-up time in work day.
                sleepTime.setNormalGetUpTime("13:30");
                // 周末起床时间(周六、周日)
                // Wake-up time on weekends (Saturdays and Sundays).
                sleepTime.setWeekGetUpTime("19:00");
                trackerManager.setUserSleep(sleepTime);
                break;

            // 设置久坐提醒
            //Set sedentary reminder.
            case R.id.setting_long_move:
                HealthSetEntity moveEntity = getHealthSetEntity();

                //久坐提醒
                //Sedentary reminder.
                trackerManager.setHealthInfo(moveEntity, HealthSetEntity.TYPE_MOVE);
                break;

            //设置用户信息
            //Set user information.
            case R.id.setting_user_info:
                TrackerUser userinfo = new TrackerUser();
                userinfo.setHeight(180);//单位CM  unit:cm
                userinfo.setAge(30);
                userinfo.setWeight(75); //单位KG   unit:kg
                userinfo.setTarget(200);
                userinfo.setSex(BaseUserInfo.SEX_MAN); // 男  male
                //设置用户信息数据,不设置使用默认值
                //Set user information. If not set, the default value will be used.
                trackerManager.setUserBodyInfo(userinfo);
                break;

            case R.id.setting_water:
                HealthSetEntity waterEntity = getHealthSetEntity();
                //喝水提醒
                //Drinking water reminder
                trackerManager.setHealthInfo(waterEntity, HealthSetEntity.TYPE_WATER);
                break;

            case R.id.setting_disturb:
                // switch_on 1：开 0：关
                //1:switch on 0:switch off
                trackerManager.setRTSwitch((rtDataSw = !rtDataSw) ? 1 : 0);
                Toast.makeText(this, "rtDataSw:" + rtDataSw, Toast.LENGTH_SHORT).show();
                break;

            //Android 消息提醒开关
            //Android message reminder switch.
            case R.id.setting_msg_switch:
                setAndroidRemindSw(androidMsgSw = !androidMsgSw);
                Toast.makeText(this, "androidMsgSw:" + androidMsgSw, Toast.LENGTH_SHORT).show();
                break;

            case R.id.setting_alarm_switch:
                // planEntity.setNumbers(0);
                // 闹钟序列号，对应设置闹钟时的 planEntity.setNumbers(0);
                //The alarm clock serial number corresponds to the planEntity. setNumbers (0) when the alarm clock is set.
                int numbers = 0;

                //RemindEntity.STATE_CLOSE 关闭闹钟 Turn off the alarm clock
                //RemindEntity.STATE_OPEN 开启闹钟 Turn on the alarm clock
                trackerManager.setAlramSwitch(numbers, (alarmClockSw = !alarmClockSw) ? RemindEntity.STATE_OPEN : RemindEntity.STATE_CLOSE);
                Toast.makeText(this, "alarmClockSw:" + alarmClockSw, Toast.LENGTH_SHORT).show();
                //sendAndroidInfo(ProtocolMarco.MsgType.EMAIL_TYPE);
                break;

            case R.id.setting_health_switch:
                //设置久坐和喝水提醒开关
                //Set up a sedentary and drinking water reminder switch
                healthRemindSw = !healthRemindSw;
                int waterStatus = healthRemindSw ? HealthSetEntity.STATE_OPEN : HealthSetEntity.STATE_CLOSE;
                int moveStatus = healthRemindSw ? HealthSetEntity.STATE_OPEN : HealthSetEntity.STATE_CLOSE;
                trackerManager.setHealthSwitch(waterStatus,
                        moveStatus);
                Toast.makeText(this, "healthRemindSw:" + healthRemindSw, Toast.LENGTH_SHORT).show();
                break;

            case R.id.setting_android_info:
                // 设置Android消息
                // set android message
                sendAndroidInfo(ProtocolMarco.MsgType.QQ_TYPE);
                break;

            case R.id.send_android_msg:
                // 设置Android消息
                //Android消息对应的UTC
                // set android message
                //The UTC corresponding to the Android message
                long localUTCInSeconds = TimeUtils.getLocalUTC();

                // Android消息对应的时间 格式  yyyy-MM-dd HH:mm:ss
                //The time corresponding to the Android message. Format: hh:mm:ss
                String timeStr = new SimpleDateFormat("hh:mm:ss").format(
                        new Date(System.currentTimeMillis()));

                // 消息内容
                //message content
                String androidMsgStr = androidMsgEditText.getText().toString();
                //   消息条数：刚连接上设备，发未读消息总数，其余为1
                //Number of messages: when the device is just connected, it is the total number of unread messages, otherwise it is 1.
                int msgTotal = 1;
                // 发送消息
                //Send message.
                trackerManager.sendAndroidMessageInfo(localUTCInSeconds, timeStr, androidMsgStr,
                        androidMsgType, msgTotal);
                Log.i("TrackerSettingActivity", "after sendAndroidMessageInfo");
                break;

            case R.id.setting_utc:
                trackerManager.setUTC();
                break;

            case R.id.setting_heartrate:
                int switchStatus = (cameraModeSw = !cameraModeSw) ? 1 : 0; // 0：退出  1：进入 0: Exit 1: Entry
                trackerManager.setHearRateSwitch(switchStatus);
                Toast.makeText(this, "cameraModeSw:" + cameraModeSw, Toast.LENGTH_SHORT).show();
                break;

            case R.id.setting_camera:
            /*    int water=HealthSetEntity.STATE_OPEN;
                int move=HealthSetEntity.STATE_OPEN;
                trackerManager.setHealthSwitch(water, move);*/
                int cameraStatus = (heartRateModeSw = !heartRateModeSw) ? 1 : 0; // 0：退出  1：进入  0: Exit 1: Entry
                trackerManager.setCameraSwitch(cameraStatus);
                Toast.makeText(this, "heartRateModeSw:" + heartRateModeSw, Toast.LENGTH_SHORT).show();
                break;

            case R.id.pair:
                dealPairClickEvent();
                break;

            default:
                break;
        }
    }

    private void setAndroidRemindSw(boolean androidMsgSw) {
        RemindEntity remindSwitch = new RemindEntity();
        remindSwitch.setFaceBook(androidMsgSw ? RemindEntity.STATE_OPEN : RemindEntity.STATE_CLOSE);
        remindSwitch.setWeChat(androidMsgSw ? RemindEntity.STATE_OPEN : RemindEntity.STATE_CLOSE);
        remindSwitch.setMail(androidMsgSw ? RemindEntity.STATE_OPEN : RemindEntity.STATE_CLOSE);
        remindSwitch.setMissedCall(androidMsgSw ? RemindEntity.STATE_OPEN : RemindEntity.STATE_CLOSE);
        remindSwitch.setQq(androidMsgSw ? RemindEntity.STATE_OPEN : RemindEntity.STATE_CLOSE);
        remindSwitch.setShortMessage(androidMsgSw ? RemindEntity.STATE_OPEN : RemindEntity.STATE_CLOSE);
        remindSwitch.setSkype(androidMsgSw ? RemindEntity.STATE_OPEN : RemindEntity.STATE_CLOSE);
        remindSwitch.setWhatsAPP(androidMsgSw ? RemindEntity.STATE_OPEN : RemindEntity.STATE_CLOSE);
        remindSwitch.setOthers(androidMsgSw ? RemindEntity.STATE_OPEN : RemindEntity.STATE_CLOSE);
        trackerManager.setAndroidRemindSwitch(remindSwitch);
    }

    /**
     * @param type
     * @see {@link ProtocolMarco.MsgType}
     */
    private void sendAndroidInfo(int type) {
        //Android消息对应的UTC
        //The UTC corresponding to the Android message
        long localUTC = TimeUtils.getLocalUTC();
        // Android消息对应的时间 格式:hh:mm:ss
        //The time corresponding to the Android message. Format: hh:mm:ss
        String time = "12:30:00";
        String androidMsg = "来一条消息";  // 消息内容 Message content

        //   消息条数：刚连接上设备，发未读消息总数，其余为1
        //Number of messages: when the device is just connected, it is the total number of unread messages, otherwise it is 1.
        int total = 1;

        // 发送消息
        //Send message.
        trackerManager.sendAndroidMessageInfo(localUTC, time, androidMsg,
                type, total);
    }

    /**
     * 获取健康提醒信息
     * Get health remind information.
     *
     * @return
     */
    private HealthSetEntity getHealthSetEntity() {
        HealthSetEntity entity = new HealthSetEntity();
        // entity.setOpenStatus(HealthSetEntity.STATE_OPEN);
        //上午 开始时间和结束时间
        //The start time and end time of the morning.
        entity.setAmStartHH(9);
        entity.setAmStartMM(00);

        entity.setAmEndHH(12);
        entity.setAmEndMM(0);

        //下午 开始时间和结束时间
        //The start time and end time of the afternoon.
        entity.setPmStartHH(14);
        entity.setPmStartMM(30);
        entity.setPmEndHH(21);
        entity.setPmEndMM(30);
        // 间隔时间
        //Intervals.time
        entity.setIntervalTime(30);
        return entity;
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
                connectSate = R.string.stpes_walk;
                break;

            case BleDevice.STATE_SERVICES_OPENCHANNELSUCCESS://通道打开成功 The channel was opened successfully.
                connectSate = R.string.stpes_walk;
                break;
        }
        // The current connection status of the device.
        setTitle(connectSate);
    }


    private void dealPairClickEvent() {
        if (trackerManager.getNeedSysBlePaired() != null) {
            trackerManager.pair(new DecimalFormat("000000").format(pairPwd));
            if (!trackerManager.getNeedSysBlePaired()) {
                if (dialog == null) {
                    pairPwdEditText = new EditText(this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.input_pair_code_reminder);
                    builder.setView(pairPwdEditText);
                    builder.setNegativeButton(R.string.common_cancel, null);
                    builder.setPositiveButton(R.string.common_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String pwd = pairPwdEditText.getText().toString();
                            if (!TextUtils.isEmpty(pwd) && pwd.matches("^\\d{6}$")) {
                                if (String.valueOf(pairPwd).equals(pwd)) {
                                    trackerManager.confirmPassword();
                                    pairPwdEditText.setText("");
                                } else {
                                    //The pairing password is incorrect.
                                    Toast.makeText(TrackerSettingActivity.this,
                                            R.string.pairing_password_incorrect_reminder, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                //The pairing password must be 6 digits.
                                Toast.makeText(TrackerSettingActivity.this,
                                        R.string.pairing_password_format_invalid, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    dialog = builder.create();
                }
                dialog.show();
            }
        } else {
            Toast.makeText(this, R.string.device_is_not_ready, Toast.LENGTH_SHORT).show();
        }
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
        }

        /**
         * 手环可以下发数据的回调，新协议称可以在此时设置用户信息、闹钟、健康开关等
         * Tracker can be used to send data callback. The new protocol tracker can be set user information, alarm clock, health switch and so on at this time.
         * @param mac
         * @param isNeedSetParam
         */
        @Override
        public void onEnableWriteToDevice(String mac, boolean isNeedSetParam) {
        }
    };


}
