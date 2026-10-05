package com.qualcomm.robotcore.hardware;

import java.util.HashMap;
import java.util.Map;

// Sim stand-in for the FTC SDK class, only what our code uses
public class HardwareMap {
    private final Map<String, Object> devices = new HashMap<>();

    public void put(String name, Object device) {
        devices.put(name, device);
    }

    public <T> T get(Class<? extends T> classOrInterface, String deviceName) {
        Object device = devices.get(deviceName);
        if (device == null || !classOrInterface.isInstance(device)) {
            // same failure the real robot gives when the config is missing a device
            throw new IllegalArgumentException("Unable to find a hardware device with name \"" + deviceName
                    + "\" and type " + classOrInterface.getSimpleName());
        }
        return classOrInterface.cast(device);
    }
}
