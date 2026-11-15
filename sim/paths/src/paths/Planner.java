package paths;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public final class Planner {
    public static final double[] HEADING_OFFSETS = {-45, -30, -15, 0, 15, 30, 45};

    public final FieldMap field;
    public final List<FieldMap.StartSpot> spots;
    public final FieldPose allianceFlower;
    public final FieldPose endFlower;
    public final List<FieldPose> gardens;
    public final boolean quickRun;
    public int candidateCount;
    public List<ShootScore> screened = new ArrayList<>();
    public List<ShootScore> quick = new ArrayList<>();
    public List<ShootScore> medium = new ArrayList<>();

    public Planner(FieldMap field, boolean quickRun) {
        this.field = field;
        this.quickRun = quickRun;
        this.spots = field.startSpots();
        this.allianceFlower = field.allianceFlowerPickup();
        this.endFlower = field.endFlowerPickup();
        this.gardens = field.gardenPickups();
    }

    public static final class ShootScore {
        public final FieldPose shoot;
        public final String stage;
        public final double total;
        public final double start;
        public final double alliance;
        public final double end;
        public final double garden;
        public final double park;
        public final long evaluated;

        ShootScore(FieldPose shoot, String stage, double start, double alliance, double end, double garden, double park, long evaluated) {
            this.shoot = shoot;
            this.stage = stage;
            this.start = start;
            this.alliance = alliance;
            this.end = end;
            this.garden = garden;
            this.park = park;
            this.evaluated = evaluated;
            double t = start + 4 * Spec.SHOOT_TIME + 3 * Spec.PICKUP_TIME + alliance + end + garden + park;
            this.total = Double.isFinite(t) ? t : Double.POSITIVE_INFINITY;
        }
    }

    public List<FieldPose> shootCandidates() {
        List<FieldPose> out = new ArrayList<>();
        for (double ax = 10; ax <= 63; ax += 3) {
            for (double y = -63; y <= 63; y += 3) {
                double x = field.side * ax;
                double bearing = Math.atan2(-y, field.hiveX - x);
                for (double off : HEADING_OFFSETS) {
                    FieldPose p = new FieldPose(x, y, bearing + Math.toRadians(off)).rounded();
                    if (field.shootProblem(p) == null) out.add(p);
                }
            }
        }
        return out;
    }

    private List<FieldMap.StartSpot> nearest(FieldPose shoot, int count) {
        List<FieldMap.StartSpot> sorted = new ArrayList<>(spots);
        sorted.sort(Comparator.comparingDouble(s -> Math.hypot(s.x - shoot.x, s.y - shoot.y)));
        return sorted.subList(0, Math.min(count, sorted.size()));
    }

    private String tag(FieldPose shoot) {
        return field.name + "|" + shoot.pedroText() + "|";
    }

    public ShootScore score(FieldPose shoot, Cem.Settings s) {
        String tag = tag(shoot);
        long evals = 0;
        double bestStart = Double.POSITIVE_INFINITY;
        for (FieldMap.StartSpot spot : nearest(shoot, 3)) {
            for (double h : FieldMap.HEADINGS) {
                Optimizer.Solution sol = Optimizer.solve(LegProblem.fixed(tag + "start|" + spot.id() + "|" + h, field, spot.pose(h), shoot), s);
                evals += sol.evaluated;
                bestStart = Math.min(bestStart, sol.time());
            }
        }
        double[] a = roundTrip(tag + "alliance", shoot, allianceFlower, s);
        double[] e = roundTrip(tag + "end", shoot, endFlower, s);
        double garden = Double.POSITIVE_INFINITY;
        evals += (long) (a[1] + e[1]);
        for (int i = 0; i < gardens.size(); i++) {
            double[] g = roundTrip(tag + "garden" + i, shoot, gardens.get(i), s);
            evals += (long) g[1];
            garden = Math.min(garden, g[0]);
        }
        Optimizer.Solution park = Optimizer.solve(LegProblem.park(tag + "park", field, shoot), s);
        evals += park.evaluated;
        return new ShootScore(shoot, s.name, bestStart, a[0], e[0], garden, park.time(), evals);
    }

    private double[] roundTrip(String key, FieldPose shoot, FieldPose pickup, Cem.Settings s) {
        Optimizer.Solution to = Optimizer.solve(LegProblem.fixed(key + "|to", field, shoot, pickup), s);
        Optimizer.Solution back = Optimizer.solve(LegProblem.fixed(key + "|back", field, pickup, shoot), s);
        return new double[]{to.time() + back.time(), to.evaluated + back.evaluated};
    }

    private List<ShootScore> scoreAll(List<FieldPose> poses, Cem.Settings s) {
        List<ShootScore> out = poses.parallelStream().map(p -> score(p, s)).collect(Collectors.toList());
        out.sort(Comparator.comparingDouble((ShootScore x) -> x.total).thenComparing(x -> x.shoot.pedroText()));
        return out;
    }

    private static List<FieldPose> top(List<ShootScore> scores, int n) {
        List<FieldPose> out = new ArrayList<>();
        for (ShootScore s : scores) {
            if (out.size() >= n) break;
            if (Double.isFinite(s.total)) out.add(s.shoot);
        }
        return out;
    }

    public Routine run() {
        List<FieldPose> candidates = shootCandidates();
        candidateCount = candidates.size();
        log("shoot pose candidates that pass range, sightline, turret and clearance rules: " + candidateCount);
        screened = scoreAll(candidates, Cem.SCREEN);
        log("screened all candidates, best " + fmt(screened.get(0).total) + " s at " + screened.get(0).shoot);
        quick = scoreAll(top(screened, quickRun ? 12 : 40), Cem.QUICK);
        log("quick CEM on top candidates, best " + fmt(quick.get(0).total) + " s at " + quick.get(0).shoot);
        medium = scoreAll(top(quick, quickRun ? 3 : 8), Cem.MEDIUM);
        log("medium CEM on finalists, best " + fmt(medium.get(0).total) + " s at " + medium.get(0).shoot);
        List<ShootScore> refined = refine(medium.get(0).shoot);
        if (!refined.isEmpty()) {
            List<ShootScore> merged = new ArrayList<>(medium);
            merged.addAll(scoreAll(top(refined, quickRun ? 2 : 4), Cem.MEDIUM));
            merged.sort(Comparator.comparingDouble((ShootScore x) -> x.total).thenComparing(x -> x.shoot.pedroText()));
            medium = merged;
            log("local CEM over the shoot pose, best " + fmt(medium.get(0).total) + " s at " + medium.get(0).shoot);
        }
        return build(medium.get(0).shoot);
    }

    private List<ShootScore> refine(FieldPose center) {
        double bearing0 = Math.atan2(-center.y, field.hiveX - center.x);
        double[] mean = {center.x, center.y, Geo.wrap(center.h - bearing0)};
        double[] std = {4, 4, Math.toRadians(12)};
        int pop = quickRun ? 6 : 12, iters = quickRun ? 2 : 4, elite = 3;
        SplittableRandom rnd = new SplittableRandom(Optimizer.seed(field.name + "|shoot refine"));
        Map<String, ShootScore> seen = new LinkedHashMap<>();
        for (ShootScore m : medium) seen.put(m.shoot.pedroText(), null);
        List<ShootScore> all = new ArrayList<>();
        for (int it = 0; it < iters; it++) {
            List<FieldPose> poses = new ArrayList<>();
            for (int j = 0; j < pop; j++) {
                double x = mean[0] + std[0] * gaussian(rnd);
                double y = mean[1] + std[1] * gaussian(rnd);
                double off = Math.max(-Spec.TURRET_LIMIT, Math.min(Spec.TURRET_LIMIT, mean[2] + std[2] * gaussian(rnd)));
                double bearing = Math.atan2(-y, field.hiveX - x);
                FieldPose p = new FieldPose(x, y, bearing + off).rounded();
                if (field.shootProblem(p) != null || seen.containsKey(p.pedroText())) continue;
                seen.put(p.pedroText(), null);
                poses.add(p);
            }
            if (poses.isEmpty()) continue;
            List<ShootScore> scores = scoreAll(poses, Cem.QUICK);
            all.addAll(scores);
            List<ShootScore> elites = scores.subList(0, Math.min(elite, scores.size()));
            for (int i = 0; i < 3; i++) {
                double m = 0;
                for (ShootScore s : elites) m += i == 0 ? s.shoot.x : i == 1 ? s.shoot.y : Geo.wrap(s.shoot.h - Math.atan2(-s.shoot.y, field.hiveX - s.shoot.x));
                m /= elites.size();
                double var = 0;
                for (ShootScore s : elites) {
                    double v = i == 0 ? s.shoot.x : i == 1 ? s.shoot.y : Geo.wrap(s.shoot.h - Math.atan2(-s.shoot.y, field.hiveX - s.shoot.x));
                    var += (v - m) * (v - m);
                }
                mean[i] = 0.7 * m + 0.3 * mean[i];
                std[i] = Math.max(i == 2 ? Math.toRadians(2) : 0.5, 0.7 * Math.sqrt(var / elites.size()) + 0.3 * std[i]);
            }
        }
        all.sort(Comparator.comparingDouble((ShootScore x) -> x.total).thenComparing(x -> x.shoot.pedroText()));
        quick.addAll(all);
        return all;
    }

    private static double gaussian(SplittableRandom rnd) {
        double u1 = Math.max(1e-300, rnd.nextDouble()), u2 = rnd.nextDouble();
        return Math.sqrt(-2 * Math.log(u1)) * Math.cos(2 * Math.PI * u2);
    }

    private static final class Task {
        final String name;
        final LegProblem problem;
        final Cem.Settings settings;
        final FieldMap.StartSpot spot;

        Task(String name, LegProblem problem, Cem.Settings settings, FieldMap.StartSpot spot) {
            this.name = name;
            this.problem = problem;
            this.settings = settings;
            this.spot = spot;
        }
    }

    public Routine build(FieldPose shoot) {
        String tag = tag(shoot) + "final|";
        Cem.Settings full = quickRun ? Cem.MEDIUM : Cem.FULL;
        Cem.Settings startSettings = quickRun ? Cem.QUICK : Cem.MEDIUM;
        List<Task> tasks = new ArrayList<>();
        for (FieldMap.StartSpot spot : spots) {
            for (double h : FieldMap.HEADINGS) {
                String name = "start|" + spot.id() + "|" + Math.round(Math.toDegrees(h));
                tasks.add(new Task(name, LegProblem.fixed(tag + name, field, spot.pose(h), shoot), startSettings, spot));
            }
        }
        tasks.add(new Task("toAllianceFlower", LegProblem.fixed(tag + "toAllianceFlower", field, shoot, allianceFlower), full, null));
        tasks.add(new Task("fromAllianceFlower", LegProblem.fixed(tag + "fromAllianceFlower", field, allianceFlower, shoot), full, null));
        tasks.add(new Task("toEndFlower", LegProblem.fixed(tag + "toEndFlower", field, shoot, endFlower), full, null));
        tasks.add(new Task("fromEndFlower", LegProblem.fixed(tag + "fromEndFlower", field, endFlower, shoot), full, null));
        for (int i = 0; i < gardens.size(); i++) {
            tasks.add(new Task("toGarden" + i, LegProblem.fixed(tag + "toGarden" + i, field, shoot, gardens.get(i)), full, null));
            tasks.add(new Task("fromGarden" + i, LegProblem.fixed(tag + "fromGarden" + i, field, gardens.get(i), shoot), full, null));
        }
        tasks.add(new Task("toPark", LegProblem.park(tag + "toPark", field, shoot), full, null));
        List<Segment> solved = tasks.parallelStream()
                .map(t -> Segment.of(t.name, Optimizer.solve(t.problem, t.settings)))
                .collect(Collectors.toList());

        Map<String, Segment> byName = new LinkedHashMap<>();
        for (Segment s : solved) byName.put(s.name, s);

        Map<String, Segment> bestPerSpot = new LinkedHashMap<>();
        Map<String, Task> taskPerSpot = new LinkedHashMap<>();
        Map<String, Long> spotEvals = new LinkedHashMap<>();
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            if (t.spot == null) continue;
            Segment s = solved.get(i);
            spotEvals.merge(t.spot.id(), s.evaluated, Long::sum);
            Segment cur = bestPerSpot.get(t.spot.id());
            if (cur == null || s.time() < cur.time()) {
                bestPerSpot.put(t.spot.id(), s);
                taskPerSpot.put(t.spot.id(), t);
            }
        }
        Map<String, String> sectionBest = new LinkedHashMap<>();
        for (FieldMap.StartSpot spot : spots) {
            Segment s = bestPerSpot.get(spot.id());
            String cur = sectionBest.get(spot.section);
            if (s.ok() && (cur == null || s.time() < bestPerSpot.get(cur).time())) sectionBest.put(spot.section, spot.id());
        }
        List<Task> refineTasks = new ArrayList<>();
        for (String id : sectionBest.values()) {
            Task t = taskPerSpot.get(id);
            refineTasks.add(new Task(t.name, LegProblem.fixed(t.problem.key + "|refine", field, t.problem.start, shoot), full, t.spot));
        }
        List<Segment> refinedStarts = refineTasks.parallelStream()
                .map(t -> Segment.of(t.name, Optimizer.solve(t.problem, t.settings)))
                .collect(Collectors.toList());
        for (int i = 0; i < refineTasks.size(); i++) {
            String id = refineTasks.get(i).spot.id();
            Segment r = refinedStarts.get(i);
            Segment old = bestPerSpot.get(id);
            spotEvals.merge(id, r.evaluated, Long::sum);
            bestPerSpot.put(id, r.time() <= old.time() ? r : old);
        }
        List<Routine.StartChoice> starts = new ArrayList<>();
        for (FieldMap.StartSpot spot : spots) {
            Segment s = bestPerSpot.get(spot.id());
            s = new Segment(s.name, s.leg, s.result, s.top, spotEvals.get(spot.id()), s.settings);
            starts.add(new Routine.StartChoice(spot, s, spot.id().equals(sectionBest.get(spot.section))));
        }

        Map<String, Segment> legs = new LinkedHashMap<>();
        legs.put("toAllianceFlower", byName.get("toAllianceFlower"));
        legs.put("fromAllianceFlower", byName.get("fromAllianceFlower"));
        legs.put("toEndFlower", byName.get("toEndFlower"));
        legs.put("fromEndFlower", byName.get("fromEndFlower"));
        int bestGarden = 0;
        long toEvals = 0, fromEvals = 0;
        for (int i = 0; i < gardens.size(); i++) {
            double t = byName.get("toGarden" + i).time() + byName.get("fromGarden" + i).time();
            double b = byName.get("toGarden" + bestGarden).time() + byName.get("fromGarden" + bestGarden).time();
            if (t < b) bestGarden = i;
            toEvals += byName.get("toGarden" + i).evaluated;
            fromEvals += byName.get("fromGarden" + i).evaluated;
        }
        Segment tg = byName.get("toGarden" + bestGarden), fg = byName.get("fromGarden" + bestGarden);
        legs.put("toGarden", new Segment("toGarden", tg.leg, tg.result, tg.top, toEvals, tg.settings));
        legs.put("fromGarden", new Segment("fromGarden", fg.leg, fg.result, fg.top, fromEvals, fg.settings));
        legs.put("toPark", byName.get("toPark"));
        log("final legs solved for shoot pose " + shoot);
        return new Routine(field, shoot, starts, legs);
    }

    public long searchEvaluations() {
        long n = 0;
        for (ShootScore s : screened) n += s.evaluated;
        for (ShootScore s : quick) n += s.evaluated;
        for (ShootScore s : medium) n += s.evaluated;
        return n;
    }

    private void log(String msg) {
        System.out.println("[" + field.name + "] " + msg);
    }

    static String fmt(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }
}
