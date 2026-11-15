package org.firstinspires.ftc.teamcode.opmodes;


import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.constants.RobotSpecs;
import org.firstinspires.ftc.teamcode.robotControllers.MecanumDriveTrainController;
import org.firstinspires.ftc.teamcode.robotControllers.TurretController;
import org.firstinspires.ftc.teamcode.utils.OpModeUtils;

@TeleOp
public class ControllerTest extends OpMode {

    final boolean ENABLE_TURRET_CONTROLLER = true;
    final boolean ENABLE_DRIVE_CONTROLLER = true;

    @Override
    public void init() {
        OpModeUtils.initSome(telemetry);

        MecanumDriveTrainController.init(hardwareMap, ENABLE_DRIVE_CONTROLLER);
        TurretController.init(hardwareMap, ENABLE_TURRET_CONTROLLER);

        OpModeUtils.updateSome();
    }
    @Override
    public void loop() {

        TurretController.setYaw(gamepad2.left_stick_x * RobotSpecs.MAX_YAW);
        TurretController.update();

        if(gamepad1.rightBumperWasPressed()) MecanumDriveTrainController.increaseSpeed();
        if(gamepad1.leftBumperWasPressed()) MecanumDriveTrainController.decreaseSpeed();
        MecanumDriveTrainController.update(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);

        OpModeUtils.updateSome();
        telemetry.update();
    }

    @Override
    public void stop() {
        OpModeUtils.stopAll();
    }
}
