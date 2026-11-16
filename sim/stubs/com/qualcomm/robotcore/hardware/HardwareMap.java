package com.qualcomm.robotcore.hardware;

import java.util.HashMap;
import java.util.Map;

public class HardwareMap {
    private final Map<String, Object> devices = new HashMap<>();

    public final DeviceMapping<DcMotor> dcMotor = new DeviceMapping<>(DcMotor.class);

    public void put(String name, Object device) {
        devices.put(name, device);
    }

    public <T> T get(Class<? extends T> classOrInterface, String deviceName) {
        Object device = devices.get(deviceName);
        if (device == null || !classOrInterface.isInstance(device)) {
            throw new IllegalArgumentException("Unable to find a hardware device with name \"" + deviceName
                    + "\" and type " + classOrInterface.getSimpleName());
        }
        return classOrInterface.cast(device);
    }

    public class DeviceMapping<T> {
        private final Class<T> type;

        public DeviceMapping(Class<T> type) {
            this.type = type;
        }

        public T get(String deviceName) {
            return HardwareMap.this.get(type, deviceName);
        }
    }
}
