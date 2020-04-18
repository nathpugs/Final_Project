package com.onecoder.device;

import android.app.Application;

import com.onecoder.device.utils.CrashHandler;
import com.onecoder.devicelib.FitBleKit;
import com.pgyersdk.crash.PgyCrashManager;

/**
 * Created by Administrator on 2017/11/12.
 */

public class FitKitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //SDK初始化
        FitBleKit.getInstance().initSDK(this);
        PgyCrashManager.register(this);
        CrashHandler.getInstance().init(this, "FitBleKit");
    }

}
