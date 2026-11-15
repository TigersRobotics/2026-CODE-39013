package org.firstinspires.ftc.teamcode.utils;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.constants.OpModeMethods;

import java.util.function.Consumer;

public class OpModeUtils {
    public static void initAll(HardwareMap hardwareMap, Telemetry telemetry) {
        initSome(telemetry);
        for (Consumer<HardwareMap> consumer: OpModeMethods.HARDWARE_MAP_INIT) consumer.accept(hardwareMap);
    }
    public static void initSome(Telemetry telemetry) {
        stopAll();
        TimedTelemetry.init(telemetry);
        for (Runnable runnable: OpModeMethods.NO_PARAM_INIT) runnable.run();
    }
    public static void updateSome(){
        for (Runnable runnable: OpModeMethods.NO_PARAM_UPDATE) runnable.run();
    }
    public static void stopAll(){
        for (Runnable runnable: OpModeMethods.NO_PARAM_STOP) runnable.run();
    }
}
