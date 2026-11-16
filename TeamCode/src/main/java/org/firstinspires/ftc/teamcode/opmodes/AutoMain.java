package org.firstinspires.ftc.teamcode.opmodes;

import static com.pedropathing.api.Paths.curve;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.AUTO_PARK_BY_MS;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.AUTO_PATH_TIMEOUT_MS;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.AUTO_PICKUP_MS;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.AUTO_SHOOT_TIMEOUT_MS;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.AUTO_SHOTS_PER_VOLLEY;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.INTAKE_POWER;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.UPTAKE_POWER;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.auto.OptimizedPaths;
import org.firstinspires.ftc.teamcode.constants.Competition;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.robotControllers.FlywheelController;
import org.firstinspires.ftc.teamcode.robotControllers.SingleIntakeController;
import org.firstinspires.ftc.teamcode.robotControllers.TurretController;
import org.firstinspires.ftc.teamcode.robotControllers.TurretUpAngleController;
import org.firstinspires.ftc.teamcode.robotControllers.UptakeController;
import org.firstinspires.ftc.teamcode.robotControllers.UptakeServoController;
import org.firstinspires.ftc.teamcode.utils.AutoAim;
import org.firstinspires.ftc.teamcode.utils.OpModeUtils;
import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;

public abstract class AutoMain extends OpMode {
    public enum PathState {
        DRIVING,
        SHOOTING,
        INTAKING,
        DONE
    }

    private Follower follower;
    private OptimizedPaths paths;
    private Path[] pathList;
    private int pathIndex;
    private int shots;
    private PathState pathState;
    private final ElapsedTime pathTimer = new ElapsedTime();
    private final ElapsedTime opModeTimer = new ElapsedTime();

    protected abstract boolean isRed();

    @Override
    public void init() {
        OpModeUtils.initAll(hardwareMap, telemetry);
        paths = new OptimizedPaths(isRed());
        pathList = paths.paths();
        follower = Constants.create(hardwareMap);
        follower.setPose(paths.start);
        OpModeUtils.updateSome();
    }

    @Override
    public void start() {
        opModeTimer.reset();
        pathIndex = 0;
        follower.follow(pathList[0]);
        setPathState(PathState.DRIVING);
    }

    @Override
    public void loop() {
        follower.update();
        if (pathIndex < pathList.length - 1 && opModeTimer.milliseconds() > AUTO_PARK_BY_MS) park();
        statePathUpdate();

        TurretController.update();
        TurretUpAngleController.update();
        FlywheelController.update();
        UptakeServoController.update();

        TimedTelemetry.addData("Path State", pathState);
        TimedTelemetry.addData("Path", (pathIndex + 1) + " / " + pathList.length);
        TimedTelemetry.addData("Pose", follower.pose());
        OpModeUtils.updateSome();
        telemetry.update();
    }

    @Override
    public void stop() {
        Pose pose = follower.pose();
        Competition.lastAutoPose = new Pose2D(DistanceUnit.INCH, pose.x(), pose.y(), AngleUnit.RADIANS, pose.heading());
        follower.stop();
        OpModeUtils.stopAll();
    }

    public void statePathUpdate() {
        switch (pathState) {
            case DRIVING:
                aim();
                if (follower.isBusy() && pathTimer.milliseconds() < AUTO_PATH_TIMEOUT_MS) break;
                if (pathIndex == pathList.length - 1) setPathState(PathState.DONE);
                else if (pathIndex % 2 == 0) setPathState(PathState.SHOOTING);
                else setPathState(PathState.INTAKING);
                break;
            case SHOOTING:
                aim();
                if (shots < AUTO_SHOTS_PER_VOLLEY && FlywheelController.isAtSpeed() && UptakeServoController.shoot()) shots++;
                boolean volleyDone = shots >= AUTO_SHOTS_PER_VOLLEY && !UptakeServoController.isBusy();
                if (volleyDone || pathTimer.milliseconds() > AUTO_SHOOT_TIMEOUT_MS) nextPath();
                break;
            case INTAKING:
                if (pathTimer.milliseconds() > AUTO_PICKUP_MS) nextPath();
                break;
            case DONE:
                FlywheelController.setTargetVel(0);
                break;
        }
        SingleIntakeController.update(pathState == PathState.DONE ? 0 : INTAKE_POWER);
        UptakeController.update(pathState == PathState.SHOOTING ? UPTAKE_POWER : 0);
    }

    public void setPathState(PathState newState) {
        pathState = newState;
        pathTimer.reset();
        shots = 0;
    }

    private void nextPath() {
        pathIndex++;
        follower.follow(pathList[pathIndex]);
        setPathState(PathState.DRIVING);
    }

    private void park() {
        Pose here = follower.pose();
        pathIndex = pathList.length - 1;
        follower.follow(curve(here, paths.park).linear(here, paths.park));
        setPathState(PathState.DRIVING);
    }

    private void aim() {
        Pose pose = follower.pose();
        boolean farUp = (pathIndex >= 4) == isRed();
        double distance = AutoAim.distance(pose.x(), pose.y(), isRed(), farUp);
        TurretController.setYaw(AutoAim.turretYaw(pose.x(), pose.y(), Math.toDegrees(pose.heading()), isRed(), farUp));
        TurretUpAngleController.setTargetAngle(AutoAim.hoodAngle(distance));
        FlywheelController.setTargetVel(AutoAim.flywheelVelocity(distance));
    }
}
