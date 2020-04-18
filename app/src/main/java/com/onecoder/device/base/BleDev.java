package com.onecoder.device.base;

import com.onecoder.devicelib.base.control.entity.BleDevice;
import com.onecoder.devicelib.base.entity.BaseDevice;
import com.onecoder.devicelib.base.entity.DeviceType;

public class BleDev extends BaseDevice {
    private int connectState = BleDevice.STATE_DISCONNECTED;

    public BleDev() {
    }

    public BleDev(int connectState) {
        this.connectState = connectState;
    }

    public BleDev(BaseDevice baseDevice, int connectState) {
        super(baseDevice != null ? baseDevice.getDeviceType() : null,
                baseDevice != null ? baseDevice.getName() : null,
                baseDevice != null ? baseDevice.getMacAddress() : null);
        this.connectState = connectState;
    }

    public BleDev(DeviceType deviceType, String name, String macAddress) {
        super(deviceType, name, macAddress);
    }

    public BleDev(DeviceType deviceType, String name, String macAddress, int connectState) {
        super(deviceType, name, macAddress);
        this.connectState = connectState;
    }

    public int getConnectState() {
        return connectState;
    }

    public void setConnectState(int connectState) {
        this.connectState = connectState;
    }

    @Override
    public String toString() {
        return "BleDev{" +
                "connectState=" + connectState +
                ", deviceType=" + deviceType +
                ", macAddress='" + macAddress + '\'' +
                '}';
    }
}
