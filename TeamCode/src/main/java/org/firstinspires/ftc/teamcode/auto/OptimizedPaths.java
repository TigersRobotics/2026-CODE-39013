package org.firstinspires.ftc.teamcode.auto;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

public class OptimizedPaths {
    private final PoseFactory p = PoseFactory.degrees();
    private final boolean red;

    public final Pose start;
    public final Pose audienceShoot;
    public final Pose farShoot;
    public final Pose garden;
    public final Pose allianceFlower;
    public final Pose endFlower;
    public final Pose park;

    public OptimizedPaths(boolean red) {
        this.red = red;
        start = pose(8.5, 12, 0);
        audienceShoot = pose(13.29, 13.79, 33.7);
        farShoot = pose(38, 120, 315);
        garden = pose(14.5, 11.5, 270);
        allianceFlower = pose(18, 48, 180);
        endFlower = pose(48, 126, 90);
        park = pose(15.5, 114, 315);
    }

    private Pose pose(double x, double y, double heading) {
        if (red) return p.of(x, y, heading);
        return p.of(144 - x, 144 - y, (heading + 180) % 360);
    }

    public Path[] paths() {
        return new Path[]{
                curve(start, pose(10.91, 12.65, 0), pose(11.5, 12.97, 0), audienceShoot).linear(start, audienceShoot),
                curve(audienceShoot, pose(13.69, 13.03, 0), pose(14.1, 12.26, 0), garden).linear(audienceShoot, garden),
                curve(garden, pose(14.1, 12.26, 0), pose(13.69, 13.03, 0), audienceShoot).linear(garden, audienceShoot),
                curve(audienceShoot, pose(18.37, 25.53, 0), pose(17.86, 20.42, 0), allianceFlower).linear(audienceShoot, allianceFlower),
                curve(allianceFlower, pose(28.27, 48, 0), pose(42.23, 115.77, 0), farShoot).reverseTangent(),
                curve(farShoot, pose(44.55, 121.62, 0), pose(45.51, 123.11, 0), endFlower).linear(farShoot, endFlower),
                curve(endFlower, pose(45.18, 122.4, 0), pose(43.79, 121.57, 0), farShoot).linear(endFlower, farShoot),
                line(farShoot, park).constant(farShoot)
        };
    }
}
