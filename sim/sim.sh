#!/bin/sh
cd "$(dirname "$0")" || exit 1

if javac -version >/dev/null 2>&1; then BIN=$(dirname "$(command -v javac)")
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/javac" ]; then BIN="$JAVA_HOME/bin"
elif [ -x /opt/homebrew/opt/openjdk/bin/javac ]; then BIN=/opt/homebrew/opt/openjdk/bin
else echo "Need a JDK (Java 11 or newer) installed"; exit 1
fi

TEAM=../TeamCode/src/main/java/org/firstinspires/ftc/teamcode
rm -rf out/sim
mkdir -p out
"$BIN/javac" -d out/sim $(find stubs src -name '*.java') \
    $TEAM/constants/*.java \
    $TEAM/utils/*.java \
    $TEAM/controller/*.java \
    $(find $TEAM/robotControllers -name '*.java' ! -name AprilTag.java) \
    $(find $TEAM/opmodes -name '*.java' ! -name OdometryTest.java) || exit 1

if [ "$1" = "test" ]; then
    "$BIN/java" -cp out/sim sim.SimTests
else
    (sleep 1 && open "http://localhost:8039" 2>/dev/null || xdg-open "http://localhost:8039" 2>/dev/null) &
    "$BIN/java" -cp out/sim sim.SimServer
fi
