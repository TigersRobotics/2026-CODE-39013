package paths;

import java.util.Locale;

public final class SvgWriter {
    private static final double S = 5;
    private static final double M = 24;
    private static final double FIELD = 2 * FieldMap.HALF * S;
    private static final double LEGEND = 190;

    private SvgWriter() {}

    private static double sx(double simX) {
        return M + (simX + FieldMap.HALF) * S;
    }

    private static double sy(double simY) {
        return M + (FieldMap.HALF - simY) * S;
    }

    private static String f(double v) {
        return String.format(Locale.US, "%.1f", v);
    }

    private static void rect(StringBuilder sb, double[] r, String fill, String stroke, String extra) {
        sb.append("<rect x=\"").append(f(sx(r[0]))).append("\" y=\"").append(f(sy(r[3])))
                .append("\" width=\"").append(f((r[2] - r[0]) * S)).append("\" height=\"").append(f((r[3] - r[1]) * S))
                .append("\" fill=\"").append(fill).append("\" stroke=\"").append(stroke).append("\" ").append(extra).append("/>\n");
    }

    private static void circle(StringBuilder sb, double x, double y, double r, String fill, String stroke) {
        sb.append("<circle cx=\"").append(f(sx(x))).append("\" cy=\"").append(f(sy(y))).append("\" r=\"").append(f(r * S))
                .append("\" fill=\"").append(fill).append("\" stroke=\"").append(stroke).append("\"/>\n");
    }

    private static void text(StringBuilder sb, double px, double py, String s, int size, String fill, String weight) {
        sb.append("<text x=\"").append(f(px)).append("\" y=\"").append(f(py)).append("\" font-size=\"").append(size)
                .append("\" font-family=\"Helvetica, Arial, sans-serif\" fill=\"").append(fill).append("\" font-weight=\"")
                .append(weight).append("\">").append(s).append("</text>\n");
    }

    private static void robot(StringBuilder sb, FieldPose p, String stroke, String fill, double width) {
        double c = Math.cos(p.h), s = Math.sin(p.h), a = Spec.HALF_SIZE;
        double[][] k = {{a, a}, {a, -a}, {-a, -a}, {-a, a}};
        StringBuilder pts = new StringBuilder();
        for (double[] q : k) {
            double x = p.x + q[0] * c - q[1] * s, y = p.y + q[0] * s + q[1] * c;
            pts.append(f(sx(x))).append(",").append(f(sy(y))).append(" ");
        }
        sb.append("<polygon points=\"").append(pts.toString().trim()).append("\" fill=\"").append(fill)
                .append("\" stroke=\"").append(stroke).append("\" stroke-width=\"").append(f(width)).append("\"/>\n");
        sb.append("<line x1=\"").append(f(sx(p.x))).append("\" y1=\"").append(f(sy(p.y))).append("\" x2=\"")
                .append(f(sx(p.x + a * c))).append("\" y2=\"").append(f(sy(p.y + a * s))).append("\" stroke=\"").append(stroke)
                .append("\" stroke-width=\"").append(f(width + 0.5)).append("\"/>\n");
    }

    private static void path(StringBuilder sb, Leg leg, String color, double width, String dash, boolean ghosts) {
        double[][] pts = Evaluator.get().samples(leg);
        StringBuilder d = new StringBuilder();
        for (int i = 0; i < pts.length; i++) {
            d.append(i == 0 ? "M" : "L").append(f(sx(pts[i][0]))).append(" ").append(f(sy(pts[i][1]))).append(" ");
        }
        if (ghosts) {
            int step = Math.max(1, pts.length / 5);
            for (int i = step; i < pts.length - step / 2; i += step) {
                robot(sb, new FieldPose(pts[i][0], pts[i][1], pts[i][2]), color, "none", 0.6);
            }
        }
        sb.append("<path d=\"").append(d.toString().trim()).append("\" fill=\"none\" stroke=\"").append(color)
                .append("\" stroke-width=\"").append(f(width)).append("\" ").append(dash.isEmpty() ? "" : "stroke-dasharray=\"" + dash + "\" ")
                .append("stroke-linecap=\"round\"/>\n");
    }

