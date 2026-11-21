#!/usr/bin/env bash
# smoke-test.sh: a quick check that the built app starts and answers the basics.
# Run after `mvn package` (CI runs it after `mvn verify`).
# No pipefail here: "grep -q" stops reading early, which must not count as a failure.
set -eu

JAR=target/ask-the-sops.jar
[ -f "$JAR" ] || { echo "Smoke test: $JAR not found. Run mvn package first."; exit 1; }

fail() { echo "Smoke test FAILED: $1"; exit 1; }

java -jar "$JAR" list | grep -q "SOP-CC-01" || fail "list does not show SOP-CC-01"
java -jar "$JAR" list | grep -q "retired" && fail "list shows a retired version"
java -jar "$JAR" show SOP-CC-01 | grep -q "Overnight excursions" || fail "show SOP-CC-01 is missing section 4.2"
java -jar "$JAR" search overnight excursion | head -n 1 | grep -q "SOP-CC-01" || fail "search did not rank SOP-CC-01 first"

echo "Smoke test passed."
