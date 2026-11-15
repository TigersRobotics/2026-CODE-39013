package paths;

public final class Spec {
    public static final double ROBOT_SIZE = 18;
    public static final double HALF_SIZE = ROBOT_SIZE / 2;
    public static final double CORNER_REACH = HALF_SIZE * Math.sqrt(2);

    public static final double MAX_SPEED = 57;
    public static final double MAX_ACCEL = 60;
    public static final double MAX_LATERAL_ACCEL = 48;
    public static final double MAX_TURN_RATE = Math.toRadians(220);
    public static final double TURN_RADIUS = 14;
    public static final double STRAFE_EFFICIENCY = 0.85;
    public static final double MIN_SPEED = 1;
    public static final double SETTLE_TIME = 0.25;

    public static final double OBSTACLE_MARGIN = 1.0;
    public static final double WALL_TOLERANCE = 0.05;
    public static final double CENTER_LINE_MARGIN = 0.5;
    public static final double SOFT_OBSTACLE = 4;
    public static final double SOFT_WALL = 2;
    public static final double OBSTACLE_WEIGHT = 0.03;
    public static final double WALL_WEIGHT = 0.01;
    public static final double CURVE_WEIGHT = 0.1;
    public static final double SOFT_RADIUS = 10;
    public static final double SAMPLE_SPACING = 0.75;

    public static final double AUTO_TIME = 30;
    public static final double TIME_MARGIN = 3;
    public static final double TIME_BUDGET = AUTO_TIME - TIME_MARGIN;
    public static final double SHOOT_TIME = 1.5;
    public static final double PICKUP_TIME = 1.0;
    public static final int PRELOAD = 4;
    public static final int POLLEN_PER_PICKUP = 4;

    public static final double LEAVE_POINTS = 3;
    public static final double TIP_POINTS = 20;
    public static final double PARK_POINTS = 5;
    public static final double VOLLEY_TIP_CHANCE = 0.5;

    public static final double FLOWER_GAP = 2;
    public static final double GARDEN_GAP = 1;
    public static final double PARK_OVERLAP = 4;
    public static final double START_CLEARANCE = 1.5;
    public static final double MIN_RANGE = 30;
    public static final double MAX_RANGE = 78;
    public static final double TURRET_LIMIT = Math.toRadians(45);
    public static final double SIGHT_MARGIN = 3;
    public static final double SHOOT_CLEARANCE = 2;

    public static final long SEED = 39013;

    private Spec() {}
}
