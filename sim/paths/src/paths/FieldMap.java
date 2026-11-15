package paths;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class FieldMap {
    public static final double HALF = 72;
    public static final double FLOWER_RADIUS = 3.5;
    public static final double LEG_RADIUS = 1.5;
    public static final double HIVE_HALF_WIDTH = 49.46 / 2;
    public static final double HIVE_HALF_DEPTH = 38.95 / 2;
    public static final double CELL_OFFSET = 18.8 / 2;
    public static final double GARDEN_DEPTH = 2;
    public static final double LOADING_DEPTH = 11;
    private static final double EPS = 1e-6;

    public static final double[][] FLOWERS = {
            {-24, HALF - 4},
            {HALF - 4, 24},
            {-HALF + 4, -24},
            {24, -HALF + 4},
    };
    public static final double[][] LEGS = {
            {-HIVE_HALF_WIDTH, -HIVE_HALF_DEPTH},
            {-HIVE_HALF_WIDTH, HIVE_HALF_DEPTH},
            {HIVE_HALF_WIDTH, -HIVE_HALF_DEPTH},
            {HIVE_HALF_WIDTH, HIVE_HALF_DEPTH},
    };
    public static final double[][] OBSTACLES = buildObstacles();
    public static final double[] HIVE_FRAME = {
            -HIVE_HALF_WIDTH - LEG_RADIUS, -HIVE_HALF_DEPTH - LEG_RADIUS,
            HIVE_HALF_WIDTH + LEG_RADIUS, HIVE_HALF_DEPTH + LEG_RADIUS};
    public static final double[] HEADINGS = {0, Math.PI / 2, Math.PI, -Math.PI / 2};

    public final String name;
    public final double side;
    public final double[] loadingZone;
    public final double[] garden;
    public final double hiveX;
    public final double[] allianceFlower;
    public final double[] endFlower;
    public final double gardenWallSign;

    private FieldMap(String name, double side, double[] loadingZone, double[] garden, double hiveX) {
        this.name = name;
        this.side = side;
        this.loadingZone = loadingZone;
        this.garden = garden;
        this.hiveX = hiveX;
        this.gardenWallSign = Math.signum(garden[1] + garden[3]);
        double[] wallFlower = null, otherFlower = null;
        for (double[] f : FLOWERS) {
            if (Math.signum(f[0]) != side) continue;
            if (Math.abs(f[0]) > Math.abs(f[1])) wallFlower = f;
            else otherFlower = f;
        }
        this.allianceFlower = wallFlower;
        this.endFlower = otherFlower;
    }

    public static FieldMap red() {
        return new FieldMap("red", -1,
                new double[]{-HALF, 24, -HALF + LOADING_DEPTH, 47},
                new double[]{-HALF, -HALF, -HALF + 23, -HALF + GARDEN_DEPTH},
                -12.4);
    }

    public static FieldMap blue() {
        return new FieldMap("blue", 1,
                new double[]{HALF - LOADING_DEPTH, -47, HALF, -24},
                new double[]{HALF - 23, HALF - GARDEN_DEPTH, HALF, HALF},
                12.4);
    }

    public boolean isRed() {
        return side < 0;
    }

    private static double[][] buildObstacles() {
        double[][] out = new double[LEGS.length + FLOWERS.length][];
        int i = 0;
        for (double[] l : LEGS) out[i++] = new double[]{l[0], l[1], LEG_RADIUS};
        for (double[] f : FLOWERS) out[i++] = new double[]{f[0], f[1], FLOWER_RADIUS};
        return out;
    }

    public double centerLineClearance(double x, double h) {
        return side * x - Geo.reach(h);
    }

    public static double obstacleClearance(double x, double y, double h) {
        double best = Double.MAX_VALUE;
        for (double[] o : OBSTACLES) best = Math.min(best, Geo.circleClearance(x, y, h, o[0], o[1], o[2]));
        return best;
    }

    public FieldPose flowerPickup(double[] flower) {
        double nx = 0, ny = 0;
        if (Math.abs(flower[0]) > Math.abs(flower[1])) nx = Math.signum(flower[0]);
        else ny = Math.signum(flower[1]);
        double d = FLOWER_RADIUS + Spec.FLOWER_GAP + Spec.HALF_SIZE;
        return new FieldPose(flower[0] - nx * d, flower[1] - ny * d, Math.atan2(ny, nx));
    }

    public FieldPose allianceFlowerPickup() {
        return flowerPickup(allianceFlower);
    }

    public FieldPose endFlowerPickup() {
        return flowerPickup(endFlower);
    }

    public List<FieldPose> gardenPickups() {
        double y = gardenWallSign * (HALF - GARDEN_DEPTH - Spec.GARDEN_GAP - Spec.HALF_SIZE);
        double lo = garden[0] + Spec.HALF_SIZE, hi = garden[2] - Spec.HALF_SIZE;
        double h = Math.atan2(gardenWallSign, 0);
        List<FieldPose> out = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            double x = lo + (hi - lo) * i / 2.0;
            out.add(new FieldPose(x, y, h));
        }
        out.sort(Comparator.comparingDouble(p -> Math.abs(p.x)));
        return out;
    }

    public double[] parkBox() {
        double inner = HALF - LOADING_DEPTH - Spec.HALF_SIZE + Spec.PARK_OVERLAP;
        double outer = HALF - Spec.HALF_SIZE;
        double x0 = side * Math.min(inner, outer), x1 = side * Math.max(inner, outer);
        return new double[]{Math.min(x0, x1), Math.max(x0, x1), loadingZone[1] + 5, loadingZone[3] - 5};
    }

    public FieldPose parkGuess() {
        double[] b = parkBox();
        return new FieldPose((b[0] + b[1]) / 2, (b[2] + b[3]) / 2, 0);
    }

    public double parkViolation(FieldPose p) {
        return Math.max(0, Geo.rectGap(p.x, p.y, p.h, loadingZone) + Spec.PARK_OVERLAP);
    }

    public boolean parked(FieldPose p) {
        return parkViolation(p) <= 1e-6 && Geo.wallClearance(p.x, p.y, p.h) >= -Spec.WALL_TOLERANCE;
    }

    public double[] hiveCenter() {
        return new double[]{hiveX, 0};
    }

    public double[][] cells() {
        return new double[][]{{hiveX, CELL_OFFSET}, {hiveX, -CELL_OFFSET}};
    }

    public String startProblem(FieldPose p) {
        if (!Geo.axisAligned(p.h)) return "heading not square to the wall";
        if (Math.abs(Geo.wallClearance(p.x, p.y, p.h)) > EPS) return "not touching the wall";
        if (centerLineClearance(p.x, p.h) < 1 - EPS) return "not fully on our side";
        if (Geo.rectGap(p.x, p.y, p.h, loadingZone) < Spec.START_CLEARANCE - EPS) return "in the loading zone";
        if (Geo.rectGap(p.x, p.y, p.h, garden) < Spec.START_CLEARANCE - EPS) return "on the garden";
        for (double[] f : FLOWERS) {
            if (Geo.circleClearance(p.x, p.y, p.h, f[0], f[1], FLOWER_RADIUS) < Spec.START_CLEARANCE - EPS) return "touching a flower";
        }
        return null;
    }

    public String shootProblem(FieldPose p) {
        if (Geo.wallClearance(p.x, p.y, p.h) < -EPS) return "outside the field";
        if (centerLineClearance(p.x, p.h) < 1 - EPS) return "not on our side";
        if (obstacleClearance(p.x, p.y, p.h) < Spec.SHOOT_CLEARANCE - EPS) return "too close to an obstacle";
        if (Geo.rectGap(p.x, p.y, p.h, HIVE_FRAME) < 1 - EPS) return "under the hive";
        double range = Math.hypot(hiveX - p.x, -p.y);
        if (range < Spec.MIN_RANGE || range > Spec.MAX_RANGE) return "out of range";
        double bearing = Math.atan2(-p.y, hiveX - p.x);
        if (Math.abs(Geo.wrap(bearing - p.h)) > Spec.TURRET_LIMIT + 1e-9) return "turret cannot reach";
        for (double[] cell : cells()) {
            for (double[] l : LEGS) {
                if (Geo.segmentDistance(p.x, p.y, cell[0], cell[1], l[0], l[1]) < LEG_RADIUS + Spec.SIGHT_MARGIN) return "hive leg blocks the shot";
            }
            for (double[] f : FLOWERS) {
                if (Geo.segmentDistance(p.x, p.y, cell[0], cell[1], f[0], f[1]) < FLOWER_RADIUS + Spec.SIGHT_MARGIN) return "flower blocks the shot";
            }
        }
        return null;
    }

    public List<StartSpot> startSpots() {
        List<StartSpot> out = new ArrayList<>();
        double edge = HALF - Spec.HALF_SIZE;
        addWall(out, "allianceWall", new String[]{"Garden", "Middle", "Far"}, true, side * edge,
                gardenWallSign * edge, -gardenWallSign * edge);
        addWall(out, "gardenWall", new String[]{"Corner", "Center", "Inner"}, false, gardenWallSign * edge,
                side * edge, 0);
        addWall(out, "flowerWall", new String[]{"Corner", "Center", "Inner"}, false, -gardenWallSign * edge,
                side * edge, 0);
        return out;
    }

    private void addWall(List<StartSpot> out, String base, String[] suffixes, boolean vertical, double fixed,
                         double from, double to) {
        List<double[]> runs = new ArrayList<>();
        double step = 0.5;
        int n = (int) Math.round(Math.abs(to - from) / step);
        double dir = Math.signum(to - from);
        double runStart = Double.NaN, last = Double.NaN;
        for (int i = 0; i <= n; i++) {
            double u = from + dir * step * i;
            double x = vertical ? fixed : u, y = vertical ? u : fixed;
            boolean ok = startProblem(new FieldPose(x, y, 0)) == null;
            if (ok && Double.isNaN(runStart)) runStart = u;
            if (ok) last = u;
            if ((!ok || i == n) && !Double.isNaN(runStart)) {
                runs.add(new double[]{runStart, last});
                runStart = Double.NaN;
            }
        }
        for (int r = 0; r < runs.size(); r++) {
            String section = runs.size() == 1 ? base : base + (r < suffixes.length ? suffixes[r] : suffixes[suffixes.length - 1] + r);
            double a = runs.get(r)[0], b = runs.get(r)[1];
            List<Double> us = new ArrayList<>();
            double spacing = 6;
            for (double u = a; Math.abs(u - a) < Math.abs(b - a) - spacing / 2; u += dir * spacing) us.add(u);
            us.add(b);
            int index = 0;
            for (double u : us) {
                double x = vertical ? fixed : u, y = vertical ? u : fixed;
                boolean dup = false;
                for (StartSpot s : out) if (Math.hypot(s.x - x, s.y - y) < 1) dup = true;
                if (dup) continue;
                out.add(new StartSpot(section, index++, x, y));
            }
        }
    }

    public static final class StartSpot {
        public final String section;
        public final int index;
        public final double x;
        public final double y;

        StartSpot(String section, int index, double x, double y) {
            this.section = section;
            this.index = index;
            this.x = x;
            this.y = y;
        }

        public FieldPose pose(double h) {
            return new FieldPose(x, y, h);
        }

        public String id() {
            return section + index;
        }
    }
}
