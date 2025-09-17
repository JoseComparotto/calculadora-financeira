#!/usr/bin/env bash
set -euo pipefail
# Build CLI and dependencies quietly, skipping integration tests
mvn -q -DskipITs -pl calculadora-financeira-cli -am package
# Run the shaded executable jar
java -jar calculadora-financeira-cli/target/calculadora-financeira-cli-*-shaded.jar
