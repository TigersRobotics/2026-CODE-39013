# Robot Sim

Runs our real TeamCode (`ControllerTest`, `MecanumDriveTrainController`, `TurretController`) on a fake robot so we can test driving without the robot.

Needs a JDK (Java 11 or newer). No Android Studio needed.

```
./sim/sim.sh          # open the sim window
./sim/sim.sh test     # run the drive tests
```

## Controls

| Key | Gamepad input |
|---|---|
| W / S | gamepad 1 left stick up / down (drive) |
| A / D | gamepad 1 left stick left / right (strafe) |
| Left / Right arrows | gamepad 1 right stick x (turn) |
| E | gamepad 1 right bumper (faster) |
| Q | gamepad 1 left bumper (slower) |
| J / L | gamepad 2 left stick x (turret) |
| R | reset robot |

## How it works

`stubs/` has fake versions of the FTC SDK classes our code uses, so the real files compile on a laptop. `src/sim/` gives them fake motors and servos and turns wheel powers into robot movement. When TeamCode uses a new SDK class, add a stub for it here and add the file to `sim.sh`.

Physics numbers (top speed, field layout) are estimates, so use the sim to check that the logic is right, not for exact tuning.
