package tuning;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.constants.HardwareNames;
import org.firstinspires.ftc.teamcode.constants.RobotSpecs;

import sim.SimMotor;
import sim.SimRobot;

public class TuningRobot extends SimRobot {
    public static final double MASS_KG = 15;
    public static final double MOTOR_STALL_TORQUE_NM = 2.38;
    public static final double STRAFE_EFFICIENCY = 0.8;
    public static final double WHEEL_GRIP = 0.7;
    public static final double FORWARD_FRICTION = 30;
    public static final double STRAFE_FRICTION = 55;
    public static final double TURN_FRICTION = 3;
    public static final double SPIN_RADIUS = Math.sqrt((ROBOT_WIDTH * ROBOT_WIDTH + ROBOT_LENGTH * ROBOT_LENGTH) / 12);

    public static final double X_POD_OFFSET = RobotSpecs.OFFSETS_OF_ODOMETRY_PODS_IN_X;
    public static final double Y_POD_OFFSET = RobotSpecs.OFFSETS_OF_ODOMETRY_PODS_IN_Y;
    public static final boolean X_POD_REVERSED = false;
    public static final boolean Y_POD_REVERSED = false;
    public static final double POD_TICKS_PER_MM = PodPinpoint.SWINGARM_TICKS_PER_MM;

    public static final double PUSH_SPEED = 12;
    public static final double PUSH_TURN_SPEED = Math.PI / 3;

    public static final double FREE_SPEED = MOTOR_RPM / 60 * Math.PI * WHEEL_DIAMETER;
    public static final double STALL_ACCEL = MOTOR_STALL_TORQUE_NM / (WHEEL_DIAMETER / 2 * 0.0254) / MASS_KG / 0.0254;
    public static final double GRIP_ACCEL = WHEEL_GRIP * 386.09 / 4;

    public final PodPinpoint pods = new PodPinpoint(this);

    private double pushForward, pushLeft, pushTurn, pushTime, pushDuration;

    public TuningRobot() {
        super(0, 0, 0);
        collisions = false;
    }

    public TuningRobot(double pedroX, double pedroY, double pedroHeading) {
        super(pedroX - sim.Field.HALF, pedroY - sim.Field.HALF, Math.PI / 2 - pedroHeading);
    }

    @Override
    public HardwareMap buildHardwareMap() {
        HardwareMap map = super.buildHardwareMap();
        map.put(HardwareNames.ODOMETRY_NAME, pods);
        return map;
    }

    @Override
    public void step(double dt) {
        double oldX = x, oldY = y, sin = Math.sin(heading), cos = Math.cos(heading);
        super.step(dt);
        forwardVel = ((x - oldX) * sin + (y - oldY) * cos) / dt;
        strafeVel = ((x - oldX) * cos - (y - oldY) * sin) / dt;
        pods.track(dt);
    }

    @Override
    protected void drive(double dt) {
        if (isBeingPushed()) push(dt);
        else physics(dt);
    }

    private void physics(double dt) {
        double forward = forwardVel, left = -strafeVel, turn = -turnVel;
        double k = TURN_RADIUS, s = STRAFE_EFFICIENCY;

        double fl = wheelForce(frontLeft, wheelFL(), forward - left / s - turn * k);
        double fr = wheelForce(frontRight, wheelFR(), forward + left / s + turn * k);
        double bl = wheelForce(backLeft, wheelBL(), forward + left / s - turn * k);
        double br = wheelForce(backRight, wheelBR(), forward - left / s + turn * k);

        forwardVel = slowDown(forward, fl + fr + bl + br, FORWARD_FRICTION, dt);
        strafeVel = -slowDown(left, s * (-fl + fr + bl - br), STRAFE_FRICTION, dt);
        turnVel = -slowDown(turn, k * (-fl + fr - bl + br) / (SPIN_RADIUS * SPIN_RADIUS), TURN_FRICTION, dt);
    }

    private double wheelForce(SimMotor motor, double power, double wheelSpeed) {
        boolean shorted = power != 0 || motor.getZeroPowerBehavior() == DcMotor.ZeroPowerBehavior.BRAKE;
        if (!shorted) return 0;
        double accel = STALL_ACCEL * (power - wheelSpeed / FREE_SPEED);
        return Math.max(-GRIP_ACCEL, Math.min(GRIP_ACCEL, accel));
    }

    private static double slowDown(double velocity, double accel, double friction, double dt) {
        if (velocity == 0 && Math.abs(accel) <= friction) return 0;
        double direction = velocity != 0 ? Math.signum(velocity) : Math.signum(accel);
        double next = velocity + (accel - friction * direction) * dt;
        if (Math.signum(next) != direction && Math.abs(accel) <= friction) return 0;
        return next;
    }

    public synchronized void push(double forward, double left, double turn) {
        pushForward = forward;
        pushLeft = left;
        pushTurn = turn;
        pushTime = 0;
        pushDuration = Math.max(1, Math.max(Math.hypot(forward, left) / PUSH_SPEED, Math.abs(turn) / PUSH_TURN_SPEED));
    }

    public synchronized boolean isBeingPushed() {
        return pushTime < pushDuration;
    }

    private void push(double dt) {
        pushTime = Math.min(pushDuration, pushTime + dt);
        double share = (1 - Math.cos(2 * Math.PI * pushTime / pushDuration)) / pushDuration;
        forwardVel = pushForward * share;
        strafeVel = -pushLeft * share;
        turnVel = -pushTurn * share;
    }

    public void resetMotors() {
        for (SimMotor motor : new SimMotor[]{frontLeft, frontRight, backLeft, backRight}) {
            motor.setPower(0);
            motor.setDirection(DcMotorSimple.Direction.FORWARD);
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }
    }

    public String spinningWheel() {
        String[] names = {"Front Left", "Front Right", "Back Left", "Back Right"};
        double[] powers = {wheelFL(), wheelFR(), wheelBL(), wheelBR()};
        int most = 0;
        for (int i = 1; i < 4; i++) if (Math.abs(powers[i]) > Math.abs(powers[most])) most = i;
        if (powers[most] == 0) return "none";
        return names[most] + (powers[most] > 0 ? " FORWARD" : " REVERSE");
    }

    public double pedroX() { return x + sim.Field.HALF; }
    public double pedroY() { return y + sim.Field.HALF; }
    public double pedroHeading() { return Math.PI / 2 - heading; }
}
