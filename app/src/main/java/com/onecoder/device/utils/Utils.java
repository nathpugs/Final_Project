package com.onecoder.device.utils;

import android.content.Context;

import com.onecoder.device.R;
import com.onecoder.devicelib.base.entity.DeviceType;
import com.onecoder.devicelib.base.protocol.entity.StepData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/12/27.
 */

public class Utils {

    public static void sortStepData(List<StepData> stepDataList) {
        if (stepDataList == null) {
            return;
        }
        Comparator comparator = new Comparator<StepData>() {

            @Override
            public int compare(StepData o1, StepData o2) {
                if (o1 == null || o2 == null) {
                    return o1 == null && o2 != null ? -1 : (o1 != null && o2 == null ? 1 : 0);
                }

                String stepTimeStr1 = o1.getSteptime();
                String stepTimeStr2 = o2.getSteptime();
                long stepTimeStamp1 = 0;
                long stepTimeStamp2 = 0;
                Date date;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    date = simpleDateFormat.parse(stepTimeStr1);
                    stepTimeStamp1 = date.getTime();
                    date = simpleDateFormat.parse(stepTimeStr2);
                    stepTimeStamp2 = date.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
                return (stepTimeStamp1 < stepTimeStamp2) ? -1 : ((stepTimeStamp1 == stepTimeStamp2) ? 0 : 1);
            }
        };
        Collections.sort(stepDataList, comparator);
    }

    public static String getDeviceTypeName(Context context, DeviceType deviceType) {
        if (context == null || deviceType == null) {
            return "";
        }
        String[] deviceTypeNames = context.getResources().getStringArray(R.array.device_type);
        if (deviceTypeNames == null || deviceTypeNames.length <= deviceType.ordinal()) {
            return "";
        }
        return deviceTypeNames[deviceType.ordinal()];
    }
}
