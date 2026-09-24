package org.firstinspires.ftc.teamcode.robotControllers;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class TurretController {
    private final String SERVO_LEFT = "0";
    private final String SERVO_RIGHT = "1";
    private Servo leftServo;
    private Servo rightServo;


    public TurretController (HardwareMap hardwareMap) {
        leftServo = hardwareMap.get(Servo.class, SERVO_LEFT);
        rightServo = hardwareMap.get(Servo.class, SERVO_RIGHT);
    }

    public void setPosition(double angle, boolean isRadians) {
        double newAngle = angle;
        if(!isRadians) {
            newAngle = Math.toRadians(angle);
        }

        leftServo.setPosition(newAngle);
        rightServo.setPosition(newAngle);
    }
}
