package com.onecoder.device.boxing;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onecoder.device.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Administrator on 2018/9/1.
 */

public class BoxingFragment extends Fragment implements View.OnClickListener {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.tv_hardware_version)
    TextView tvHardwareVersion;
    @BindView(R.id.tv_battery_level)
    TextView tvBatteryLevel;
    @BindView(R.id.get_battery_level_btn)
    Button getBatteryLevelBtn;
    @BindView(R.id.setting_utc)
    Button settingUtc;
    @BindView(R.id.real_time_data_txt)
    TextView realTimeDataTxt;
    @BindView(R.id.nathTestId)
    TextView nathTest;
    @BindView(R.id.get_history_data_btn)
    Button getHistoryDataBtn;
    @BindView(R.id.history_data_txt)
    TextView historyDataTxt;
    @BindView(R.id.real_time_data_layout)
    LinearLayout realTimeDataLayout;
    @BindView(R.id.history_data_layout)
    LinearLayout historyDataLayout;

    private int realTimeDataLayoutVisibility = View.VISIBLE;
    private int historyDataLayoutVisibility = View.VISIBLE;

    private String mac;
    private Unbinder unbind;
    private boolean init = false;
    private OnClickListener onClickListener;

    public interface OnClickListener {
        void onClick(String mac, View v);
    }

    public BoxingFragment() {
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.boxing_fragment_layout, null);
        unbind = ButterKnife.bind(this, view);
        tvHardwareVersion.setText("");
        title.setText(mac);
        realTimeDataLayout.setVisibility(realTimeDataLayoutVisibility);
        historyDataLayout.setVisibility(historyDataLayoutVisibility);

        getBatteryLevelBtn.setOnClickListener(this);
        settingUtc.setOnClickListener(this);
        getHistoryDataBtn.setOnClickListener(this);
        init = true;
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbind != null) {
            unbind.unbind();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_battery_level_btn:
                break;

            case R.id.setting_utc:
                break;

            case R.id.get_history_data_btn:
                break;

            default:
                return;
        }
        if (onClickListener != null) {
            onClickListener.onClick(mac, v);
        }
    }

    public void setRealTimeDataLayoutVisibility(int visibility) {
        realTimeDataLayoutVisibility = visibility;
        if (realTimeDataLayout != null) {
            realTimeDataLayout.setVisibility(visibility);
        }
    }

    public void getHistoryDataLayoutVisibility(int visibility) {
        historyDataLayoutVisibility = visibility;
        if (historyDataLayout != null) {
            historyDataLayout.setVisibility(visibility);
        }
    }

    public void updateConnectStatus(String statusStr) {
        title.setText(mac + "当前设备状态： " + statusStr);
    }

    public void setHardwareVersion(Integer hardwareVersion) {
        if (!init) {
            return;
        }
        tvHardwareVersion.setText("硬件版本:" + hardwareVersion);
    }

    public void setBatteryLevel(int batteryLevel) {
        if (!init) {
            return;
        }
        tvBatteryLevel.setText("" + batteryLevel);
    }

    public void setRealTimeDataInfo(String info) {
        if (!init) {
            return;
        }
        realTimeDataTxt.setText(info);
    }

    public void setNathTest(String info) {
        if (!init) {
            return;
        }
        nathTest.setText(info);
    }

    public void setHistoryDataInfo(String info) {
        if (!init) {
            return;
        }
        historyDataTxt.setText(info);
    }

}
