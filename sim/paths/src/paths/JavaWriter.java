package paths;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class JavaWriter {
    private JavaWriter() {}

    public static final String[] CYCLE_LEGS = {
            "toAllianceFlower", "fromAllianceFlower", "toEndFlower", "fromEndFlower", "toGarden", "fromGarden", "toPark"};

    public static String num(double v, int places) {
        BigDecimal d = BigDecimal.valueOf(v).setScale(places, java.math.RoundingMode.HALF_UP).stripTrailingZeros();
        if (d.scale() < 0) d = d.setScale(0);
        String s = d.toPlainString();
        return s.equals("-0") ? "0" : s;
    }

    public static String pose(FieldPose p) {
        return "p.of(" + num(p.pedroX(), 2) + ", " + num(p.pedroY(), 2) + ", " + num(p.pedroDegrees(), 1) + ")";
    }

    public static String point(double simX, double simY) {
        return "p.of(" + num(simX + FieldMap.HALF, 2) + ", " + num(simY + FieldMap.HALF, 2) + ", 0)";
    }

    public static String startName(Routine.StartChoice c) {
        return c.spot.section + "Start";
    }

    public static String write(Routine r, Routine.Plan best, String className) {
        List<String> fields = new ArrayList<>();
        List<String> controls = new ArrayList<>();
        List<String> methods = new ArrayList<>();
        boolean[] used = new boolean[2];

        List<Routine.StartChoice> sectionStarts = new ArrayList<>();
        for (Routine.StartChoice c : r.starts) if (c.sectionBest) sectionStarts.add(c);
        for (Routine.StartChoice c : sectionStarts) fields.add(field(startName(c), pose(c.segment.leg.start)));
        fields.add(field("start", startName(best.start)));
        fields.add(field("shoot", pose(r.shoot)));
        fields.add(field("allianceFlower", pose(r.legs.get("toAllianceFlower").leg.end)));
        fields.add(field("endFlower", pose(r.legs.get("toEndFlower").leg.end)));
        fields.add(field("garden", pose(r.legs.get("toGarden").leg.end)));
        fields.add(field("park", pose(r.legs.get("toPark").leg.end)));

        for (Routine.StartChoice c : sectionStarts) {
            String method = startName(c) + "ToShoot";
            methods.add(method(method, c.segment.leg, startName(c), "shoot", controls, used));
        }
        methods.add("    public Path startToShoot() {\n        return " + startName(best.start) + "ToShoot();\n    }\n");
        String[][] ends = {
                {"toAllianceFlower", "shoot", "allianceFlower"},
                {"fromAllianceFlower", "allianceFlower", "shoot"},
                {"toEndFlower", "shoot", "endFlower"},
                {"fromEndFlower", "endFlower", "shoot"},
                {"toGarden", "shoot", "garden"},
                {"fromGarden", "garden", "shoot"},
                {"toPark", "shoot", "park"},
        };
        for (String[] e : ends) methods.add(method(e[0], r.legs.get(e[0]).leg, e[1], e[2], controls, used));

        StringBuilder plan = new StringBuilder("    public Path[] bestPlan() {\n        return new Path[]{\n                startToShoot()");
        for (String c : best.cycles) plan.append(",\n                to").append(c).append("(),\n                from").append(c).append("()");
        if (best.park) plan.append(",\n                toPark()");
        plan.append("\n        };\n    }\n");
        methods.add(plan.toString());

        StringBuilder sb = new StringBuilder();
        sb.append("package org.firstinspires.ftc.teamcode.auto;\n\n");
        if (used[0]) sb.append("import static com.pedropathing.api.Paths.curve;\n");
        if (used[1]) sb.append("import static com.pedropathing.api.Paths.line;\n");
        sb.append("\nimport com.pedropathing.api.PoseFactory;\n");
        sb.append("import com.pedropathing.math.Pose;\n");
        sb.append("import com.pedropathing.paths.Path;\n\n");
        sb.append("public class ").append(className).append(" {\n");
        sb.append("    private final PoseFactory p = PoseFactory.degrees();\n\n");
        for (String f : fields) sb.append(f);
        sb.append("\n");
        for (String c : controls) sb.append(c);
        for (String m : methods) sb.append("\n").append(m);
        sb.append("}\n");
        return sb.toString();
    }

    private static String field(String name, String value) {
        return "    public final Pose " + name + " = " + value + ";\n";
    }

    private static String method(String name, Leg leg, String from, String to, List<String> controls, boolean[] used) {
        String geometry;
        if (leg.straight && leg.exportMode() != Leg.Mode.LINEAR) {
            used[1] = true;
            geometry = "line(" + from + ", " + to + ")";
        } else {
            used[0] = true;
            controls.add("    private final Pose " + name + "A = " + point(leg.c1x, leg.c1y) + ";\n");
            controls.add("    private final Pose " + name + "B = " + point(leg.c2x, leg.c2y) + ";\n");
            geometry = "curve(" + from + ", " + name + "A, " + name + "B, " + to + ")";
        }
        String heading;
        switch (leg.exportMode()) {
            case CONSTANT:
                heading = ".constant(" + from + ")";
                break;
            case TANGENT:
                heading = ".tangent()";
                break;
            case REVERSE_TANGENT:
                heading = ".reverseTangent()";
                break;
            default:
                heading = ".linear(" + from + ", " + to + ")";
        }
        return String.format(Locale.US, "    public Path %s() {\n        return %s%s;\n    }\n", name, geometry, heading);
    }
}
