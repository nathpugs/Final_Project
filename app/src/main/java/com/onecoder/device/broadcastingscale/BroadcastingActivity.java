package com.onecoder.device.broadcastingscale;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.chipsea.healthscale.CsAlgoBuilderEx;
import com.chipsea.healthscale.ScaleActivateStatusEvent;
import com.onecoder.device.R;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.devicelib.broadcastingscale.api.BroadcastingScaleManager;
import com.onecoder.devicelib.broadcastingscale.api.entity.DeviceState;
import com.onecoder.devicelib.broadcastingscale.api.interfaces.DeviceStateChangeCallback;
import com.onecoder.devicelib.broadcastingscale.api.interfaces.OnWeighResultListener;
import com.onecoder.devicelib.broadcastingscale.protocol.entity.MeasureData;

public class BroadcastingActivity extends BaseActivity implements
        DeviceStateChangeCallback, OnWeighResultListener, ScaleActivateStatusEvent {
    private static final String TAG = BroadcastingActivity.class.getSimpleName();
    public static final String KEY_MAC = "mac";
    private TextView connectStatus;
    private TextView measureDataTxt;
    private TextView bodyFatDataTxt;
    private String mac;
    private BroadcastingScaleManager broadcastingScaleManager;
    private CsAlgoBuilderEx csAlgoBuilderEx;
    private boolean bodyFatSdkActivated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcasting_scale);
        connectStatus = findViewById(R.id.device_connect_status);
        measureDataTxt = findViewById(R.id.measure_data_txt);
        bodyFatDataTxt = findViewById(R.id.body_fat_data_txt);

        Intent intent = getIntent();
        if (intent != null) {
            mac = intent.getStringExtra(KEY_MAC);
            Log.i(TAG, "onCreate mac:" + mac + " currentThread:" + Thread.currentThread().getId());
        }
        if (TextUtils.isEmpty(mac)) {
            finish();
            return;
        }

        broadcastingScaleManager = new BroadcastingScaleManager();
        broadcastingScaleManager.registerStateChangeCallback(this);
        broadcastingScaleManager.registerCheckSystemBleCallback(this);
        broadcastingScaleManager.registerOnWeighResultListener(this);
        broadcastingScaleManager.init();
        broadcastingScaleManager.bind(mac);
        onStateChange(mac, broadcastingScaleManager.getDeviceState());

        csAlgoBuilderEx = new CsAlgoBuilderEx(getApplicationContext());
        if (!csAlgoBuilderEx.getAuthStatus()) {
            csAlgoBuilderEx.Authorize(mac, this);
        }
    }

    /**
     * 状态变化回调
     *
     * @param mac    设备MAC
     * @param status 设备状态
     */
    @Override
    public void onStateChange(final String mac, final DeviceState status) {
        Log.i(TAG, "onStateChange mac:" + mac + " status:" + status + " currentThread:" + Thread.currentThread().getId());
        int connectSate;
        switch (status) {
            case Connected:  //已连接
                connectSate = R.string.connected;
                break;
            case Disconnected:  //已断开
                connectSate = R.string.disconnected;
                break;
            default:
                return;
        }
        connectStatus.setText("设备当前状态 " + getString(connectSate));
    }

    /**
     * 称量出结果了
     *
     * @param mac         设备MAC
     * @param measureData 称量出的数据
     */
    @Override
    public void onWeighResult(String mac, final MeasureData measureData) {
        if (measureData == null) {
            return;
        }
        Log.i(TAG, "onWeighResult mac:" + mac + " measureData:" + measureData + " currentThread:" + Thread.currentThread().getId());
        String devTypeStr = "";
        switch (measureData.getDeviceType()) {
            case MeasureData.DEVICE_TYPE_WEIGH_SCALE:
                devTypeStr = "体重秤";
                break;
            case MeasureData.DEVICE_TYPE_BODY_FAT_SCALE:
                devTypeStr = "体脂秤";
                break;
            default:
                break;
        }
        String unitStr = "";
        switch (measureData.getUnit()) {
            case MeasureData.UNIT_KG:
                unitStr = "千克";
                break;
            case MeasureData.UNIT_JIN:
                unitStr = "斤";
                break;

            case MeasureData.UNIT_LB:
                unitStr = "磅";
                break;

            case MeasureData.UNIT_ST_LB:
                unitStr = "石英";
                break;
            default:
                break;
        }

        StringBuffer stringBuffer = new StringBuffer();
        measureData.getDeviceId();
        stringBuffer.append("版本号:" + measureData.getVersion() + "\n");
        stringBuffer.append("设备ID:" + measureData.getDeviceId() + "\n");
        stringBuffer.append("设备类型:" + devTypeStr + "\n");
        stringBuffer.append("单位:" + unitStr + "\n");
        stringBuffer.append("测量流水号:" + measureData.getMeasuringNumber() + "\n");
        stringBuffer.append("重量:" + measureData.getWeight() + "\n");
        stringBuffer.append("电阻:" + measureData.getResistance() + "\n");
        measureData.convertWeightUnitToKg();
        stringBuffer.append(">>>>>转换成KG后的重量:" + measureData.getWeight() + "KG\n");
        measureDataTxt.setText(stringBuffer + "\n");
        stringBuffer.setLength(0);//清空

        if (!csAlgoBuilderEx.getAuthStatus()) {
            Log.i(TAG, "SDK未被激活!");
        }
        StringBuffer bodyFatStringBuffer = calcBodyFat(measureData.getWeight(), measureData.getResistance());
        bodyFatDataTxt.setText(bodyFatStringBuffer);
        bodyFatStringBuffer.setLength(0);//清空
    }

    private StringBuffer calcBodyFat(double weight, double resistance) {
        StringBuffer stringBuffer = new StringBuffer();

        /**
         * 设置用户信息
         * @param height 身高 单位cm
         * @param sex 男-1 女-0
         * @param age 年龄
         * @param weight 当前测量体重 单位kg
         * @param r 当前测量电阻
         *
         */
        csAlgoBuilderEx.setUserInfo(168f, (byte) 1, 29,
                new Double(weight).floatValue(), new Double(resistance).floatValue());

        /**如果需要进行电阻滤波，请调用以下方法
         * public float setUserInfo(float height, byte sex, int age, float curWeight, float curR, Date curTime, float lastR, Date lastTime)
         * 加入电阻滤波后的构造函数（用于在根据蓝牙秤上传的数据后调用并计算结果）
         * @param height 身高
         * @param sex 男-1 女-0
         * @param age 年龄
         * @param curWeight 当前测量体重
         * @param curR 当前测量电阻
         * @param curTime 当前测量时间
         * @param lastR 上一次测量电阻
         * @param lastTime 上一次测量时间
         * @return 返回滤波后的电阻
         */

        stringBuffer.append("细胞外液EXF:" + csAlgoBuilderEx.getEXF() + "\n");
        stringBuffer.append("细胞内液Inf:" + csAlgoBuilderEx.getInF() + "\n");
        stringBuffer.append("总水重TF:" + csAlgoBuilderEx.getTF() + "\n");
        stringBuffer.append("含水百分比TFR:" + csAlgoBuilderEx.getTFR() + "\n");
        stringBuffer.append("去脂体重LBM:" + csAlgoBuilderEx.getLBM() + "\n");
        stringBuffer.append("肌肉重(含水)SLM:" + csAlgoBuilderEx.getSLM() + "\n");
        stringBuffer.append("肌肉率SLMPercent:" + csAlgoBuilderEx.getSLMPercent() + "\n");
        stringBuffer.append("骨骼肌SMM:" + csAlgoBuilderEx.getSMM() + "\n");
        stringBuffer.append("蛋白质PM:" + csAlgoBuilderEx.getPM() + "\n");
        stringBuffer.append("脂肪重FM:" + csAlgoBuilderEx.getFM() + "\n");
        stringBuffer.append("脂肪百份比BFR:" + csAlgoBuilderEx.getBFR() + "\n");
        stringBuffer.append("水肿测试EE:" + csAlgoBuilderEx.getEE() + "\n");
        stringBuffer.append("肥胖度OD:" + csAlgoBuilderEx.getOD() + "\n");
        stringBuffer.append("肌肉控制MC:" + csAlgoBuilderEx.getMC() + "\n");
        stringBuffer.append("脂肪控制FC:" + csAlgoBuilderEx.getFC() + "\n");
        stringBuffer.append("体重控制WC:" + csAlgoBuilderEx.getWC() + "\n");
        stringBuffer.append("基础代谢BMR:" + csAlgoBuilderEx.getBMR() + "\n");
        stringBuffer.append("骨(无机盐)MSW:" + csAlgoBuilderEx.getMSW() + "\n");
        stringBuffer.append("内脏脂肪等级VFR:" + csAlgoBuilderEx.getVFR() + "\n");
        stringBuffer.append("身体年龄BodyAge:" + csAlgoBuilderEx.getBodyAge() + "\n");
        stringBuffer.append("标准体重BW:" + csAlgoBuilderEx.getBW() + "\n");
        stringBuffer.append("评分:" + csAlgoBuilderEx.getScore() + "\n");
        return stringBuffer;
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
            broadcastingScaleManager.bind(mac);
        } else {
            broadcastingScaleManager.unbind();
        }
    }

    @Override
    public void onActivateSuccess() {
        //激活成功
        bodyFatSdkActivated = true;
        Log.i(TAG, "激活成功!");
    }

    @Override
    public void onActivateFailed() {
        //激活失败,SDK被冻结!
        bodyFatSdkActivated = false;
        Log.i(TAG, "激活失败,SDK被冻结!");
    }

    @Override
    public void onHttpError(int i, String s) {
        //网络错误 激活失败,需要重新激活
        bodyFatSdkActivated = false;
        Log.i(TAG, "网络错误。激活失败,需要重新激活,ErrCode:" + i + " msg:" + s);
    }

    @Override
    protected void onDestroy() {
        broadcastingScaleManager.unregisterStateChangeCallback(this);
        broadcastingScaleManager.unregisterCheckSystemBleCallback(this);
        broadcastingScaleManager.unregisterOnWeighResultListener(this);
        broadcastingScaleManager.reset();
        super.onDestroy();
    }

}
