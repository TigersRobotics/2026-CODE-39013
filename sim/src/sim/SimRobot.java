package sim;

import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Physics for an 18 in mecanum robot on the 144 in BIOBUZZ field.
 * Field coords are inches from the center, +x right and +y away from the audience.
 * Heading is clockwise from +y, in radians.
 */
public class SimRobot {
    public static final double FIELD_SIZE = 144;
    public static final double ROBOT_SIZE = 18;
    // goBILDA 312 rpm with 104 mm wheels is about 67 in/s free speed, a bit less under load
    public static final double MAX_SPEED = 60;
    // half the track width plus half the wheelbase
    public static final double TURN_RADIUS = 14;
    // how long the robot takes to get up to speed
    public static final double ACCEL_TIME = 0.15;

    // HIVE frame base in the middle of the field
    public static final double HIVE_WIDTH = 49.46;
    public static final double HIVE_DEPTH = 38.95;

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

    /** Hardware names have to match what the robot code asks for */
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
        // rotated square still fits inside this half size
        double half = ROBOT_SIZE / 2 * (Math.abs(Math.cos(heading)) + Math.abs(Math.sin(heading)));
        double limit = FIELD_SIZE / 2 - half;
        x = Math.max(-limit, Math.min(limit, x));
        y = Math.max(-limit, Math.min(limit, y));

        // push out of the HIVE frame along the shortest way out
        double hx = HIVE_WIDTH / 2 + half, hy = HIVE_DEPTH / 2 + half;
        if (Math.abs(x) < hx && Math.abs(y) < hy) {
            if (hx - Math.abs(x) < hy - Math.abs(y)) x = Math.copySign(hx, x);
            else y = Math.copySign(hy, y);
        }
    }

    /** Turret angle from the servo, assuming a 180 degree servo centered on the robot front */
    public double turretAngle() {
        return (turretLeft.getPosition() - 0.5) * Math.PI;
    }
}
