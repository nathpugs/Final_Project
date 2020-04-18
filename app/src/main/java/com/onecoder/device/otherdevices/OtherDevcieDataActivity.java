package com.onecoder.device.otherdevices;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.onecoder.device.R;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.device.utils.GToast;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.control.entity.BleDevice;
import com.onecoder.devicelib.base.control.interfaces.DeviceStateChangeCallback;
import com.onecoder.devicelib.base.entity.BaseDevice;
import com.onecoder.devicelib.base.entity.DeviceType;
import com.onecoder.devicelib.base.protocol.entity.RTHeartRate;
import com.onecoder.devicelib.cadence.api.CadenceManager;
import com.onecoder.devicelib.cadence.api.entity.CadencePrimitivEntity;
import com.onecoder.devicelib.cadence.api.entity.CadenceSportEntity;
import com.onecoder.devicelib.cadence.api.interfaces.CadenceListener;
import com.onecoder.devicelib.heartrate.api.HeartRateMonitorManager;
import com.onecoder.devicelib.heartrate.api.interfaces.RealTimeDataListener;
import com.onecoder.devicelib.heartrate.api.interfaces.SynchHistoryDataListener;
import com.onecoder.devicelib.jump.api.JumpManager;
import com.onecoder.devicelib.jump.api.entity.JumpPrimitivData;
import com.onecoder.devicelib.jump.api.entity.JumpSportData;
import com.onecoder.devicelib.jump.api.interfaces.JumpDataListener;

import java.util.ArrayList;
import java.util.List;

