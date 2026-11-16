package tuning;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.auto.OptimizedPaths;
import org.firstinspires.ftc.teamcode.opmodes.RedAuto;

import sim.SimClock;
import sim.SimSession;
import sim.SimTelemetry;

public class AutoTiming {
    public static final double AUTO_SECONDS = 30;

    public static void main(String[] args) throws Exception {
        SimClock.realTime = true;
        Pose start = new OptimizedPaths(true).start;
        TuningRobot robot = new TuningRobot(start.x(), start.y(), start.heading());
        TunerSim.runPhysics(robot);

        RedAuto auto = new RedAuto();
        SimTelemetry telemetry = new SimTelemetry();
        auto.hardwareMap = robot.buildHardwareMap();
        auto.telemetry = telemetry;
        auto.gamepad1 = new Gamepad();
        auto.gamepad2 = new Gamepad();
        auto.init();
        auto.start();

        long startTime = System.nanoTime();
        long loopNanos = Math.round(SimSession.LOOP_TIME * 1e9);
        double stepStart = 0;
        String step = "";
        System.out.println("  time   took");
        while (true) {
            auto.loop();
            double now = (System.nanoTime() - startTime) / 1e9;
            String current = find(telemetry, "Path ", "/") + " " + find(telemetry, "Path State ", "");
            if (!current.equals(step)) {
                if (!step.isEmpty()) print(now, now - stepStart, step, robot, find(telemetry, "Pose ", ""));
                step = current;
                stepStart = now;
            }
            if (step.endsWith("DONE") || now > AUTO_SECONDS) {
                print(now, now - stepStart, step, robot, find(telemetry, "Pose ", ""));
                System.out.printf("Auto finished at %.2f s%n", now);
                break;
            }
            long next = startTime + loopNanos * (long) Math.ceil((System.nanoTime() - startTime) / (double) loopNanos);
            Thread.sleep(Math.max(0, (next - System.nanoTime()) / 1_000_000));
        }
        auto.stop();
        System.exit(0);
    }

    private static String find(SimTelemetry telemetry, String start, String mustHave) {
        for (String line : telemetry.getShown()) {
            if (line.startsWith(start) && line.contains(mustHave) && !(mustHave.equals("/") && line.startsWith("Path State"))) {
                return line.substring(start.length());
            }
        }
        return "?";
    }

    private static void print(double now, double took, String step, TuningRobot robot, String followerPose) {
        synchronized (robot) {
            System.out.printf("%6.2f %6.2f  path %-20s robot at (%.1f, %.1f, %.0f deg)  follower thinks %s%n", now, took, step,
                    robot.pedroX(), robot.pedroY(), Math.toDegrees(robot.pedroHeading()), followerPose);
        }
    }
}
