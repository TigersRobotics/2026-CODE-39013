package org.firstinspires.ftc.teamcode.pedro;

import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_BRAKE_KV;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_COAST_KV;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_FORWARD_BRAKE_LINEAR;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_FORWARD_BRAKE_QUADRATIC;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_FORWARD_DECELERATION;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_FORWARD_TRANSLATIONAL_PRIMARY_KP;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_FORWARD_TRANSLATIONAL_SECONDARY_KP;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_HEADING_BRAKE_LINEAR;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_HEADING_BRAKE_QUADRATIC;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_HEADING_KP;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_MAX_FORWARD_VELOCITY;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_MAX_STRAFE_VELOCITY;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_STRAFE_BRAKE_LINEAR;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_STRAFE_BRAKE_QUADRATIC;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_STRAFE_DECELERATION;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_STRAFE_TRANSLATIONAL_PRIMARY_KP;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_STRAFE_TRANSLATIONAL_SECONDARY_KP;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.FORESIGHT_TRANSLATIONAL_SWITCH_DISTANCE;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.constants.HardwareNames;
import org.firstinspires.ftc.teamcode.constants.RobotSpecs;

public class Constants {
    public static Follower create(HardwareMap h) {
        return new Follower(
                new PinpointLocalizer(h, localizerConfig),
                new Mecanum(h, drivetrainConfig),
                new Foresight(foresightConfig)
        );
    }

    public static MecanumConfig drivetrainConfig = new MecanumConfig(
            c -> {
                c.frontLeftName.set(HardwareNames.FRONT_LEFT_MOTOR_NAME);
                c.backLeftName.set(HardwareNames.BACK_LEFT_MOTOR_NAME);
                c.frontRightName.set(HardwareNames.FRONT_RIGHT_MOTOR_NAME);
                c.backRightName.set(HardwareNames.BACK_RIGHT_MOTOR_NAME);

                c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
                c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
                c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
                c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);

                c.manualBrakeMode.set(true);
            }
    );
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set(HardwareNames.ODOMETRY_NAME);
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD);
        c.xPodOffset.set(RobotSpecs.OFFSETS_OF_ODOMETRY_PODS_IN_X);
        c.yPodOffset.set(RobotSpecs.OFFSETS_OF_ODOMETRY_PODS_IN_Y);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });



    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(FORESIGHT_FORWARD_TRANSLATIONAL_PRIMARY_KP);
                Controller secondaryTranslationalForward = Controller.proportional(FORESIGHT_FORWARD_TRANSLATIONAL_SECONDARY_KP);
                Controller primaryTranslationalLateral = Controller.proportional(FORESIGHT_STRAFE_TRANSLATIONAL_PRIMARY_KP);
                Controller secondaryTranslationalLateral = Controller.proportional(FORESIGHT_STRAFE_TRANSLATIONAL_SECONDARY_KP);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(FORESIGHT_TRANSLATIONAL_SWITCH_DISTANCE, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(FORESIGHT_TRANSLATIONAL_SWITCH_DISTANCE, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(FORESIGHT_COAST_KV));
                c.brake.set(Controller.proportionalFeedforward(FORESIGHT_BRAKE_KV));

                c.headingFeedback.set(Controller.proportional(FORESIGHT_HEADING_KP));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(FORESIGHT_HEADING_BRAKE_LINEAR, FORESIGHT_HEADING_BRAKE_QUADRATIC));

                c.linearBrakeCoefficients.set(Matrix.diag(FORESIGHT_FORWARD_BRAKE_LINEAR, FORESIGHT_STRAFE_BRAKE_LINEAR));
                c.quadraticBrakeCoefficients.set(Matrix.diag(FORESIGHT_FORWARD_BRAKE_QUADRATIC, FORESIGHT_STRAFE_BRAKE_QUADRATIC));

                c.maxAchievableForwardVelocity.set(FORESIGHT_MAX_FORWARD_VELOCITY);
                c.maxAchievableStrafeVelocity.set(FORESIGHT_MAX_STRAFE_VELOCITY);
                c.naturalForwardDeceleration.set(FORESIGHT_FORWARD_DECELERATION);
                c.naturalStrafeDeceleration.set(FORESIGHT_STRAFE_DECELERATION);
            }
    );
}
