package org.firstinspires.ftc.teamcode.robotControllers;

import static org.firstinspires.ftc.teamcode.constants.HardwareNames.LIMELIGHT_NAME;
import static org.firstinspires.ftc.teamcode.constants.HardwareNames.LIMELIGHT_TILT_SERVO_NAME;
import static org.firstinspires.ftc.teamcode.constants.RobotSpecs.LIMELIGHT_TILT_DEFAULT_POSITION;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.LIMELIGHT_PIPELINE;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;
import org.firstinspires.ftc.teamcode.utils.UtilMethods;

public class LimelightController {
    private static boolean isInitialized = false;
    private static Limelight3A limelight;
    private static Servo tiltServo;
    private static LLResult latestResult;
    private static double tilt = LIMELIGHT_TILT_DEFAULT_POSITION;

    public static void init(HardwareMap hardwareMap, boolean actuallyInitialize) {
        if(actuallyInitialize) {
            limelight = hardwareMap.get(Limelight3A.class, LIMELIGHT_NAME);
            tiltServo = hardwareMap.get(Servo.class, LIMELIGHT_TILT_SERVO_NAME);

            limelight.pipelineSwitch(LIMELIGHT_PIPELINE);
            limelight.start();

            tilt = LIMELIGHT_TILT_DEFAULT_POSITION;
            tiltServo.setPosition(tilt);
            latestResult = null;
            isInitialized = true;

            TimedTelemetry.addLine("Limelight Initialized");
        } else stop();
    }

    public static void init(HardwareMap hardwareMap) {
        init(hardwareMap, true);
    }

    public static void update() {
        if(isInitialized) {
            tiltServo.setPosition(tilt);
            latestResult = limelight.getLatestResult();

            if (hasTarget()) TimedTelemetry.addData("Limelight", "tx %.1f ty %.1f", 1, getTx(), getTy());
            else TimedTelemetry.addLine("Limelight: no target");
        } else TimedTelemetry.addLine("Limelight being updated but not initialized!");
    }

    public static boolean hasTarget() {
        return isInitialized && latestResult != null && latestResult.isValid();
    }

    public static double getTx() {
        return hasTarget() ? latestResult.getTx() : 0;
    }

    public static double getTy() {
        return hasTarget() ? latestResult.getTy() : 0;
    }

    public static void setTilt(double position) {
        tilt = UtilMethods.clip(position, 0, 1);
    }

    public static double getTilt() {
        return tilt;
    }

    public static void stop() {
        if (limelight != null) limelight.stop();
        limelight = null;
        tiltServo = null;
        latestResult = null;
        tilt = LIMELIGHT_TILT_DEFAULT_POSITION;
        isInitialized = false;
    }
}
