
package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.api.Paths;
import com.pedropathing.paths.Path;
import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

@Autonomous
public class Auto extends OpMode {

    private Follower follower;

    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose start = poseFactory.of(56, 8, 90);
    private final Pose point1 = poseFactory.of(55.6771, 15.0039, -87.3604);
    private final Pose point2 = poseFactory.of(93.1244, 15.6652, 1.0118);

    public Path path1() {//lines created by joining the 2 points

        return Paths.line(start, point1).reverseTangent();

    }

    public Path path2() {//lines created by joining the 2 points
        return Paths.line(point1, point2).tangent();
    }

    public enum RobotAutoState { // the most basic states i've put in based off of the pedro pathing generated
        start,
        pickup,
        shoot,
        done
    }

    // This stores the CURRENT state
    private RobotAutoState currentState = RobotAutoState.start;

    public void updateState() {

        switch (currentState) {

            case start:
                // Wait until path1 is finished
                if (!follower.isBusy()) {
                    currentState = RobotAutoState.pickup;
                }
                break;

            case pickup:
                // TODO: Add pickup mechanism here.

                follower.follow(path2());

                currentState = RobotAutoState.shoot;
                break;

            case shoot:
                // Wait until path2 is finished
                if (!follower.isBusy()) {
                    // TODO: Add shooting mechanism here.
                    currentState = RobotAutoState.done;
                }
                break;

            case done:
                // Autonomous sequence complete
                break;
        }
    }

    @Override
    public void init() {
        follower = Constants.create(hardwareMap);


        assert follower != null;
        follower.setPose(start);
    }

    @Override
    public void start() {

        follower.follow(path1());
    }

    @Override
    public void loop() {

        // Keep the path follower running
        follower.update();

        // Check the autonomous state
        updateState();

        telemetry.addData("State", currentState);
        telemetry.addData("Follower Busy", follower.isBusy());
        telemetry.update();
    }
}
