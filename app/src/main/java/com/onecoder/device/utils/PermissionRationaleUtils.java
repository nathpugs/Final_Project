package com.onecoder.device.utils;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import com.onecoder.device.R;

/**
 * Created by Administrator on 2018/2/28.
 */

public class PermissionRationaleUtils {

    public static String getPermissionRationaleString(Context context, String permission) {
        if (context == null) {
            return "";
        }
        int strResId = 0;
        switch (permission) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                strResId = R.string.permission_rationale_storage;
                break;

            case Manifest.permission.ACCESS_COARSE_LOCATION:
            case Manifest.permission.ACCESS_FINE_LOCATION:
                strResId = R.string.permission_rationale_ble_location;
                break;

            case Manifest.permission.READ_PHONE_STATE:
                //case Manifest.permission.READ_LOGS:
                strResId = R.string.permission_rationale_phone_wifi_state;
                break;

            case Manifest.permission.RECORD_AUDIO:
                strResId = R.string.permission_rationale_record_audio;
                break;

            case Manifest.permission.GET_TASKS:
                strResId = R.string.permission_rationale_get_tasks;
                break;

            default:
                break;
        }
        return strResId > 0 ? context.getString(strResId) : "";
    }

}
