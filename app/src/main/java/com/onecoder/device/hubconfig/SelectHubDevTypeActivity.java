package com.onecoder.device.hubconfig;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.onecoder.device.R;
import com.onecoder.device.base.BaseActivity;

import butterknife.ButterKnife;

public class SelectHubDevTypeActivity extends BaseActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_hub_dev_type);
        ButterKnife.bind(this);
    }

    @Override
    protected String[] getNeedCheckPermissions() {
        return null;
    }

    @Override
    public void onClick(View v) {
        boolean isFullFunction;
        switch (v.getId()) {
            case R.id.hub_dev_type_rc900c_txt:
                isFullFunction = true;
                break;

            case R.id.hub_dev_type_rc902_txt:
                isFullFunction = false;
                break;

            default:
                return;
        }

        Intent intent = new Intent(this, HubConfigScanActivity.class);
        intent.putExtra(HubConfigScanActivity.KEY_IS_FULL_FUNCTION, isFullFunction);
        startActivity(intent);
    }
}
