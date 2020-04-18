package com.onecoder.device;

import com.onecoder.devicelib.armband.api.ArmBandManager;
import com.onecoder.devicelib.base.api.Manager;
import com.onecoder.devicelib.base.entity.DeviceType;
import com.onecoder.devicelib.bikecomputer.api.BikeComputerManager;
import com.onecoder.devicelib.boxing.api.BoxingManager;
import com.onecoder.devicelib.cadence.api.CadenceManager;
import com.onecoder.devicelib.heartrate.api.HeartRateMonitorManager;
import com.onecoder.devicelib.hubconfig.api.HubConfigManager;
import com.onecoder.devicelib.kettlebell.api.KettleBellManager;
import com.onecoder.devicelib.scale.api.ScaleManager;
import com.onecoder.devicelib.tracker.api.TrackerManager;

import java.util.List;
import java.util.Vector;

public class ManagerContainer<T extends Manager> {
    protected final List<T> managerList = new Vector<T>();
    protected DeviceType deviceType;

    public static Manager instanceManager(DeviceType deviceType) {
        if (deviceType == null) {
            return null;
        }
        Manager manager = null;
        switch (deviceType) {
            // 手环
            case Tracker:
                manager = TrackerManager.getInstance();
                break;

            // 秤
            case Scale:
                manager = ScaleManager.getInstance();
                break;

            // 心率带
            case HRMonitor:
                manager = TrackerManager.getInstance();
                break;

            // 踏频
            case Cadence:
                manager = CadenceManager.getInstance();
                break;

            // 心率带
            case Jump:
                manager = HeartRateMonitorManager.getInstance();
                break;

            // 臂带
            case ArmBand:
                manager = ArmBandManager.getInstance();
                break;

            // 壶铃
            case KettleBell:
                manager = KettleBellManager.getInstance();
                break;

            // 码表
            case BikeComputer:
                manager = BikeComputerManager.getInstance();
                break;

            // Hub配置
            case HubConfig:
                manager = HubConfigManager.getInstance();
                break;

            // 拳击
            case Boxing:
                manager = BoxingManager.getInstance();
                break;

            default:
                break;
        }
        return manager;
    }

    public ManagerContainer(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public DeviceType getType() {
        return deviceType;
    }

    public boolean contains(Manager manager) {
        return managerList.contains(manager);
    }

    public List<T> getManagerList() {
        return managerList;
    }

    public Manager getManager(String mac) {
        if (mac == null) {
            return null;
        }
        Manager manager = null;
        for (Manager managerTemp : managerList) {
            if (managerTemp == null || managerTemp.getBaseDevice() == null
                    || managerTemp.getBaseDevice().getMacAddress() == null) {
                continue;
            }
            if (managerTemp.getBaseDevice().getMacAddress().equalsIgnoreCase(mac)) {
                manager = managerTemp;
                break;
            }
        }
        return manager;
    }

}
