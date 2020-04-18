package com.onecoder.device.scale;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onecoder.device.R;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.device.utils.GToast;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.control.entity.BleDevice;
import com.onecoder.devicelib.base.control.interfaces.DeviceStateChangeCallback;
import com.onecoder.devicelib.base.protocol.CommonProtocol;
import com.onecoder.devicelib.base.protocol.entity.ScaleStableData;
import com.onecoder.devicelib.scale.api.ScaleManager;
import com.onecoder.devicelib.scale.api.interfaces.ScaleNewProtocolDataListener;
import com.onecoder.devicelib.scale.api.interfaces.ScaleOldProtocolDataListener;
import com.onecoder.devicelib.scale.api.interfaces.ScaleUserInfoCallBack;
import com.onecoder.devicelib.scale.protocol.entity.BHistoryDataInfo;
import com.onecoder.devicelib.scale.protocol.entity.BUserFatInfo;
import com.onecoder.devicelib.scale.protocol.entity.BUserInfo;

import java.util.List;

public class ScaleDataActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = ScaleDataActivity.class.getSimpleName();

    private TextView connectStatus;
    private TextView weight_weight;
    private TextView weight_bmi;
    private TextView weight_bofy_fat;
    private TextView weight_bone;
    private TextView weight_visceral_fat;
    private TextView weight_basal;
    private TextView weight_mass;
    private TextView weight_water;
    private LinearLayout impedanceLayout;
    private TextView eleImpedanceTv;
    private TextView encryptImpedanceTv;
    private TextView historyTv;

    private Button newProtocolScaleSysch;

    int[] weightDataName = new int[]{R.string.weight_index, R.string.weight_body_fat,
            R.string.weight_bone,
            R.string.weight_visceral_fat, R.string.weight_metabolism,
            R.string.weight_muscle, R.string.weight_water,};
    private TextView device_mac;
    private TextView device_user_id;
    private ScaleManager scaleManager;
    private Button oldScaleeditUser;
    private Button settingUnit;

    @Override
    protected Manager getManager(Bundle savedInstanceState) {
        if (scaleManager != null) {
            return scaleManager;
        }
        scaleManager = ScaleManager.getInstance();
        scaleManager.registerStateChangeCallback(stateChangeCallback);
        scaleManager.setOldScaleDataListener(oldProtocolDataListener);
        scaleManager.setScaleUserInfoCallBack(userInfoCallBack);
        scaleManager.setNewScaleDataListener(scaleNewProtocolDataListener);
        return scaleManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scale_data_act);
        findView();
        updateConnectStatus(BleDevice.STATE_CONNECTED);
    }


    private void findView() {

        connectStatus = (TextView) findViewById(R.id.device_connect_status);

        device_mac = (TextView) findViewById(R.id.text_mac);

        device_user_id = (TextView) findViewById(R.id.text_id);


        weight_weight = (TextView) findViewById(R.id.weight_weight);
        weight_bmi = (TextView) findViewById(R.id.weight_bmi);
        weight_bofy_fat = (TextView) findViewById(R.id.weight_bofy_fat);
        weight_bone = (TextView) findViewById(R.id.weight_bone);
        weight_visceral_fat = (TextView) findViewById(R.id.weight_visceral_fat);
        weight_basal = (TextView) findViewById(R.id.weight_basal);
        weight_mass = (TextView) findViewById(R.id.weight_mass);
        weight_water = (TextView) findViewById(R.id.weight_water);
        impedanceLayout = (LinearLayout) findViewById(R.id.impedance_layout);
        eleImpedanceTv = (TextView) findViewById(R.id.eleImpedance_tv);
        encryptImpedanceTv = (TextView) findViewById(R.id.encryptImpedance_tv);
        historyTv = (TextView) findViewById(R.id.history_tv);

        newProtocolScaleSysch = (Button) findViewById(R.id.new_scale_history);
        oldScaleeditUser = (Button) findViewById(R.id.old_scale_editUser);
        settingUnit = (Button) findViewById(R.id.setting_unit);

        impedanceLayout.setVisibility(scaleManager.getProtocolType() == CommonProtocol.NEW_PROTOCOL
                ? View.VISIBLE : View.GONE);

        newProtocolScaleSysch.setOnClickListener(this);
        oldScaleeditUser.setOnClickListener(this);
        settingUnit.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.new_scale_history:
                if (scaleManager.getProtocolType() == CommonProtocol.OLD_PROTOCOL) {
                    GToast.show(this, "旧协议秤的历史数据为主动上传");
                    return;
                }
                scaleManager.requestHistory();
                break;
            case R.id.old_scale_editUser:
                if (scaleManager.getProtocolType() == CommonProtocol.NEW_PROTOCOL) {
                    GToast.show(this, "新协议秤不支持该功能");
                    return;
                }
                BUserInfo scaleUser = getScaleUser();
                int editState = 2;  //  指定用戶(指定秤重这用户信息)：2    添加用戶：0
                scaleManager.editUserInfo(scaleUser, editState);
                break;
            case R.id.setting_unit:
                int unit = 0;  // 设置单位   （0--KG   1---lb  2---ST  3----公斤）
                scaleManager.setUnit(unit);
                break;

        }
    }

    private BUserInfo getScaleUser() {
        BUserInfo bUserInfo = new BUserInfo();
        bUserInfo.setAge(22);
        bUserInfo.setHeight(170);
        bUserInfo.setSex(0);  //用户性别  0.女  1.男
        //用户的号码 （0-7），由秤分配，若是添加ID未255
        bUserInfo.setId(2);
        return bUserInfo;
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
                connectSate = R.string.stpes_walk;
                break;

            case BleDevice.STATE_SERVICES_OPENCHANNELSUCCESS:
                connectSate = R.string.stpes_walk;
                break;
        }
        connectStatus.setText(getString(connectSate));
    }


    /**
     * 展示秤上传的体重数据
     *
     * @param mBMeasureResultInfo
     */
    private void showWeightData(BUserFatInfo mBMeasureResultInfo) {
        Log.i(TAG, "showWeightData mBMeasureResultInfo:" + mBMeasureResultInfo);
        weight_weight.setText(getString(R.string.Weight) + "\t\t" + mBMeasureResultInfo.getUser().getWeight() + "");

        weight_bmi.setText(getString(weightDataName[0]) + "\t\t" + mBMeasureResultInfo.getBmi());

        weight_bofy_fat.setText(getString(weightDataName[1]) + "\t\t" + mBMeasureResultInfo.getFat());

        weight_bone.setText(getString(weightDataName[2]) + "\t\t" + mBMeasureResultInfo.getBone());
        weight_basal.setText(getString(weightDataName[4]) + "\t\t" + mBMeasureResultInfo.getBmr());

        weight_visceral_fat.setText(getString(weightDataName[3]) + "\t\t" + mBMeasureResultInfo.getVisceralFat());

        weight_mass.setText(getString(weightDataName[5]) + "\t\t" + mBMeasureResultInfo.getMuscle());

        weight_water.setText(getString(weightDataName[6]) + "\t\t" + mBMeasureResultInfo.getWater());

    }


    /**
     * 更新秤分配的用户信息
     */
    private void updateBUserInfo(String mac, int id) {
        device_mac.setText("mac： " + mac);
        device_user_id.setText("ID: " + id);
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_OK);
        super.onDestroy();
        //断开设备，断开后不重新连接
        scaleManager.disconnect(false);
        scaleManager.unregistStateChangeCallback(stateChangeCallback);
        scaleManager.closeDevice();
    }

    /**
     * 新协议称的称重数据接口
     */
    private ScaleNewProtocolDataListener scaleNewProtocolDataListener = new ScaleNewProtocolDataListener() {
        /**
         * 实时数据
         *
         * @param weight 体重 单位:kg
         */
        @Override
        public void onRealTimeData(float weight) {
            //Log.i(TAG, "onRealTimeData weight:" + weight);
            weight_weight.setText("体重:" + weight);
        }

        /**
         * 称重的稳定数据
         * @param entity
         */
        @Override
        public void onStableWeightData(ScaleStableData entity) {
            if (entity == null) {
                return;
            }
            Log.i(TAG, "onStableWeightData entity:" + entity);

            String weightUitStr = "";
            switch (entity.getWeightUnit()) {
                case ScaleStableData.UNIT_KG:
                    weightUitStr = "Kg";
                    break;

                case ScaleStableData.UNIT_LB:
                    weightUitStr = "Lb";
                    break;

                case ScaleStableData.UNIT_ST:
                    weightUitStr = "St";
                    break;

                case ScaleStableData.UNIT_G:
                    weightUitStr = "Jin";
                    break;

                default:
                    break;
            }
            weight_weight.setText("体重:" + entity.getWeight() + " " + weightUitStr);
            eleImpedanceTv.setText(String.valueOf(entity.getEleImpedance()));
            encryptImpedanceTv.setText(String.valueOf(entity.getEncryptImpedance()));
        }

        /**
         * 历史数据,历史数据需发送请求
         * @param weightList
         */
        @Override
        public void onHistoryWeightData(List<ScaleStableData> weightList) {
            if (weightList == null) {
                return;
            }
            Log.i(TAG, "onHistoryWeightData weights.size:" + weightList.size() + " weights:" + weightList);
            historyTv.setText(weightList != null ? weightList.toString() : "null");
        }
    };

    /**
     * 用户信息同步接口
     */
    private ScaleUserInfoCallBack userInfoCallBack = new ScaleUserInfoCallBack() {

        /**
         * 用户成功同步到称的用户信息回调
         * @param userList
         */
        @Override
        public void onSucess(List<BUserInfo> userList) {
            if (userList == null) {
                return;
            }
            Log.i(TAG, "ScaleUserInfoCallBack onSucess userList.size:" + userList.size() + " userList:" + userList);
        }

        /**
         *
         * @param mac 设备的mac地址
         * @param id  秤分配好的用户ID（若连接时设置的用户ID不是255，分配的还是设置的ID）
         */
        @Override
        public void onSuccessGetID(String mac, int id) {
            Log.i(TAG, "ScaleUserInfoCallBack onSuccessGetID:" + " mac:" + mac + " id:" + id);
        }
    };

    /**
     * 旧协议称的体重数据接口回调
     */
    private ScaleOldProtocolDataListener oldProtocolDataListener = new ScaleOldProtocolDataListener() {

        /**
         * 历史体重数据的回调
         * @param mac
         * @param historyData
         */
        @Override
        public void onHistoryDataChange(String mac, BHistoryDataInfo historyData) {
            Log.i(TAG, "ScaleOldProtocolDataListener onHistoryDataChange"
                    + " mac:" + mac + " historyData:" + historyData);
            historyTv.setText(historyData != null ? historyData.toString() : "null");
        }

        /**
         * 脂肪数据
         * @param mac
         * @param fatData
         */
        @Override
        public void onFatDataChange(String mac, BUserFatInfo fatData) {
            Log.i(TAG, "ScaleOldProtocolDataListener onFatDataChange mac:" + mac + " fatData:" + fatData);
            showWeightData(fatData);
        }

        /**
         *
         * @param mac  设备的mac地址
         * @param weight 稳定体重
         */
        @Override
        public void onStableWeightData(String mac, double weight) {
            Log.i(TAG, "ScaleOldProtocolDataListener onStableWeightData mac:" + mac + " weight:" + weight);
        }
    };


    /**
     * 秤的连接状态接口
     */
    private DeviceStateChangeCallback stateChangeCallback = new DeviceStateChangeCallback() {

        /**
         * 秤的连接状态变化回调
         * @param mac ：mac地址
         * @param status ：状态值
         */
        @Override
        public void onStateChange(String mac, int status) {
            Log.i(TAG, "DeviceStateChangeCallback onStateChange mac:" + mac + " status:" + status);
            updateConnectStatus(status);
            //连接成功，并发现服务进行页面跳转，
        }

        @Override
        public void onEnableWriteToDevice(String mac, boolean isNeedSetParam) {
            Log.i(TAG, "DeviceStateChangeCallback onEnableWriteToDevice mac:"
                    + mac + " isNeedSetParam:" + isNeedSetParam);
        }
    };


}
