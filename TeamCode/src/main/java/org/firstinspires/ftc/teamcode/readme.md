This is the code for the taem 39013 

How to create opmods:
create file 
at the top do @TeleOp
and then make the class extend OpMode or LinearOpMode
create methods

Code



11/11/2026:

Auto code is in pedro/ auto.java

11/15/2026:

Code layout (same structure as team 34161):
- constants/: HardwareNames (robot config names), RobotSpecs (measurements), TuningConstants (numbers to tune), Competition, OpModeMethods (what OpModeUtils inits, updates and stops)
- robotControllers/: one static controller per mechanism with init, update and stop
  - MecanumDriveTrainController: drive with 25/50/75/100% speed levels
  - SingleIntakeController: intake motor
  - UptakeController: uptake (transfer) motor
  - FlywheelController: shooter flywheel velocity control
  - UptakeServoController: kicker servo, pushes the ball into the shooter for a set time
  - TurretUpAngleController: hood servo
  - TurretController: the two turret servos
  - LimelightController: Limelight 3A and its tilt servo
- opmodes/: TeleOpMain, RedAuto and BlueAuto (both run AutoMain), plus one small test OpMode per mechanism
- auto/: OptimizedPaths, the best auto from sim/paths (blue is red rotated around the field center)
- utils/: Timer, TimedTelemetry, OpModeUtils, UtilMethods, VectorMath, AutoAim (turret angle, flywheel speed and hood angle from the robot position)
- pedro/: Constants (follower setup) and Tuning (tuners on the Pedro tuning page)

Every OpMode calls OpModeUtils in init, every loop, and in stop.

TeleOp Main controls:
- Gamepad 1: left stick drives and strafes, right stick x turns, R1 faster, L1 slower
- Gamepad 2: left stick x turret, R2 spins up the flywheel, Cross fires the kicker once the flywheel is at speed, L2 runs intake and uptake in, L1 reverses the intake, dpad up/down moves the hood, R1 turns auto aim on and off, dpad left/right flips which CELL is up
- During init, gamepad 2 dpad left picks red and dpad right picks blue
- Auto aim turns the turret with the Limelight when it sees our CELL, otherwise with odometry. With odometry it also sets the flywheel and hood from the shot table in TuningConstants

11/16/2026:

Limelight setup: make 4 AprilTag pipelines and set the ID filter on each one
- pipeline 0: red audience CELL, tags 34-37
- pipeline 1: red far CELL, tags 30-33
- pipeline 2: blue audience CELL, tags 38-41
- pipeline 3: blue far CELL, tags 42-45

Turret (from the team CAD): a 48 tooth ring gear turned by two 10 tooth servo gears, so 4.8:1. Set TURRET_SERVO_RANGE_DEGREES in RobotSpecs to the servo's own range (300 for a normal servo, 1800 for a 5 turn servo). With 300 the turret only turns 31 degrees each way, so auto faces the HIVE when it shoots and TeleOp tells the driver to turn when the turret cannot reach.

Drivetrain (from the team CAD): 15.6 x 15.6 in frame, 16.9 in long with the intake, 104 mm mecanum wheels 12.28 in apart side to side and 10.12 in front to back.

Auto: start against the alliance wall next to the garden. It shoots the preload, then picks up from the garden, the alliance FLOWER and the end FLOWER, shooting after each, and parks by 26.5 s. Numbers to tune are the AUTO_ values in TuningConstants. TeleOp keeps the robot position from the end of auto.

Before pushing run ./sim/compile.sh (compiles all of TeamCode against the real SDK) and ./sim/sim.sh test.
