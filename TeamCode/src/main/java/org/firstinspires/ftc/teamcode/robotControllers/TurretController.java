package org.firstinspires.ftc.teamcode.robotControllers;

import static org.firstinspires.ftc.teamcode.constants.HardwareNames.TURRET_LEFT_SERVO_NAME;
import static org.firstinspires.ftc.teamcode.constants.HardwareNames.TURRET_RIGHT_SERVO_NAME;
import static org.firstinspires.ftc.teamcode.constants.RobotSpecs.LIMELIGHT_ON_TURRET;
import static org.firstinspires.ftc.teamcode.constants.RobotSpecs.MAX_YAW;
import static org.firstinspires.ftc.teamcode.constants.RobotSpecs.MIN_YAW;
import static org.firstinspires.ftc.teamcode.constants.RobotSpecs.TURRET_SERVOS_MIRRORED;
import static org.firstinspires.ftc.teamcode.constants.RobotSpecs.TURRET_SERVO_RANGE_DEGREES;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.TURRET_AIM_DEADBAND;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.TURRET_AIM_GAIN;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;
import org.firstinspires.ftc.teamcode.utils.UtilMethods;

public class TurretController {
    private static boolean isInitialized;
    private static Servo leftServo;
    private static Servo rightServo;
    private static double yaw = 0;

    public static void init(HardwareMap hardwareMap, boolean actuallyEnabled) {
        if(actuallyEnabled) {
            leftServo = hardwareMap.get(Servo.class, TURRET_LEFT_SERVO_NAME);
            rightServo = hardwareMap.get(Servo.class, TURRET_RIGHT_SERVO_NAME);
            leftServo.setDirection(Servo.Direction.FORWARD);
            rightServo.setDirection(TURRET_SERVOS_MIRRORED ? Servo.Direction.REVERSE : Servo.Direction.FORWARD);

            yaw = 0;
            isInitialized = true;

            TimedTelemetry.addLine("TURRET CONTROLLER INITIALIZED");
        } else stop();
    }

    public static void init(HardwareMap hardwareMap) {
        init(hardwareMap, true);
    }

    public static void update() {
        if(isInitialized) {
            double position = 0.5 + yaw / TURRET_SERVO_RANGE_DEGREES;
            leftServo.setPosition(position);
            rightServo.setPosition(position);
            TimedTelemetry.addData("Turret Yaw", "%.1f deg", 1, yaw);
        } else TimedTelemetry.addLine("Turret Updating but not Initialized!");
    }

    public static void setYaw(double yaw) {
        TurretController.yaw = UtilMethods.clip(yaw, MIN_YAW, MAX_YAW);
    }

    public static void aim(double tx) {
        if (LIMELIGHT_ON_TURRET) {
            if (Math.abs(tx) > TURRET_AIM_DEADBAND) setYaw(yaw + TURRET_AIM_GAIN * tx);
        } else setYaw(tx);
    }

    public static double getCurrentYaw() {
        return yaw;
    }

    public static void stop() {
        leftServo = null;
        rightServo = null;
        yaw = 0;
        isInitialized = false;
    }
}
