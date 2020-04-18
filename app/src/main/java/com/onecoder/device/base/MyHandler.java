package com.onecoder.device.base;

import android.os.Handler;
import android.os.Message;

public class MyHandler extends Handler {

    private OnHandleMessageListener onHandleMessageListener;

    public interface OnHandleMessageListener {
        void onHandleMessage(Message msg);
    }

    public MyHandler() {
    }

    public MyHandler(OnHandleMessageListener onHandleMessageListener) {
        this.onHandleMessageListener = onHandleMessageListener;
    }

    public void setOnHandleMessageListener(OnHandleMessageListener onHandleMessageListener) {
        this.onHandleMessageListener = onHandleMessageListener;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (onHandleMessageListener != null) {
            onHandleMessageListener.onHandleMessage(msg);
        }
    }
}
