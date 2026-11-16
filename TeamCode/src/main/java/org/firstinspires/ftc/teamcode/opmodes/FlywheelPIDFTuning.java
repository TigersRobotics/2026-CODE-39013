package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.constants.TuningConstants;
import org.firstinspires.ftc.teamcode.robotControllers.FlywheelController;
import org.firstinspires.ftc.teamcode.utils.OpModeUtils;
import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;

@TeleOp(name = "Flywheel PIDF Tuning")
public class FlywheelPIDFTuning extends OpMode {

    public double highVelocity = TuningConstants.FLYWHEEL_HIGH_VELOCITY;
    public double lowVelocity = TuningConstants.FLYWHEEL_LOW_VELOCITY;
    private double currentTargetVelocity = highVelocity;

    private double P = TuningConstants.FLYWHEEL_P_VALUE;
    private double F = TuningConstants.FLYWHEEL_F_VALUE;

    private final double[] stepSizes = {10.0, 1.0, 0.1, 0.01, 0.001, 0.0001};
    private int stepIndex = 1;

    @Override
    public void init() {
        OpModeUtils.initSome(telemetry);
        FlywheelController.init(hardwareMap);

        TimedTelemetry.addLine("Init complete");
        OpModeUtils.updateSome();
    }

    @Override
    public void loop() {
        if (gamepad1.circleWasPressed()) {
            currentTargetVelocity = (currentTargetVelocity == highVelocity) ? lowVelocity : highVelocity;
        }

        if (gamepad1.triangleWasPressed()) {
            stepIndex = (stepIndex + 1) % stepSizes.length;
        }
        if(gamepad1.squareWasPressed()) stepIndex = (stepIndex -1 + stepSizes.length) % stepSizes.length;

        if (gamepad1.leftTriggerWasPressed()) F += stepSizes[stepIndex];
        if (gamepad1.rightTriggerWasPressed()) F -= stepSizes[stepIndex];

        if (gamepad1.leftBumperWasPressed()) P += stepSizes[stepIndex];
        if (gamepad1.rightBumperWasPressed()) P -= stepSizes[stepIndex];

        FlywheelController.setPFCoefficients(P, F);
        FlywheelController.setTargetVel(currentTargetVelocity);
        FlywheelController.update();

        double currVel = FlywheelController.getVelocity();
        double err = currentTargetVelocity - currVel;

        TimedTelemetry.addData("Target Velocity", currentTargetVelocity);
        TimedTelemetry.addData("Current Velocity", "%.2f", 1, currVel);
        TimedTelemetry.addData("Error", "%.2f", 1, err);
        TimedTelemetry.addLine("-----------------------");
        TimedTelemetry.addData("P", "%.4f", 1, P);
        TimedTelemetry.addData("F", "%.4f", 1, F);
        TimedTelemetry.addData("Step Size", "%.4f (index %d)", 1, stepSizes[stepIndex], stepIndex);
        TimedTelemetry.addLine("-----------------------");
        TimedTelemetry.addLine("Circle: toggle target | Triangle/Square: step size");
        TimedTelemetry.addLine("LT/RT: F +/- | LB/RB: P +/-");

        OpModeUtils.updateSome();
        telemetry.update();
    }

    @Override
    public void stop() {
        OpModeUtils.stopAll();
    }
}
