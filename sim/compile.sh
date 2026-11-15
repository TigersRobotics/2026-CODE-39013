#!/bin/sh
cd "$(dirname "$0")" || exit 1

if javac -version >/dev/null 2>&1; then BIN=$(dirname "$(command -v javac)")
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/javac" ]; then BIN="$JAVA_HOME/bin"
elif [ -x /opt/homebrew/opt/openjdk/bin/javac ]; then BIN=/opt/homebrew/opt/openjdk/bin
else echo "Need a JDK (Java 11 or newer) installed"; exit 1
fi

FTC_VERSION=12.0.0
PEDRO_VERSION=3.0.0
TUNING_VERSION=1.0.0
ANDROID_PLATFORM=platform-30_r03.zip
ANDROID_PLATFORM_SHA1=e7c6280901dcfa511af098d67dd88c4dfcbc6ea2

MAVEN=https://repo1.maven.org/maven2
GOOGLE=https://dl.google.com/dl/android/maven2
DEPS=.deps
DOWNLOADS=$DEPS/downloads
JARS=$DEPS/jars
mkdir -p "$DOWNLOADS" "$JARS"

fetch() {
    if [ ! -s "$2" ]; then
        echo "Downloading $1"
        if ! curl -fsSL -o "$2.part" "$1"; then
            rm -f "$2.part"
            echo "Could not download $1"
            exit 1
        fi
        mv "$2.part" "$2"
    fi
}

get_jar() {
    fetch "$1" "$JARS/$2.jar"
}

get_aar() {
    fetch "$1" "$DOWNLOADS/$2.aar"
    if [ ! -s "$JARS/$2.jar" ]; then
        unzip -p "$DOWNLOADS/$2.aar" classes.jar > "$JARS/$2.jar.part" && mv "$JARS/$2.jar.part" "$JARS/$2.jar"
    fi
}

sha1() {
    if command -v shasum >/dev/null 2>&1; then shasum -a 1 "$1" | cut -d ' ' -f 1
    else sha1sum "$1" | cut -d ' ' -f 1
    fi
}

for lib in RobotCore Hardware FtcCommon Vision RobotServer Inspection; do
    get_aar "$MAVEN/org/firstinspires/ftc/$lib/$FTC_VERSION/$lib-$FTC_VERSION.aar" "ftc-$lib"
done
get_aar "$MAVEN/org/openftc/easyopencv/1.7.3/easyopencv-1.7.3.aar" openftc-easyopencv
get_aar "$MAVEN/org/openftc/apriltag/2.1.0/apriltag-2.1.0.aar" openftc-apriltag
get_aar "$MAVEN/org/openftc/opencv-repackaged-bundled-dylibs/4.10.0-A/opencv-repackaged-bundled-dylibs-4.10.0-A.aar" openftc-opencv

get_jar "$MAVEN/com/pedropathing/core/$PEDRO_VERSION/core-$PEDRO_VERSION.jar" pedro-core
get_aar "$MAVEN/com/pedropathing/revhub/$PEDRO_VERSION/revhub-$PEDRO_VERSION.aar" pedro-revhub
get_aar "$MAVEN/com/pedropathing/tuning/$TUNING_VERSION/tuning-$TUNING_VERSION.aar" pedro-tuning
get_jar "$MAVEN/org/jetbrains/kotlin/kotlin-stdlib/2.3.21/kotlin-stdlib-2.3.21.jar" kotlin-stdlib
get_jar "$GOOGLE/androidx/annotation/annotation/1.2.0/annotation-1.2.0.jar" androidx-annotation

if [ ! -s "$DEPS/android.jar" ]; then
    fetch "https://dl.google.com/android/repository/$ANDROID_PLATFORM" "$DOWNLOADS/$ANDROID_PLATFORM"
    if [ "$(sha1 "$DOWNLOADS/$ANDROID_PLATFORM")" != "$ANDROID_PLATFORM_SHA1" ]; then
        echo "$ANDROID_PLATFORM checksum does not match, delete $DOWNLOADS/$ANDROID_PLATFORM and try again"
        exit 1
    fi
    unzip -p "$DOWNLOADS/$ANDROID_PLATFORM" '*/android.jar' > "$DEPS/android.jar.part" && mv "$DEPS/android.jar.part" "$DEPS/android.jar"
fi

javac8() {
    "$BIN/javac" -source 8 -target 8 -Xlint:-options -nowarn -encoding UTF-8 -proc:none "$@"
}

rm -rf out/compile out/android-stubs
mkdir -p out/compile out/android-stubs
javac8 -bootclasspath "$DEPS/android.jar" -d out/android-stubs $(find android-stubs -name '*.java') || exit 1

CLASSPATH=$(find "$JARS" -name '*.jar' | sort | tr '\n' ':')
find ../TeamCode/src/main/java -name '*.java' > out/compile-sources.txt
COUNT=$(wc -l < out/compile-sources.txt | tr -d ' ')

if javac8 -bootclasspath "out/android-stubs:$DEPS/android.jar" -cp "$CLASSPATH" -d out/compile @out/compile-sources.txt; then
    echo "TeamCode compiles ($COUNT files)"
else
    echo "TeamCode does not compile"
    exit 1
fi
