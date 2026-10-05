package sim;

import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Physics for our mecanum robot on the BIOBUZZ field.
 * Field coords are inches from the center (see Field). Heading is clockwise from +y, in radians.
 */
public class SimRobot {
    // Robot specs, change these to match the real robot
    public static final double ROBOT_WIDTH = 18;
    public static final double ROBOT_LENGTH = 18;
    public static final double TRACK_WIDTH = 15;
    public static final double WHEELBASE = 13;
    // goBILDA 104 mm mecanum wheels on 312 rpm Yellow Jackets
    public static final double WHEEL_DIAMETER = 4.094;
    public static final double MOTOR_RPM = 312;
    // real robots lose some speed to weight and friction
    public static final double LOAD_FACTOR = 0.85;
    // how long the robot takes to get up to speed
    public static final double ACCEL_TIME = 0.15;

    public static final double MAX_SPEED = MOTOR_RPM / 60 * Math.PI * WHEEL_DIAMETER * LOAD_FACTOR;
    public static final double TURN_RADIUS = (TRACK_WIDTH + WHEELBASE) / 2;

    public final SimMotor frontLeft = new SimMotor();
    public final SimMotor frontRight = new SimMotor();
    public final SimMotor backLeft = new SimMotor();
    public final SimMotor backRight = new SimMotor();
    public final SimMotor flywheel = new SimMotor();
    public final SimServo turretLeft = new SimServo();
    public final SimServo turretRight = new SimServo();

    public double x, y, heading;
    public double forwardVel, strafeVel, turnVel;
    public boolean collisions = true;

    public SimRobot(double x, double y, double heading) {
        this.x = x;
        this.y = y;
        this.heading = heading;
    }

    public static SimRobot atStart(boolean red) {
        double[] s = Field.start(red);
        return new SimRobot(s[0], s[1], s[2]);
    }

    /** Hardware names have to match the robot config and what the robot code asks for */
    public HardwareMap buildHardwareMap() {
        HardwareMap map = new HardwareMap();
        map.put("frontLeft", frontLeft);
        map.put("frontRight", frontRight);
        map.put("backLeft", backLeft);
        map.put("backRight", backRight);
        map.put("flywheel", flywheel);
        map.put("0", turretLeft);
        map.put("1", turretRight);
        return map;
    }

    /**
     * How hard each wheel pushes the robot forward.
     * Left side motors are mounted mirrored, so a FORWARD motor on the left spins its wheel backwards.
     */
    public double wheelFL() { return -frontLeft.shaftPower(); }
    public double wheelBL() { return -backLeft.shaftPower(); }
    public double wheelFR() { return frontRight.shaftPower(); }
    public double wheelBR() { return backRight.shaftPower(); }

    public void step(double dt) {
        double fl = wheelFL(), fr = wheelFR(), bl = wheelBL(), br = wheelBR();

        // inverse of the mecanum mixing in MecanumDriveTrainController
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

        // perimeter walls, using how far the rotated robot reaches in x and y
        double sin = Math.abs(Math.sin(heading)), cos = Math.abs(Math.cos(heading));
        double reachX = ROBOT_WIDTH / 2 * cos + ROBOT_LENGTH / 2 * sin;
        double reachY = ROBOT_WIDTH / 2 * sin + ROBOT_LENGTH / 2 * cos;
        x = Math.max(-Field.HALF + reachX, Math.min(Field.HALF - reachX, x));
        y = Math.max(-Field.HALF + reachY, Math.min(Field.HALF - reachY, y));
    }

    /** Moves the robot so its rectangle no longer overlaps a round obstacle */
    private void pushOutOfCircle(double cx, double cy, double r) {
        double sin = Math.sin(heading), cos = Math.cos(heading);
        double dx = cx - x, dy = cy - y;
        // obstacle center in robot coords, +right and +forward
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
            // center is inside the robot, leave through the closest side
            double toSide = hw - Math.abs(right), toEnd = hl - Math.abs(fwd);
            pushRight = toSide < toEnd ? -Math.signum(right) * (toSide + r) : 0;
            pushFwd = toSide < toEnd ? 0 : -Math.signum(fwd) * (toEnd + r);
        }
        x += pushRight * cos + pushFwd * sin;
        y += -pushRight * sin + pushFwd * cos;
    }

    /** Turret angle from the servo, assuming a 180 degree servo centered on the robot front */
    public double turretAngle() {
        return (turretLeft.getPosition() - 0.5) * Math.PI;
    }
}
