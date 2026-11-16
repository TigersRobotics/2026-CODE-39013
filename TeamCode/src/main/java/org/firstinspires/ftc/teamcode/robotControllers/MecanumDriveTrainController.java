package org.firstinspires.ftc.teamcode.robotControllers;

import static org.firstinspires.ftc.teamcode.constants.HardwareNames.BACK_LEFT_MOTOR_NAME;
import static org.firstinspires.ftc.teamcode.constants.HardwareNames.BACK_RIGHT_MOTOR_NAME;
import static org.firstinspires.ftc.teamcode.constants.HardwareNames.FRONT_LEFT_MOTOR_NAME;
import static org.firstinspires.ftc.teamcode.constants.HardwareNames.FRONT_RIGHT_MOTOR_NAME;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.DRIVE_SPEED_LEVELS;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.controller.CustomController;
import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;

public class MecanumDriveTrainController{
    public static boolean isInitialized = false;
    private static DcMotor frontLeft;
    private static DcMotor frontRight;
    private static DcMotor backLeft;
    private static DcMotor backRight;
    private static double frontLeftPower;
    private static double frontRightPower;
    private static double backLeftPower;
    private static double backRightPower;
    private static int speedLevel = DRIVE_SPEED_LEVELS.length - 1;

    public static void init(HardwareMap hardwareMap, boolean actuallyInitialize) {
        if(actuallyInitialize) {

            frontLeft = hardwareMap.get(DcMotor.class, FRONT_LEFT_MOTOR_NAME);
            frontRight = hardwareMap.get(DcMotor.class, FRONT_RIGHT_MOTOR_NAME);
            backLeft = hardwareMap.get(DcMotor.class, BACK_LEFT_MOTOR_NAME);
            backRight = hardwareMap.get(DcMotor.class, BACK_RIGHT_MOTOR_NAME);

            frontLeft.setDirection(DcMotor.Direction.REVERSE);
            backLeft.setDirection(DcMotor.Direction.REVERSE);
            frontRight.setDirection(DcMotor.Direction.FORWARD);
            backRight.setDirection(DcMotor.Direction.FORWARD);

            frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

            speedLevel = DRIVE_SPEED_LEVELS.length - 1;

            TimedTelemetry.addLine("MECANUM DRIVE TRAIN INITIALIZED");

            isInitialized = true;
        } else stop();
    }

    public static void init(HardwareMap hardwareMap) {
        init(hardwareMap, true);
    }

    public static void stop() {
        if (frontLeft != null) frontLeft.setPower(0);
        if (frontRight != null) frontRight.setPower(0);
        if (backLeft != null) backLeft.setPower(0);
        if (backRight != null) backRight.setPower(0);

        isInitialized = false;
        frontLeft = null;
        frontRight = null;
        backLeft = null;
        backRight = null;
        frontLeftPower = 0;
        frontRightPower = 0;
        backLeftPower = 0;
        backRightPower = 0;
        speedLevel = DRIVE_SPEED_LEVELS.length - 1;
    }

    public static void update(double driveY, double driveX) {
        update(driveY,driveX, 0);
    }

    public static void update() {
        update(0, 0, 0);
    }

    public static void update(double driveY, double driveX, double turn) {
        if(isInitialized) {
            TimedTelemetry.addData("driveY: ", driveY);
            TimedTelemetry.addData("driveX: ", driveX);
            TimedTelemetry.addData("turn: ", turn);

            double fl = driveY + driveX + turn;
            double fr = driveY - driveX - turn;
            double bl = driveY - driveX + turn;
            double br = driveY + driveX - turn;

            double max = Math.max(Math.max(Math.abs(fl), Math.abs(fr)), Math.max(Math.abs(bl), Math.abs(br)));

            if (max > 1.0) {
                fl /= max;
                fr /= max;
                bl /= max;
                br /= max;
            }

            double speed = getSpeed();

            frontLeftPower = fl * speed;
            frontRightPower = fr * speed;
            backRightPower = br * speed;
            backLeftPower = bl * speed;

            frontLeft.setPower(frontLeftPower);
            frontRight.setPower(frontRightPower);
            backLeft.setPower(backLeftPower);
            backRight.setPower(backRightPower);

            TimedTelemetry.addData("Motors", "front left (%.2f), front right (%.2f)", 1, frontLeftPower, frontRightPower);
            TimedTelemetry.addData("Motors", "back left (%.2f), back right (%.2f)", 1, backLeftPower, backRightPower);
            TimedTelemetry.addData("Speed", "%.0f%%", 1, speed * 100);
        } else TimedTelemetry.addLine("Drivetrain being updated but not initialized!");
    }
    public static void update(CustomController controller) {
        update(controller.getDriveY(), controller.getDriveX(), controller.getTurn());
    }

    public static void increaseSpeed() {
        speedLevel = Math.min(speedLevel + 1, DRIVE_SPEED_LEVELS.length - 1);
    }

    public static void decreaseSpeed() {
        speedLevel = Math.max(speedLevel - 1, 0);
    }

    public static double getSpeed() {
        return DRIVE_SPEED_LEVELS[speedLevel];
    }

    public static double[] getPowers() {
        return new double[]{frontLeftPower, frontRightPower, backLeftPower, backRightPower};
    }
}
