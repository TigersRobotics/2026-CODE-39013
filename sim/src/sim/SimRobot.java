package sim;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.constants.HardwareNames;
import org.firstinspires.ftc.teamcode.constants.RobotSpecs;

public class SimRobot {
    public static final double ROBOT_WIDTH = 18;
    public static final double ROBOT_LENGTH = 18;
    public static final double TRACK_WIDTH = 15;
    public static final double WHEELBASE = 13;
    public static final double WHEEL_DIAMETER = 4.094;
    public static final double MOTOR_RPM = 312;
    public static final double LOAD_FACTOR = 0.85;
    public static final double ACCEL_TIME = 0.15;

    public static final double MAX_SPEED = MOTOR_RPM / 60 * Math.PI * WHEEL_DIAMETER * LOAD_FACTOR;
    public static final double TURN_RADIUS = (TRACK_WIDTH + WHEELBASE) / 2;

    public final SimMotor frontLeft = new SimMotor();
    public final SimMotor frontRight = new SimMotor();
    public final SimMotor backLeft = new SimMotor();
    public final SimMotor backRight = new SimMotor();
    public final SimMotor flywheel = new SimMotor();
    public final SimMotor intake = new SimMotor();
    public final SimMotor uptake = new SimMotor();
    public final SimServo turretLeft = new SimServo();
    public final SimServo turretRight = new SimServo();
    public final SimServo kicker = new SimServo();
    public final SimServo hood = new SimServo();
    public final SimServo limelightTilt = new SimServo();
    public final SimLimelight limelight = new SimLimelight(this);

    public double x, y, heading;
    public double forwardVel, strafeVel, turnVel;
    public boolean collisions = true;
    public boolean red = true;
    public double targetX = Field.RED_HIVE_X, targetY = 0;

    public SimRobot(double x, double y, double heading) {
        this.x = x;
        this.y = y;
        this.heading = heading;
    }

    public static SimRobot atStart(boolean red) {
        double[] s = Field.start(red);
        SimRobot robot = new SimRobot(s[0], s[1], s[2]);
        robot.red = red;
        robot.targetX = red ? Field.RED_HIVE_X : Field.BLUE_HIVE_X;
        return robot;
    }

    public HardwareMap buildHardwareMap() {
        HardwareMap map = new HardwareMap();
        map.put(HardwareNames.FRONT_LEFT_MOTOR_NAME, frontLeft);
        map.put(HardwareNames.FRONT_RIGHT_MOTOR_NAME, frontRight);
        map.put(HardwareNames.BACK_LEFT_MOTOR_NAME, backLeft);
        map.put(HardwareNames.BACK_RIGHT_MOTOR_NAME, backRight);
        map.put(HardwareNames.FLYWHEEL_NAME, flywheel);
        map.put(HardwareNames.INTAKE_MOTOR_NAME, intake);
        map.put(HardwareNames.UPTAKE_MOTOR_NAME, uptake);
        map.put(HardwareNames.TURRET_LEFT_SERVO_NAME, turretLeft);
        map.put(HardwareNames.TURRET_RIGHT_SERVO_NAME, turretRight);
        map.put(HardwareNames.UPTAKE_SERVO_NAME, kicker);
        map.put(HardwareNames.TURRET_UP_ANGLE_SERVO_NAME, hood);
        map.put(HardwareNames.LIMELIGHT_TILT_SERVO_NAME, limelightTilt);
        map.put(HardwareNames.LIMELIGHT_NAME, limelight);
        return map;
    }

    public double wheelFL() { return -frontLeft.shaftPower(); }
    public double wheelBL() { return -backLeft.shaftPower(); }
    public double wheelFR() { return frontRight.shaftPower(); }
    public double wheelBR() { return backRight.shaftPower(); }

    public void step(double dt) {
        flywheel.step(dt);
        intake.step(dt);
        uptake.step(dt);

        double fl = wheelFL(), fr = wheelFR(), bl = wheelBL(), br = wheelBR();

        double targetForward = (fl + fr + bl + br) / 4 * MAX_SPEED;
        double targetStrafe = (fl - fr - bl + br) / 4 * MAX_SPEED;
        double targetTurn = (fl - fr + bl - br) / 4 * MAX_SPEED / TURN_RADIUS;

        double blend = Math.min(1, dt / ACCEL_TIME);
        forwardVel += (targetForward - forwardVel) * blend;
        strafeVel += (targetStrafe - strafeVel) * blend;
        turnVel += (targetTurn - turnVel) * blend;

        double sin = Math.sin(heading), cos = Math.cos(heading);
        x += (forwardVel * sin + strafeVel * cos) * dt;
        y += (forwardVel * cos - strafeVel * sin) * dt;
        heading += turnVel * dt;

        if (collisions) collide();
    }

    private void collide() {
        for (double[] leg : Field.HIVE_LEGS) pushOutOfCircle(leg[0], leg[1], Field.HIVE_LEG_RADIUS);
        for (double[] flower : Field.FLOWERS) pushOutOfCircle(flower[0], flower[1], Field.FLOWER_RADIUS);

        double sin = Math.abs(Math.sin(heading)), cos = Math.abs(Math.cos(heading));
        double reachX = ROBOT_WIDTH / 2 * cos + ROBOT_LENGTH / 2 * sin;
        double reachY = ROBOT_WIDTH / 2 * sin + ROBOT_LENGTH / 2 * cos;
        x = Math.max(-Field.HALF + reachX, Math.min(Field.HALF - reachX, x));
        y = Math.max(-Field.HALF + reachY, Math.min(Field.HALF - reachY, y));
    }

    private void pushOutOfCircle(double cx, double cy, double r) {
        double sin = Math.sin(heading), cos = Math.cos(heading);
        double dx = cx - x, dy = cy - y;
        double right = dx * cos - dy * sin;
        double fwd = dx * sin + dy * cos;
        double hw = ROBOT_WIDTH / 2, hl = ROBOT_LENGTH / 2;

        double nearRight = Math.max(-hw, Math.min(hw, right));
        double nearFwd = Math.max(-hl, Math.min(hl, fwd));
        double offRight = right - nearRight, offFwd = fwd - nearFwd;
        double dist = Math.hypot(offRight, offFwd);

        double pushRight, pushFwd;
        if (dist > 0) {
            if (dist >= r) return;
            pushRight = -offRight / dist * (r - dist);
            pushFwd = -offFwd / dist * (r - dist);
        } else {
            double toSide = hw - Math.abs(right), toEnd = hl - Math.abs(fwd);
            pushRight = toSide < toEnd ? -Math.signum(right) * (toSide + r) : 0;
            pushFwd = toSide < toEnd ? 0 : -Math.signum(fwd) * (toEnd + r);
        }
        x += pushRight * cos + pushFwd * sin;
        y += -pushRight * sin + pushFwd * cos;
    }

    public double turretAngle() {
        return (turretLeft.shaftPosition() - 0.5) * Math.toRadians(RobotSpecs.TURRET_SERVO_RANGE_DEGREES);
    }
}
