package com.onecoder.device.utils;

import android.content.Context;
import android.widget.Toast;


public class GToast {

    public static void show(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void show(Context context, int resId){
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    }

    public static void show(Context context, String text, int duration){
        Toast.makeText(context, text, duration).show();
    }

    public static void show(Context context, int resId, int duration){
        Toast.makeText(context, resId, duration).show();
    }

}
