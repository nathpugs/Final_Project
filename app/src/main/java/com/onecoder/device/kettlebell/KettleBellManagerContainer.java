package com.onecoder.device.kettlebell;

import com.onecoder.device.ManagerContainer;
import com.onecoder.devicelib.base.entity.DeviceType;
import com.onecoder.devicelib.kettlebell.api.KettleBellManager;

public class KettleBellManagerContainer extends ManagerContainer<KettleBellManager> {

    private static KettleBellManagerContainer kettleBellManagerContainer;

    public static KettleBellManagerContainer getInstance() {
        synchronized (KettleBellManagerContainer.class) {
            if (kettleBellManagerContainer == null) {
                kettleBellManagerContainer = new KettleBellManagerContainer();
            }
            return kettleBellManagerContainer;
        }
    }

    private KettleBellManagerContainer() {
        super(DeviceType.KettleBell);
    }

}
