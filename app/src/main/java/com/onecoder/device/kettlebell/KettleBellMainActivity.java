package com.onecoder.device.kettlebell;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.onecoder.device.R;
import com.onecoder.device.adpater.FraPagerAdapter;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.device.base.MyHandler;
import com.onecoder.device.boxing.BoxingFragment;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.control.entity.BleDevice;
import com.onecoder.devicelib.base.control.interfaces.DeviceStateChangeCallback;
import com.onecoder.devicelib.base.entity.BaseDevice;
import com.onecoder.devicelib.base.protocol.entity.SportTypeCntEntity;
import com.onecoder.devicelib.kettlebell.api.KettleBellManager;
import com.onecoder.devicelib.kettlebell.api.entity.HistoryDataEntity;
import com.onecoder.devicelib.kettlebell.api.interfaces.RealTimeDataListener;
import com.onecoder.devicelib.kettlebell.api.interfaces.SynchHistoryDataCallBack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/9/16.
 */
public class KettleBellMainActivity extends BaseActivity implements View.OnClickListener,
        BoxingFragment.OnClickListener, MyHandler.OnHandleMessageListener {
    private static final String TAG = KettleBellMainActivity.class.getSimpleName();
    private static final String KEY_MAC = "mac";
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    private BaseDevice baseDevice;

    private ProgressDialog dialog;

    private FraPagerAdapter fraPagerAdapter;
    private List<BoxingFragment> fragmentList = new ArrayList<>();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final int MSG_ID_UPDATE_SYNC_HISTORY_STATUS = 0;
    private static final int MSG_ID_UPDATE_HISTORY_DATA = 1;
    private MyHandler handler = new MyHandler(this);

    private KettleBellManager kettleBellManager;

    @Override
    protected Manager getManager(Bundle savedInstanceState) {
        if (kettleBellManager != null) {
            return kettleBellManager;
        }
//        kettleBellManager = KettleBellManager.getInstance();
//        //注册状态回调
//        kettleBellManager.registerStateChangeCallback(stateChangeCallback);

        List<KettleBellManager> managerList = KettleBellManagerContainer.getInstance().getManagerList();
        if (managerList == null || managerList.size() == 0) {
            return null;
        }
        BoxingFragment fragment = null;
        for (KettleBellManager kettleBellManager : managerList) {
            if (kettleBellManager == null || kettleBellManager.getBaseDevice() == null
                    || kettleBellManager.getBaseDevice().getMacAddress() == null) {
            }
            fragment = new BoxingFragment();
            fragment.setRealTimeDataLayoutVisibility(View.GONE);
            fragment.setMac(kettleBellManager.getBaseDevice().getMacAddress());
            fragment.setOnClickListener(this);
            fragmentList.add(fragment);

            //注册状态回调
            kettleBellManager.registerStateChangeCallback(stateChangeCallback);
            kettleBellManager.registerRealTimeDataListener(realTimeDataListener);
            // 注册历史数据回调
            kettleBellManager.registerSynchHistoryDataCallBack(historyDataCallBack);
            this.kettleBellManager = kettleBellManager;
        }
        return kettleBellManager;
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
        viewPager.setAdapter(fraPagerAdapter);
    }

    @Override
    public void onHandleMessage(Message msg) {
        if (msg == null) {
            return;
        }
        switch (msg.what) {
            case MSG_ID_UPDATE_SYNC_HISTORY_STATUS:
                if (!(msg.obj instanceof String)) {
                    break;
                }
                String str = (String) msg.obj;
                if (!TextUtils.isEmpty(str)) {
                    Log.i(TAG, "onHandleMessage str:" + str);
                    //infoTextView.setText(str);
                }
                break;

            case MSG_ID_UPDATE_HISTORY_DATA:
                if (!(msg.obj instanceof HistoryDataEntity)) {
                    break;
                }
                Bundle bundle = msg.getData();
                if (bundle == null || bundle.get(KEY_MAC) == null) {
                    break;
                }
                String mac = bundle.getString(KEY_MAC);
                BoxingFragment boxingFragment = getBoxingFragment(mac);
                if (boxingFragment == null) {
                    break;
                }
                HistoryDataEntity historyDataEntity = (HistoryDataEntity) msg.obj;
                List<SportTypeCntEntity> listSportTypeCntData = historyDataEntity.getListSportTypeCntData();
                if (listSportTypeCntData == null) {
                    break;
                }
                StringBuffer stringBuffer = new StringBuffer();
                int cnt = 0;
                for (SportTypeCntEntity sportTypeCntEntity : listSportTypeCntData) {
                    if (sportTypeCntEntity == null) {
                        continue;
                    }
                    cnt++;
                    stringBuffer.append(getDisplayStr(false, sportTypeCntEntity));
                }
                stringBuffer.insert(0, "历史数据数目:" + cnt + "\n");
                boxingFragment.setHistoryDataInfo(stringBuffer.toString());
                break;

            default:
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
                if (kettleBellManager.getConnectState()
                        >= BleDevice.STATE_CONNECTED) {
                    kettleBellManager.disconnect(false);
                }
                break;

            case R.id.main_menu_connect:
                if (kettleBellManager.getConnectState()
                        < BleDevice.STATE_CONNECTED) {
                    kettleBellManager.connectDevice(baseDevice);
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
            case R.id.btn_sync_data:
                if (SynchHistoryDataCallBack.SYNCH_STATUS_SYNCHING ==
                        kettleBellManager.synchHistoryData()) {
                    showDialog(getString(R.string.synch_history), true);
                }
                break;

            case R.id.setting_utc:
                kettleBellManager.setUTC();
                break;

            default:
                break;
        }
    }


    @Override
    public void onClick(String mac, View v) {
        Manager manager = KettleBellManagerContainer.getInstance().getManager(mac);
        if (!(manager instanceof KettleBellManager)) {
            return;
        }
        KettleBellManager kettleBellManager = (KettleBellManager) manager;
        switch (v.getId()) {
            case R.id.get_battery_level_btn:
                kettleBellManager.getBatteryLevel();
                break;

            case R.id.setting_utc:
                kettleBellManager.setUTC();
                break;

            case R.id.get_history_data_btn:
                if (SynchHistoryDataCallBack.SYNCH_STATUS_SYNCHING == kettleBellManager.synchHistoryData()) {
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
     * 设备的连接状态回调
     */
    private DeviceStateChangeCallback stateChangeCallback = new DeviceStateChangeCallback() {
        /**
         * 设备的连接状态变化回调
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
         * 设备可以下发数据的回调
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
        public void onGotBatteryLevel(String mac, int batteryLevel) {
            Log.i(TAG, "onGotBatteryLevel mac:" + mac + " batteryLevel:" + batteryLevel);
            BoxingFragment boxingFragment = getBoxingFragment(mac);
            if (boxingFragment == null) {
                return;
            }
            Manager manager = KettleBellManagerContainer.getInstance().getManager(mac);
            if (manager instanceof KettleBellManager) {
                boxingFragment.setHardwareVersion(((KettleBellManager) manager).getHardwareVersion());
            }
            boxingFragment.setBatteryLevel(batteryLevel);
        }
    };

    /**
     * 历史数据回调接口
     */
    private SynchHistoryDataCallBack historyDataCallBack = new SynchHistoryDataCallBack() {
        /**
         *
         * @param mac 设备MAC
         * @param status 同步状态
         * int SYNCH_STATUS_NOT_SYNCH = -1; //未同步或同步已完成
         * int SYNCH_STATUS_START = 7; //开始同步
         * int SYNCH_STATUS_SYNCHING = 8; //同步中
         * int SYNCH_STATUS_SUCCESS = 9; //同步成功
         * int SYNCH_STATUS_FEILURE = 10; //同步失败
         */
        @Override
        public void onSynchStateChange(String mac, int status) {
            BoxingFragment boxingFragment = getBoxingFragment(mac);
            if (boxingFragment == null) {
                return;
            }
            String str = "";

            switch (status) {
                case SynchHistoryDataCallBack.SYNCH_STATUS_START:
                    str = "开始同步";
                    break;

                case SynchHistoryDataCallBack.SYNCH_STATUS_SYNCHING:
                    str = "同步中";
                    break;

                case SynchHistoryDataCallBack.SYNCH_STATUS_SUCCESS:
                    showDialog(null, false);
                    // dialog.dismiss();
                    str = "同步成功";
                    break;

                case SynchHistoryDataCallBack.SYNCH_STATUS_FEILURE:
                    showDialog(null, false);
                    // dialog.dismiss();
                    str = "同步失败";
                    break;
            }

            if (!TextUtils.isEmpty(str)) {
                Message message = handler.obtainMessage(MSG_ID_UPDATE_SYNC_HISTORY_STATUS, str);
                handler.sendMessage(message);
            }
        }

        /**
         * 历史数据
         * @param historyDataEntity
         */
        @Override
        public void onSynchAllHistoryData(String mac, final HistoryDataEntity historyDataEntity) {
            Log.i(TAG, "onSynchAllHistoryData start mac:" + mac + " historyDataEntity:" + historyDataEntity);
            Message message = handler.obtainMessage(MSG_ID_UPDATE_HISTORY_DATA, historyDataEntity);
            Bundle bundle = new Bundle();
            bundle.putString(KEY_MAC, mac);
            message.setData(bundle);
            handler.sendMessage(message);
            Log.i(TAG, "onSynchAllHistoryData end");
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

    private String getDisplayStr(boolean realtimeData, SportTypeCntEntity fistInfoBase) {
        StringBuffer ret = new StringBuffer();
        if (fistInfoBase == null) {
            return ret.toString();
        }

        ret.append("\n>>>>>>单条历史数据开始>>>>>");
        ret.append("\n重量:" + fistInfoBase.getWeight() + " LB");
        ret.append("\n用户编号:" + fistInfoBase.getUserNumber());
        ret.append("\n运动次数:" + fistInfoBase.getSportCnt());
        ret.append("\n起始时间:" + simpleDateFormat.format(fistInfoBase.getStartSportUtc()));
        ret.append("\n结束时间:" + simpleDateFormat.format(fistInfoBase.getEndSportUtc()));
        ret.append("\n运动详情:");

        List<SportTypeCntEntity.SingleSportTypeTimer> singleSportTypeCntList = fistInfoBase.getSingleSportTypeCntList();
        if (singleSportTypeCntList == null || singleSportTypeCntList.size() == 0) {
            return ret.toString();
        }
        for (SportTypeCntEntity.SingleSportTypeTimer singleSportTypeTimer : singleSportTypeCntList) {
            if (singleSportTypeTimer == null) {
                continue;
            }
            ret.append("\n运动类型:" + getSportTypeStr(singleSportTypeTimer.getSportType()));
            ret.append("\n持续时间:" + singleSportTypeTimer.getSportDuration() + " ms");
        }
        ret.append("\n<<<<<<单条历史数据结束<<<<<<\n");
        return ret.toString();
    }

    private String getSportTypeStr(SportTypeCntEntity.SportType sportType) {
        String ret = "";
        if (sportType == null) {
            return ret;
        }
        switch (sportType) {
            case Other:
                ret = "其它运动";
                break;

            case AtlasSwing:
                ret = "阿特拉斯秋千运动";
                break;

            case PullOver:
                ret = "拉伸运动";
                break;

            case HighPull:
                ret = "高拉运动";
                break;

            case Extension:
                ret = "伸展运动";
                break;

            default:
                break;
        }
        return ret;
    }

    @Override
    protected void onStop() {
        showDialog(null, false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_OK);
        //注销各种回调
//        kettleBellManager.unregistStateChangeCallback(stateChangeCallback);
//        kettleBellManager.disconnect(false);
//        kettleBellManager.closeDevice();

        KettleBellManagerContainer kettleBellManagerContainer = KettleBellManagerContainer.getInstance();
        if (kettleBellManagerContainer != null) {
            List<KettleBellManager> managerList = kettleBellManagerContainer.getManagerList();
            if (managerList != null) {
                for (KettleBellManager kettleBellManager : managerList) {
                    if (kettleBellManager == null) {
                        continue;
                    }
                    kettleBellManager.unregistStateChangeCallback(stateChangeCallback);
                    kettleBellManager.unregisterRealTimeDataListener(realTimeDataListener);
                    kettleBellManager.unregisterSynchHistoryDataCallBack(historyDataCallBack);
                }
            }
        }
        super.onDestroy();
    }

}
