package com.example.simpleconnect.dial;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dial implements SimpleConnect {

    public Dial() {
        this.listener = null;
    }

    public List<Device> getDevices() {
        List<Device> dialServers;

        try {
            MSearch mSearch = new MSearch(0, 1500);
            dialServers = mSearch.sendAndReceive();
        } catch (IOException e) {
            return Collections.emptyList();
        }

        List<Device> devicesToRemove = new ArrayList<>();
        for (Device device : dialServers) {
            try {
                DeviceDescriptorResource deviceDescriptorResource = new DeviceDescriptorResource();
                DeviceDescriptor descriptor = deviceDescriptorResource.getDescriptor(device.deviceDescriptorUrl);
                if (descriptor != null) {
                    device.friendlyName = descriptor.friendlyName;
                    device.modelName = descriptor.modelName;
                    device.modelNumber = descriptor.modelNumber;
                    device.applicationResourceUrl = descriptor.applicationResourceUrl;
                    device.manufacturer = descriptor.manufacturer;
                } else {
                    devicesToRemove.add(device);
                }
            } catch (IOException e) {
                devicesToRemove.add(device);
            }
        }
        dialServers.removeAll(devicesToRemove);
        return dialServers;
    }

    public void requestDevicesTask() {
        AsyncTask task = new AsyncTask() {
            @Override
            protected List<Device> doInBackground(Object[] objects) {
                List<Device> devices = getDevices();
                return devices;
            }

            @Override
            protected void onPostExecute(Object o) {
                List<Device> devices = (List<Device>) o;
                super.onPostExecute(o);
                if (listener != null) {
                    listener.onDataLoaded(devices);
                }
            }
        };
        task.execute();

    }

    public interface DevicesListener {
        public void onDataLoaded(List<Device> devices);
    }

    // Member variable was defined earlier
    private DevicesListener listener;

    @Override
    public void requestDevices(DevicesListener listener) {
        this.listener = listener;
        requestDevicesTask();
    }

}
