package sim;

/**
 * BIOBUZZ field layout from the Competition Manual (section 9 and figure 9-2).
 * Inches from the field center, +x toward the blue ALLIANCE AREA, +y away from the audience.
 */
public final class Field {
    public static final double SIZE = 144;
    public static final double HALF = SIZE / 2;

    // LOADING ZONES are 23 in along the wall by 11 in deep
    public static final double[] RED_LOADING_ZONE = {-HALF, 24, -HALF + 11, 47};
    public static final double[] BLUE_LOADING_ZONE = {HALF - 11, -47, HALF, -24};

    // GARDENS are 23 in by 2 in, in opposite corners
    public static final double[] RED_GARDEN = {-HALF, -HALF, -HALF + 23, -HALF + 2};
    public static final double[] BLUE_GARDEN = {HALF - 23, HALF - 2, HALF, HALF};

    // FLOWERS sit against the perimeter wall, one per wall
    public static final double FLOWER_RADIUS = 3.5;
    public static final double[][] FLOWERS = {
            {-24, HALF - 4},
            {HALF - 4, 24},
            {-HALF + 4, -24},
            {24, -HALF + 4},
    };

    // HIVE frame is 49.46 in wide by 38.95 in deep, robots can drive under it but not through the legs
    public static final double HIVE_WIDTH = 49.46;
    public static final double HIVE_DEPTH = 38.95;
    public static final double HIVE_LEG_RADIUS = 1.5;
    public static final double[][] HIVE_LEGS = {
            {-HIVE_WIDTH / 2, -HIVE_DEPTH / 2},
            {-HIVE_WIDTH / 2, HIVE_DEPTH / 2},
            {HIVE_WIDTH / 2, -HIVE_DEPTH / 2},
            {HIVE_WIDTH / 2, HIVE_DEPTH / 2},
    };

    // each HIVE has 2 CELLS 18.8 in apart, red HIVE on the red side
    public static final double CELL_WIDTH = 20;
    public static final double CELL_DEPTH = 12;
    public static final double CELL_SPACING = 18.8;
    public static final double RED_HIVE_X = -12.4;
    public static final double BLUE_HIVE_X = 12.4;

    // ALLIANCE AREAS are outside the field, 97 in wide by 54 in deep
    public static final double ALLIANCE_AREA_WIDTH = 97;
    public static final double ALLIANCE_AREA_DEPTH = 54;

    /** Starting spots: touching the wall, on our side, not in the LOADING ZONE, facing the HIVE */
    public static double[] start(boolean red) {
        return red ? new double[]{-HALF + 9, 0, Math.PI / 2} : new double[]{HALF - 9, 0, -Math.PI / 2};
    }

    private Field() {}
}
