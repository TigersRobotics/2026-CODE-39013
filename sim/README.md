# Robot Sim

Runs our real TeamCode (`TeleOpMain` and the mechanism controllers) on a simulated robot on the BIOBUZZ field, so we can test driving and the operator controls without the robot.

Needs a JDK (Java 11 or newer). No Android Studio needed.

```
./sim/sim.sh          # start the sim, opens http://localhost:8039
./sim/sim.sh test     # run the sim tests
./sim/compile.sh      # compile ALL of TeamCode against the real FTC SDK and Pedro jars
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
| K | gamepad 2 R2 | spin up the flywheel |
| Space | gamepad 2 Cross | fire the kicker (only when the flywheel is at speed) |
| I | gamepad 2 L2 | intake and uptake in |
| U | gamepad 2 L1 | reverse the intake |
| Up / Down arrows | gamepad 2 dpad | hood angle |
| O | gamepad 2 R1 | Limelight auto aim on/off |
| R | | reset robot |

## How it works

`stubs/` has fake versions of the FTC SDK classes our code uses, so the real files compile on a laptop. `src/sim/` gives them fake motors, servos and a fake Limelight, turns wheel powers into robot movement, and runs the OpMode at 50 Hz like the robot does. `web/index.html` draws the field and reads the controller.

- Field layout and sizes are from the BIOBUZZ Competition Manual (`Field.java`). Robots can drive under the HIVE but hit its legs, the FLOWERS, and the walls.
- Robot specs (size, wheels, motor rpm) are at the top of `SimRobot.java`. Update them when the real robot is measured.
- The fake Limelight sees our alliance's HIVE when it is inside the camera's field of view and reports `tx` like the real one.
- The flywheel spins up toward the velocity the code asks for. It only holds speed in `RUN_USING_ENCODER` with nonzero PIDF.
- The sim has its own clock, so timers (kicker, telemetry) run the same in tests as on the robot.
- When TeamCode uses a new SDK class, add a stub for it and add the file to `sim.sh`.

The sim checks that the logic is right. It is not exact enough for tuning.

## Real compile check

`compile.sh` downloads the FTC SDK 12.0.0, Pedro Pathing and Android API 30 jars into `sim/.deps/` (ignored by git) the first time, then runs `javac` over every TeamCode file. Run it before pushing. `android-stubs/` holds the one class the Android jar leaves out (`LambdaMetafactory`), the same thing Android Studio adds.
