package org.firstinspires.ftc.teamcode.robotControllers;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;


/**
 * This class is used to control the mecanum drivetrain
 * @author Cole L
 * @version 1.0
 */
public class MecanumDriveTrainController {
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    private Telemetry telemetry;
    private double frontLeftPower;
    private double frontRightPower;
    private double backLeftPower;
    private double backRightPower;

    private double driveY;
    private double driveX;
    private double turn;

    public MecanumDriveTrainController(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");

        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.FORWARD);

        telemetry.addLine("MECANUM DRIVE TRAIN INITIALIZED");

    }

    /**
     * Updates the turn value
     * @param turn
     * does not actually update the movement. updateMovement must be called to update
     */
    public void updateTurn(double turn) {
        this.turn = turn;
    }

    /**
     * Updates the drive values
     * @param driveY drive in accordance to the front of the robot
     * @param driveX drive in accordance to the right of the robot
     * does not actually update the movement. updateMovement must be called to update
     */
    public void updateDrive(double driveY, double driveX) {
        this.driveY = driveY;
        this.driveX = driveX;
    }

    /**
     * Starts the movement of the robot.
     * Moves based off the values of driveY, driveX, and turn, set by updateDrive and updateTurn
     */
    public void updateMovement() {
        telemetry.addData("driveY: ", driveY);
        telemetry.addData("driveX: ", driveX);
        telemetry.addData("turn: ", turn);

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


        frontLeftPower = fl;
        frontRightPower = fr;
        backRightPower = br;
        backLeftPower = bl;

        // Send calculated power to wheels
        frontLeft.setPower(frontLeftPower);
        frontRight.setPower(frontRightPower);
        backLeft.setPower(backLeftPower);
        backRight.setPower(backRightPower);


        telemetry.addData("Motors", "front left (%.2f), front right (%.2f)", frontLeftPower, frontRightPower);
        telemetry.addData("Motors", "back left (%.2f), back right (%.2f)", backLeftPower, backRightPower);

    }

    /**
     * Starts the movement of the robot. Can be used instead of using updateDrive() and updateTurn()
     * @param driveY drive in accordance to the front of the robot
     * @param driveX drive in accordance to the right of the robot
     * @param turn turn in the clockwise direction(0-1)
     */
    public void updateMovement(double driveY, double driveX, double turn) {
        telemetry.addData("driveY: ", driveY);
        telemetry.addData("driveX: ", driveX);
        telemetry.addData("turn: ", turn);

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


        frontLeftPower = fl;
        frontRightPower = fr;
        backRightPower = br;
        backLeftPower = bl;

        // Send calculated power to wheels
        frontLeft.setPower(frontLeftPower);
        frontRight.setPower(frontRightPower);
        backLeft.setPower(backLeftPower);
        backRight.setPower(backRightPower);


        telemetry.addData("Motors", "front left (%.2f), front right (%.2f)", frontLeftPower, frontRightPower);
        telemetry.addData("Motors", "back left (%.2f), back right (%.2f)", backLeftPower, backRightPower);

    }

}
