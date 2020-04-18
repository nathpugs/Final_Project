package com.onecoder.device.base;

import android.content.Context;

/**
 * Created by Administrator on 2018/2/27.
 */

public interface PermissionView {

    Context getPermissionContext();

    void onGranted(String permission);

    String getPermissionRationaleString(String permission);

    void onShowRequestPermissionRationale(String permission, String permissionRationaleString);

    void onGotoSettingsForPermission(String permission, String permissionRationaleString);

    void onRequestPermissionError(Throwable throwable);

    void onRequestPermissionCompleted();

}
