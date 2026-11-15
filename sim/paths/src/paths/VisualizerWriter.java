package paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class VisualizerWriter {
    private VisualizerWriter() {}

    private static String n(double v) {
        return JavaWriter.num(v, 2);
    }

    private static String point(double simX, double simY) {
        return "{ \"x\": " + n(simX + FieldMap.HALF) + ", \"y\": " + n(simY + FieldMap.HALF) + " }";
    }

    private static String heading(Leg leg) {
        switch (leg.exportMode()) {
            case CONSTANT:
                return "{ \"type\": \"constant\", \"degrees\": " + JavaWriter.num(leg.start.pedroDegrees(), 1) + " }";
            case TANGENT:
                return "{ \"type\": \"tangential\", \"reverse\": false }";
            case REVERSE_TANGENT:
                return "{ \"type\": \"tangential\", \"reverse\": true }";
            default:
                double start = leg.start.pedroDegrees();
                double end = start + Math.toDegrees(Geo.wrap(leg.end.h - leg.start.h));
                return "{ \"type\": \"linear\", \"startDeg\": " + JavaWriter.num(start, 1) + ", \"endDeg\": " + JavaWriter.num(end, 1) + " }";
        }
    }

    private static String line(String id, String color, Leg leg, String endName, int waitMs, String waitName) {
        StringBuilder sb = new StringBuilder();
        sb.append("    {\n");
        sb.append("      \"kind\": \"atomic\",\n");
        sb.append("      \"id\": \"").append(id).append("\",\n");
        sb.append("      \"name\": \"").append(id).append("\",\n");
        sb.append("      \"endPoint\": { \"x\": ").append(n(leg.end.pedroX())).append(", \"y\": ").append(n(leg.end.pedroY()))
                .append(", \"name\": \"").append(endName).append("\" },\n");
        sb.append("      \"controlPoints\": [");
        if (!leg.straight) sb.append(point(leg.c1x, leg.c1y)).append(", ").append(point(leg.c2x, leg.c2y));
        sb.append("],\n");
        sb.append("      \"heading\": ").append(heading(leg)).append(",\n");
        if (waitMs > 0) {
            sb.append("      \"waitAfterMs\": ").append(waitMs).append(",\n");
            sb.append("      \"waitAfterName\": \"").append(waitName).append("\",\n");
        }
        sb.append("      \"color\": \"").append(color).append("\"\n");
        sb.append("    }");
        return sb.toString();
    }

    private static String shape(String id, String name, double[][] vertices, String color, String fill) {
        StringBuilder sb = new StringBuilder();
        sb.append("    {\n      \"id\": \"").append(id).append("\",\n      \"name\": \"").append(name).append("\",\n      \"vertices\": [\n");
        List<String> vs = new ArrayList<>();
        for (double[] v : vertices) vs.add("        " + point(v[0], v[1]));
        sb.append(String.join(",\n", vs)).append("\n      ],\n");
        sb.append("      \"color\": \"").append(color).append("\",\n      \"fillColor\": \"").append(fill).append("\"\n    }");
        return sb.toString();
    }

    private static double[][] polygon(double cx, double cy, double r, int sides) {
        double[][] out = new double[sides][];
        double rr = r / Math.cos(Math.PI / sides);
        for (int i = 0; i < sides; i++) {
            double a = 2 * Math.PI * (i + 0.5) / sides;
            out[i] = new double[]{cx + rr * Math.cos(a), cy + rr * Math.sin(a)};
        }
        return out;
    }

    public static String write(Routine r, Routine.Plan plan) {
        int shoot = (int) Math.round(Spec.SHOOT_TIME * 1000);
        int pickup = (int) Math.round(Spec.PICKUP_TIME * 1000);
        String[] colors = {"#EF6C00", "#6A1B9A", "#2E7D32"};
        List<String> lines = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        lines.add(line("startToShoot", "#212121", plan.start.segment.leg, "shoot", shoot, "shoot preload"));
        ids.add("startToShoot");
        for (String c : plan.cycles) {
            String color = colors[java.util.Arrays.asList(Routine.SOURCES).indexOf(c)];
            String name = c.substring(0, 1).toLowerCase(Locale.US) + c.substring(1);
            lines.add(line("to" + c, color, r.to(c).leg, name, pickup, "pick up"));
            lines.add(line("from" + c, color, r.from(c).leg, "shoot", shoot, "shoot"));
            ids.add("to" + c);
            ids.add("from" + c);
        }
        if (plan.park) {
            lines.add(line("toPark", "#00838F", r.park().leg, "park", 0, ""));
            ids.add("toPark");
        }
        List<String> shapes = new ArrayList<>();
        int i = 0;
        for (double[] l : FieldMap.LEGS) {
            double a = FieldMap.LEG_RADIUS;
            shapes.add(shape("leg" + i, "HIVE leg " + (i++ + 1),
                    new double[][]{{l[0] - a, l[1] - a}, {l[0] + a, l[1] - a}, {l[0] + a, l[1] + a}, {l[0] - a, l[1] + a}}, "#3E2723", "#8D6E63"));
        }
        i = 0;
        for (double[] f : FieldMap.FLOWERS) {
            shapes.add(shape("flower" + i, "FLOWER " + (i++ + 1), polygon(f[0], f[1], FieldMap.FLOWER_RADIUS, 8), "#F9A825", "#FFF176"));
        }
        FieldPose s = plan.start.segment.leg.start;
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("  \"startPoint\": { \"x\": ").append(n(s.pedroX())).append(", \"y\": ").append(n(s.pedroY()))
                .append(", \"headingDeg\": ").append(JavaWriter.num(s.pedroDegrees(), 1)).append(", \"name\": \"start\" },\n");
        sb.append("  \"lines\": [\n").append(String.join(",\n", lines)).append("\n  ],\n");
        sb.append("  \"shapes\": [\n").append(String.join(",\n", shapes)).append("\n  ],\n");
        sb.append("  \"sequence\": [\n");
        List<String> seq = new ArrayList<>();
        for (String id : ids) seq.add("    { \"kind\": \"path\", \"lineId\": \"" + id + "\" }");
        sb.append(String.join(",\n", seq)).append("\n  ],\n");
        sb.append("  \"fieldPoints\": [],\n");
        sb.append("  \"robot\": { \"rLength\": ").append(n(Spec.ROBOT_SIZE)).append(", \"rWidth\": ").append(n(Spec.ROBOT_SIZE))
                .append(", \"safetyMargin\": ").append(n(Spec.OBSTACLE_MARGIN))
                .append(", \"forwardVelocity\": ").append(n(Spec.MAX_SPEED))
                .append(", \"strafeVelocity\": ").append(n(Spec.MAX_SPEED * Spec.STRAFE_EFFICIENCY))
                .append(", \"aVelocity\": ").append(n(Spec.MAX_TURN_RATE))
                .append(", \"maxAcceleration\": ").append(n(Spec.MAX_ACCEL))
                .append(", \"maxDeceleration\": ").append(n(Spec.MAX_ACCEL)).append(" },\n");
        sb.append("  \"version\": \"2.0.0\"\n}\n");
        return sb.toString();
    }
}
