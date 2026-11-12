package org.firstinspires.ftc.teamcode.robotControllers;

import static org.firstinspires.ftc.teamcode.utils.Constants.INTAKE_MOTOR_NAME;
import static org.firstinspires.ftc.teamcode.utils.Constants.SHOOTER_MOTOR_NAME;
import static org.firstinspires.ftc.teamcode.utils.Constants.UPTAKE_MOTOR_NAME;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class ShootingSys {
    Telemetry telemetry;
    DcMotorEx intake;
    DcMotorEx uptake;
    DcMotorEx shooter;
    public ShootingSys(HardwareMap hardwareMap, Telemetry telemetry){
        this.telemetry = telemetry;
        intake = hardwareMap.get(DcMotorEx.class, INTAKE_MOTOR_NAME);
        uptake = hardwareMap.get(DcMotorEx.class,UPTAKE_MOTOR_NAME);
        shooter = hardwareMap.get(DcMotorEx.class,SHOOTER_MOTOR_NAME);

        //init direction
        shooter.setDirection(DcMotor.Direction.REVERSE);
        uptake.setDirection(DcMotor.Direction.REVERSE);
        intake.setDirection(DcMotor.Direction.FORWARD);


        telemetry.addLine("MECANUM DRIVE TRAIN INITIALIZED");

    }
    /**
     * Starts the movement of the robot. Can be used instead of using updateDrive() and updateTurn()
     * @param speedIntake input the speed in rotations/s
     * @param speedUptake input the speed in rotations/s
     *
     */
    public void updateMoveBallUp(float speedIntake,float speedUptake){
        float radSpeedIntake = (float) (2*Math.PI * speedIntake);
        float radSpeedUptake = (float) (2*Math.PI * speedIntake);
    }

    /**
     * Starts the movement of the robot. Can be used instead of using updateDrive() and updateTurn()
     * @param speed input the speed in rotations/s
     *
     */
    public void updateShoot(float speed){
        float radSpeed = (float) (2*Math.PI * speed);

        shooter.setVelocity(radSpeed);


    }

}
