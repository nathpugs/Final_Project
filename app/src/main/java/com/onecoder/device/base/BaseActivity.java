package com.onecoder.device.base;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.onecoder.device.R;
import com.onecoder.device.utils.PermissionRationaleUtils;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.control.interfaces.CheckSystemBleCallback;
import com.onecoder.devicelib.base.control.manage.BleScanner;
import com.onecoder.devicelib.utils.BluetoothUtils;

public class BaseActivity extends AppCompatActivity
        implements CheckSystemBleCallback, PermissionView {
    public static final String KEY_BASE_DEVICE = "baseDevice";
    public static final String KEY_PROTOCOL_TYPE = "protocolType";

    protected static final int CMD_REQUEST_ENABLE_BLE = 1;
    protected Bundle savedInstanceState;

    protected PermissionPresenter permissionPresenter;
    protected AlertDialog requestPermissionRationaleDialog;
    protected AlertDialog requestPermissionInSettingsDialog;
    protected boolean isShowingRequestPermissionInSettingsDialog;

    protected boolean isRequestingSwitchOnBle = false;

    protected BleScanner getBleScanner() {
        return null;
    }

    protected Manager getManager(Bundle savedInstanceState) {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPermission();//初始化权限

        BleScanner bleScanner = getBleScanner();
        if (bleScanner != null) {
            bleScanner.registerCheckSystemBleCallback(this);
            bleScanner.init();
        }
        Manager manager = getManager(savedInstanceState);
        if (manager != null) {
            manager.registerCheckSystemBleCallback(this);
        }
    }

    protected void initPermissionView() {
        isShowingRequestPermissionInSettingsDialog = false;

        DialogInterface.OnClickListener rationaleOnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        requestPermissions();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        finish();
                        break;

                    default:
                        break;
                }
            }
        };

        DialogInterface.OnClickListener gotoOnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        PermissionPresenter.getAppDetailSettingIntent(BaseActivity.this);
                    case DialogInterface.BUTTON_NEGATIVE:
                        finish();
                        break;

                    default:
                        break;
                }
            }
        };

        requestPermissionRationaleDialog = new AlertDialog.Builder(this).setTitle(R.string.permission_request_title).create();
        requestPermissionRationaleDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.common_confirm), rationaleOnClickListener);
        requestPermissionRationaleDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.common_cancel), rationaleOnClickListener);
        requestPermissionRationaleDialog.setCanceledOnTouchOutside(false);
        requestPermissionRationaleDialog.setCancelable(false);

        requestPermissionInSettingsDialog = new AlertDialog.Builder(this).setTitle(R.string.permission_request_title).create();
        requestPermissionInSettingsDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.permission_request_goto_settings), gotoOnClickListener);
        requestPermissionInSettingsDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.common_cancel), gotoOnClickListener);
        requestPermissionInSettingsDialog.setCanceledOnTouchOutside(false);
        requestPermissionInSettingsDialog.setCancelable(false);
        requestPermissionInSettingsDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                isShowingRequestPermissionInSettingsDialog = true;
            }
        });

    }

    protected void initPermission() {
        initPermissionView();
        permissionPresenter = new PermissionPresenter(this, this);
        permissionPresenter.setCombinePermissionRationaleString(true);
        permissionPresenter.setDenyResultRemind(getString(R.string.permission_deny_result_remind));
        permissionPresenter.setRequestInSettingsPagePath(getString(R.string.permission_request_in_settings_page_path));
        if (requestPermissionsInOnCreate()) {
            requestPermissions();
        }
    }

    protected boolean requestPermissionsInOnCreate() {
        return true;
    }

    @Override
    public Context getPermissionContext() {
        return this;
    }

    protected String[] getNeedCheckPermissions() {
        return new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                //Manifest.permission.READ_LOGS,
                Manifest.permission.ACCESS_NETWORK_STATE,//集成蒲公英SDK需要
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.GET_TASKS
        };
    }

    protected boolean needRequestPermissions() {
        boolean needRequestPermissions = permissionPresenter != null
                && permissionPresenter.needRequestPermissions(getNeedCheckPermissions());
        if (!needRequestPermissions) {
            isShowingRequestPermissionInSettingsDialog = needRequestPermissions;
        }
        return needRequestPermissions;
    }

    protected boolean requestPermissions() {
        if (!needRequestPermissions()) {
            return false;
        }
        String[] permissions = getNeedCheckPermissions();
        if (permissions != null && permissions.length > 0) {
            permissionPresenter.request(permissions);
        }
        return true;
    }

    @Override
    public void onGranted(String permission) {
        if (permissionPresenter == null) {
            return;
        }
        if (!needRequestPermissions()) {
            if (requestPermissionRationaleDialog != null && requestPermissionRationaleDialog.isShowing()) {
                requestPermissionRationaleDialog.dismiss();
            }
            if (requestPermissionInSettingsDialog != null && requestPermissionInSettingsDialog.isShowing()) {
                requestPermissionInSettingsDialog.dismiss();
            }
            isShowingRequestPermissionInSettingsDialog = false;
        }
    }

    @Override
    public String getPermissionRationaleString(String permission) {
        return PermissionRationaleUtils.getPermissionRationaleString(this, permission);
    }

    @Override
    public void onShowRequestPermissionRationale(String permission, String permissionRationaleString) {
        if (requestPermissionRationaleDialog != null) {
            requestPermissionRationaleDialog.setMessage(permissionRationaleString);
            requestPermissionRationaleDialog.show();
        }
    }

    @Override
    public void onGotoSettingsForPermission(String permission, String permissionRationaleString) {
        if (requestPermissionInSettingsDialog != null) {
            requestPermissionInSettingsDialog.setMessage(permissionRationaleString);
            requestPermissionInSettingsDialog.show();
        }
    }

    @Override
    public void onRequestPermissionError(Throwable throwable) {
        Toast.makeText(this, R.string.permission_request_failed, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionCompleted() {
    }

    /**
     * ble被系统打开/关闭
     *
     * @param switchOn true:开启,即蓝牙由关闭状态切换至开启状态，此时需用户自己连接设备 false:关闭
     */
    @Override
    public void onBleSwitchedBySystem(boolean switchOn) {
        isRequestingSwitchOnBle = false;
        Log.i("BaseActivity", "onBleSwitchedBySystem switchOn:" + switchOn + " currentThread:"
                + Thread.currentThread().getId() + " " + Thread.currentThread().getName());
    }

    /**
     * 请求打开ble
     */
    @Override
    public void onRequestSwitchOnBle(String mac) {
        Log.i("BaseActivity", "onRequestSwitchOnBle currentThread:" + Thread.currentThread().getId()
                + " " + Thread.currentThread().getName());
        if (!isRequestingSwitchOnBle) {
            isRequestingSwitchOnBle = true;
            BluetoothUtils.checkEnabledBluetooth(this, CMD_REQUEST_ENABLE_BLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CMD_REQUEST_ENABLE_BLE) {
            isRequestingSwitchOnBle = false;
        }
    }

    @Override
    protected void onDestroy() {
        BleScanner bleScanner = getBleScanner();
        if (bleScanner != null) {
            bleScanner.unregisterCheckSystemBleCallback(this);
            bleScanner.reset();
        }
        Manager manager = getManager(savedInstanceState);
        if (manager != null) {
            manager.unregisterCheckSystemBleCallback(this);
        }
        super.onDestroy();
    }
}
