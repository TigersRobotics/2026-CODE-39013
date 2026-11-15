package org.firstinspires.ftc.teamcode.robotControllers;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.constants.HardwareNames;
import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;

public class SingleIntakeController {
    private static boolean isInitialized = false;
    private static DcMotor intakeMotor;
    private static double power;

    public static void init(HardwareMap hardwareMap, boolean actuallyInitialize) {
        if(actuallyInitialize) {
            intakeMotor = hardwareMap.get(DcMotor.class, HardwareNames.INTAKE_MOTOR_NAME);
            intakeMotor.setDirection(DcMotorSimple.Direction.FORWARD);
            power = 0;
            isInitialized = true;
            TimedTelemetry.addLine("Intake Initialized");
        } else {
            stop();
            TimedTelemetry.addLine("Single Intake Not Initialized!", 5000);
        }
    }
    public static void init(HardwareMap hardwareMap) {
        init(hardwareMap, true);
    }



    public static double getPower() {
        return power;
    }

    public static void update(double power) {
        if(isInitialized) {
            SingleIntakeController.power = power;

            intakeMotor.setPower(power);
            TimedTelemetry.addData("Intake Power", power);
        } else TimedTelemetry.addLine("Intake being updated but not initialized!");
    }
    public static void stop() {
        if (intakeMotor != null) intakeMotor.setPower(0);
        intakeMotor = null;
        power = 0;
        isInitialized = false;
    }
}
