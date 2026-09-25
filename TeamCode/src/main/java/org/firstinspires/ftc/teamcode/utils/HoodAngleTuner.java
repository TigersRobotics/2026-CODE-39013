package org.firstinspires.ftc.teamcode.utils;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

@TeleOp
public class HoodAngleTuner extends OpMode {

    private double angle = 45;
    private double delta = 22.5;

    private Servo hoodServo;

    @Override
    public void init() {
        hoodServo = hardwareMap.get(Servo.class, "hood");
    }

    @Override
    public void loop() {

        hoodServo.setPosition(angle/180);

        if(gamepad1.dpadUpWasPressed()) {
            angle += delta;
            delta /= 2;
        }
        if(gamepad1.dpadDownWasPressed()) {
            angle -= delta;
            delta /= 2;
        }
        telemetry.addLine("Angle: " + angle);
    }
}
