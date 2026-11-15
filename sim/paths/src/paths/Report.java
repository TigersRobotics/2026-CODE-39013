package paths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Report {
    public static final String AUTO_DIR = "TeamCode/src/main/java/org/firstinspires/ftc/teamcode/auto";
    public static final String RESULTS_DIR = "sim/paths/results";

    private final Planner planner;
    private final Routine red;
    private final Routine blue;
    private final Routine mirrored;
    private final List<Routine.Plan> redPlans;
    private final List<Routine.Plan> bluePlans;
    private final long searchEvals;
    private final long finalEvals;
    private final long totalEvals;
    private final double seconds;
    private final boolean quickRun;

    public Report(Planner planner, Routine red, Routine blue, Routine mirrored, List<Routine.Plan> redPlans,
                  List<Routine.Plan> bluePlans, long searchEvals, long finalEvals, long totalEvals, double seconds, boolean quickRun) {
        this.planner = planner;
        this.red = red;
        this.blue = blue;
        this.mirrored = mirrored;
        this.redPlans = redPlans;
        this.bluePlans = bluePlans;
        this.searchEvals = searchEvals;
        this.finalEvals = finalEvals;
        this.totalEvals = totalEvals;
        this.seconds = seconds;
        this.quickRun = quickRun;
    }

    private static String f(double v, int places) {
        return String.format(Locale.US, "%." + places + "f", v);
    }

    public void writeAll(Path root) throws IOException {
        Path results = root.resolve(RESULTS_DIR);
        Path auto = root.resolve(AUTO_DIR);
        Files.createDirectories(results);
        Files.createDirectories(auto);
        if (redPlans.isEmpty() || bluePlans.isEmpty()) throw new IllegalStateException("no plan fits in the time budget");
        write(results.resolve("segments.csv"), segmentsCsv());
        write(results.resolve("plans.csv"), plansCsv());
        write(results.resolve("shoot_candidates.csv"), shootCsv());
        write(results.resolve("summary.json"), summaryJson());
        write(results.resolve("red_auto.svg"), SvgWriter.write(red, redPlans.get(0), totalEvals));
        write(results.resolve("blue_auto.svg"), SvgWriter.write(blue, bluePlans.get(0), totalEvals));
        write(results.resolve("red_auto.pp.json"), VisualizerWriter.write(red, redPlans.get(0)));
        write(results.resolve("blue_auto.pp.json"), VisualizerWriter.write(blue, bluePlans.get(0)));
        write(auto.resolve("RedAutoPaths.java"), JavaWriter.write(red, redPlans.get(0), "RedAutoPaths"));
        write(auto.resolve("BlueAutoPaths.java"), JavaWriter.write(blue, bluePlans.get(0), "BlueAutoPaths"));
    }

    private static void write(Path p, String s) throws IOException {
        Files.write(p, s.getBytes(StandardCharsets.UTF_8));
    }

    private String segmentsCsv() {
        StringBuilder sb = new StringBuilder("alliance,segment,rank,heading,shape,time_s,cost,length_in,min_clearance_in,"
                + "start_x,start_y,start_deg,c1_x,c1_y,c2_x,c2_y,end_x,end_y,end_deg,evaluated\n");
        for (Routine r : new Routine[]{red, blue}) {
            for (Routine.StartChoice c : r.starts) row(sb, r.field.name, "start_" + c.spot.id() + (c.sectionBest ? "_best" : ""), 1, c.segment.leg,
                    c.segment.result, c.segment.evaluated);
            for (Map.Entry<String, Segment> e : r.legs.entrySet()) {
                Segment s = e.getValue();
                row(sb, r.field.name, e.getKey(), 1, s.leg, s.result, s.evaluated);
                if (r == red) {
                    for (int i = 1; i < s.top.size(); i++) {
                        Cem.Candidate c = s.top.get(i);
                        row(sb, r.field.name, e.getKey(), i + 1, c.leg, c.result, 0);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static void row(StringBuilder sb, String alliance, String name, int rank, Leg leg, Result res, long evals) {
        sb.append(alliance).append(',').append(name).append(',').append(rank).append(',')
                .append(leg.exportMode().name().toLowerCase(Locale.US)).append(',').append(leg.straight ? "line" : "curve").append(',')
                .append(res.ok ? f(res.time, 3) : "fail").append(',').append(res.ok ? f(res.cost, 3) : "fail").append(',')
                .append(f(res.length, 1)).append(',').append(res.minClearance > 1e6 ? "far" : f(res.minClearance, 2)).append(',')
                .append(f(leg.start.pedroX(), 2)).append(',').append(f(leg.start.pedroY(), 2)).append(',').append(f(leg.start.pedroDegrees(), 1)).append(',')
                .append(f(leg.c1x + 72, 2)).append(',').append(f(leg.c1y + 72, 2)).append(',')
                .append(f(leg.c2x + 72, 2)).append(',').append(f(leg.c2y + 72, 2)).append(',')
                .append(f(leg.end.pedroX(), 2)).append(',').append(f(leg.end.pedroY(), 2)).append(',').append(f(leg.end.pedroDegrees(), 1)).append(',')
                .append(evals).append('\n');
    }

    private String plansCsv() {
        StringBuilder sb = new StringBuilder("alliance,rank,points,time_s,volleys,pollen,cycles,park,start,start_x,start_y,start_deg\n");
        for (List<Routine.Plan> plans : List.of(redPlans, bluePlans)) {
            List<Routine.Plan> keep = new ArrayList<>();
            List<String> kinds = new ArrayList<>();
            for (Routine.Plan p : plans) {
                String kind = p.cycles + "|" + p.park;
                if (keep.size() < 10 || !kinds.contains(kind)) keep.add(p);
                if (!kinds.contains(kind)) kinds.add(kind);
            }
            int rank = 1;
            for (Routine.Plan p : keep) {
                FieldPose s = p.start.segment.leg.start;
                sb.append(p.alliance).append(',').append(rank++).append(',').append(f(p.points, 2)).append(',').append(f(p.time, 2)).append(',')
                        .append(p.volleys).append(',').append(p.pollen).append(',').append(String.join("+", p.cycles.isEmpty() ? List.of("none") : p.cycles))
                        .append(',').append(p.park ? "yes" : "no").append(',').append(p.start.spot.id()).append(',')
                        .append(f(s.pedroX(), 2)).append(',').append(f(s.pedroY(), 2)).append(',').append(f(s.pedroDegrees(), 1)).append('\n');
            }
        }
        return sb.toString();
    }

    private String shootCsv() {
        StringBuilder sb = new StringBuilder("stage,rank,total_s,start_s,alliance_flower_s,end_flower_s,garden_s,park_s,x,y,deg,evaluated\n");
        Object[][] stages = {{"screen", planner.screened, 40}, {"quick", planner.quick, 40}, {"medium", planner.medium, 20}};
        for (Object[] st : stages) {
            @SuppressWarnings("unchecked")
            List<Planner.ShootScore> list = new ArrayList<>((List<Planner.ShootScore>) st[1]);
            list.sort((a, b) -> Double.compare(a.total, b.total));
            int n = Math.min((Integer) st[2], list.size());
            for (int i = 0; i < n; i++) {
                Planner.ShootScore s = list.get(i);
                sb.append(st[0]).append(',').append(i + 1).append(',').append(f(s.total, 3)).append(',').append(f(s.start, 3)).append(',')
                        .append(f(s.alliance, 3)).append(',').append(f(s.end, 3)).append(',').append(f(s.garden, 3)).append(',')
                        .append(f(s.park, 3)).append(',').append(f(s.shoot.pedroX(), 2)).append(',').append(f(s.shoot.pedroY(), 2)).append(',')
                        .append(f(s.shoot.pedroDegrees(), 1)).append(',').append(s.evaluated).append('\n');
            }
        }
        return sb.toString();
    }

    private String planJson(Routine r, Routine.Plan p) {
        StringBuilder sb = new StringBuilder();
        sb.append("    {\n");
        sb.append("      \"alliance\": \"").append(r.field.name).append("\",\n");
        sb.append("      \"points\": ").append(f(p.points, 2)).append(",\n");
        sb.append("      \"time_s\": ").append(f(p.time, 2)).append(",\n");
        sb.append("      \"volleys\": ").append(p.volleys).append(",\n");
        sb.append("      \"pollen_launched\": ").append(p.pollen).append(",\n");
        sb.append("      \"steps\": \"").append(p.describe()).append("\",\n");
        sb.append("      \"pedro_poses\": {\n");
        List<String> poses = new ArrayList<>();
        poses.add(pj("start", p.start.segment.leg.start));
        poses.add(pj("shoot", r.shoot));
        for (String c : p.cycles) poses.add(pj(c.substring(0, 1).toLowerCase(Locale.US) + c.substring(1), r.to(c).leg.end));
        if (p.park) poses.add(pj("park", r.park().leg.end));
        sb.append(String.join(",\n", poses)).append("\n      },\n");
        sb.append("      \"leg_times_s\": {\n");
        List<String> legs = new ArrayList<>();
        legs.add("        \"startToShoot\": " + f(p.start.segment.time(), 3));
        for (String c : p.cycles) {
            legs.add("        \"to" + c + "\": " + f(r.to(c).time(), 3));
            legs.add("        \"from" + c + "\": " + f(r.from(c).time(), 3));
        }
        if (p.park) legs.add("        \"toPark\": " + f(r.park().time(), 3));
        sb.append(String.join(",\n", legs)).append("\n      },\n");
        sb.append("      \"legality_problems\": ").append(r.problems.size()).append("\n");
        sb.append("    }");
        return sb.toString();
    }

    private static String pj(String name, FieldPose p) {
        return "        \"" + name + "\": [" + f(p.pedroX(), 2) + ", " + f(p.pedroY(), 2) + ", " + f(p.pedroDegrees(), 1) + "]";
    }

    private String summaryJson() {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("  \"method\": \"cross-entropy method over cubic Bezier control points with random restarts, seed ").append(Spec.SEED).append("\",\n");
        sb.append("  \"mode\": \"").append(quickRun ? "quick" : "full").append("\",\n");
        sb.append("  \"candidate_paths_evaluated\": ").append(totalEvals).append(",\n");
        sb.append("  \"shoot_search_evaluations\": ").append(searchEvals).append(",\n");
        sb.append("  \"final_leg_evaluations\": ").append(finalEvals).append(",\n");
        sb.append("  \"shoot_pose_candidates\": ").append(planner.candidateCount).append(",\n");
        sb.append("  \"coordinates\": \"pedroX = simX + 72, pedroY = simY + 72, heading in degrees counterclockwise from +x\",\n");
        sb.append("  \"blue_transform\": \"180 degree rotation about field center\",\n");
        sb.append("  \"mirror_check_problems\": ").append(mirrored.problems.size()).append(",\n");
        sb.append("  \"assumptions\": {\n");
        sb.append("    \"max_speed_in_s\": ").append(f(Spec.MAX_SPEED, 1)).append(",\n");
        sb.append("    \"max_accel_in_s2\": ").append(f(Spec.MAX_ACCEL, 1)).append(",\n");
        sb.append("    \"max_lateral_accel_in_s2\": ").append(f(Spec.MAX_LATERAL_ACCEL, 1)).append(",\n");
        sb.append("    \"max_turn_rate_deg_s\": ").append(f(Math.toDegrees(Spec.MAX_TURN_RATE), 0)).append(",\n");
        sb.append("    \"strafe_efficiency\": ").append(f(Spec.STRAFE_EFFICIENCY, 2)).append(",\n");
        sb.append("    \"settle_time_s\": ").append(f(Spec.SETTLE_TIME, 2)).append(",\n");
        sb.append("    \"shoot_time_s\": ").append(f(Spec.SHOOT_TIME, 2)).append(",\n");
        sb.append("    \"pickup_time_s\": ").append(f(Spec.PICKUP_TIME, 2)).append(",\n");
        sb.append("    \"time_budget_s\": ").append(f(Spec.TIME_BUDGET, 1)).append(",\n");
        sb.append("    \"volley_tip_chance\": ").append(f(Spec.VOLLEY_TIP_CHANCE, 2)).append(",\n");
        sb.append("    \"obstacle_margin_in\": ").append(f(Spec.OBSTACLE_MARGIN, 2)).append("\n");
        sb.append("  },\n");
        sb.append("  \"best_plans\": [\n");
        sb.append(planJson(red, redPlans.get(0))).append(",\n").append(planJson(blue, bluePlans.get(0))).append("\n");
        sb.append("  ]\n}\n");
        return sb.toString();
    }

    public void printSummary() {
        System.out.println();
        System.out.println(String.format(Locale.US, "Evaluated %,d candidate paths in %.1f s (%,d in the shoot pose search, %,d on the final legs).",
                totalEvals, seconds, searchEvals, finalEvals));
        System.out.println("Shoot pose candidates passing the rules: " + planner.candidateCount);
        for (Routine r : new Routine[]{red, blue}) {
            List<Routine.Plan> plans = r == red ? redPlans : bluePlans;
            Routine.Plan p = plans.get(0);
            System.out.println();
            System.out.println(String.format(Locale.US, "%s best plan: %.2f expected pts, %.2f s, %d volleys, %d pollen",
                    r.field.name.toUpperCase(Locale.US), p.points, p.time, p.volleys, p.pollen));
            System.out.println("  " + p.describe());
            System.out.println("  start " + p.start.segment.leg.start + "  shoot " + r.shoot + "  park " + r.park().leg.end);
            for (String c : p.cycles) System.out.println("  " + c + " pickup " + r.to(c).leg.end);
            System.out.println("  legality problems: " + (r.problems.isEmpty() ? "none" : String.join("; ", r.problems)));
        }
        System.out.println();
        System.out.println("Mirroring red across x = 0 instead of rotating gives " + mirrored.problems.size() + " legality problems, for example:");
        for (int i = 0; i < Math.min(3, mirrored.problems.size()); i++) System.out.println("  " + mirrored.problems.get(i));
    }
}
