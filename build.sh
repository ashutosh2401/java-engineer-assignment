#!/usr/bin/env bash
# Tasks 1-4 are plain JDK. Task 5 uses SLF4J
# slf4j-api is bundled under lib/ so no Maven/network access is needed.
set -euo pipefail

OUT="out"
rm -rf "$OUT"
mkdir -p "$OUT"

# Tasks 1-4 - no extra classpath
javac -d "$OUT" Task1.java Task2Analysis.java Task3.java Task4.java

# Task 5 - needs slf4j-api on the classpath
javac -cp "lib/slf4j-api-2.0.13.jar" -d "$OUT" Task5.java

echo "Compiled OK -> $OUT/"
