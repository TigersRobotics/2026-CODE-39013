package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.api.Paths;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;


public class AutoPath {

    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose start = poseFactory.of(56, 8, 90);
    private final Pose path1 = poseFactory.of(56, 36, 90);
    private final Pose point2 = poseFactory.of(20.7677, 53.0269, 154.2067);
    private final Pose point3 = poseFactory.of(12.9783, 103.0409, 98.8524);
    public Pose returnStartPose(){
        return start;
    }
    public com.pedropathing.paths.Path path1() {
        return Paths.line(start, path1).constant(path1);
    }

    public com.pedropathing.paths.Path path2() {
        return Paths.line(path1, point2).tangent();
    }

    public com.pedropathing.paths.Path path3() {
        return Paths.line(point2, point3).tangent();
    }
}
