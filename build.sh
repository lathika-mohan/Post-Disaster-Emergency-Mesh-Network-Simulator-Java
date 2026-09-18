#!/usr/bin/env bash
# Thin wrapper over Maven. Falls back to a manual javac/@sources.txt path if
# Maven is unavailable, per the syllabus's "compilation and execution process" bullet.
set -e
if command -v mvn &> /dev/null; then
    mvn -q package
    echo "Built target/mesh-sim.jar"
else
    echo "Maven not found - falling back to manual javac compilation."
    echo "Note: this path skips the sqlite-jdbc dependency; persistence classes will not compile."
    mkdir -p out
    find src/main/java -name "*.java" ! -path "*persistence*" > sources.txt
    javac -d out --release 21 --enable-preview @sources.txt
    echo "Compiled to ./out (persistence package excluded - needs sqlite-jdbc.jar on the classpath)"
fi
