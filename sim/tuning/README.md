# Pedro Tuner Sim

Runs the real Pedro Pathing AutoTune website and our tuners (`TeamCode/.../pedro/Tuning.java`) on a simulated robot, so we can practice tuning without the robot.

```
./sim/tuning/tune.sh          # start it, opens http://localhost:10158
./sim/tuning/tune.sh check    # run every tuner from a script and print the numbers
./sim/tuning/tune.sh auto     # run Red Auto with the Pedro follower and print how long each path takes
```

Needs a JDK (Java 11 or newer). The first run downloads NanoHTTPD, its websocket add-on and jnanoid into `sim/.deps/tuning/` (ignored by git). Gson comes out of the FTC RobotCore AAR, the same one the robot uses.

## How it works

- The website is not a copy. It is read straight out of `sim/.deps/downloads/pedro-tuning.aar`, and it is served by Pedro's own `WebServer` (port 10158) and `WebSocketServer` (port 12649), started the same way the robot app starts them.
- When a tuner runs an OpMode, `SimOpModeManager` does what the Driver Station and robot controller do: inits it, starts it on its own thread, and stops it when you press Stop.
- `TuningRobot` is the drivetrain physics: motor torque drops with speed, BRAKE shorts the motor and stops hard, FLOAT coasts on friction, strafing is weaker than driving forward, the robot has mass and spin inertia. Everything runs in real time because Pedro uses the real clock.
- `PodPinpoint` acts like the goBILDA Pinpoint: two pods at real offsets, encoder directions, pod resolution, and the IMU heading.

## Things a person does on the real robot

- Pinpoint Tuner: the sim pushes the robot for you (24 in forward, 24 in left, then a 180 degree spin). Wait for the console to say it pushed, then press Stop OpMode.
- Mecanum Tuner: you can't see the wheel, so the console prints which wheel spun and which way. Answer the question on the website with that.
- Driving and Localization tests: there is no controller, so the robot just sits still.

## Robot numbers

Mass, motor torque, strafe efficiency, grip and friction are at the top of `TuningRobot.java`. Size, track width, wheelbase, wheel size and motor rpm come from `sim/src/sim/SimRobot.java`. The Pinpoint pod offsets come from `RobotSpecs`.

The numbers the sim gives are only as good as these guesses. Use them to learn the steps and to sanity check the real tuning, not instead of tuning the real robot.
