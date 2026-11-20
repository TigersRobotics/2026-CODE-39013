package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.utils.Constants.*;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.robotControllers.MecanumDriveTrainController;
import org.firstinspires.ftc.teamcode.robotControllers.ShootingSys;

@TeleOp
public class TeleopMain extends OpMode {
    ShootingSys shootingSys;
    double speed = 1;
    GoBildaOdometry odometry;
    private boolean isShooting = false;
    private boolean wasShootPressed = false;

    @Override
    public void init() {

        Pose2D startPos = new Pose2D(DistanceUnit.CM, 0, 0d, AngleUnit.DEGREES, 0);
        MecanumDriveTrainController.init(hardwareMap);
        odometry = new GoBildaOdometry(hardwareMap, "odo");
        shootingSys = new ShootingSys(hardwareMap, telemetry);
        Boolean isShooting = false;
        telemetry.update();

    }

    @Override
    public void loop() {

        //uses the controller obj method to move the robot
        float leftStickY = gamepad1.left_stick_y;
        float leftStickX = gamepad1.left_stick_x;
        float rightStickX = gamepad1.right_stick_x;
        boolean intakePressed = gamepad1.left_trigger_pressed;
        boolean shootPressed = gamepad1.right_trigger_pressed;
        boolean squarePressed = gamepad1.squareWasPressed();
        boolean circlePressed = gamepad1.circleWasPressed();

        odometry.update();
        telemetry.addData("xcontroller", leftStickX);
        telemetry.addData("ycontroller", leftStickY);
        telemetry.addData("intakeispressed", intakePressed);
        telemetry.addData("shotispressed", shootPressed);
        telemetry.addData("pose: ", odometry.getPose());
        telemetry.addData("heading: ", odometry.getHeading(AngleUnit.DEGREES));
        telemetry.update();

        MecanumDriveTrainController.update(leftStickY, -leftStickX, rightStickX);

        if (intakePressed) {
            shootingSys.intakeSetPower(150);
        } else {
            shootingSys.intakeSetPower(0);

        }
        if (shootPressed && !wasShootPressed) {
            isShooting = !isShooting;
        }

        wasShootPressed = shootPressed;

        if (squarePressed) {
            speed = Math.max(0, speed - 0.3);
        } else if (circlePressed) {
            speed = Math.min(1, speed + 0.3);
        }

        shootingSys.shootSetPower(isShooting ? speed : 0);


    }
}