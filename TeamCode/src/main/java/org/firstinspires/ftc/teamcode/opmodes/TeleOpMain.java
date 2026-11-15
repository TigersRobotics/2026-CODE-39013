package org.firstinspires.ftc.teamcode.opmodes;

import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FLYWHEEL_TARGET_VELOCITY;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.INTAKE_POWER;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.INTAKE_REVERSE_POWER;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.TRIGGER_THRESHOLD;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.UPTAKE_POWER;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.constants.RobotSpecs;
import org.firstinspires.ftc.teamcode.robotControllers.FlywheelController;
import org.firstinspires.ftc.teamcode.robotControllers.LimelightController;
import org.firstinspires.ftc.teamcode.robotControllers.MecanumDriveTrainController;
import org.firstinspires.ftc.teamcode.robotControllers.SingleIntakeController;
import org.firstinspires.ftc.teamcode.robotControllers.TurretController;
import org.firstinspires.ftc.teamcode.robotControllers.TurretUpAngleController;
import org.firstinspires.ftc.teamcode.robotControllers.UptakeController;
import org.firstinspires.ftc.teamcode.robotControllers.UptakeServoController;
import org.firstinspires.ftc.teamcode.utils.OpModeUtils;
import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;

@TeleOp(name = "TeleOp Main")
public class TeleOpMain extends OpMode {
    private boolean autoAim = false;

    @Override
    public void init() {
        OpModeUtils.initAll(hardwareMap, telemetry);
        autoAim = false;
        OpModeUtils.updateSome();
    }

    @Override
    public void loop() {
        if (gamepad1.rightBumperWasPressed()) MecanumDriveTrainController.increaseSpeed();
        if (gamepad1.leftBumperWasPressed()) MecanumDriveTrainController.decreaseSpeed();
        MecanumDriveTrainController.update(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);

        if (gamepad2.rightBumperWasPressed()) autoAim = !autoAim;
        LimelightController.update();
        if (!autoAim) TurretController.setYaw(gamepad2.left_stick_x * RobotSpecs.MAX_YAW);
        else if (LimelightController.hasTarget()) TurretController.aim(LimelightController.getTx());
        TurretController.update();

        FlywheelController.setTargetVel(gamepad2.right_trigger > TRIGGER_THRESHOLD ? FLYWHEEL_TARGET_VELOCITY : 0);
        FlywheelController.update();

        double intakePower = 0;
        double uptakePower = 0;
        if (gamepad2.left_bumper) {
            intakePower = INTAKE_REVERSE_POWER;
        } else if (gamepad2.left_trigger > TRIGGER_THRESHOLD) {
            intakePower = INTAKE_POWER;
            uptakePower = UPTAKE_POWER;
        }
        SingleIntakeController.update(intakePower);
        UptakeController.update(uptakePower);

        if (gamepad2.crossWasPressed()) {
            if (FlywheelController.isAtSpeed()) UptakeServoController.shoot();
            else TimedTelemetry.addLine("Flywheel not at speed, not firing", 1000);
        }
        UptakeServoController.update();

        boolean hoodUp = gamepad2.dpadUpWasPressed();
        boolean hoodDown = gamepad2.dpadDownWasPressed();
        TurretUpAngleController.update(hoodUp ? 1 : hoodDown ? -1 : 0);

        TimedTelemetry.addData("Auto Aim", autoAim ? "ON" : "OFF");
        OpModeUtils.updateSome();
        telemetry.update();
    }

    @Override
    public void stop() {
        OpModeUtils.stopAll();
    }
}
