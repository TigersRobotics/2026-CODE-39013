package org.firstinspires.ftc.teamcode.constants;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.robotControllers.FlywheelController;
import org.firstinspires.ftc.teamcode.robotControllers.LimelightController;
import org.firstinspires.ftc.teamcode.robotControllers.MecanumDriveTrainController;
import org.firstinspires.ftc.teamcode.robotControllers.SingleIntakeController;
import org.firstinspires.ftc.teamcode.robotControllers.TurretController;
import org.firstinspires.ftc.teamcode.robotControllers.TurretUpAngleController;
import org.firstinspires.ftc.teamcode.robotControllers.UptakeController;
import org.firstinspires.ftc.teamcode.robotControllers.UptakeServoController;
import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;
import org.firstinspires.ftc.teamcode.utils.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
@Deprecated
public class OpModeMethods {
    public static ArrayList<Runnable> NO_PARAM_INIT = new ArrayList<>(
            List.of(
                    Timer::init
            )
    );
    public static ArrayList<Consumer<HardwareMap>> HARDWARE_MAP_INIT = new ArrayList<>(
            List.of(
                    MecanumDriveTrainController::init,
                    SingleIntakeController::init,
                    UptakeController::init,
                    FlywheelController::init,
                    UptakeServoController::init,
                    TurretUpAngleController::init,
                    TurretController::init,
                    LimelightController::init
            )
    );

    public static ArrayList<Runnable> NO_PARAM_UPDATE = new ArrayList<>(
            List.of(
                    Timer::update,
                    TimedTelemetry::update
            )
    );

    public static ArrayList<Runnable> NO_PARAM_STOP = new ArrayList<>(
            List.of(
                    MecanumDriveTrainController::stop,
                    SingleIntakeController::stop,
                    UptakeController::stop,
                    FlywheelController::stop,
                    UptakeServoController::stop,
                    TurretUpAngleController::stop,
                    TurretController::stop,
                    LimelightController::stop,
                    TimedTelemetry::stop
            )
    );
}
