package com.example.simpleconnect.dial;

import java.io.Serializable;
import java.net.URL;

public class Device implements Serializable {

    // The friendly name is only set if the device exposes it via upnp device descriptor
    public String friendlyName;

    public String modelName;

    public String modelNumber;

    public String manufacturer;

    // The url to the application rest resource
    public URL applicationResourceUrl;

    // A unique identifier of the device
    public String uniqueServiceName;

    // The url to the upnp device descriptor
    public URL deviceDescriptorUrl;

    // Set if the server supports wol
    public boolean wakeOnLanSupport;

    // The MAC address to wake up the device
    public String wakeOnLanMAC;

    // The wake on lan timeout.
    public String wakeOnLanTimeout;

    // A technical description string of the server
    public String serverDescription;

    @Override
    public String toString() {
        String result = "";
        result = "{friendlyName: '"+friendlyName+"'";
        if (applicationResourceUrl != null) {
            result = result + ", applicationResourceUrl: '"+applicationResourceUrl+"'";
        }
        if (deviceDescriptorUrl != null) {
            result = result + ", deviceDescriptorUrl: '"+deviceDescriptorUrl+"'";
        }
        if (manufacturer != null) {
            result = result + ", manufacturer: '"+manufacturer+"'";
        }
        if (modelNumber != null) {
            result = result + ", modelNumber: '"+modelNumber+"'";
        }
        if (modelName != null) {
            result = result + ", modelNumber: '"+modelName+"'}";
        }
        return result;
    }

    public DialConnection connect() {
        return new DialConnection(new ApplicationResource("jdial", this.applicationResourceUrl, this));
    }

    public boolean launch(String applicationId, String[] parameters) {
        return false;
    }

    public boolean isRunning(String applicationId) {
        return false;
    }

    public boolean stop(String applicationId) {
        return false;
    }

    /**
     * Stop any app that is playing
     *
     * @return
     */
    public boolean stop() {
        return false;
    }

    public boolean install(String applicationId) {
        return false;
    }

}