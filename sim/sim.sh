#!/bin/sh
# ./sim.sh           start the sim and open it in the browser
# ./sim.sh test      run the drive tests
# Compiles the sim together with the real TeamCode files it runs
cd "$(dirname "$0")"

if javac -version >/dev/null 2>&1; then BIN=$(dirname "$(command -v javac)")
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/javac" ]; then BIN="$JAVA_HOME/bin"
elif [ -x /opt/homebrew/opt/openjdk/bin/javac ]; then BIN=/opt/homebrew/opt/openjdk/bin
else echo "Need a JDK (Java 11 or newer) installed"; exit 1
fi

TEAM=../TeamCode/src/main/java/org/firstinspires/ftc/teamcode
rm -rf out
"$BIN/javac" -d out $(find stubs src -name '*.java') \
    $TEAM/ControllerTest.java \
    $TEAM/utils/Constants.java \
    $TEAM/robotControllers/MecanumDriveTrainController.java \
    $TEAM/robotControllers/TurretController.java || exit 1

if [ "$1" = "test" ]; then
    "$BIN/java" -cp out sim.SimTests
else
    (sleep 1 && open "http://localhost:8039" 2>/dev/null || xdg-open "http://localhost:8039" 2>/dev/null) &
    "$BIN/java" -cp out sim.SimServer
fi
