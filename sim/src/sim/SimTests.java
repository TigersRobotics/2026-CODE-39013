package sim;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.constants.Competition;
import org.firstinspires.ftc.teamcode.constants.RobotSpecs;
import org.firstinspires.ftc.teamcode.constants.TuningConstants;
import org.firstinspires.ftc.teamcode.opmodes.ControllerTest;
import org.firstinspires.ftc.teamcode.opmodes.IntakeTest;
import org.firstinspires.ftc.teamcode.opmodes.LimelightTest;
import org.firstinspires.ftc.teamcode.opmodes.OdometryTest;
import org.firstinspires.ftc.teamcode.opmodes.TeleOpMain;
import org.firstinspires.ftc.teamcode.opmodes.TurretUpDownControllerV1Test;
import org.firstinspires.ftc.teamcode.opmodes.UptakeServoTest;
import org.firstinspires.ftc.teamcode.opmodes.UptakeTest;
import org.firstinspires.ftc.teamcode.robotControllers.FlywheelController;
import org.firstinspires.ftc.teamcode.robotControllers.LimelightController;
import org.firstinspires.ftc.teamcode.robotControllers.MecanumDriveTrainController;
import org.firstinspires.ftc.teamcode.robotControllers.TurretController;
import org.firstinspires.ftc.teamcode.robotControllers.TurretUpAngleController;
import org.firstinspires.ftc.teamcode.robotControllers.UptakeServoController;
import org.firstinspires.ftc.teamcode.utils.AutoAim;
import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;
import org.firstinspires.ftc.teamcode.utils.Timer;

import java.util.ArrayList;
import java.util.List;

public class SimTests {
    private static final List<String> failures = new ArrayList<>();
    private static int passed;

