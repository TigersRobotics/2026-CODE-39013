package sim;

import java.util.ArrayList;
import java.util.List;

/** Checks the real drive code by driving the sim robot */
public class SimTests {
    private static final List<String> failures = new ArrayList<>();
    private static int passed;

    public static void main(String[] args) {
        test("init sets up drivetrain and turret", () -> {
            SimSession s = session();
            check(s.telemetry.getHistory().contains("MECANUM DRIVE TRAIN INITIALIZED"), "drivetrain init line");
            check(s.telemetry.getHistory().contains("TURRET CONTROLLER INITIALIZED"), "turret init line");
        });

        test("left stick up drives forward", () -> {
            SimSession s = session();
            s.gamepad1.left_stick_y = -1;
            s.run(1);
            check(s.robot.y > 0.8 * SimRobot.MAX_SPEED, "moved forward, y = " + s.robot.y);
            near(s.robot.x, 0, 0.01, "no sideways drift");
            near(s.robot.heading, 0, 0.001, "no turning");
            check(s.robot.wheelFL() > 0 && s.robot.wheelFR() > 0 && s.robot.wheelBL() > 0 && s.robot.wheelBR() > 0,
                    "all 4 wheels push forward (motor directions are right)");
        });

        test("left stick down drives backward", () -> {
            SimSession s = session();
            s.gamepad1.left_stick_y = 1;
            s.run(1);
            check(s.robot.y < -0.8 * SimRobot.MAX_SPEED, "moved backward, y = " + s.robot.y);
        });

        test("left stick right strafes right", () -> {
            SimSession s = session();
            s.gamepad1.left_stick_x = 1;
            s.run(1);
            check(s.robot.x > 0.8 * SimRobot.MAX_SPEED, "moved right, x = " + s.robot.x);
            near(s.robot.y, 0, 0.01, "no forward drift");
            near(s.robot.heading, 0, 0.001, "no turning");
        });

        test("right stick right turns clockwise in place", () -> {
            SimSession s = session();
            s.gamepad1.right_stick_x = 1;
            s.run(0.5);
            check(s.robot.heading > 1, "turned clockwise, heading = " + s.robot.heading);
            near(s.robot.x, 0, 0.01, "stayed in place x");
            near(s.robot.y, 0, 0.01, "stayed in place y");
        });

        test("full diagonal plus turn never goes over full power", () -> {
            SimSession s = session();
            s.gamepad1.left_stick_y = -1;
            s.gamepad1.left_stick_x = 1;
            s.gamepad1.right_stick_x = 1;
            s.tick();
            SimRobot r = s.robot;
            double max = Math.max(Math.max(Math.abs(r.wheelFL()), Math.abs(r.wheelFR())),
                    Math.max(Math.abs(r.wheelBL()), Math.abs(r.wheelBR())));
            near(max, 1, 1e-9, "biggest wheel is at exactly full power");
        });

        test("starts at full speed", () -> {
            SimSession s = session();
            s.gamepad1.left_stick_y = -1;
            s.tick();
            near(s.robot.wheelFL(), 1, 1e-9, "full power at start");
            check(s.telemetry.getShown().contains("Speed: 100%"), "telemetry shows 100%");
        });

        test("left bumper slows down one step per press", () -> {
            double[] expected = {0.75, 0.5, 0.25};
            SimSession s = session();
            for (double level : expected) {
                s.tap(() -> s.gamepad1.left_bumper = true, () -> s.gamepad1.left_bumper = false);
                s.gamepad1.left_stick_y = -1;
                s.tick();
                near(s.robot.wheelFL(), level, 1e-9, "power at " + (int) (level * 100) + "%");
                check(s.telemetry.getShown().contains("Speed: " + (int) (level * 100) + "%"), "telemetry matches");
                s.gamepad1.left_stick_y = 0;
            }
        });

        test("speed stops at 25% and 100%", () -> {
            SimSession s = session();
            for (int i = 0; i < 6; i++) s.tap(() -> s.gamepad1.left_bumper = true, () -> s.gamepad1.left_bumper = false);
            s.gamepad1.left_stick_y = -1;
            s.tick();
            near(s.robot.wheelFL(), 0.25, 1e-9, "bottom is 25%");
            for (int i = 0; i < 6; i++) s.tap(() -> s.gamepad1.right_bumper = true, () -> s.gamepad1.right_bumper = false);
            s.tick();
            near(s.robot.wheelFL(), 1, 1e-9, "top is 100%");
        });

        test("holding a bumper only counts once", () -> {
            SimSession s = session();
            s.gamepad1.left_bumper = true;
            s.run(1);
            s.gamepad1.left_bumper = false;
            s.gamepad1.left_stick_y = -1;
            s.tick();
            near(s.robot.wheelFL(), 0.75, 1e-9, "one step down, not four");
        });

        test("slow mode drives a shorter distance", () -> {
            SimSession full = session();
            full.gamepad1.left_stick_y = -1;
            full.run(1);
            SimSession half = session();
            half.tap(() -> half.gamepad1.left_bumper = true, () -> half.gamepad1.left_bumper = false);
            half.tap(() -> half.gamepad1.left_bumper = true, () -> half.gamepad1.left_bumper = false);
            half.gamepad1.left_stick_y = -1;
            half.run(1);
            near(half.robot.y / full.robot.y, 0.5, 0.02, "50% speed goes about half as far");
        });

        test("slow mode keeps the same steering", () -> {
            SimSession s = session();
            s.tap(() -> s.gamepad1.left_bumper = true, () -> s.gamepad1.left_bumper = false);
            s.gamepad1.left_stick_y = -0.6f;
            s.gamepad1.left_stick_x = 0.8f;
            s.tick();
            SimRobot r = s.robot;
            // 0.6 forward + 0.8 strafe gives 1.4 and -0.2 before scaling, ratio has to stay -7
            near(r.wheelFL() / r.wheelFR(), -7, 1e-5, "wheel ratio unchanged");
        });

        test("driving does not move the turret", () -> {
            SimSession s = session();
            double before = s.robot.turretLeft.getPosition();
            s.gamepad1.left_stick_x = 1;
            s.run(0.2);
            near(s.robot.turretLeft.getPosition(), before, 1e-9, "turret stays put");
        });

        test("gamepad 2 left stick moves the turret", () -> {
            SimSession s = session();
            s.gamepad2.left_stick_x = 0.25f;
            s.tick();
            near(s.robot.turretLeft.getPosition(), 0.5, 1e-6, "left servo");
            near(s.robot.turretRight.getPosition(), 0.5, 1e-6, "right servo");
        });

        test("robot stops at the field wall", () -> {
            SimRobot robot = new SimRobot(0, -54, 0);
            SimSession s = new SimSession(robot);
            s.gamepad1.left_stick_x = -1;
            s.run(3);
            near(robot.x, -Field.HALF + SimRobot.ROBOT_WIDTH / 2, 0.01, "against left wall");
        });

        test("starts touching the wall on the red side, outside the LOADING ZONE", () -> {
            SimRobot robot = SimRobot.atStart(true);
            near(robot.x - SimRobot.ROBOT_LENGTH / 2, -Field.HALF, 1e-9, "touching the red wall");
            check(robot.y + SimRobot.ROBOT_WIDTH / 2 < Field.RED_LOADING_ZONE[1], "below the LOADING ZONE");
        });

        test("HIVE legs block the robot", () -> {
            // drive right along the HIVE edge, straight at a leg
            SimRobot robot = new SimRobot(-50, Field.HIVE_DEPTH / 2, 0);
            SimSession s = new SimSession(robot);
            s.gamepad1.left_stick_x = 1;
            s.run(2);
            check(robot.x < -Field.HIVE_WIDTH / 2, "stopped at the leg, x = " + robot.x);
        });

        test("robot can drive under the HIVE between the legs", () -> {
            SimRobot robot = new SimRobot(0, -50, 0);
            SimSession s = new SimSession(robot);
            s.gamepad1.left_stick_y = -1;
            s.run(1.2);
            check(robot.y > 0, "made it under the HIVE, y = " + robot.y);
            near(robot.x, 0, 0.01, "did not get pushed sideways");
        });

        test("FLOWERS block the robot", () -> {
            // drive along the audience wall into the FLOWER there
            SimRobot robot = new SimRobot(0, -Field.HALF + 9, 0);
            SimSession s = new SimSession(robot);
            s.gamepad1.left_stick_x = 1;
            s.run(2);
            check(robot.x < Field.FLOWERS[3][0], "stopped at the FLOWER, x = " + robot.x);
        });

        System.out.println();
        System.out.println(passed + " passed, " + failures.size() + " failed");
        if (!failures.isEmpty()) {
            failures.forEach(f -> System.out.println("  FAIL " + f));
            System.exit(1);
        }
    }

    private static SimSession session() {
        SimRobot robot = new SimRobot(0, 0, 0);
        robot.collisions = false;
        return new SimSession(robot);
    }

    private interface Body { void run(); }

    private static void test(String name, Body body) {
        int before = failures.size();
        try {
            body.run();
        } catch (RuntimeException e) {
            failures.add(name + ": threw " + e);
        }
        boolean ok = failures.size() == before;
        if (ok) passed++;
        System.out.println((ok ? "PASS " : "FAIL ") + name);
    }

    private static void check(boolean condition, String what) {
        if (!condition) failures.add(what);
    }

    private static void near(double actual, double expected, double tolerance, String what) {
        check(Math.abs(actual - expected) <= tolerance, what + " (expected " + expected + ", got " + actual + ")");
    }
}
