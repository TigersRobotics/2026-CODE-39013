package org.firstinspires.ftc.teamcode.robotControllers;

import static org.firstinspires.ftc.teamcode.constants.HardwareNames.FLYWHEEL_NAME;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FLYWHEEL_F_VALUE;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FLYWHEEL_P_VALUE;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FLYWHEEL_VELOCITY_TOLERANCE;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;

public class FlywheelController {
    private static boolean isInitialized;
    private static DcMotorEx flywheel;
    private static double P = FLYWHEEL_P_VALUE, F = FLYWHEEL_F_VALUE, targetVel = 0;

    public static void init(HardwareMap hardwareMap, boolean actuallyEnabled) {
        if(actuallyEnabled) {
            flywheel = hardwareMap.get(DcMotorEx.class, FLYWHEEL_NAME);
            flywheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            flywheel.setDirection(DcMotorSimple.Direction.REVERSE);

            P = FLYWHEEL_P_VALUE;
            F = FLYWHEEL_F_VALUE;
            targetVel = 0;
            flywheel.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new PIDFCoefficients(P, 0, 0, F));

            isInitialized = true;

            TimedTelemetry.addLine("Flywheel Controller Initialized");

        } else stop();
    }

    public static void init(HardwareMap hardwareMap) {
        init(hardwareMap, true);
    }


    public static void update() {
        if(isInitialized) {
            if (targetVel != 0) flywheel.setVelocity(targetVel);
            else flywheel.setPower(0);

            TimedTelemetry.addData("Flywheel", "%.0f / %.0f ticks/s%s", 1, getVelocity(), targetVel, isAtSpeed() ? " READY" : "");
        } else TimedTelemetry.addLine("Flywheel Updating but not Initialized!");
    }

    public static void setTargetVel(double targetVel) {
        FlywheelController.targetVel = targetVel;
    }
    public static void setPFCoefficients(double P, double F) {
        if (!isInitialized || (P == FlywheelController.P && F == FlywheelController.F)) return;
        FlywheelController.P = P;
        FlywheelController.F = F;
        PIDFCoefficients pidfCoefficients = new PIDFCoefficients(P, 0, 0, F);
        flywheel.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidfCoefficients);
    }

    public static double getTargetVel() {
        return targetVel;
    }
    public static double getVelocity() {
        if (!isInitialized) return 0;
        return flywheel.getVelocity();
    }
    public static boolean isAtSpeed() {
        return isInitialized && targetVel != 0 && Math.abs(getVelocity() - targetVel) <= FLYWHEEL_VELOCITY_TOLERANCE;
    }

    public static void stop() {
        if (flywheel != null) flywheel.setPower(0);
        flywheel = null;
        targetVel = 0;
        isInitialized = false;
    }
}
