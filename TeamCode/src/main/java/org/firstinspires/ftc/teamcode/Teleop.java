package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.robotControllers.MecanumDriveTrainController;

public class Teleop extends OpMode {
    MecanumDriveTrainController driveTrain;

    @Override
    public void init() {


        driveTrain = new MecanumDriveTrainController(hardwareMap,telemetry);

    }

    @Override
    public void loop() {
        //uses the controller obj method to move the robot
        driveTrain.updateMovement(gamepad1.left_stick_y, gamepad1.left_stick_x,gamepad1.right_stick_x);

    }
}
