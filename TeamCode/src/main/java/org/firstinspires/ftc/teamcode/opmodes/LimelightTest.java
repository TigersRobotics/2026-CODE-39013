package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.constants.RobotSpecs;
import org.firstinspires.ftc.teamcode.constants.TuningConstants;
import org.firstinspires.ftc.teamcode.robotControllers.LimelightController;
import org.firstinspires.ftc.teamcode.robotControllers.TurretController;
import org.firstinspires.ftc.teamcode.utils.OpModeUtils;
import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;

@TeleOp(name = "Limelight Test")
public class LimelightTest extends OpMode {
    private boolean autoAim = false;

    @Override
    public void init() {
        OpModeUtils.initSome(telemetry);
        LimelightController.init(hardwareMap);
        TurretController.init(hardwareMap);
        autoAim = false;
        OpModeUtils.updateSome();
    }

    @Override
    public void loop() {
        if (gamepad1.dpadUpWasPressed()) LimelightController.setTilt(LimelightController.getTilt() + TuningConstants.LIMELIGHT_TILT_STEP);
        if (gamepad1.dpadDownWasPressed()) LimelightController.setTilt(LimelightController.getTilt() - TuningConstants.LIMELIGHT_TILT_STEP);
        if (gamepad1.rightBumperWasPressed()) autoAim = !autoAim;

        LimelightController.update();

        if (!autoAim) TurretController.setYaw(gamepad1.left_stick_x * RobotSpecs.MAX_YAW);
        else if (LimelightController.hasTarget()) TurretController.aim(LimelightController.getTx());
        TurretController.update();

        TimedTelemetry.addData("Target", LimelightController.hasTarget() ? "yes" : "no");
        TimedTelemetry.addData("Tilt", "%.2f", 1, LimelightController.getTilt());
        TimedTelemetry.addData("Auto Aim", autoAim ? "ON" : "OFF");
        OpModeUtils.updateSome();
        telemetry.update();
    }

    @Override
    public void stop() {
        OpModeUtils.stopAll();
    }
}
