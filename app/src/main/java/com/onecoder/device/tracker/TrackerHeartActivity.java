package com.onecoder.device.tracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ScrollView;
import android.widget.TextView;

import com.onecoder.device.R;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.protocol.entity.RTHeartRate;
import com.onecoder.devicelib.tracker.api.TrackerManager;
import com.onecoder.devicelib.tracker.api.interfaces.HeartRateValueListener;


public class TrackerHeartActivity extends BaseActivity {

    TextView mTvHeart;
    ScrollView mainView;

    TrackerManager trackerManager;

    @Override
    protected Manager getManager(Bundle savedInstanceState) {
        if (trackerManager != null) {
            return trackerManager;
        }
        trackerManager = TrackerManager.getInstance();
        return trackerManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainView = (ScrollView) LayoutInflater.from(this).inflate(R.layout.tracker_heart_act, null);
        setContentView(mainView);
        mTvHeart = (TextView) mainView.findViewById(R.id.heart_tv);

        setTitle(R.string.real_time_heart_rate);//Real-time heart rate.
        // 设置新协议手环进入心率模式
        //Set the new protocol bracelet to enter the heart rate mode.
        trackerManager.setHearRateSwitch(1);
        trackerManager.setHeartRateValueListener(valueListener);
    }


    private HeartRateValueListener valueListener = new HeartRateValueListener() {
        @Override
        public void onRTHeartRate(RTHeartRate value) {
            String text = mTvHeart.getText().toString() + "heart rate = " + (value.toString()) + "\r\n";
            mTvHeart.setText(text);
        }
    };


}
