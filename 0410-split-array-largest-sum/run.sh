#!/usr/bin/env bash
# Compile every solution and run the harness (JDK 25, what LeetCode runs).  ./run.sh [--no-timing] [seed]
set -euo pipefail
cd "$(dirname "$0")"
JBIN=${JAVA_HOME:+$JAVA_HOME/bin/}
ver() { "${1}javac" -version 2>&1 | grep '^javac' | grep -o '[0-9][0-9]*' | head -1; }
[ "$(ver "$JBIN")" -ge 25 ] 2>/dev/null || JBIN=""
[ -z "$JBIN" ] && [ -d /usr/lib/jvm/java-25-openjdk-amd64 ] && JBIN=/usr/lib/jvm/java-25-openjdk-amd64/bin/
v=$(ver "$JBIN")
[ "$v" -ge 25 ] || { echo "need JDK 25 (found $v); set JAVA_HOME"; exit 1; }
rm -rf build && mkdir -p build/src build/classes build/lc
for dir in solutions/brute solutions/dp solutions/dpfast solutions/optimal solutions/modern homework; do   # each LeetCode file alone, Java 25, no warnings
  "${JBIN}javac" --release 25 -Xlint:all -Werror -d "build/lc/$(basename $dir)" "$dir/Solution.java"
done
for pair in brute:Brute dp:Dp dpfast:DpFast optimal:Optimal modern:Modern; do
  dir=${pair%%:*}; name=${pair##*:}
  sed "s/^class Solution /class $name /" "solutions/$dir/Solution.java" > "build/src/$name.java"
done
sed "s/^class Solution /class Koko /" homework/Solution.java > build/src/Koko.java
cp solutions/sharded/*.java solutions/variants/*.java Harness.java build/src/
"${JBIN}javac" --release 25 -Xlint:all -Werror -d build/classes build/src/*.java
"${JBIN}java" -Dstdout.encoding=UTF-8 -cp build/classes Harness "$@"
