package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.constants.HardwareNames.FLOWER_DESCORE_NAME;
import static org.firstinspires.ftc.teamcode.constants.HardwareNames.ODOMETRY_NAME;
import static org.firstinspires.ftc.teamcode.utils.Constants.*;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.robotControllers.MecanumDriveTrainController;
import org.firstinspires.ftc.teamcode.robotControllers.ShootingSys;

@TeleOp
public class TeleopMain extends OpMode {
    ShootingSys shootingSys;
    double speed = 0.65;
    GoBildaOdometry odometry;
    private boolean isShooting = false;
    private boolean wasShootPressed = false;
    private Servo flower;
    private boolean flowerExtended = false;

    private static final double FLOWER_VERTICAL = 1.8;
    private static final double FLOWER_HORIZONTAL = 0;

    @Override
    public void init() {
        //servo init
        flower = hardwareMap.get(Servo.class, "flower");

        flower.setPosition(FLOWER_VERTICAL);


        MecanumDriveTrainController.init(hardwareMap);
        odometry = new GoBildaOdometry(hardwareMap, ODOMETRY_NAME);
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
        boolean intakePressed = gamepad2.left_trigger_pressed;
        boolean shootPressed = gamepad2.right_trigger_pressed;
        boolean squarePressed = gamepad2.squareWasPressed();
        boolean circlePressed = gamepad2.circleWasPressed();
        //two controllers setup
        odometry.update();
        telemetry.addData("xcontroller", leftStickX);
        telemetry.addData("ycontroller", leftStickY);
        telemetry.addData("intakeispressed", intakePressed);
        telemetry.addData("shotispressed", shootPressed);
        telemetry.addData("x from 0: ", odometry.getX(DistanceUnit.INCH));
        telemetry.addData("y from 0: ", odometry.getY(DistanceUnit.INCH));
        telemetry.addData("heading: ", odometry.getHeading(AngleUnit.RADIANS));
        telemetry.addData("flywheelspeed:   ",speed);
        telemetry.update();

        MecanumDriveTrainController.update(-leftStickY, leftStickX, rightStickX, odometry.getHeading(AngleUnit.RADIANS));


        if (gamepad1.squareWasPressed()) {
            flowerExtended = !flowerExtended;

            flower.setPosition(
                    flowerExtended ? FLOWER_HORIZONTAL : FLOWER_VERTICAL
            );
        }
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
            speed = Math.max(0.1, speed - 0.025);
        } else if (circlePressed) {
            speed = Math.min(1, speed + 0.025);
        }



        if (Math.abs(gamepad2.left_stick_y) > 0.1) {
            shootingSys.reverse(Math.abs(gamepad2.left_stick_y));
        } else {
            shootingSys.shootSetPower(isShooting ? speed : 0);
        }


    }
}