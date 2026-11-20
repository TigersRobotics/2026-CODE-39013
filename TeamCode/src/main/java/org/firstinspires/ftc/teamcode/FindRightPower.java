package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

public class FindRightPower {
    GoBildaOdometry odometry;
    FindRightPower(){
        odometry = new GoBildaOdometry(hardwareMap,"odo");
    }


}
