package org.firstinspires.ftc.teamcode;
import robotcontroller

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.robotControllers.MecanumDriveTrainController;

public class TeleopMain extends LinearOpMode{



    @Override
    public void runOpMode() throws InterruptedException {
        initThis();
        boolean running = true;
        while (running) {

            if(gamepad1.a){
                running = false;
            }

        }
    }

    private void initThis() {
        MecanumDriveTrainController Drivetrain;




    }
}
