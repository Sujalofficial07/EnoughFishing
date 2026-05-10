#!/bin/sh
#
# Gradle start up script for UN*X
#

DIRNAME=$(dirname "$0")
APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$DIRNAME" && pwd)

# Add default JVM options here if needed
DEFAULT_JVM_OPTS=""

CLASSPATH=$APP_HOME/android/gradle/wrapper/gradle-wrapper.jar

# Use JAVA_HOME if set, otherwise use java from PATH
if [ -n "$JAVA_HOME" ] ; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD="java"
fi

exec "$JAVA_CMD" $DEFAULT_JVM_OPTS -cp "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
