package org.firstinspires.ftc.teamcode.robotControllers;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.constants.HardwareNames;
import org.firstinspires.ftc.teamcode.constants.RobotSpecs;
import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;

public class UptakeServoController {
    private static boolean isInitialized = false;
    private static Servo upTakeServo;
    private static boolean uptakeBusy = false;
    private static boolean kicking = false;
    private static final ElapsedTime kickTimer = new ElapsedTime();

    public static void init(HardwareMap hardwareMap, boolean actuallyInitialize){
        if(actuallyInitialize) {
            upTakeServo = hardwareMap.get(Servo.class, HardwareNames.UPTAKE_SERVO_NAME);
            upTakeServo.setPosition(RobotSpecs.UPTAKE_SERVO_DEFAULT_ANGLE);
            uptakeBusy = false;
            kicking = false;
            TimedTelemetry.addLine("Kicker Controller Initialized");
            isInitialized = true;
        } else stop();
    }
    public static void init(HardwareMap hardwareMap) {
        init(hardwareMap, true);
    }

    public static boolean shoot(){
        if(isInitialized) {
            if (uptakeBusy) {
                TimedTelemetry.addLine("Kicker busy, wait for it to come back down");
                return false;
            }

            upTakeServo.setPosition(RobotSpecs.UPTAKE_SERVO_UP_ANGLE);
            uptakeBusy = true;
            kicking = true;
            kickTimer.reset();
            return true;
        } else return false;
    }

    public static void update() {
        if(isInitialized) {
            if (kicking && kickTimer.milliseconds() >= RobotSpecs.UPTAKE_SERVO_KICK_MS) {
                upTakeServo.setPosition(RobotSpecs.UPTAKE_SERVO_DEFAULT_ANGLE);
                kicking = false;
                kickTimer.reset();
            }
            if (uptakeBusy && !kicking && kickTimer.milliseconds() >= RobotSpecs.UPTAKE_SERVO_RETRACT_MS) {
                uptakeBusy = false;
            }

            if (kicking) {
                TimedTelemetry.addLine("Kicker up");
            } else if (uptakeBusy) {
                TimedTelemetry.addLine("Kicker moving down");
            } else {
                TimedTelemetry.addLine("Kicker free");
            }
        } else TimedTelemetry.addLine("Kicker being updated but not initialized!");
    }

    public static boolean isBusy() {
        return uptakeBusy;
    }

    public static void stop() {
        isInitialized = false;
        upTakeServo = null;
        uptakeBusy = false;
        kicking = false;
    }
}
