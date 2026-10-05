package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robotControllers.MecanumDriveTrainController;
import org.firstinspires.ftc.teamcode.robotControllers.TurretController;

@TeleOp
public class ControllerTest extends OpMode {
    TurretController turretController;
    MecanumDriveTrainController driveController;

    final boolean ENABLE_TURRET_CONTROLLER = true;
    final boolean ENABLE_DRIVE_CONTROLLER = true;

    @Override
    public void init() {

        if(ENABLE_TURRET_CONTROLLER) turretController = new TurretController(hardwareMap, telemetry);
        if(ENABLE_DRIVE_CONTROLLER) driveController = new MecanumDriveTrainController(hardwareMap, telemetry);
    }
    @Override
    public void loop() {

        if(ENABLE_TURRET_CONTROLLER) {
            turretController.setTurretPosition(gamepad2.left_stick_x*Math.PI, true);
        }

        if(ENABLE_DRIVE_CONTROLLER) {
            if(gamepad1.rightBumperWasPressed()) driveController.increaseSpeed();
            if(gamepad1.leftBumperWasPressed()) driveController.decreaseSpeed();

            // stick y is negative when pushed up
            driveController.updateMovement(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
        }



        telemetry.update();
    }


}
