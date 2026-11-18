package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.utils.Constants.*;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.robotControllers.MecanumDriveTrainController;
import org.firstinspires.ftc.teamcode.robotControllers.ShootingSys;

@Autonomous
public class Teleop extends OpMode {
    ShootingSys shootingSys;
    @Override
    public void init() {


        MecanumDriveTrainController.init(hardwareMap);
        shootingSys = new ShootingSys(hardwareMap,telemetry);

    }

    @Override
    public void loop() {
        //uses the controller obj method to move the robot
        float leftStickY = gamepad1.left_stick_y;
        float leftStickX = gamepad1.left_stick_x;
        float rightStickX = gamepad1.right_stick_x;
        boolean intakePressed = gamepad1.left_trigger_pressed;
        boolean shootPressed = gamepad1.right_trigger_pressed;


        MecanumDriveTrainController.update(leftStickY,leftStickX ,rightStickX);

        if(intakePressed){
            shootingSys.CompShooting(INTAKE_SPEED, UPTAKE_SPEED);
        }
        else {
            shootingSys.CompShooting(0, 0);
        }
    }
}
