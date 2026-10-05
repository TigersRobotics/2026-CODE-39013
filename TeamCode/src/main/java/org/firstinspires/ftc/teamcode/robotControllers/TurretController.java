package org.firstinspires.ftc.teamcode.robotControllers;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class TurretController {
    private final String SERVO_LEFT = "0";
    private final String SERVO_RIGHT = "1";
    private Servo leftServo;
    private Servo rightServo;
    private Servo hoodServo;
    private DcMotorEx flywheel;
    private double flywheelMaxVel = 0.0;
    private double currentVelocity;

    public TurretController (HardwareMap hardwareMap, Telemetry telemetry) {
        leftServo = hardwareMap.get(Servo.class, SERVO_LEFT);
        rightServo = hardwareMap.get(Servo.class, SERVO_RIGHT);

        flywheel = hardwareMap.get(DcMotorEx.class, "flywheel");
        flywheel.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);

        telemetry.addLine("TURRET CONTROLLER INITIALIZED");
    }



    /// [...](https://docs.revrobotics.com/first-global/fgc-pid)
    /// This is just for tuning, not the actual tuned thing yet
    public void updateFlyWheel(Telemetry telemetry) {
        flywheel.setPower(1.0);

        currentVelocity = flywheel.getVelocity();

        if(currentVelocity > flywheelMaxVel) {
            flywheelMaxVel = currentVelocity;
        }

        telemetry.addLine("Current Velocity: " + currentVelocity);
        telemetry.addLine("Max Velocity: " + flywheelMaxVel);

    }
    public void setTurretPosition(double angle, boolean isRadians) {
        double newAngle = angle;
        if(!isRadians) {
            newAngle = Math.toRadians(angle);
        }

        // 0 is straight ahead, -pi/2 is full left, pi/2 is full right (180 degree servo, centered at 0.5)
        double position = 0.5 + newAngle/Math.PI;
        leftServo.setPosition(position);
        rightServo.setPosition(position);
    }

    public void setHoodAngle(double angle, boolean isRadians) {
        double newAngle = angle;
        if(!isRadians) {
            newAngle = Math.toRadians(angle);
        }

        setPositionRads(hoodServo, newAngle);
    }

    private void setPositionRads(Servo servo, double angle) {
     servo.setPosition(angle/Math.PI*2);
    }
}
