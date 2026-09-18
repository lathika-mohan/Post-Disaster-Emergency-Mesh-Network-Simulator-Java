#!/bin/bash
set -e
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
find src -name "*.java" > sources.txt
javac -d out @sources.txt
rm -f sources.txt
echo "Build complete -> out/"
