package com.onecoder.device.hubconfig;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.onecoder.device.R;
import com.onecoder.device.adpater.WifiListAdapter;
import com.onecoder.device.base.BaseActivity;
import com.onecoder.devicelib.base.protocol.entity.hubconfig.ScannedWifiInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WifiListActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    public static final String ACTION_SCANNED_WIFI_INFO_LIST = "com.onecoder.device.hubconfig.action.scannedWifiInfoList";
    public static final String KEY_SCANNED_WIFI_INFO_LIST = "scannedWifiInfoList";
    public static final String KEY_SELECTED_SCANNED_WIFI_INFO = "selectedScannedWifiInfo";
    public static final String TAG = WifiListActivity.class.getSimpleName();

    @BindView(R.id.info)
    TextView info;
    @BindView(R.id.list_view_wifi_list)
    ListView listViewWifiList;
    private WifiListAdapter wifiListAdapter;

    private ProgressDialog dialog;
    private List<ScannedWifiInfo> scannedWifiInfoList;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);
        ButterKnife.bind(this);

        scannedWifiInfoList = new ArrayList<>();

        wifiListAdapter = new WifiListAdapter(this);
        listViewWifiList.setAdapter(wifiListAdapter);
        listViewWifiList.setOnItemClickListener(this);

        dialog = new ProgressDialog(this);
        showDialog("HUB设备正在扫描WIFI，请稍后", true);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!ACTION_SCANNED_WIFI_INFO_LIST.equals(intent.getAction())) {
                    return;
                }
                List<Parcelable> list = intent.getParcelableArrayListExtra(KEY_SCANNED_WIFI_INFO_LIST);
                Log.i(TAG, "onReceive list" + list);
                if (list == null) {
                    return;
                }
                scannedWifiInfoList.clear();
                for (Parcelable parcelable : list) {
                    if (parcelable instanceof ScannedWifiInfo) {
                        scannedWifiInfoList.add((ScannedWifiInfo) parcelable);
                    }
                }
                showDialog(null, false);
                wifiListAdapter.setData(scannedWifiInfoList);
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_SCANNED_WIFI_INFO_LIST));
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object o = wifiListAdapter.getItem(position);
        if (o instanceof ScannedWifiInfo) {
            Intent intent = new Intent();
            ScannedWifiInfo scannedWifiInfo = (ScannedWifiInfo) o;
            intent.putExtra(KEY_SELECTED_SCANNED_WIFI_INFO, scannedWifiInfo);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onStop() {
        showDialog(null, false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
