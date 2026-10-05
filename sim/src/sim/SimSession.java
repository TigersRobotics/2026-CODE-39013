package sim;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.ControllerTest;

/** Runs a real team OpMode against the simulated robot */
public class SimSession {
    public static final double LOOP_TIME = 0.02;

    public final SimRobot robot;
    public final SimTelemetry telemetry = new SimTelemetry();
    public final Gamepad gamepad1 = new Gamepad();
    public final Gamepad gamepad2 = new Gamepad();
    public final OpMode opMode;
    public double time;

    public SimSession(SimRobot robot) {
        this(robot, new ControllerTest());
    }

    public SimSession(SimRobot robot, OpMode opMode) {
        this.robot = robot;
        this.opMode = opMode;
        opMode.hardwareMap = robot.buildHardwareMap();
        opMode.telemetry = telemetry;
        opMode.gamepad1 = gamepad1;
        opMode.gamepad2 = gamepad2;
        opMode.init();
        telemetry.update();
        opMode.start();
    }

    /** One robot loop: new gamepad data, run the OpMode, move the robot */
    public void tick() {
        gamepad1.latchPresses();
        gamepad2.latchPresses();
        opMode.loop();
        robot.step(LOOP_TIME);
        time += LOOP_TIME;
    }

    public void run(double seconds) {
        int loops = (int) Math.round(seconds / LOOP_TIME);
        for (int i = 0; i < loops; i++) tick();
    }

    /** Presses and releases a button over two loops */
    public void tap(Runnable press, Runnable release) {
        press.run();
        tick();
        release.run();
        tick();
    }
}
