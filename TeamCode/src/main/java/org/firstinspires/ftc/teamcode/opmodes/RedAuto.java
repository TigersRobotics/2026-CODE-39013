package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Red Auto", preselectTeleOp = "TeleOp Main")
public class RedAuto extends AutoMain {
    @Override
    protected boolean isRed() {
        return true;
    }
}
