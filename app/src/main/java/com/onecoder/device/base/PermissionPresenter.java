package com.onecoder.device.base;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Administrator on 2018/2/26.
 */

public class PermissionPresenter {
    private static final String TAG = PermissionPresenter.class.getSimpleName();

    private RxPermissions rxPermissions;
    private PermissionView permissionView;
    private String permissionRationaleString;
    private String denyResultRemind;
    private String requestInSettingsPagePath;
    private boolean combinePermissionRationaleString;

    public PermissionPresenter(@NonNull Activity activity, @NonNull PermissionView permissionView) {
        if (activity == null) {
            throw new IllegalArgumentException("activity can not be null");
        }
        if (permissionView == null) {
            throw new IllegalArgumentException("permissionView can not be null");
        }
        rxPermissions = new RxPermissions(activity);
        this.permissionView = permissionView;
        combinePermissionRationaleString = false;
    }

    public String getDenyResultRemind() {
        return denyResultRemind;
    }

    public void setDenyResultRemind(String denyResultRemind) {
        this.denyResultRemind = denyResultRemind;
    }

    public String getRequestInSettingsPagePath() {
        return requestInSettingsPagePath;
    }

    public void setRequestInSettingsPagePath(String requestInSettingsPagePath) {
        this.requestInSettingsPagePath = requestInSettingsPagePath;
    }

    public boolean isCombinePermissionRationaleString() {
        return combinePermissionRationaleString;
    }

    public void setCombinePermissionRationaleString(boolean combinePermissionRationaleString) {
        this.combinePermissionRationaleString = combinePermissionRationaleString;
    }

    public boolean needRequestPermissions(String... permissions) {
        String[] permissionArray = getNeedRequestPermissions(permissions);
        return permissionArray != null && permissionArray.length > 0;
    }

    public String[] getNeedRequestPermissions(String... permissions) {
        if (permissionView == null || permissionView.getPermissionContext() == null
                || rxPermissions == null || permissions == null || permissions.length == 0) {
            return null;
        }
        List<String> needRequestPermissionList = new ArrayList<String>();
        for (String permission : permissions) {
            if (!rxPermissions.isGranted(permission)) {
                needRequestPermissionList.add(permission);
            }
        }
        return needRequestPermissionList.toArray(new String[needRequestPermissionList.size()]);
    }

    private String getPermissionRationaleString(String... permissions) {
        if (permissionView == null || permissionView.getPermissionContext() == null
                || permissions == null || permissions.length == 0) {
            return "";
        }
        String permissionRationaleString = "";
        String temp;
        for (String permission : permissions) {
            if (TextUtils.isEmpty(permission)) {
                continue;
            }
            temp = permissionView.getPermissionRationaleString(permission);
            if (TextUtils.isEmpty(temp)) {
                continue;
            }
            permissionRationaleString += temp + "\n";
        }
        /*int index = permissionRationaleString.lastIndexOf("\n");
        if (index > 0) {
            permissionRationaleString = permissionRationaleString.substring(0, index);
        }*/
        return permissionRationaleString;
    }

    /**
     * 跳转到权限设置界面
     */
    public static void getAppDetailSettingIntent(Activity activity) {
        Intent intent = new Intent();
        //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", activity.getPackageName());
        }
        activity.startActivity(intent);
    }

    public boolean request(String... permissions) {
        if (rxPermissions == null || permissionView.getPermissionContext() == null) {
            return false;
        }
        if (permissions == null || permissions.length == 0) {
            throw new IllegalArgumentException(TAG + ".request requires at least one input permission");
        }
        String[] targetPermissions = getNeedRequestPermissions(permissions);
        if (targetPermissions == null || targetPermissions.length == 0) {
            if (permissionView != null) {
                permissionView.onRequestPermissionCompleted();
            }
            return true;
        }
        permissionRationaleString = getPermissionRationaleString(targetPermissions);

        rxPermissions.requestEach(targetPermissions).subscribe(new Action1<Permission>() {
            @Override
            public void call(Permission permission) {
                if (permission == null) {
                    if (permissionView != null) {
                        permissionView.onRequestPermissionError(new Throwable("permission is null"));
                    }
                }
                String targetPermissionRationaleString = (combinePermissionRationaleString
                        ? permissionRationaleString
                        : permissionView.getPermissionRationaleString(permission.name))
                        + (TextUtils.isEmpty(denyResultRemind) ? "" : denyResultRemind);
                if (permission.granted) {
                    // `permission.name` is granted !
                    if (permissionView != null) {
                        permissionView.onGranted(permission.name);
                    }
                } else if (permission.shouldShowRequestPermissionRationale) {
                    // Denied permission without ask never again
                    if (permissionView != null) {
                        permissionView.onShowRequestPermissionRationale(permission.name,
                                targetPermissionRationaleString);
                    }
                } else {
                    // Denied permission with ask never again
                    // Need to go to the settings
                    if (permissionView != null) {
                        permissionView.onGotoSettingsForPermission(permission.name,
                                targetPermissionRationaleString
                                        + (TextUtils.isEmpty(requestInSettingsPagePath) ? "" : "\n" + requestInSettingsPagePath));
                    }
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                if (permissionView != null) {
                    permissionView.onRequestPermissionError(throwable);
                }
            }
        }, new Action0() {
            @Override
            public void call() {
                if (permissionView != null) {
                    permissionView.onRequestPermissionCompleted();
                }
            }
        });
        return true;
    }
}
