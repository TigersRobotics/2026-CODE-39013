package org.firstinspires.ftc.teamcode.robotControllers;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.constants.HardwareNames;
import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;

public class UptakeController {
    private static DcMotor uptakeMotor;
    private static double power;
    private static boolean isInitialized;


    public static void init(HardwareMap hardwareMap, boolean actuallyEnabled) {
        if(actuallyEnabled) {
            uptakeMotor = hardwareMap.get(DcMotor.class, HardwareNames.UPTAKE_MOTOR_NAME);
            uptakeMotor.setDirection(DcMotorSimple.Direction.REVERSE);
            power = 0;
            isInitialized = true;
            TimedTelemetry.addLine("Uptake Initialized");
        } else stop();
    }

    public static void init(HardwareMap hardwareMap) {
        init(hardwareMap, true);
    }

    public static void update(double power) {
        if(isInitialized) {
            UptakeController.power = power;
            uptakeMotor.setPower(power);
            TimedTelemetry.addData("Uptake Power", power);
        } else TimedTelemetry.addLine("Uptake being updated but not initialized!");
    }

    public static double getPower() {
        if(!isInitialized) return 0;
        return  power;
    }

    public static void stop() {
        if (uptakeMotor != null) uptakeMotor.setPower(0);
        isInitialized = false;
        uptakeMotor = null;
        power = 0;
    }
}
