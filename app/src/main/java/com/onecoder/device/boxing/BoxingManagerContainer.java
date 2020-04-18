package com.onecoder.device.boxing;

import com.onecoder.device.ManagerContainer;
import com.onecoder.devicelib.base.entity.DeviceType;
import com.onecoder.devicelib.boxing.api.BoxingManager;

public class BoxingManagerContainer extends ManagerContainer<BoxingManager> {

    private static BoxingManagerContainer boxingManagerContainer;

    public static BoxingManagerContainer getInstance() {
        synchronized (BoxingManagerContainer.class) {
            if (boxingManagerContainer == null) {
                boxingManagerContainer = new BoxingManagerContainer();
            }
            return boxingManagerContainer;
        }
    }

    private BoxingManagerContainer() {
        super(DeviceType.Boxing);
    }



}
