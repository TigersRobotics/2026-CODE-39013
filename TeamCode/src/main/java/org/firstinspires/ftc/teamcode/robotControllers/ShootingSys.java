package org.firstinspires.ftc.teamcode.robotControllers;

import static org.firstinspires.ftc.teamcode.constants.HardwareNames.FLYWHEEL_NAME;
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
    //DcMotorEx uptake;
    DcMotorEx shooter;



    public ShootingSys(HardwareMap hardwareMap, Telemetry telemetry){
        this.telemetry = telemetry;
        intake = hardwareMap.get(DcMotorEx.class, INTAKE_MOTOR_NAME);

        shooter = hardwareMap.get(DcMotorEx.class,"shooter");

        //init direction
        shooter.setDirection(DcMotor.Direction.REVERSE);

        intake.setDirection(DcMotor.Direction.FORWARD);


        telemetry.addLine("ShootingSys initialized");


    }

    public void intakeSetPower(double power){
        intake.setPower(power);

    }

    public void shootSetPower(double power){
        shooter.setPower(power);

    }






}
