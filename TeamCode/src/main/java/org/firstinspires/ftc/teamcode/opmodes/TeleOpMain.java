package org.firstinspires.ftc.teamcode.opmodes;

import static org.firstinspires.ftc.teamcode.constants.Competition.BLUE_STARTING_HEADING_DEG;
import static org.firstinspires.ftc.teamcode.constants.Competition.BLUE_STARTING_X_IN;
import static org.firstinspires.ftc.teamcode.constants.Competition.BLUE_STARTING_Y_IN;
import static org.firstinspires.ftc.teamcode.constants.Competition.RED_STARTING_HEADING_DEG;
import static org.firstinspires.ftc.teamcode.constants.Competition.RED_STARTING_X_IN;
import static org.firstinspires.ftc.teamcode.constants.Competition.RED_STARTING_Y_IN;
import static org.firstinspires.ftc.teamcode.constants.Competition.lastAutoPose;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FLYWHEEL_TARGET_VELOCITY;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.INTAKE_POWER;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.INTAKE_REVERSE_POWER;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.TRIGGER_THRESHOLD;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.UPTAKE_POWER;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.constants.HardwareNames;
import org.firstinspires.ftc.teamcode.constants.RobotSpecs;
import org.firstinspires.ftc.teamcode.robotControllers.FlywheelController;
import org.firstinspires.ftc.teamcode.robotControllers.LimelightController;
import org.firstinspires.ftc.teamcode.robotControllers.MecanumDriveTrainController;
import org.firstinspires.ftc.teamcode.robotControllers.SingleIntakeController;
import org.firstinspires.ftc.teamcode.robotControllers.TurretController;
import org.firstinspires.ftc.teamcode.robotControllers.TurretUpAngleController;
import org.firstinspires.ftc.teamcode.robotControllers.UptakeController;
import org.firstinspires.ftc.teamcode.robotControllers.UptakeServoController;
import org.firstinspires.ftc.teamcode.sensors.GoBildaOdometry;
import org.firstinspires.ftc.teamcode.utils.AutoAim;
import org.firstinspires.ftc.teamcode.utils.OpModeUtils;
import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;

@TeleOp(name = "TeleOp Main")
public class TeleOpMain extends OpMode {
    private boolean autoAim = false;
    private boolean red = true;
    private boolean farUp = false;
    private GoBildaOdometry odometry;

    @Override
    public void init() {
        OpModeUtils.initAll(hardwareMap, telemetry);
        autoAim = false;
        setAlliance(true);
        try {
            odometry = new GoBildaOdometry(hardwareMap, HardwareNames.ODOMETRY_NAME);
        } catch (IllegalArgumentException e) {
            odometry = null;
            TimedTelemetry.addLine("No Pinpoint found, aiming uses the Limelight only", 5000);
        }
        OpModeUtils.updateSome();
    }

    @Override
    public void init_loop() {
        if (gamepad2.dpadLeftWasPressed()) setAlliance(true);
        if (gamepad2.dpadRightWasPressed()) setAlliance(false);
        TimedTelemetry.addData("Alliance", red ? "RED" : "BLUE");
        TimedTelemetry.addLine("Gamepad 2 dpad left for red, dpad right for blue");
        OpModeUtils.updateSome();
        telemetry.update();
    }

    @Override
    public void start() {
        if (odometry == null) return;
        if (lastAutoPose != null) odometry.setPose(lastAutoPose);
        else if (red) odometry.setPose(new Pose2D(DistanceUnit.INCH, RED_STARTING_X_IN, RED_STARTING_Y_IN, AngleUnit.DEGREES, RED_STARTING_HEADING_DEG));
        else odometry.setPose(new Pose2D(DistanceUnit.INCH, BLUE_STARTING_X_IN, BLUE_STARTING_Y_IN, AngleUnit.DEGREES, BLUE_STARTING_HEADING_DEG));
        lastAutoPose = null;
    }

    @Override
    public void loop() {
        if (odometry != null) odometry.update();
        boolean hasPose = odometry != null && odometry.getStatus() == GoBildaPinpointDriver.DeviceStatus.READY;

        if (gamepad1.rightBumperWasPressed()) MecanumDriveTrainController.increaseSpeed();
        if (gamepad1.leftBumperWasPressed()) MecanumDriveTrainController.decreaseSpeed();
        MecanumDriveTrainController.update(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);

        if (gamepad2.rightBumperWasPressed()) autoAim = !autoAim;
        if (gamepad2.dpadLeftWasPressed() || gamepad2.dpadRightWasPressed()) {
            farUp = !farUp;
            LimelightController.setPipeline(AutoAim.limelightPipeline(red, farUp));
        }

        double distance = 0;
        if (hasPose) distance = AutoAim.distance(odometry.getX(DistanceUnit.INCH), odometry.getY(DistanceUnit.INCH), red, farUp);
        boolean poseAim = autoAim && hasPose;

        LimelightController.update();
        if (!autoAim) TurretController.setYaw(gamepad2.left_stick_x * RobotSpecs.MAX_YAW);
        else if (LimelightController.hasTarget()) TurretController.aim(LimelightController.getTx());
        else if (hasPose) TurretController.setYaw(AutoAim.turretYaw(odometry.getX(DistanceUnit.INCH), odometry.getY(DistanceUnit.INCH), odometry.getHeading(AngleUnit.DEGREES), red, farUp));
        TurretController.update();

        double shotVelocity = poseAim ? AutoAim.flywheelVelocity(distance) : FLYWHEEL_TARGET_VELOCITY;
        FlywheelController.setTargetVel(gamepad2.right_trigger > TRIGGER_THRESHOLD ? shotVelocity : 0);
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

        if (poseAim) TurretUpAngleController.setTargetAngle(AutoAim.hoodAngle(distance));
        boolean hoodUp = gamepad2.dpadUpWasPressed();
        boolean hoodDown = gamepad2.dpadDownWasPressed();
        TurretUpAngleController.update(poseAim ? 0 : hoodUp ? 1 : hoodDown ? -1 : 0);

        TimedTelemetry.addData("Auto Aim", autoAim ? "ON" : "OFF");
        TimedTelemetry.addData("Alliance", red ? "RED" : "BLUE");
        TimedTelemetry.addData("Up Cell", farUp ? "far" : "audience");
        if (hasPose) {
            TimedTelemetry.addData("Pose", "x %.1f y %.1f heading %.0f", 1,
                    odometry.getX(DistanceUnit.INCH), odometry.getY(DistanceUnit.INCH), odometry.getHeading(AngleUnit.DEGREES));
            TimedTelemetry.addData("Distance", "%.0f in", 1, distance);
        } else TimedTelemetry.addLine("Pose: not available");
        OpModeUtils.updateSome();
        telemetry.update();
    }

    @Override
    public void stop() {
        OpModeUtils.stopAll();
    }

    private void setAlliance(boolean red) {
        this.red = red;
        farUp = !red;
        LimelightController.setPipeline(AutoAim.limelightPipeline(red, farUp));
    }
}
