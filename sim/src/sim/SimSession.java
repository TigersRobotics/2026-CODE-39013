package sim;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.opmodes.TeleOpMain;

public class SimSession {
    public static final double LOOP_TIME = 0.02;

    public final SimRobot robot;
    public final SimTelemetry telemetry = new SimTelemetry();
    public final Gamepad gamepad1 = new Gamepad();
    public final Gamepad gamepad2 = new Gamepad();
    public final OpMode opMode;
    public double time;

    public SimSession(SimRobot robot) {
        this(robot, new TeleOpMain());
    }

    public SimSession(SimRobot robot, OpMode opMode) {
        this(robot, opMode, true);
    }

    public SimSession(SimRobot robot, OpMode opMode, boolean startNow) {
        this.robot = robot;
        this.opMode = opMode;
        opMode.hardwareMap = robot.buildHardwareMap();
        opMode.telemetry = telemetry;
        opMode.gamepad1 = gamepad1;
        opMode.gamepad2 = gamepad2;
        opMode.init();
        telemetry.updateIfDirty();
        if (startNow) start();
    }

    public void initLoop() {
        SimClock.advance(LOOP_TIME);
        gamepad1.latchPresses();
        gamepad2.latchPresses();
        opMode.init_loop();
        telemetry.updateIfDirty();
    }

    public void chooseAlliance(boolean red) {
        if (red) gamepad2.dpad_left = true;
        else gamepad2.dpad_right = true;
        initLoop();
        gamepad2.dpad_left = false;
        gamepad2.dpad_right = false;
        initLoop();
    }

    public void start() {
        opMode.start();
        telemetry.updateIfDirty();
    }

    public void tick() {
        SimClock.advance(LOOP_TIME);
        gamepad1.latchPresses();
        gamepad2.latchPresses();
        opMode.loop();
        telemetry.updateIfDirty();
        robot.step(LOOP_TIME);
        time += LOOP_TIME;
    }

    public void run(double seconds) {
        int loops = (int) Math.round(seconds / LOOP_TIME);
        for (int i = 0; i < loops; i++) tick();
    }

    public void tap(Runnable press, Runnable release) {
        press.run();
        tick();
        release.run();
        tick();
    }

    public void stop() {
        opMode.stop();
    }
}