    public static void main(String[] args) {
        test("TeleOp init sets up every mechanism", () -> {
            SimSession s = session();
            List<String> history = s.telemetry.getHistory();
            for (String line : new String[]{"MECANUM DRIVE TRAIN INITIALIZED", "TURRET CONTROLLER INITIALIZED", "Intake Initialized",
                    "Uptake Initialized", "Flywheel Controller Initialized", "Kicker Controller Initialized",
                    "Hood Controller Initialized", "Limelight Initialized"}) {
                check(history.contains(line), "init line: " + line);
            }
        });

        test("ControllerTest still drives and aims the turret", () -> {
            SimRobot robot = new SimRobot(0, 0, 0);
            robot.collisions = false;
            SimSession s = new SimSession(robot, new ControllerTest());
            check(s.telemetry.getHistory().contains("TURRET CONTROLLER INITIALIZED"), "turret init line");
            s.gamepad1.left_stick_y = -1;
            s.gamepad2.left_stick_x = 0.5f;
            s.run(1);
            check(robot.y > 0.8 * SimRobot.MAX_SPEED, "drove forward");
            near(robot.turretLeft.getPosition(), 0.75, 1e-6, "turret half right");
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
            near(r.wheelFL() / r.wheelFR(), -7, 1e-5, "wheel ratio unchanged");
        });

        test("driving does not move the turret", () -> {
            SimSession s = session();
            s.tick();
            double before = s.robot.turretLeft.getPosition();
            s.gamepad1.left_stick_x = 1;
            s.run(0.2);
            near(s.robot.turretLeft.getPosition(), before, 1e-9, "turret stays put");
        });

        test("turret is centered with the stick centered", () -> {
            SimSession s = session();
            s.tick();
            near(s.robot.turretLeft.getPosition(), 0.5, 1e-9, "left servo centered");
            near(s.robot.turretRight.getPosition(), 0.5, 1e-9, "right servo centered");
        });

        test("gamepad 2 left stick turns the turret both ways", () -> {
            SimSession s = session();
            s.gamepad2.left_stick_x = 0.5f;
            s.tick();
            near(s.robot.turretLeft.getPosition(), 0.75, 1e-6, "half right");
            s.gamepad2.left_stick_x = -0.5f;
            s.tick();
            near(s.robot.turretLeft.getPosition(), 0.25, 1e-6, "half left");
        });

        test("turret servos point the same way unless set as mirrored", () -> {
            SimSession s = session();
            s.gamepad2.left_stick_x = 0.5f;
            s.tick();
            Servo.Direction right = RobotSpecs.TURRET_SERVOS_MIRRORED ? Servo.Direction.REVERSE : Servo.Direction.FORWARD;
            check(s.robot.turretLeft.getDirection() == Servo.Direction.FORWARD, "left servo forward");
            check(s.robot.turretRight.getDirection() == right, "right servo direction matches TURRET_SERVOS_MIRRORED");
            near(s.robot.turretRight.getPosition(), s.robot.turretLeft.getPosition(), 1e-9, "both servos get the same position");
        });

        test("full turret stick uses the whole servo range without clamping", () -> {
            SimSession s = session();
            s.gamepad2.left_stick_x = 1;
            s.tick();
            near(s.robot.turretLeft.getLastRequested(), 1, 1e-6, "full right asks for exactly 1");
            s.gamepad2.left_stick_x = -1;
            s.tick();
            near(s.robot.turretLeft.getLastRequested(), 0, 1e-6, "full left asks for exactly 0");
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
            SimRobot robot = new SimRobot(0, -Field.HALF + 9, 0);
            SimSession s = new SimSession(robot);
            s.gamepad1.left_stick_x = 1;
            s.run(2);
            check(robot.x < Field.FLOWERS[3][0], "stopped at the FLOWER, x = " + robot.x);
        });

        test("stopping the drivetrain zeroes the motors", () -> {
            SimSession s = session();
            s.gamepad1.left_stick_y = -1;
            s.tick();
            check(s.robot.wheelFL() > 0, "driving before stop");
            s.stop();
            near(s.robot.frontLeft.getPower(), 0, 1e-9, "front left stopped");
            near(s.robot.backRight.getPower(), 0, 1e-9, "back right stopped");
        });

        test("drivetrain stop and disabled init are safe without hardware", () -> {
            MecanumDriveTrainController.stop();
            MecanumDriveTrainController.stop();
            MecanumDriveTrainController.init(new SimRobot(0, 0, 0).buildHardwareMap(), false);
            check(!MecanumDriveTrainController.isInitialized, "drivetrain stays off");
        });

        test("timed telemetry stays small over a long run", () -> {
            SimSession s = session();
            s.gamepad1.left_stick_y = -1;
            s.run(20);
            check(TimedTelemetry.getTelemetryListAsStrings().size() < 30,
                    "lines are removed, " + TimedTelemetry.getTelemetryListAsStrings().size() + " left");
        });

        test("a new OpMode run starts with clean telemetry", () -> {
            session();
            TimedTelemetry.addLineForever("left over from the last run");
            SimSession s = session();
            s.run(0.2);
            check(!s.telemetry.getHistory().contains("left over from the last run"), "old line is gone");
        });

        test("timer triggers fire after their delay", () -> {
            session();
            Timer.init();
            boolean[] fired = new boolean[2];
            Timer.addTrigger(100, () -> {
                fired[0] = true;
                Timer.addTrigger(40, () -> fired[1] = true);
            });
            for (int i = 0; i < 4; i++) { SimClock.advance(0.02); Timer.update(); }
            check(!fired[0], "not fired at 80 ms");
            SimClock.advance(0.02);
            Timer.update();
            check(fired[0], "fired at 100 ms");
            for (int i = 0; i < 2; i++) { SimClock.advance(0.02); Timer.update(); }
            check(fired[1], "a trigger can add another trigger");
            check(Timer.getTriggerCount() == 0, "finished triggers are removed");
        });

        test("intake test runs the intake from the triggers", () -> {
            SimRobot robot = new SimRobot(0, 0, 0);
            SimSession s = new SimSession(robot, new IntakeTest());
            check(s.telemetry.getHistory().contains("Intake Initialized"), "intake init line");
            s.gamepad1.left_trigger = 1;
            s.tick();
            near(robot.intake.shaftPower(), 1, 1e-9, "left trigger pulls in");
            s.gamepad1.left_trigger = 0;
            s.gamepad1.right_trigger = 0.5f;
            s.tick();
            near(robot.intake.shaftPower(), -0.5, 1e-9, "right trigger pushes out");
            s.stop();
            near(robot.intake.getPower(), 0, 1e-9, "stop turns the intake off");
        });

        test("uptake test runs the uptake from the triggers", () -> {
            SimRobot robot = new SimRobot(0, 0, 0);
            SimSession s = new SimSession(robot, new UptakeTest());
            check(s.telemetry.getHistory().contains("Uptake Initialized"), "uptake init line");
            s.gamepad1.right_trigger = 1;
            s.tick();
            near(robot.uptake.getPower(), 1, 1e-9, "right trigger moves balls up");
            near(robot.intake.getPower(), 0, 1e-9, "intake is not touched");
            s.gamepad1.right_trigger = 0;
            s.gamepad1.left_trigger = 1;
            s.tick();
            near(robot.uptake.getPower(), -1, 1e-9, "left trigger moves balls down");
            s.stop();
            near(robot.uptake.getPower(), 0, 1e-9, "stop turns the uptake off");
        });

        test("kicker pushes up, then comes back down after the kick time", () -> {
            SimRobot robot = new SimRobot(0, 0, 0);
            SimSession s = new SimSession(robot, new UptakeServoTest());
            near(robot.kicker.getPosition(), RobotSpecs.UPTAKE_SERVO_DEFAULT_ANGLE, 1e-9, "starts down");
            s.gamepad1.a = true;
            s.tick();
            s.gamepad1.a = false;
            near(robot.kicker.getPosition(), RobotSpecs.UPTAKE_SERVO_UP_ANGLE, 1e-9, "goes up on the press");
            int kickLoops = (int) Math.round(RobotSpecs.UPTAKE_SERVO_KICK_MS / 1000 / SimSession.LOOP_TIME);
            for (int i = 0; i < kickLoops - 1; i++) s.tick();
            near(robot.kicker.getPosition(), RobotSpecs.UPTAKE_SERVO_UP_ANGLE, 1e-9, "stays up for the whole kick, not one loop");
            s.tick();
            near(robot.kicker.getPosition(), RobotSpecs.UPTAKE_SERVO_DEFAULT_ANGLE, 1e-9, "comes back down after the kick time");
        });

        test("kicker ignores presses until it is back down", () -> {
            SimRobot robot = new SimRobot(0, 0, 0);
            SimSession s = new SimSession(robot, new UptakeServoTest());
            s.tap(() -> s.gamepad1.a = true, () -> s.gamepad1.a = false);
            s.run(RobotSpecs.UPTAKE_SERVO_KICK_MS / 1000 + 0.04);
            check(UptakeServoController.isBusy(), "still busy while moving down");
            s.tap(() -> s.gamepad1.a = true, () -> s.gamepad1.a = false);
            near(robot.kicker.getPosition(), RobotSpecs.UPTAKE_SERVO_DEFAULT_ANGLE, 1e-9, "second press ignored");
            s.run(RobotSpecs.UPTAKE_SERVO_RETRACT_MS / 1000);
            check(!UptakeServoController.isBusy(), "free again");
            s.gamepad1.a = true;
            s.tick();
            near(robot.kicker.getPosition(), RobotSpecs.UPTAKE_SERVO_UP_ANGLE, 1e-9, "kicks again once free");
        });

        test("dpad steps the hood angle and stops at the limits", () -> {
            SimRobot robot = new SimRobot(0, 0, 0);
            SimSession s = new SimSession(robot, new TurretUpDownControllerV1Test());
            near(robot.hood.getPosition(), RobotSpecs.HOOD_DEFAULT_ANGLE / RobotSpecs.HOOD_SERVO_MAX_ANGLE, 1e-9, "starts at the default angle");
            s.gamepad1.dpad_up = true;
            s.run(1);
            s.gamepad1.dpad_up = false;
            s.tick();
            near(TurretUpAngleController.getTargetAngle(), RobotSpecs.HOOD_DEFAULT_ANGLE + TuningConstants.HOOD_STEP_ANGLE, 1e-9, "holding up counts as one step");
            near(robot.hood.getPosition(), TurretUpAngleController.getTargetAngle() / RobotSpecs.HOOD_SERVO_MAX_ANGLE, 1e-9, "servo follows");
            for (int i = 0; i < 40; i++) s.tap(() -> s.gamepad1.dpad_up = true, () -> s.gamepad1.dpad_up = false);
            near(TurretUpAngleController.getTargetAngle(), RobotSpecs.HOOD_MAX_ANGLE, 1e-9, "stops at the top");
            for (int i = 0; i < 40; i++) s.tap(() -> s.gamepad1.dpad_down = true, () -> s.gamepad1.dpad_down = false);
            near(TurretUpAngleController.getTargetAngle(), RobotSpecs.HOOD_MIN_ANGLE, 1e-9, "stops at the bottom");
        });

        test("limelight sees the HIVE straight ahead from the start", () -> {
            SimRobot robot = SimRobot.atStart(true);
            SimSession s = new SimSession(robot, new LimelightTest());
            check(robot.limelight.isRunning(), "camera started");
            s.tick();
            check(LimelightController.hasTarget(), "target visible");
            near(LimelightController.getTx(), 0, 0.5, "target centered");
        });

        test("limelight tx is positive when the target is to the right", () -> {
            SimRobot robot = SimRobot.atStart(true);
            robot.heading -= Math.toRadians(10);
            SimSession s = new SimSession(robot, new LimelightTest());
            s.tick();
            near(LimelightController.getTx(), 10, 0.5, "target 10 degrees right");
        });

        test("limelight has no target when facing away", () -> {
            SimRobot robot = SimRobot.atStart(true);
            robot.heading += Math.PI;
            SimSession s = new SimSession(robot, new LimelightTest());
            s.tick();
            check(!LimelightController.hasTarget(), "no target");
            near(LimelightController.getTx(), 0, 1e-9, "tx reads 0 with no target");
            check(s.telemetry.getShown().contains("Limelight: no target"), "telemetry says no target");
        });

        test("dpad tilts the limelight and stop turns it off", () -> {
            SimRobot robot = SimRobot.atStart(true);
            SimSession s = new SimSession(robot, new LimelightTest());
            near(robot.limelightTilt.getPosition(), RobotSpecs.LIMELIGHT_TILT_DEFAULT_POSITION, 1e-9, "starts at default tilt");
            s.tap(() -> s.gamepad1.dpad_up = true, () -> s.gamepad1.dpad_up = false);
            near(robot.limelightTilt.getPosition(), RobotSpecs.LIMELIGHT_TILT_DEFAULT_POSITION + TuningConstants.LIMELIGHT_TILT_STEP, 1e-9, "tilted up one step");
            s.stop();
            check(!robot.limelight.isRunning(), "camera stopped");
        });

        test("auto aim turns the turret onto the target", () -> {
            SimRobot robot = SimRobot.atStart(true);
            robot.heading -= Math.toRadians(15);
            SimSession s = new SimSession(robot, new LimelightTest());
            s.tick();
            near(LimelightController.getTx(), 15, 0.5, "target starts 15 degrees right");
            s.tap(() -> s.gamepad1.right_bumper = true, () -> s.gamepad1.right_bumper = false);
            s.run(1.5);
            check(Math.abs(LimelightController.getTx()) <= TuningConstants.TURRET_AIM_DEADBAND + 0.1, "target centered, tx = " + LimelightController.getTx());
            near(TurretController.getCurrentYaw(), 15, TuningConstants.TURRET_AIM_DEADBAND + 0.5, "turret turned right");
        });

        test("auto aim holds the turret when the target is lost", () -> {
            SimRobot robot = SimRobot.atStart(true);
            robot.heading -= Math.toRadians(15);
            SimSession s = new SimSession(robot, new LimelightTest());
            s.tap(() -> s.gamepad1.right_bumper = true, () -> s.gamepad1.right_bumper = false);
            s.run(1.5);
            double aimed = TurretController.getCurrentYaw();
            robot.heading += Math.PI;
            s.run(0.5);
            check(!LimelightController.hasTarget(), "target lost");
            near(TurretController.getCurrentYaw(), aimed, 1e-9, "turret holds where it was");
            s.tap(() -> s.gamepad1.right_bumper = true, () -> s.gamepad1.right_bumper = false);
            near(TurretController.getCurrentYaw(), 0, 1e-9, "turning auto aim off goes back to the stick");
        });

        test("right trigger spins the flywheel up and holds the target", () -> {
            SimSession s = session();
            s.gamepad2.right_trigger = 1;
            s.run(2);
            near(s.robot.flywheel.getVelocity(), TuningConstants.FLYWHEEL_TARGET_VELOCITY, TuningConstants.FLYWHEEL_VELOCITY_TOLERANCE, "up to speed");
            s.run(3);
            near(s.robot.flywheel.getTargetVelocity(), TuningConstants.FLYWHEEL_TARGET_VELOCITY, 1e-9, "target held");
            check(FlywheelController.isAtSpeed(), "at speed");
            check(s.telemetry.getShown().stream().anyMatch(l -> l.contains("READY")), "telemetry says ready");
            s.gamepad2.right_trigger = 0;
            s.tick();
            near(s.robot.flywheel.getPower(), 0, 1e-9, "letting go coasts the flywheel");
        });

        test("a zero flywheel target coasts instead of braking", () -> {
            SimSession s = session();
            s.gamepad2.right_trigger = 1;
            s.run(2);
            check(s.robot.flywheel.getMode() == DcMotor.RunMode.RUN_USING_ENCODER, "velocity control while spinning");
            s.gamepad2.right_trigger = 0;
            s.tick();
            check(s.robot.flywheel.getMode() == DcMotor.RunMode.RUN_WITHOUT_ENCODER, "velocity loop off at target 0");
            check(FlywheelController.isCoasting(), "controller reports coasting");
            s.run(0.5);
            check(s.robot.flywheel.getVelocity() > 0.7 * TuningConstants.FLYWHEEL_TARGET_VELOCITY,
                    "still spinning after 0.5 s, velocity = " + s.robot.flywheel.getVelocity());
            s.robot.flywheel.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new PIDFCoefficients(0, 0, 0, 0));
            s.gamepad2.right_trigger = 1;
            s.tick();
            check(s.robot.flywheel.getMode() == DcMotor.RunMode.RUN_USING_ENCODER, "back to velocity control");
            check(s.robot.flywheel.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER).f > 0, "PIDF applied again on spin up");
            s.run(2);
            check(FlywheelController.isAtSpeed(), "back at speed");
        });

        test("drivetrain init sets brake mode even after a FLOAT OpMode", () -> {
            SimRobot robot = new SimRobot(0, 0, 0);
            for (SimMotor m : new SimMotor[]{robot.frontLeft, robot.frontRight, robot.backLeft, robot.backRight}) {
                m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            }
            new SimSession(robot, new TeleOpMain());
            for (SimMotor m : new SimMotor[]{robot.frontLeft, robot.frontRight, robot.backLeft, robot.backRight}) {
                check(m.getZeroPowerBehavior() == DcMotor.ZeroPowerBehavior.BRAKE, "drive motor brakes");
            }
        });

        test("cross does not fire before the flywheel is at speed", () -> {
            SimSession s = session();
            s.gamepad2.right_trigger = 1;
            s.tick();
            s.tap(() -> s.gamepad2.a = true, () -> s.gamepad2.a = false);
            near(s.robot.kicker.getPosition(), RobotSpecs.UPTAKE_SERVO_DEFAULT_ANGLE, 1e-9, "kicker stays down");
            check(s.telemetry.getHistory().contains("Flywheel not at speed, not firing"), "driver is told why");
        });

        test("cross fires the kicker once the flywheel is at speed", () -> {
            SimSession s = session();
            s.gamepad2.right_trigger = 1;
            s.run(2);
            s.gamepad2.a = true;
            s.tick();
            s.gamepad2.a = false;
            near(s.robot.kicker.getPosition(), RobotSpecs.UPTAKE_SERVO_UP_ANGLE, 1e-9, "kicker up");
            s.run(RobotSpecs.UPTAKE_SERVO_KICK_MS / 1000 + 0.02);
            near(s.robot.kicker.getPosition(), RobotSpecs.UPTAKE_SERVO_DEFAULT_ANGLE, 1e-9, "kicker back down");
        });

        test("left trigger runs the intake and uptake in", () -> {
            SimSession s = session();
            s.gamepad2.left_trigger = 1;
            s.tick();
            near(s.robot.intake.getPower(), TuningConstants.INTAKE_POWER, 1e-9, "intake in");
            near(s.robot.uptake.getPower(), TuningConstants.UPTAKE_POWER, 1e-9, "uptake in");
            s.gamepad2.left_trigger = 0;
            s.tick();
            near(s.robot.intake.getPower(), 0, 1e-9, "intake off when released");
            near(s.robot.uptake.getPower(), 0, 1e-9, "uptake off when released");
        });

        test("left bumper reverses only the intake", () -> {
            SimSession s = session();
            s.gamepad2.left_trigger = 1;
            s.gamepad2.left_bumper = true;
            s.tick();
            near(s.robot.intake.getPower(), TuningConstants.INTAKE_REVERSE_POWER, 1e-9, "intake out, even with the trigger held");
            near(s.robot.uptake.getPower(), 0, 1e-9, "uptake stays off");
        });

        test("gamepad 2 dpad steps the hood", () -> {
            SimSession s = session();
            s.tap(() -> s.gamepad2.dpad_up = true, () -> s.gamepad2.dpad_up = false);
            s.tap(() -> s.gamepad2.dpad_up = true, () -> s.gamepad2.dpad_up = false);
            s.tap(() -> s.gamepad2.dpad_down = true, () -> s.gamepad2.dpad_down = false);
            near(TurretUpAngleController.getTargetAngle(), RobotSpecs.HOOD_DEFAULT_ANGLE + TuningConstants.HOOD_STEP_ANGLE, 1e-9, "up two, down one");
            near(s.robot.hood.getPosition(), TurretUpAngleController.getTargetAngle() / RobotSpecs.HOOD_SERVO_MAX_ANGLE, 1e-9, "servo follows");
        });

        test("gamepad 2 right bumper toggles auto aim", () -> {
            SimRobot robot = SimRobot.atStart(true);
            robot.heading -= Math.toRadians(15);
            SimSession s = new SimSession(robot, new TeleOpMain());
            s.run(0.5);
            near(TurretController.getCurrentYaw(), 0, 1e-9, "manual turret by default");
            s.tap(() -> s.gamepad2.right_bumper = true, () -> s.gamepad2.right_bumper = false);
            s.run(1.5);
            check(s.telemetry.getShown().contains("Auto Aim ON"), "telemetry shows auto aim on");
            near(TurretController.getCurrentYaw(), 15, TuningConstants.TURRET_AIM_DEADBAND + 0.5, "turret aimed at the target");
            s.tap(() -> s.gamepad2.right_bumper = true, () -> s.gamepad2.right_bumper = false);
            near(TurretController.getCurrentYaw(), 0, 1e-9, "back to the stick");
        });

        test("driver controls do not run the mechanisms", () -> {
            SimSession s = session();
            s.gamepad1.left_trigger = 1;
            s.gamepad1.right_trigger = 1;
            s.gamepad1.a = true;
            s.gamepad1.dpad_up = true;
            s.run(0.5);
            near(s.robot.intake.getPower(), 0, 1e-9, "intake off");
            near(s.robot.flywheel.getPower(), 0, 1e-9, "flywheel off");
            near(s.robot.kicker.getPosition(), RobotSpecs.UPTAKE_SERVO_DEFAULT_ANGLE, 1e-9, "kicker down");
            near(TurretUpAngleController.getTargetAngle(), RobotSpecs.HOOD_DEFAULT_ANGLE, 1e-9, "hood unchanged");
        });

        test("stopping TeleOp turns every motor off", () -> {
            SimSession s = session();
            s.gamepad1.left_stick_y = -1;
            s.gamepad2.right_trigger = 1;
            s.gamepad2.left_trigger = 1;
            s.run(1);
            s.stop();
            for (SimMotor m : new SimMotor[]{s.robot.frontLeft, s.robot.frontRight, s.robot.backLeft, s.robot.backRight,
                    s.robot.flywheel, s.robot.intake, s.robot.uptake}) {
                near(m.getPower(), 0, 1e-9, "motor off");
            }
            check(!s.robot.limelight.isRunning(), "limelight stopped");
        });

        test("alliance choice during init sets the start pose", () -> {
            SimRobot robot = SimRobot.atStart(false);
            SimSession s = new SimSession(robot, new TeleOpMain(), false);
            s.chooseAlliance(false);
            check(s.telemetry.getShown().contains("Alliance BLUE"), "init telemetry shows blue");
            s.start();
            s.tick();
            near(robot.pinpoint.getPosition().getX(DistanceUnit.INCH), Competition.BLUE_STARTING_X_IN, 1e-6, "blue start x");
            near(robot.pinpoint.getPosition().getY(DistanceUnit.INCH), Competition.BLUE_STARTING_Y_IN, 1e-6, "blue start y");
            near(robot.pinpoint.getPosition().getHeading(AngleUnit.DEGREES), Competition.BLUE_STARTING_HEADING_DEG, 1e-6, "blue start heading");
            check(s.telemetry.getShown().contains("Alliance BLUE"), "loop telemetry shows blue");

            SimRobot redRobot = SimRobot.atStart(true);
            SimSession r = new SimSession(redRobot, new TeleOpMain(), false);
            r.chooseAlliance(false);
            r.chooseAlliance(true);
            check(r.telemetry.getShown().contains("Alliance RED"), "switching back to red");
            r.start();
            r.tick();
            near(redRobot.pinpoint.getPosition().getX(DistanceUnit.INCH), Competition.RED_STARTING_X_IN, 1e-6, "red start x");
            near(redRobot.pinpoint.getPosition().getY(DistanceUnit.INCH), Competition.RED_STARTING_Y_IN, 1e-6, "red start y");
            near(redRobot.pinpoint.getPosition().getHeading(AngleUnit.DEGREES), Competition.RED_STARTING_HEADING_DEG, 1e-6, "red start heading");
        });

        test("start poses match where the sim places the robot", () -> {
            for (boolean red : new boolean[]{true, false}) {
                SimRobot robot = SimRobot.atStart(red);
                near(robot.pinpoint.trueX(), red ? Competition.RED_STARTING_X_IN : Competition.BLUE_STARTING_X_IN, 1e-9, "x");
                near(robot.pinpoint.trueY(), red ? Competition.RED_STARTING_Y_IN : Competition.BLUE_STARTING_Y_IN, 1e-9, "y");
                double expected = Math.toRadians(red ? Competition.RED_STARTING_HEADING_DEG : Competition.BLUE_STARTING_HEADING_DEG);
                double error = robot.pinpoint.trueHeading() - expected;
                near(Math.atan2(Math.sin(error), Math.cos(error)), 0, 1e-9, "heading");
            }
        });

        test("TeleOp odometry follows the robot", () -> {
            SimRobot robot = SimRobot.atStart(true);
            SimSession s = new SimSession(robot, new TeleOpMain());
            s.gamepad1.left_stick_y = -1;
            s.gamepad1.left_stick_x = 0.5f;
            s.run(1);
            s.gamepad1.left_stick_y = 0;
            s.gamepad1.left_stick_x = 0;
            s.run(1);
            near(robot.pinpoint.getPosition().getX(DistanceUnit.INCH), robot.pinpoint.trueX(), 0.01, "x matches the robot");
            near(robot.pinpoint.getPosition().getY(DistanceUnit.INCH), robot.pinpoint.trueY(), 0.01, "y matches the robot");
            check(robot.pinpoint.trueX() > Competition.RED_STARTING_X_IN + 20, "robot drove toward the HIVE");
            check(s.telemetry.getShown().stream().anyMatch(l -> l.startsWith("Pose: x")), "pose in telemetry");
        });

        test("TeleOp still runs without a Pinpoint", () -> {
            SimRobot robot = SimRobot.atStart(true);
            robot.hasPinpoint = false;
            SimSession s = new SimSession(robot, new TeleOpMain());
            check(s.telemetry.getHistory().contains("No Pinpoint found, aiming uses the Limelight only"), "driver is told");
            s.gamepad1.left_stick_y = -1;
            s.run(0.5);
            check(robot.x > -Field.HALF + 9 + 5, "still drives");
            check(s.telemetry.getShown().contains("Pose: not available"), "telemetry says no pose");
        });

        test("odometry test drives field centric", () -> {
            SimRobot robot = new SimRobot(0, 0, 0);
            robot.collisions = false;
            SimSession s = new SimSession(robot, new OdometryTest());
            robot.heading = Math.PI / 2;
            s.tick();
            s.gamepad1.left_stick_y = -1;
            s.run(1);
            check(robot.y > 0.8 * SimRobot.MAX_SPEED, "moved along the starting forward direction, y = " + robot.y);
            near(robot.x, 0, 0.5, "no drift sideways on the field");
        });

        test("auto aim points at the up cell", () -> {
            near(AutoAim.distance(13.29, 13.79, true, false), 62.5, 0.1, "distance from the red audience shoot spot");
            near(AutoAim.turretYaw(13.29, 13.79, 33.7, true, false), -8.9, 0.1, "turret yaw from the red audience shoot spot");
            near(AutoAim.turretYaw(144 - 13.29, 144 - 13.79, 213.7, false, true), -8.9, 0.1, "blue mirror gives the same yaw");
            check(Math.abs(AutoAim.turretYaw(38, 120, 315, true, true)) < RobotSpecs.MAX_YAW, "far shoot spot is inside the turret range");
            near(AutoAim.flywheelVelocity(65), 1095, 0.5, "flywheel velocity between table rows");
            near(AutoAim.hoodAngle(10), 75, 1e-9, "hood angle clamps below the table");
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