    public static String write(Routine r, Routine.Plan plan, long evaluated) {
        FieldMap fm = r.field;
        boolean red = fm.isRed();
        String allianceColor = red ? "#c62828" : "#1565c0";
        double w = FIELD + 2 * M, h = FIELD + 2 * M + LEGEND;
        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(f(w)).append("\" height=\"").append(f(h))
                .append("\" viewBox=\"0 0 ").append(f(w)).append(" ").append(f(h)).append("\">\n");
        sb.append("<rect x=\"0\" y=\"0\" width=\"").append(f(w)).append("\" height=\"").append(f(h)).append("\" fill=\"#ffffff\"/>\n");
        double H = FieldMap.HALF;
        rect(sb, new double[]{-H, -H, 0, H}, "#fbecec", "none", "");
        rect(sb, new double[]{0, -H, H, H}, "#e9f0fb", "none", "");
        for (int i = 1; i < 6; i++) {
            double v = -H + 24 * i;
            sb.append("<line x1=\"").append(f(sx(v))).append("\" y1=\"").append(f(sy(H))).append("\" x2=\"").append(f(sx(v)))
                    .append("\" y2=\"").append(f(sy(-H))).append("\" stroke=\"#d0d0d0\" stroke-width=\"1\"/>\n");
            sb.append("<line x1=\"").append(f(sx(-H))).append("\" y1=\"").append(f(sy(v))).append("\" x2=\"").append(f(sx(H)))
                    .append("\" y2=\"").append(f(sy(v))).append("\" stroke=\"#d0d0d0\" stroke-width=\"1\"/>\n");
        }
        sb.append("<line x1=\"").append(f(sx(0))).append("\" y1=\"").append(f(sy(H))).append("\" x2=\"").append(f(sx(0)))
                .append("\" y2=\"").append(f(sy(-H))).append("\" stroke=\"#555555\" stroke-width=\"2\" stroke-dasharray=\"8 6\"/>\n");
        FieldMap redMap = FieldMap.red(), blueMap = FieldMap.blue();
        rect(sb, redMap.loadingZone, "#ef9a9a", "#c62828", "stroke-width=\"1.5\"");
        rect(sb, blueMap.loadingZone, "#90caf9", "#1565c0", "stroke-width=\"1.5\"");
        rect(sb, redMap.garden, "#a5d6a7", "#c62828", "stroke-width=\"1.5\"");
        rect(sb, blueMap.garden, "#a5d6a7", "#1565c0", "stroke-width=\"1.5\"");
        rect(sb, new double[]{-FieldMap.HIVE_HALF_WIDTH, -FieldMap.HIVE_HALF_DEPTH, FieldMap.HIVE_HALF_WIDTH, FieldMap.HIVE_HALF_DEPTH},
                "none", "#8d6e63", "stroke-width=\"1.5\" stroke-dasharray=\"4 4\"");
        for (FieldMap m : new FieldMap[]{redMap, blueMap}) {
            for (double[] cell : m.cells()) {
                rect(sb, new double[]{cell[0] - 10, cell[1] - 6, cell[0] + 10, cell[1] + 6}, m.isRed() ? "#ffcdd2" : "#bbdefb",
                        m.isRed() ? "#c62828" : "#1565c0", "stroke-width=\"1\"");
            }
        }
        for (double[] l : FieldMap.LEGS) circle(sb, l[0], l[1], FieldMap.LEG_RADIUS, "#5d4037", "#3e2723");
        for (double[] fl : FieldMap.FLOWERS) circle(sb, fl[0], fl[1], FieldMap.FLOWER_RADIUS, "#fff176", "#f9a825");
        sb.append("<rect x=\"").append(f(M)).append("\" y=\"").append(f(M)).append("\" width=\"").append(f(FIELD))
                .append("\" height=\"").append(f(FIELD)).append("\" fill=\"none\" stroke=\"#222222\" stroke-width=\"3\"/>\n");

        for (Routine.StartChoice c : r.starts) {
            FieldPose s = c.segment.leg.start;
            circle(sb, s.x, s.y, 1.2, "#9e9e9e", "none");
            if (c.sectionBest && c != plan.start) robot(sb, s, "#bdbdbd", "none", 0.8);
        }
        for (Routine.StartChoice c : r.starts) {
            if (c.sectionBest && c != plan.start) path(sb, c.segment.leg, "#757575", 1.5, "5 4", false);
        }
        String[] colors = {"#ef6c00", "#6a1b9a", "#2e7d32"};
        for (int i = 0; i < Routine.SOURCES.length; i++) {
            String src = Routine.SOURCES[i];
            boolean inPlan = plan.cycles.contains(src);
            path(sb, r.to(src).leg, colors[i], inPlan ? 3 : 1.5, inPlan ? "" : "3 4", inPlan);
            path(sb, r.from(src).leg, colors[i], inPlan ? 3 : 1.5, inPlan ? "8 4" : "3 4", false);
            robot(sb, r.to(src).leg.end, colors[i], "none", 1.5);
        }
        path(sb, plan.start.segment.leg, "#212121", 3, "", true);
        path(sb, r.park().leg, "#00838f", plan.park ? 3 : 1.5, plan.park ? "" : "3 4", plan.park);
        robot(sb, plan.start.segment.leg.start, "#212121", "rgba(33,33,33,0.15)", 2);
        robot(sb, r.park().leg.end, "#00838f", "rgba(0,131,143,0.15)", 2);
        robot(sb, r.shoot, allianceColor, "rgba(255,255,255,0.6)", 2.5);
        double[] hive = fm.hiveCenter();
        sb.append("<line x1=\"").append(f(sx(r.shoot.x))).append("\" y1=\"").append(f(sy(r.shoot.y))).append("\" x2=\"")
                .append(f(sx(hive[0]))).append("\" y2=\"").append(f(sy(hive[1]))).append("\" stroke=\"").append(allianceColor)
                .append("\" stroke-width=\"1.5\" stroke-dasharray=\"2 3\"/>\n");

        double ly = M + FIELD + 26;
        String title = (red ? "Red" : "Blue") + " auto, best plan: " + String.format(Locale.US, "%.1f s, %.2f expected pts, %d volleys",
                plan.time, plan.points, plan.volleys);
        text(sb, M, ly, title, 17, allianceColor, "bold");
        text(sb, M, ly + 22, esc(plan.describe()), 13, "#212121", "normal");
        text(sb, M, ly + 42, "Shoot " + r.shoot.pedroText() + "   start " + plan.start.segment.leg.start.pedroText()
                + "   park " + r.park().leg.end.pedroText() + "   (Pedro x, y, heading deg)", 12, "#424242", "normal");
        String[][] legend = {
                {"#212121", "start to shoot (solid = plan start, dashed gray = best start in other wall sections)"},
                {colors[0], "alliance wall flower cycle"},
                {colors[1], "end wall flower cycle"},
                {colors[2], "garden cycle"},
                {"#00838f", "park in loading zone"},
        };
        for (int i = 0; i < legend.length; i++) {
            double yy = ly + 66 + i * 18;
            sb.append("<line x1=\"").append(f(M)).append("\" y1=\"").append(f(yy - 4)).append("\" x2=\"").append(f(M + 26))
                    .append("\" y2=\"").append(f(yy - 4)).append("\" stroke=\"").append(legend[i][0]).append("\" stroke-width=\"3\"/>\n");
            text(sb, M + 34, yy, legend[i][1], 12, "#212121", "normal");
        }
        text(sb, M, ly + 66 + legend.length * 18 + 4, String.format(Locale.US,
                "Solid = to pickup, long dash = back to shoot, short dash = not in plan. Gray dots = legal starts. %,d candidate paths evaluated.",
                evaluated), 11, "#616161", "normal");
        sb.append("</svg>\n");
        return sb.toString();
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
