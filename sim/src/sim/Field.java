package sim;

public final class Field {
    public static final double SIZE = 144;
    public static final double HALF = SIZE / 2;

    public static final double[] RED_LOADING_ZONE = {-HALF, 24, -HALF + 11, 47};
    public static final double[] BLUE_LOADING_ZONE = {HALF - 11, -47, HALF, -24};

    public static final double[] RED_GARDEN = {-HALF, -HALF, -HALF + 23, -HALF + 2};
    public static final double[] BLUE_GARDEN = {HALF - 23, HALF - 2, HALF, HALF};

    public static final double FLOWER_RADIUS = 3.5;
    public static final double[][] FLOWERS = {
            {-24, HALF - 4},
            {HALF - 4, 24},
            {-HALF + 4, -24},
            {24, -HALF + 4},
    };

    public static final double HIVE_WIDTH = 49.46;
    public static final double HIVE_DEPTH = 38.95;
    public static final double HIVE_LEG_RADIUS = 1.5;
    public static final double[][] HIVE_LEGS = {
            {-HIVE_WIDTH / 2, -HIVE_DEPTH / 2},
            {-HIVE_WIDTH / 2, HIVE_DEPTH / 2},
            {HIVE_WIDTH / 2, -HIVE_DEPTH / 2},
            {HIVE_WIDTH / 2, HIVE_DEPTH / 2},
    };

    public static final double CELL_WIDTH = 20;
    public static final double CELL_DEPTH = 12;
    public static final double CELL_SPACING = 18.8;
    public static final double RED_HIVE_X = -12.4;
    public static final double BLUE_HIVE_X = 12.4;

    public static final double ALLIANCE_AREA_WIDTH = 97;
    public static final double ALLIANCE_AREA_DEPTH = 54;

    public static double[] start(boolean red) {
        return red ? new double[]{-HALF + SimRobot.ROBOT_LENGTH / 2, 0, Math.PI / 2} : new double[]{HALF - SimRobot.ROBOT_LENGTH / 2, 0, -Math.PI / 2};
    }

    private Field() {}
}
