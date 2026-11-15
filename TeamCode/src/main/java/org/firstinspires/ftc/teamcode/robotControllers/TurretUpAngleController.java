package org.firstinspires.ftc.teamcode.robotControllers;

import static org.firstinspires.ftc.teamcode.constants.RobotSpecs.HOOD_DEFAULT_ANGLE;
import static org.firstinspires.ftc.teamcode.constants.RobotSpecs.HOOD_MAX_ANGLE;
import static org.firstinspires.ftc.teamcode.constants.RobotSpecs.HOOD_MIN_ANGLE;
import static org.firstinspires.ftc.teamcode.constants.RobotSpecs.HOOD_SERVO_MAX_ANGLE;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.HOOD_STEP_ANGLE;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.constants.HardwareNames;
import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;
import org.firstinspires.ftc.teamcode.utils.UtilMethods;

public class TurretUpAngleController {
    private static boolean isInitialized = false;
    private static double targetAngle = HOOD_DEFAULT_ANGLE;
    private static Servo turretUpAngleServo;
    public static void init(HardwareMap hardwareMap, boolean actuallyEnabled) {
        if(actuallyEnabled) {
            turretUpAngleServo = hardwareMap.get(Servo.class, HardwareNames.TURRET_UP_ANGLE_SERVO_NAME);
            targetAngle = HOOD_DEFAULT_ANGLE;
            turretUpAngleServo.setPosition(targetAngle / HOOD_SERVO_MAX_ANGLE);
            TimedTelemetry.addLine("Hood Controller Initialized");
            isInitialized = true;
        }else stop();
    }


    public static void init(HardwareMap hardwareMap){
        init(hardwareMap, true);
    }

    public static void update(float power){
        if (!isInitialized){
            TimedTelemetry.addLine("Hood controller not initialized!");
            return;
        }
        if (power > 0.5) targetAngle += HOOD_STEP_ANGLE;
        else if (power < -0.5) targetAngle -= HOOD_STEP_ANGLE;
        targetAngle = UtilMethods.clip(targetAngle, HOOD_MIN_ANGLE, HOOD_MAX_ANGLE);
        turretUpAngleServo.setPosition(targetAngle / HOOD_SERVO_MAX_ANGLE);
        TimedTelemetry.addData("Hood Angle: ", targetAngle);
    }

    public static void update(){
        update(0);
    }

    public static void setTargetAngle(double angle){
        targetAngle = UtilMethods.clip(angle, HOOD_MIN_ANGLE, HOOD_MAX_ANGLE);
    }

    public static double getTargetAngle(){
        return targetAngle;
    }

    public static void stop() {
        turretUpAngleServo = null;
        isInitialized = false;
        targetAngle = HOOD_DEFAULT_ANGLE;
    }

}
