#!/bin/bash

JAVA_OPTS="-server -Xms1G -Xmx1G -XX:+AggressiveOpts -XX:CompileThreshold=200 -XX:+UseConcMarkSweepGC -Dfile.encoding=utf8"

java $JAVA_OPTS -jar target/Unite-0.1-SNAPSHOT-jar-with-dependencies.jar $@
