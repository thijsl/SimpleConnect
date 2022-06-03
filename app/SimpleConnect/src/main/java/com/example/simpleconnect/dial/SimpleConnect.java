package com.example.simpleconnect.dial;

import java.util.List;

public interface SimpleConnect {

    List<Device> getDevices();

    /**
     * Request devices
     *
     * @param listener
     */
    void requestDevices(Dial.DevicesListener listener);

    void requestDevicesTask();

}
