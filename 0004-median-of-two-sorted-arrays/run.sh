#!/usr/bin/env bash
# Compile every solution and run the harness (JDK 17+).
#   ./run.sh               examples, edge cases, differential test, timing
#   ./run.sh --no-timing   skip the timing table (a few seconds instead of a minute)
#   ./run.sh 42            use seed 42 for the random cases
# Each solution stays a paste-ready LeetCode `class Solution`; this script copies them into
# build/ under distinct class names so the harness can load them side by side.
set -euo pipefail
cd "$(dirname "$0")"
rm -rf build && mkdir -p build/src build/classes
for s in brute better optimal modern kth value; do
  name="$(tr '[:lower:]' '[:upper:]' <<< "${s:0:1}")${s:1}"
  sed "s/^class Solution /class $name /" "solutions/$s/Solution.java" > "build/src/$name.java"
done
cp solutions/sharded/ShardedMedian.java homework/KthSmallest.java Harness.java build/src/
javac -Xlint:all -Werror -d build/classes build/src/*.java
java -cp build/classes Harness "$@"
