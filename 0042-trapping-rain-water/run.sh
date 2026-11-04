#!/usr/bin/env bash
# Compile every solution and run the harness. Needs JDK 24+ (Gatherers); the video's timings were measured on JDK 25,
# the version LeetCode runs.  ./run.sh [--no-timing] [seed]
set -euo pipefail
cd "$(dirname "$0")"
JBIN=${JAVA_HOME:+$JAVA_HOME/bin/}
ver() { "${1}javac" -version 2>&1 | grep '^javac' | grep -o '[0-9][0-9]*' | head -1; }
[ "$(ver "$JBIN")" -ge 24 ] 2>/dev/null || JBIN=""
[ -z "$JBIN" ] && [ -d /usr/lib/jvm/java-25-openjdk-amd64 ] && JBIN=/usr/lib/jvm/java-25-openjdk-amd64/bin/
v=$(ver "$JBIN")
[ "$v" -ge 24 ] || { echo "need JDK 24+ (found $v); set JAVA_HOME"; exit 1; }
rm -rf build && mkdir -p build/src build/classes build/java8
# every paste-ready LeetCode file must also compile at the Java 8 language level
for dir in solutions/brute solutions/better solutions/optimal solutions/stack solutions/modern solutions/grid solutions/grid-modern homework; do
  out="build/java8/$(basename $dir)"
  "${JBIN}javac" --release 8 -Xlint:all,-options -Werror -d "$out" "$dir/Solution.java" || true
  [ -f "$out/Solution.class" ] || { echo "$dir/Solution.java does not compile for Java 8"; exit 1; }
done
for pair in brute:Brute better:Better optimal:Optimal stack:StackInts modern:Modern grid:Grid grid-modern:GridModern; do
  dir=${pair%%:*}; name=${pair##*:}
  sed "s/^class Solution /class $name /" "solutions/$dir/Solution.java" > "build/src/$name.java"
done
sed "s/^class Solution /class Container /" homework/Solution.java > build/src/Container.java
cp solutions/sharded/*.java solutions/variants/*.java Harness.java build/src/
"${JBIN}javac" -Xlint:all -Werror -d build/classes build/src/*.java
"${JBIN}java" -cp build/classes Harness "$@"
# Java 25 preview: structured concurrency (compiled separately with --enable-preview)
"${JBIN}javac" --release 25 --enable-preview -Xlint:-preview -cp build/classes -d build/classes solutions/preview/ScopedFanOut.java
"${JBIN}java" --enable-preview -cp build/classes ScopedFanOut
