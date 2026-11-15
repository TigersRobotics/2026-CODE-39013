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
- opmodes/: TeleOpMain plus one small test OpMode per mechanism
- utils/: Timer, TimedTelemetry, OpModeUtils, UtilMethods, VectorMath
- pedro/: Constants (follower setup)

Every OpMode calls OpModeUtils in init, every loop, and in stop.

TeleOp Main controls:
- Gamepad 1: left stick drives and strafes, right stick x turns, R1 faster, L1 slower
- Gamepad 2: left stick x turret, R2 spins up the flywheel, Cross fires the kicker once the flywheel is at speed, L2 runs intake and uptake in, L1 reverses the intake, dpad up/down moves the hood, R1 turns Limelight auto aim on and off

Before pushing run ./sim/compile.sh (compiles all of TeamCode against the real SDK) and ./sim/sim.sh test.
