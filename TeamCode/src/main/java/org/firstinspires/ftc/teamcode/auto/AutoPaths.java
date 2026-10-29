package org.firstinspires.ftc.teamcode.auto;

import static com.pedropathing.api.Paths.line;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

/**
 * Paths for autonomous, positions are in inches and headings are in degrees
 */
public class AutoPaths {
    private final PoseFactory p = PoseFactory.degrees();

    private final Pose startPose = p.of(24, 24, 345);
    private final Pose endPose   = p.of(48, 96, 345);

    /**
     * Straight line from startPose to endPose, holding the start heading the whole way
     */
    public Path myPath() {
        return line(startPose, endPose).constant(startPose);
    }
}
