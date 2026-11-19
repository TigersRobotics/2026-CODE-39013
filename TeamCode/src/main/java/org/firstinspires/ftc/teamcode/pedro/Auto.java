
package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.api.Paths;
import com.pedropathing.paths.Path;
import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.robotControllers.ShootingSys;

@Autonomous
public class Auto extends OpMode {
    ShootingSys shootingSys;
    private ElapsedTime shootTimer;
    AutoPath autoPath;// this comes from the autopathing visualizer which returns vector like paths which we use to construct events
    //I seperated this class out to organize

    private Follower follower;

    public enum RobotAutoState { // the most basic states i've put in based off of the pedro pathing generated
        start,
        shoot,
        p2,
        p3,
        done
    }

    // This stores the CURRENT state
    private RobotAutoState currentState = RobotAutoState.start;

    private void startShoot(){
        // TODO: Add shooting mechanism here.
        shootingSys.shootSetPower(100);
        shootingSys.intakeSetPower(100);
    }
    private void stopShoot(){
        // TODO: Add shooting mechanism here.
        shootingSys.shootSetPower(0);
        shootingSys.intakeSetPower(0);
    }


    public void pickup(){
        // TODO: Add pickup mechanism here.
    }


    public void updateState() { //updating function so essentially decides which path the robot should be following in the currrent time
        switch (currentState) {

            case start:
                // Wait until path1 is finished
                if (!follower.isBusy()) {//is not busy means it isn't currently following a path
                    currentState = RobotAutoState.shoot; // to position 2 not at position 2, like two vectors

                }
                break;


            case shoot://at p1
                startShoot();  // Your function sets both motors' power

                if (shootTimer.seconds() >= 3.0) {
                    stopShoot();  // Set both motors' power to 0
                    currentState = RobotAutoState.p2;
                    follower.follow(autoPath.path2());

                }
                break;

            case p2: // position from p1 to p2


                if (!follower.isBusy()) {
                    currentState = RobotAutoState.p3;
                    follower.follow(autoPath.path3());
                }



                break;

            case p3:
                // Wait until path2 is finished
                if (!follower.isBusy()) {

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

        autoPath = new AutoPath();
        shootingSys = new ShootingSys(hardwareMap,telemetry);
        follower.setPose(autoPath.returnStartPose());//sets the initial position of the robot
        shootTimer = new ElapsedTime();
    }

    // |||
    // VVV  I assume this function is called manually, and at the same time that the "start" state is triggered.  But idk.
    @Override
    public void start() {
        //the line below is the original.  I've changed it to use path1() from the AutoPath script.
        //follower.follow(path1());
        follower.follow(autoPath.path1()); //init to follow the first avaible path
    }

    @Override
    public void loop() {

        // Keep the path follower running
        follower.update(); // the most important part of the code, which helps coordinate the followers movement according to its current designated path

        // Check the autonomous state
        updateState();
        //executeState();


        telemetry.addData("State", currentState);
        telemetry.addData("Follower Busy", follower.isBusy());
        telemetry.update();
    }
}
