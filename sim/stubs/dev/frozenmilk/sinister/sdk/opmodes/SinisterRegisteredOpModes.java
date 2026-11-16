package dev.frozenmilk.sinister.sdk.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SinisterRegisteredOpModes {
    public static final SinisterRegisteredOpModes INSTANCE = new SinisterRegisteredOpModes();

    private final Map<String, OpMode> opModes = new ConcurrentHashMap<>();

    public void register(OpModeMeta meta, OpMode opMode) { opModes.put(meta.name, opMode); }
    public void unregister(OpModeMeta meta) { opModes.remove(meta.name); }
    public OpMode get(String name) { return opModes.get(name); }

    private SinisterRegisteredOpModes() {}
}
