#!/bin/sh
cd "$(dirname "$0")/.." || exit 1

if javac -version >/dev/null 2>&1; then BIN=$(dirname "$(command -v javac)")
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/javac" ]; then BIN="$JAVA_HOME/bin"
elif [ -x /opt/homebrew/opt/openjdk/bin/javac ]; then BIN=/opt/homebrew/opt/openjdk/bin
else echo "Need a JDK (Java 11 or newer) installed"; exit 1
fi

if [ ! -s .deps/downloads/pedro-tuning.aar ] || [ ! -s .deps/downloads/ftc-RobotCore.aar ]; then
    ./compile.sh || exit 1
fi

MAVEN=https://repo1.maven.org/maven2
LIBS=.deps/tuning
mkdir -p "$LIBS"

fetch() {
    if [ ! -s "$LIBS/$2.jar" ]; then
        echo "Downloading $2"
        if ! curl -fsSL -o "$LIBS/$2.jar.part" "$MAVEN/$1"; then
            rm -f "$LIBS/$2.jar.part"
            echo "Could not download $1"
            exit 1
        fi
        mv "$LIBS/$2.jar.part" "$LIBS/$2.jar"
    fi
}

fetch org/nanohttpd/nanohttpd/2.3.1/nanohttpd-2.3.1.jar nanohttpd
fetch org/nanohttpd/nanohttpd-websocket/2.3.1/nanohttpd-websocket-2.3.1.jar nanohttpd-websocket
fetch com/aventrix/jnanoid/jnanoid/2.0.0/jnanoid-2.0.0.jar jnanoid

for lib in gson-2.8.0 gson-extras-0.2.1; do
    if [ ! -s "$LIBS/$lib.jar" ]; then
        unzip -p .deps/downloads/ftc-RobotCore.aar "libs/$lib.jar" > "$LIBS/$lib.jar.part" && mv "$LIBS/$lib.jar.part" "$LIBS/$lib.jar"
    fi
done

CP=.deps/jars/pedro-core.jar:.deps/jars/pedro-revhub.jar:.deps/jars/pedro-tuning.jar
CP=$CP:$LIBS/nanohttpd.jar:$LIBS/nanohttpd-websocket.jar:$LIBS/jnanoid.jar:$LIBS/gson-2.8.0.jar:$LIBS/gson-extras-0.2.1.jar

rm -rf out/tuning
mkdir -p out
"$BIN/javac" -nowarn -encoding UTF-8 -d out/tuning -cp "$CP" -sourcepath src:../TeamCode/src/main/java \
    $(find stubs tuning/src -name '*.java') || exit 1

case "$1" in
    check) "$BIN/java" -cp "out/tuning:$CP" tuning.TunerCheck ;;
    auto) "$BIN/java" -cp "out/tuning:$CP" tuning.AutoTiming ;;
    *)
        (sleep 2 && open "http://localhost:10158" 2>/dev/null || xdg-open "http://localhost:10158" 2>/dev/null) &
        "$BIN/java" -cp "out/tuning:$CP" tuning.TunerSim
        ;;
esac
