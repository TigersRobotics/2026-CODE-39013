
package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.api.Paths;
import com.pedropathing.paths.Path;
import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import org.firstinspires.ftc.teamcode.robotControllers.ShootingSys;

@Autonomous
public class Auto extends OpMode {
    ShootingSys shootingSys;

    AutoPath autoPath;
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
        park,
        done
    }

    // This stores the CURRENT state
    private RobotAutoState currentState = RobotAutoState.start;


    /**  Alright, currently it looks like the auto script follows point1 and point2, which are defined somewhere above this.
     *   AutoPath doesn't seem to have any usages... I don't think we're using the path from pedropathing.  AutoPath script
     *   does have start, path1, point2, and point3, though...
      */
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

                //I changed this to use AutoPath's path2 instead of what was inputted here
                //follower.follow(path2());
                follower.follow(autoPath.path2());

                currentState = RobotAutoState.shoot;
                break;

            case shoot:
                // Wait until path2 is finished
                if (!follower.isBusy()) {
                    shoot();
                    currentState = RobotAutoState.park;
                }
                break;

            case park:
                //follow path 3, to move to the parking area
                follower.follow(autoPath.path3());

                currentState = RobotAutoState.done;
                break;

            case done:
                // Autonomous sequence complete
                break;
        }
    }


    public void shoot(){
        // TODO: Add shooting mechanism here.
    }



    @Override
    public void init() {
        follower = Constants.create(hardwareMap);


        assert follower != null;
        follower.setPose(start);
    }

    // |||
    // VVV  I assume this function is called manually, and at the same time that the "start" state is triggered.  But idk.
    @Override
    public void start() {
        //the line below is the original.  I've changed it to use path1() from the AutoPath script.
        //follower.follow(path1());
        follower.follow(autoPath.path1());
    }

    @Override
    public void loop() {

        // Keep the path follower running
        follower.update();

        // Check the autonomous state
        updateState();
        //executeState();


        telemetry.addData("State", currentState);
        telemetry.addData("Follower Busy", follower.isBusy());
        telemetry.update();
    }
}
