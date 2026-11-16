package tuning;

import com.pedropathing.tuning.autotune.TuningOpMode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;
import com.qualcomm.robotcore.hardware.Gamepad;

import dev.frozenmilk.sinister.sdk.opmodes.SinisterRegisteredOpModes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sim.SimTelemetry;

public class SimOpModeManager extends OpModeManagerImpl {
    public static final double PUSH_DISTANCE = 24;

    private final TuningRobot robot;
    private LinearOpMode active;
    private SimTelemetry telemetry;
    private Thread thread;

    public SimOpModeManager(TuningRobot robot) {
        this.robot = robot;
    }

    @Override
    public void initOpMode(String name) {
        LinearOpMode opMode = (LinearOpMode) SinisterRegisteredOpModes.INSTANCE.get(name);
        synchronized (robot) {
            robot.resetMotors();
        }
        telemetry = new SimTelemetry();
        opMode.hardwareMap = robot.buildHardwareMap();
        opMode.telemetry = telemetry;
        opMode.gamepad1 = new Gamepad();
        opMode.gamepad2 = new Gamepad();
        active = opMode;
        thread = new Thread(() -> {
            try {
                opMode.runOpMode();
            } catch (Throwable e) {
                System.out.println("OpMode crashed: " + e);
            }
        }, "OpMode");
        thread.start();
    }

    @Override
    public void startActiveOpMode() {
        active.start();
        String title = active instanceof TuningOpMode ? ((TuningOpMode<?>) active).name : "OpMode";
        String description = active instanceof TuningOpMode ? ((TuningOpMode<?>) active).description : "";
        System.out.println("Running " + title);
        actLikeHuman(description);
    }

    private void actLikeHuman(String description) {
        Matcher exact = Pattern.compile("forward ([0-9.]+) inches exactly").matcher(description);
        if (exact.find()) pushRobot(Double.parseDouble(exact.group(1)), 0, 0, "forward " + exact.group(1) + " in");
        else if (description.contains("Push your robot forward")) pushRobot(PUSH_DISTANCE, 0, 0, "forward " + PUSH_DISTANCE + " in");
        else if (description.contains("Push your robot to the left")) pushRobot(0, PUSH_DISTANCE, 0, "left " + PUSH_DISTANCE + " in");
        else if (description.contains("Spin your robot in place 180 degrees counterclockwise")) pushRobot(0, 0, Math.PI, "180 degrees counterclockwise");
        else if (description.contains("motor will spin")) new Thread(this::reportWheel).start();
    }

    private void pushRobot(double forward, double left, double turn, String what) {
        System.out.println("  sim: a teammate pushes the robot " + what + ", then you press Stop");
        synchronized (robot) {
            robot.push(forward, left, turn);
        }
    }

    private void reportWheel() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            return;
        }
        synchronized (robot) {
            System.out.println("  sim: the wheel spinning is " + robot.spinningWheel());
        }
    }

    @Override
    public void requestOpModeStop(OpMode opMode) {
        opMode.stop();
        try {
            thread.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        synchronized (robot) {
            robot.resetMotors();
        }
        if (!telemetry.getShown().isEmpty()) System.out.println("  telemetry: " + String.join(" | ", telemetry.getShown()));
    }
}
