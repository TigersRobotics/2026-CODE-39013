# Path optimizer

Searches Pedro Pathing paths for our BIOBUZZ auto and keeps the fastest legal ones. The last full run tried 3,466,850 paths, using the robot size from the team CAD and a turret that only turns 31 degrees each way.

```
./optimize.sh quick   small test run, output goes to out/quick
./optimize.sh         full run, output goes to results
```

What is in `results`:

- `summary.json` best red and blue plan, shoot spots, leg times
- `red_auto.svg`, `blue_auto.svg` pictures of the best autos
- `red_auto.pp.json`, `blue_auto.pp.json` open in the Pedro Pathing visualizer
- `plans.csv`, `segments.csv`, `shoot_candidates.csv` everything else it tried
- `auto/` generated path classes after a full run

The robot uses the best red plan in `TeamCode/.../auto/OptimizedPaths.java`, and blue is the same plan rotated around the field center. After a new run, copy the new numbers from `results/auto/RedAutoPaths.java` into that file.

How it works: every leg is a Bezier curve. It picks random control points, throws out paths that hit the HIVE, a FLOWER, a wall or cross the center line, times the rest with the drive speed limits, keeps the fastest eighth and repeats.
