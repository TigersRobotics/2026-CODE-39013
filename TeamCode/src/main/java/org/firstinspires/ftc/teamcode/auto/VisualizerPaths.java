package org.firstinspires.ftc.teamcode.auto;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

public class VisualizerPaths {

    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose firstpath = poseFactory.of(0, 0, 90);
    private final Pose path1 = poseFactory.of(56, 36, 180);
    private final Pose point2 = poseFactory.of(140, 30, 0);
    private final Pose point3 = poseFactory.of(42.9407, 95.9237, 145.8152);

    public Path path1() {
        return curve(firstpath, path1).linear(firstpath, path1);
    }

    public Path path2() {
        return line(path1, point2).constant(point2);
    }

    public Path path3() {
        return line(point2, point3).tangent();
    }
}
