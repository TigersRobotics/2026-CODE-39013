package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Blue Auto", preselectTeleOp = "TeleOp Main")
public class BlueAuto extends AutoMain {
    @Override
    protected boolean isRed() {
        return false;
    }
}
