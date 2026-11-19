package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.utils.Constants.*;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.robotControllers.MecanumDriveTrainController;
import org.firstinspires.ftc.teamcode.robotControllers.ShootingSys;

@Autonomous
public class TeleopMain extends OpMode {
    ShootingSys shootingSys;
    GoBildaOdometry odometry;
    @Override
    public void init() {

        Pose2D startPos = new Pose2D(DistanceUnit.CM,0,0d, AngleUnit.DEGREES,0);
        MecanumDriveTrainController.init(hardwareMap);
        odometry = new GoBildaOdometry(hardwareMap,"odo");
        shootingSys = new ShootingSys(hardwareMap,telemetry);

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

        odometry.update();
        telemetry.addData("pose: ",odometry.getPose());
        telemetry.addData("heading: ",odometry.getHeading(AngleUnit.DEGREES));
        telemetry.update();

        MecanumDriveTrainController.update(leftStickY,leftStickX ,rightStickX);

        if(intakePressed){
            shootingSys.intakeSetPower(100);
        }
        else{
            shootingSys.intakeSetPower(0);
        }
        if(shootPressed){
            shootingSys.shootSetPower(100);
        }
        else{
            shootingSys.shootSetPower(0);
        }
    }
}
