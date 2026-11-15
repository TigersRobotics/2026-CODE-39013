package paths;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Routine {
    public static final String[] SOURCES = {"AllianceFlower", "EndFlower", "Garden"};

    public final FieldMap field;
    public final FieldPose shoot;
    public final List<StartChoice> starts;
    public final Map<String, Segment> legs;
    public final List<String> problems = new ArrayList<>();

    public Routine(FieldMap field, FieldPose shoot, List<StartChoice> starts, Map<String, Segment> legs) {
        this.field = field;
        this.shoot = shoot;
        this.starts = starts;
        this.legs = legs;
    }

    public static final class StartChoice {
        public final FieldMap.StartSpot spot;
        public final Segment segment;
        public final boolean sectionBest;

        public StartChoice(FieldMap.StartSpot spot, Segment segment, boolean sectionBest) {
            this.spot = spot;
            this.segment = segment;
            this.sectionBest = sectionBest;
        }
    }

    public Segment to(String source) {
        return legs.get("to" + source);
    }

    public Segment from(String source) {
        return legs.get("from" + source);
    }

    public Segment park() {
        return legs.get("toPark");
    }

    public double roundTrip(String source) {
        return to(source).time() + Spec.PICKUP_TIME + from(source).time() + Spec.SHOOT_TIME;
    }

    public StartChoice bestStart() {
        StartChoice best = null;
        for (StartChoice c : starts) if (best == null || c.segment.time() < best.segment.time()) best = c;
        return best;
    }

    public long evaluated() {
        long n = 0;
        for (StartChoice c : starts) n += c.segment.evaluated;
        for (Segment s : legs.values()) n += s.evaluated;
        return n;
    }

    public Routine transformed(FieldMap target, boolean rotate) {
        Evaluator ev = Evaluator.get();
        List<FieldMap.StartSpot> targetSpots = target.startSpots();
        List<StartChoice> outStarts = new ArrayList<>();
        Map<String, Segment> outLegs = new LinkedHashMap<>();
        List<String> issues = new ArrayList<>();
        FieldPose newShoot = rotate ? shoot.rotated() : shoot.mirrored();
        String shootIssue = target.shootProblem(newShoot);
        if (shootIssue != null) issues.add("shoot pose " + newShoot + ": " + shootIssue);
        for (StartChoice c : starts) {
            Leg leg = rotate ? c.segment.leg.rotated() : c.segment.leg.mirrored();
            Result r = ev.evaluate(leg, target);
            FieldMap.StartSpot match = null;
            for (FieldMap.StartSpot s : targetSpots) if (Math.hypot(s.x - leg.start.x, s.y - leg.start.y) < 0.01) match = s;
            String why = target.startProblem(leg.start);
            if (why != null) issues.add("start " + leg.start + ": " + why);
            if (!r.ok) issues.add("start leg from " + leg.start + " collides or leaves our half");
            if (match == null) match = new FieldMap.StartSpot(c.spot.section, c.spot.index, leg.start.x, leg.start.y);
            outStarts.add(new StartChoice(match, new Segment(c.segment.name, leg, r, new ArrayList<>(), 0, "check"), c.sectionBest));
        }
        for (Map.Entry<String, Segment> e : legs.entrySet()) {
            Leg leg = rotate ? e.getValue().leg.rotated() : e.getValue().leg.mirrored();
            Result r = ev.evaluate(leg, target);
            if (e.getKey().equals("toPark")) {
                r = r.withExtraViolation(target.parkViolation(leg.end));
                if (!target.parked(leg.end)) issues.add("park pose " + leg.end + " is not in the loading zone");
            }
            if (!r.ok) issues.add(e.getKey() + " collides, leaves the field, or crosses the center line");
            outLegs.put(e.getKey(), new Segment(e.getKey(), leg, r, new ArrayList<>(), 0, "check"));
        }
        checkPickup(target, outLegs, "AllianceFlower", target.allianceFlowerPickup(), issues);
        checkPickup(target, outLegs, "EndFlower", target.endFlowerPickup(), issues);
        boolean gardenOk = false;
        for (FieldPose g : target.gardenPickups()) if (g.distance(outLegs.get("toGarden").leg.end) < 0.01) gardenOk = true;
        if (!gardenOk) issues.add("garden pickup " + outLegs.get("toGarden").leg.end + " is not at our garden");
        Routine out = new Routine(target, newShoot, outStarts, outLegs);
        out.problems.addAll(issues);
        return out;
    }

    private static void checkPickup(FieldMap target, Map<String, Segment> legs, String source, FieldPose expected, List<String> issues) {
        FieldPose got = legs.get("to" + source).leg.end;
        if (got.distance(expected) > 0.01 || Math.abs(Geo.wrap(got.h - expected.h)) > 1e-3) {
            issues.add(source + " pickup " + got + " is not at our flower pickup " + expected);
        }
    }

    public List<Plan> plans() {
        List<Plan> out = new ArrayList<>();
        for (StartChoice c : starts) {
            if (!c.segment.ok()) continue;
            for (int mask = 0; mask < 8; mask++) {
                List<String> cycles = new ArrayList<>();
                for (int i = 0; i < SOURCES.length; i++) if ((mask & (1 << i)) != 0) cycles.add(SOURCES[i]);
                cycles.sort((a, b) -> Double.compare(roundTrip(a), roundTrip(b)));
                for (int park = 0; park < 2; park++) {
                    boolean parks = park == 1;
                    double t = c.segment.time() + Spec.SHOOT_TIME;
                    boolean ok = true;
                    for (String s : cycles) {
                        t += roundTrip(s);
                        ok &= to(s).ok() && from(s).ok();
                    }
                    if (parks) {
                        t += park().time();
                        ok &= park().ok();
                    }
                    if (!ok || t > Spec.TIME_BUDGET) continue;
                    out.add(new Plan(field.name, c, cycles, parks, t));
                }
            }
        }
        out.sort((a, b) -> a.points != b.points ? Double.compare(b.points, a.points) : Double.compare(a.time, b.time));
        return out;
    }

    public static final class Plan {
        public final String alliance;
        public final StartChoice start;
        public final List<String> cycles;
        public final boolean park;
        public final double time;
        public final int volleys;
        public final int pollen;
        public final double points;

        Plan(String alliance, StartChoice start, List<String> cycles, boolean park, double time) {
            this.alliance = alliance;
            this.start = start;
            this.cycles = cycles;
            this.park = park;
            this.time = time;
            this.volleys = 1 + cycles.size();
            this.pollen = Spec.PRELOAD + Spec.POLLEN_PER_PICKUP * cycles.size();
            double tip = 1 - Math.pow(1 - Spec.VOLLEY_TIP_CHANCE, volleys);
            this.points = Spec.LEAVE_POINTS + Spec.TIP_POINTS * tip + (park ? Spec.PARK_POINTS : 0);
        }

        public String describe() {
            StringBuilder sb = new StringBuilder("start " + start.spot.id() + ", shoot preload");
            for (String c : cycles) sb.append(", ").append(c).append(" cycle");
            if (park) sb.append(", park");
            return sb.toString();
        }
    }
}
