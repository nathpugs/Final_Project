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
import com.onecoder.device.boxing.BoxingScanActivity;
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


    private Button boxingBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_act_main);
        FitBleKit.getInstance().initSDK(this);

        initView();
        initEvent();
    }

    private void initView() {

        boxingBtn = (Button) findViewById(R.id.boxing_btn);


        boxingBtn.setVisibility(Configs.DeviceEnable.BOXING_ENABLE ? View.VISIBLE : View.GONE);

    }

    private void initEvent() {

        boxingBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, OtherDeviceScanActivity.class);
        BaseDevice baseDevice = new BaseDevice();
        switch (view.getId()) {


            case R.id.boxing_btn:
//                intent = new Intent(this, BoxingScanActivity.class);
//                startActivity(intent);
                intent = new Intent(this, DeviceManagerActivity.class);
                intent.putExtra(DeviceManagerActivity.KEY_DEVICE_TYPE, DeviceType.Boxing);
                startActivity(intent);
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
