#!/usr/bin/env bash
# Usage: ./run.sh [--cli|--gui]
set -e
MODE="${1:---gui}"
if [ -f target/mesh-sim.jar ]; then
    java --enable-preview -jar target/mesh-sim.jar "$MODE"
elif [ -d out ]; then
    java --enable-preview -cp out com.meshsim.Main "$MODE"
else
    echo "Nothing built yet. Run ./build.sh first."
    exit 1
fi
