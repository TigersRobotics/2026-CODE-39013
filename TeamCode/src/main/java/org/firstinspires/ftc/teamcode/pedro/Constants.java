package org.firstinspires.ftc.teamcode.pedro;

import static org.firstinspires.ftc.teamcode.utils.Constants.BACK_LEFT_MOTOR_NAME;
import static org.firstinspires.ftc.teamcode.utils.Constants.BACK_RIGHT_MOTOR_NAME;
import static org.firstinspires.ftc.teamcode.utils.Constants.FRONT_LEFT_MOTOR_NAME;
import static org.firstinspires.ftc.teamcode.utils.Constants.FRONT_RIGHT_MOTOR_NAME;

import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants {
    // Pedro 3 configuration for the four mecanum drive motors.
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        // Use the same hardware names as MecanumDriveTrainController.
        // These must match the names in the Robot Controller configuration.
        c.frontLeftName.set(FRONT_LEFT_MOTOR_NAME);
        c.frontRightName.set(FRONT_RIGHT_MOTOR_NAME);
        c.backLeftName.set(BACK_LEFT_MOTOR_NAME);
        c.backRightName.set(BACK_RIGHT_MOTOR_NAME);

        // Match the directions currently used by MecanumDriveTrainController.
        // Verify on the robot; positive power should drive each wheel forward.
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });

    // The Pinpoint computer reads two odometry pods to estimate robot position.
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        // TODO: Match the Pinpoint device name in your robot configuration.
        c.name.set("pinpoint");

        // Matches the pod type in TwoWheelOdometry. The computer model alone
        // does not determine pod type: confirm which pods are attached.
        // For goBILDA 4-Bar pods, use goBILDA_4_BAR_POD instead.
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD);

        // The forward pod connects to X; the sideways pod connects to Y.
        // TODO: Verify directions with PinpointTuner; use REVERSED if needed.
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);

        // Pedro positions and the offsets below are in inches.
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);

        // TODO: Replace these zero placeholders with measured/tuned offsets.
        // Each offset is the signed perpendicular distance from the robot's
        // center of rotation to that pod's measurement line. PinpointTuner
        // calculates these values, including their signs, in inches.
        // Do not copy the millimetre values from utils.Constants directly.
        c.xPodOffset.set(0.0);
        c.yPodOffset.set(0.0);
    });

    public static Follower create(HardwareMap h) {
        // TODO: Add the tuned Foresight configuration, then construct a Follower
        // using PinpointLocalizer(h, localizerConfig),
        // Mecanum(h, drivetrainConfig), and Foresight(foresightConfig), in that order.
        // This factory is still unfinished; it cannot be used by Auto yet.
        return null;

    }
}
