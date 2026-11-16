#!/bin/sh
cd "$(dirname "$0")" || exit 1
ROOT=$(cd ../.. && pwd)

if javac -version >/dev/null 2>&1; then BIN=$(dirname "$(command -v javac)")
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/javac" ]; then BIN="$JAVA_HOME/bin"
elif [ -x /opt/homebrew/opt/openjdk/bin/javac ]; then BIN=/opt/homebrew/opt/openjdk/bin
else echo "Need a JDK (Java 11 or newer) installed"; exit 1
fi

JAR=.deps/core-3.0.0.jar
if [ ! -f "$JAR" ]; then
    mkdir -p .deps
    curl -sSfL -o "$JAR.part" https://repo1.maven.org/maven2/com/pedropathing/core/3.0.0/core-3.0.0.jar \
        && mv "$JAR.part" "$JAR" || { echo "Could not download Pedro core 3.0.0"; exit 1; }
fi

rm -rf out
"$BIN/javac" -nowarn -cp "$JAR" -d out/opt src/paths/*.java || exit 1

compile_auto() {
    "$BIN/javac" -cp "$JAR" -d out/auto "$1/RedAutoPaths.java" "$1/BlueAutoPaths.java" || exit 1
    echo "RedAutoPaths.java and BlueAutoPaths.java compile against Pedro core 3.0.0"
}

case "$1" in
    quick)
        "$BIN/java" -cp "$JAR:out/opt" paths.Main quick "$ROOT" || exit 1
        compile_auto "$ROOT/sim/paths/out/quick/auto"
        ;;
    "")
        "$BIN/java" -cp "$JAR:out/opt" paths.Main full "$ROOT" || exit 1
        compile_auto "$ROOT/sim/paths/results/auto"
        ;;
    *)
        echo "usage: ./optimize.sh [quick]"
        exit 1
        ;;
esac
