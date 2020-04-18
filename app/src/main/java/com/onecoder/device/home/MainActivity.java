package com.onecoder.device.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.onecoder.device.Configs;
import com.onecoder.device.R;
import com.onecoder.device.armband.ArmBandScanActivity;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.device.bikecomputer.BikeComputerScanActivity;
import com.onecoder.device.devicemanager.DeviceManagerActivity;
import com.onecoder.device.hubconfig.SelectHubDevTypeActivity;
import com.onecoder.device.otherdevices.OtherDeviceScanActivity;
import com.onecoder.device.scale.ScaleMainActivity;
import com.onecoder.device.tracker.TrackerScanActivity;
import com.onecoder.devicelib.FitBleKit;
import com.onecoder.devicelib.base.entity.BaseDevice;
import com.onecoder.devicelib.base.entity.DeviceType;
import com.onecoder.devicelib.base.protocol.CommonProtocol;

public class MainActivity extends BaseActivity implements View.OnClickListener {


    private static final String TAG = MainActivity.class.getSimpleName();

    private Button oldProtocolScaleBtn;
    private Button newProtocolScaleBtn;
    private Button oldProtocolTrackerBtn;
    private Button newProtocolTrackerBtn;
    private Button heartRateBtn;
    private Button cadenceBtn;
    private Button jumpBtn;
    private Button armbandBtn;
    private Button kettleBellBtn;
    private Button bikeComputerBtn;
    private Button hubBtn;
    private Button boxingBtn;
    private Button broadcastingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_act_main);
        FitBleKit.getInstance().initSDK(this);

        initView();
        initEvent();
    }

    private void initView() {
        oldProtocolScaleBtn = (Button) findViewById(R.id.old_protocol_scale_btn);
        newProtocolScaleBtn = (Button) findViewById(R.id.new_protocol_scale_btn);
        oldProtocolTrackerBtn = (Button) findViewById(R.id.old_protocol_traker_btn);
        newProtocolTrackerBtn = (Button) findViewById(R.id.new_protocol_traker_btn);
        heartRateBtn = (Button) findViewById(R.id.heart_rate_btn);
        cadenceBtn = (Button) findViewById(R.id.cadence_btn);
        jumpBtn = (Button) findViewById(R.id.jump_btn);
        armbandBtn = (Button) findViewById(R.id.armband_btn);
        kettleBellBtn = (Button) findViewById(R.id.kettle_bell_btn);
        bikeComputerBtn = (Button) findViewById(R.id.bike_computer_btn);
        hubBtn = (Button) findViewById(R.id.hub_btn);
        boxingBtn = (Button) findViewById(R.id.boxing_btn);
        broadcastingBtn = (Button) findViewById(R.id.broadcasting_btn);

        oldProtocolScaleBtn.setVisibility(Configs.DeviceEnable.SCALE_ENABLE ? View.VISIBLE : View.GONE);
        newProtocolScaleBtn.setVisibility(Configs.DeviceEnable.SCALE_ENABLE ? View.VISIBLE : View.GONE);
        oldProtocolTrackerBtn.setVisibility(Configs.DeviceEnable.TRACKER_ENABLE ? View.VISIBLE : View.GONE);
        newProtocolTrackerBtn.setVisibility(Configs.DeviceEnable.TRACKER_ENABLE ? View.VISIBLE : View.GONE);
        heartRateBtn.setVisibility(Configs.DeviceEnable.HEART_RATE_ENABLE ? View.VISIBLE : View.GONE);
        cadenceBtn.setVisibility(Configs.DeviceEnable.CADENCE_ENABLE ? View.VISIBLE : View.GONE);
        jumpBtn.setVisibility(Configs.DeviceEnable.JUMP_ENABLE ? View.VISIBLE : View.GONE);
        armbandBtn.setVisibility(Configs.DeviceEnable.ARMBAND_ENABLE ? View.VISIBLE : View.GONE);
        kettleBellBtn.setVisibility(Configs.DeviceEnable.KETTLE_BELL_ENABLE ? View.VISIBLE : View.GONE);
        bikeComputerBtn.setVisibility(Configs.DeviceEnable.BIKE_COMPUTER_ENABLE ? View.VISIBLE : View.GONE);
        hubBtn.setVisibility(Configs.DeviceEnable.HUB_ENABLE ? View.VISIBLE : View.GONE);
        boxingBtn.setVisibility(Configs.DeviceEnable.BOXING_ENABLE ? View.VISIBLE : View.GONE);
        broadcastingBtn.setVisibility(Configs.DeviceEnable.BROADCASTING_ENABLE ? View.VISIBLE : View.GONE);
    }

    private void initEvent() {
        oldProtocolScaleBtn.setOnClickListener(this);
        newProtocolScaleBtn.setOnClickListener(this);
        oldProtocolTrackerBtn.setOnClickListener(this);
        newProtocolTrackerBtn.setOnClickListener(this);
        heartRateBtn.setOnClickListener(this);
        cadenceBtn.setOnClickListener(this);
        jumpBtn.setOnClickListener(this);
        armbandBtn.setOnClickListener(this);
        kettleBellBtn.setOnClickListener(this);
        bikeComputerBtn.setOnClickListener(this);
        hubBtn.setOnClickListener(this);
        boxingBtn.setOnClickListener(this);
        broadcastingBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, OtherDeviceScanActivity.class);
        BaseDevice baseDevice = new BaseDevice();
        switch (view.getId()) {
            case R.id.old_protocol_scale_btn:
            case R.id.new_protocol_scale_btn:
                intent = new Intent(this, ScaleMainActivity.class);
                intent.putExtra(KEY_PROTOCOL_TYPE,
                        view.getId() == R.id.old_protocol_scale_btn ? CommonProtocol.OLD_PROTOCOL : CommonProtocol.NEW_PROTOCOL);
                startActivity(intent);
                break;

            case R.id.old_protocol_traker_btn:
            case R.id.new_protocol_traker_btn:
                intent = new Intent(this, TrackerScanActivity.class);
                intent.putExtra(KEY_PROTOCOL_TYPE,
                        view.getId() == R.id.old_protocol_traker_btn ? CommonProtocol.OLD_PROTOCOL : CommonProtocol.NEW_PROTOCOL);
                startActivity(intent);
                break;

            case R.id.armband_btn:
                intent = new Intent(this, ArmBandScanActivity.class);
                startActivity(intent);
                break;

            case R.id.kettle_bell_btn:
//                intent = new Intent(this, KettleBellScanActivity.class);
//                startActivity(intent);
                intent = new Intent(this, DeviceManagerActivity.class);
                intent.putExtra(DeviceManagerActivity.KEY_DEVICE_TYPE, DeviceType.KettleBell);
                startActivity(intent);
                break;

            case R.id.bike_computer_btn:
                intent = new Intent(this, BikeComputerScanActivity.class);
                startActivity(intent);
                break;

            case R.id.hub_btn:
                intent = new Intent(this, SelectHubDevTypeActivity.class);
                startActivity(intent);
                break;

            case R.id.boxing_btn:
//                intent = new Intent(this, BoxingScanActivity.class);
//                startActivity(intent);
                intent = new Intent(this, DeviceManagerActivity.class);
                intent.putExtra(DeviceManagerActivity.KEY_DEVICE_TYPE, DeviceType.Boxing);
                startActivity(intent);
                break;

            case R.id.heart_rate_btn:
                baseDevice.setDeviceType(DeviceType.HRMonitor);
                break;

            case R.id.cadence_btn:
                baseDevice.setDeviceType(DeviceType.Cadence);
                break;

            case R.id.jump_btn:
                baseDevice.setDeviceType(DeviceType.Jump);
                break;

            case R.id.broadcasting_btn:
                baseDevice.setDeviceType(DeviceType.BroadcastingScale);
                break;

            default:
                break;
        }
        if (baseDevice.getDeviceType() != null) {
            intent.putExtra(KEY_BASE_DEVICE, baseDevice);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        FitBleKit.getInstance().resetSDK();
        super.onDestroy();
    }
}