public class OtherDevcieDataActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = OtherDevcieDataActivity.class.getSimpleName();

    private TextView connectStatus;
    private ScrollView mainView;
    private ScrollView rtScrollView;
    private TextView rtData;
    private TextView nrtData;
    private Button syncHistoryDataBtn;

    private BaseDevice baseDevice;
    private Manager manager;
    private int showRealTimeDataMax = 100;
    private List<RTHeartRate> rtHeartRateList = new ArrayList<>();

    private ProgressDialog dialog;

    @Override
    protected Manager getManager(Bundle savedInstanceState) {
        if (manager != null) {
            return manager;
        }
        baseDevice = (BaseDevice) getIntent().getSerializableExtra(KEY_BASE_DEVICE);
        if (baseDevice == null || baseDevice.getDeviceType() == null) {
            GToast.show(this, "Device type is invalid");
            finish();
        }

        // 根据设备类型，设置数据回调接口
        switch (baseDevice.getDeviceType()) {
            case HRMonitor:
                // 设置心率带数据监听器
                HeartRateMonitorManager.getInstance().registerRealTimeDataListener(realTimeDataListener);
                HeartRateMonitorManager.getInstance().registerSynchHistoryDataListener(synchHistoryDataListener);
                manager = HeartRateMonitorManager.getInstance();
                break;
            case Cadence:
                // 设置踏频数据监听器
                CadenceManager.getInstance().setCadenceDataListener(cadenceListener);
                manager = CadenceManager.getInstance();
                break;
            case Jump:
                // 设置跳绳数据监听器
                JumpManager.getInstance().setJumpDataListener(jumpDataListener);
                manager = JumpManager.getInstance();
                break;

            default:
                GToast.show(this, "Device type is invalid");
                finish();
                break;
        }

        if (manager != null) {
            manager.registerStateChangeCallback(stateChangeCallback);
        }
        return manager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate " + this);
        mainView = (ScrollView) LayoutInflater.from(this).inflate(R.layout.other_devcie_data_act, null);
        setContentView(mainView);

        findView();
        updateConnectStatus(BleDevice.STATE_CONNECTED);
        dialog = new ProgressDialog(this);
    }

    private void findView() {
        connectStatus = mainView.findViewById(R.id.device_connect_status);
        rtScrollView = mainView.findViewById(R.id.rt_scrollview);
        rtData = mainView.findViewById(R.id.rt_data_tv);
        nrtData = mainView.findViewById(R.id.nrt_data_tv);
        syncHistoryDataBtn = mainView.findViewById(R.id.sync_history_data_btn);
        boolean isHRMDev = baseDevice.getDeviceType() == DeviceType.HRMonitor;
        syncHistoryDataBtn.setVisibility(isHRMDev ? View.VISIBLE : View.GONE);
        nrtData.setVisibility(isHRMDev ? View.VISIBLE : View.GONE);
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
        connectStatus.setText("设备当前状态 " + getString(connectSate));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sync_history_data_btn:
                if (SynchHistoryDataListener.SYNCH_STATUS_SYNCHING ==
                        HeartRateMonitorManager.getInstance().synchHistoryData()) {
                    showDialog(getString(R.string.synch_history), true);
                }
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
    protected void onStop() {
        showDialog(null, false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //断开设备，断开后不重新连接
        if (manager != null) {
            if (manager instanceof HeartRateMonitorManager) {
                HeartRateMonitorManager.getInstance().unregisterRealTimeDataListener(realTimeDataListener);
                HeartRateMonitorManager.getInstance().unregisterSynchHistoryDataListener(synchHistoryDataListener);
            }
            manager.unregistStateChangeCallback(stateChangeCallback);
            manager.disconnect(false);
            manager.closeDevice();
        }
        setResult(RESULT_OK);
    }

    private RealTimeDataListener realTimeDataListener = new RealTimeDataListener() {
        /**
         * 接收到实时数据
         *
         * @param mac         设备MAC
         * @param rtHeartRate 实时心率
         */
        @Override
        public void onRealTimeData(String mac, RTHeartRate rtHeartRate) {
            if (rtHeartRate != null) {
                rtHeartRateList.add(rtHeartRate);
                if (rtHeartRateList.size() > showRealTimeDataMax) {
                    rtHeartRateList.remove(0);
                }
                rtData.setText("");
                for (RTHeartRate data : rtHeartRateList) {
                    rtData.append("heart rate:" + (data.getHeartRate()) + " time:" + data.getTime() + "\r\n");
                }
                rtScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }
    };

    /**
     * 历史数据回调接口
     */
    private SynchHistoryDataListener synchHistoryDataListener = new SynchHistoryDataListener() {
        /**
         * 同步状态
         * @param status ：
         *         SYNCH_STATUS_NOT_SYNCH = -1; //未同步或同步已完成
         *         SYNCH_STATUS_START = 7; //开始同步
         *         SYNCH_STATUS_SYNCHING = 8; //同步中
         *         SYNCH_STATUS_SUCCESS = 9; //同步成功
         *         SYNCH_STATUS_FEILURE = 10; //同步失败
         */
        @Override
        public void onSynchStateChange(String mac, int status) {
            String str;
            switch (status) {
                case SynchHistoryDataListener.SYNCH_STATUS_START:
                    str = "开始同步";
                    break;

                case SynchHistoryDataListener.SYNCH_STATUS_SYNCHING:
                    str = "同步中";
                    break;

                case SynchHistoryDataListener.SYNCH_STATUS_SUCCESS:
                    showDialog(null, false);
                    // dialog.dismiss();
                    str = "同步成功";
                    break;

                case SynchHistoryDataListener.SYNCH_STATUS_FEILURE:
                    showDialog(null, false);
                    // dialog.dismiss();
                    str = "同步失败";
                    break;

                default:
                    return;
            }
            if (!TextUtils.isEmpty(str)) {
                Toast.makeText(OtherDevcieDataActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * 获取到历史数据
         *
         * @param mac             设备MAC
         * @param rtHeartRateList 获取到的历史数据
         */
        @Override
        public void onGotAllHistoryData(String mac, List<RTHeartRate> rtHeartRateList) {
            Log.i(TAG, "onGotAllHistoryData start rtHeartRateList:" + rtHeartRateList);
            nrtData.setText("心率历史数据:\n"
                    + "数据条数:" + (rtHeartRateList != null ? rtHeartRateList.size() : 0) + "\n"
                    + rtHeartRateList);
            Log.i(TAG, "onGotAllHistoryData end");
        }
    };

    /**
     * 跳绳实时数据接口
     */
    private JumpDataListener jumpDataListener = new JumpDataListener() {
        /**
         * 跳绳实时数据回调函数
         * @param sportData
         * @param primitivData
         */
        @Override
        public void onJumpData(JumpSportData sportData, JumpPrimitivData primitivData) {
            if (primitivData != null) {
                String text = rtData.getText().toString() + "jump = " + (primitivData.toString()) + "\r\n";
                rtData.setText(text);
            }
        }
    };

    /**
     * 踏频数据回调接口
     */
    private CadenceListener cadenceListener = new CadenceListener() {
        /**
         * 踏频实时数据回调函数
         * @param data
         * @param sportEntity
         */
        @Override
        public void onCadenceData(CadencePrimitivEntity data, CadenceSportEntity sportEntity) {
            if (data != null) {
                String text = rtData.getText().toString() + "cadence = " + (data.toString()) + "\r\n";
                rtData.setText(text);
            }
        }
    };

    /**
     * 设备的连接状态接口
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
            //连接成功，并发现服务进行页面跳转，
        }

        @Override
        public void onEnableWriteToDevice(String mac, boolean isNeedSetParam) {

        }
    };


}
