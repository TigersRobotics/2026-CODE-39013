# Robot Sim

Runs our real TeamCode (`ControllerTest`, `MecanumDriveTrainController`, `TurretController`) on a simulated robot on the BIOBUZZ field, so we can test driving without the robot.

Needs a JDK (Java 11 or newer). No Android Studio needed.

```
./sim/sim.sh          # start the sim, opens http://localhost:8039
./sim/sim.sh test     # run the drive tests
```

## Controllers

Plug in a PS5 controller (USB or Bluetooth) and press any button. The first controller is gamepad 1, the second is gamepad 2, same as on the Driver Station. Buttons map the same way the FTC SDK maps them (Cross = a, Circle = b, Square = x, Triangle = y, L1 = left bumper, R1 = right bumper).

With no controller, the keyboard works:

| Key | Input | Does |
|---|---|---|
| W A S D | gamepad 1 left stick | drive and strafe |
| Left / Right arrows | gamepad 1 right stick x | turn |
| E | gamepad 1 R1 | faster |
| Q | gamepad 1 L1 | slower |
| J / L | gamepad 2 left stick x | turret |
| R | | reset robot |

## How it works

`stubs/` has fake versions of the FTC SDK classes our code uses, so the real files compile on a laptop. `src/sim/` gives them fake motors and servos, turns wheel powers into robot movement, and runs the OpMode at 50 Hz like the robot does. `web/index.html` draws the field and reads the controller.

- Field layout and sizes are from the BIOBUZZ Competition Manual (`Field.java`). Robots can drive under the HIVE but hit its legs, the FLOWERS, and the walls.
- Robot specs (size, wheels, motor rpm) are at the top of `SimRobot.java`. Update them when the real robot is measured.
- When TeamCode uses a new SDK class, add a stub for it and add the file to `sim.sh`.

The sim checks that the logic is right. It is not exact enough for tuning.
