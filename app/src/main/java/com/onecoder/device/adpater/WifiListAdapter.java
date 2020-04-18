package com.onecoder.device.adpater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.onecoder.device.R;
import com.onecoder.devicelib.base.protocol.entity.hubconfig.ScannedWifiInfo;
import com.onecoder.devicelib.base.protocol.entity.hubconfig.WifiStaInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/6/14.
 */

public class WifiListAdapter extends BaseAdapter {
    private static final int[] signalImgResIdArray = new int[]{
            R.mipmap.xinhaotiao00,
            R.mipmap.xinhaotiao01,
            R.mipmap.xinhaotiao02,
            R.mipmap.xinhaotiao03,
            R.mipmap.xinhaotiao04
    };
    private Context context;
    private List<ScannedWifiInfo> wifiInfoList;

    public WifiListAdapter(Context context) {
        this.context = context;
    }

    public WifiListAdapter(Context context, List<ScannedWifiInfo> wifiInfoList) {
        this.context = context;
        this.wifiInfoList = wifiInfoList;
    }

    public void setData(List<ScannedWifiInfo> wifiInfoList) {
        this.wifiInfoList = wifiInfoList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return wifiInfoList != null ? wifiInfoList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return wifiInfoList != null && position >= 0 && position < wifiInfoList.size() ? wifiInfoList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object o = getItem(position);
        if (!(o instanceof ScannedWifiInfo)) {
            return convertView;
        }
        ScannedWifiInfo wifiInfo = (ScannedWifiInfo) o;
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_wifi_info, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (wifiInfo.getSignalStrengthPercent() >= 0 && wifiInfo.getSignalStrengthPercent() <= 100) {
            int percentIndex = wifiInfo.getSignalStrengthPercent() / 20;
            if (percentIndex >= signalImgResIdArray.length) {
                percentIndex = signalImgResIdArray.length - 1;
            }
            viewHolder.imgSignalStrengthPercent.setImageResource(signalImgResIdArray[percentIndex]);
        }
        viewHolder.tvSignalStrengthPercent.setText(String.valueOf(wifiInfo.getSignalStrengthPercent()));
        viewHolder.tvSsid.setText(wifiInfo.getSSID());
        viewHolder.tvBssid.setText(wifiInfo.getBSSID());

        WifiStaInfo.AuthMode authMode = wifiInfo.getAuthMode();
        WifiStaInfo.EncryptionAlgorithm encryptionAlgorithm = wifiInfo.getEncryptionAlgorithm();
        viewHolder.tvSecurityMode.setText("" + (authMode != null ? authMode.name() : "")
                + "/" + (encryptionAlgorithm != null ? encryptionAlgorithm.name() : ""));
        viewHolder.tvSignalChannel.setText(String.valueOf(wifiInfo.getSignalChannel()));
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_ssid)
        TextView tvSsid;
        @BindView(R.id.tv_bssid)
        TextView tvBssid;
        @BindView(R.id.img_signal_strength_percent)
        ImageView imgSignalStrengthPercent;
        @BindView(R.id.tv_signal_strength_percent)
        TextView tvSignalStrengthPercent;
        @BindView(R.id.tv_security_mode)
        TextView tvSecurityMode;
        @BindView(R.id.tv_signal_channel)
        TextView tvSignalChannel;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
