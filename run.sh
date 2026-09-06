#!/bin/bash
# Compiles the project and runs all experiments:
# baseline + RoundRobin + PABFD + MBFD (ThrMmt).
# Usage: ./run.sh

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "Compiling..."
mkdir -p out
javac -cp lib/cloudsim-3.0.3.jar -d out $(find src -name "*.java")

echo ""
echo "=========================================="
echo "Running baseline (NonPowerAware)..."
echo "=========================================="
java -cp lib/cloudsim-3.0.3.jar:out org.cloudbus.cloudsim.examples.power.random.NonPowerAware

echo ""
echo "=========================================="
echo "Running RoundRobin (power-unaware cyclic)..."
echo "=========================================="
java -cp lib/cloudsim-3.0.3.jar:out org.cloudbus.cloudsim.examples.power.random.RoundRobin

echo ""
echo "=========================================="
echo "Running PABFD (power-aware best-fit, no migration)..."
echo "=========================================="
java -cp lib/cloudsim-3.0.3.jar:out org.cloudbus.cloudsim.examples.power.random.Pabfd

echo ""
echo "=========================================="
echo "Running MBFD (ThrMmt: Static Threshold + Minimum Migration Time)..."
echo "=========================================="
java -cp lib/cloudsim-3.0.3.jar:out org.cloudbus.cloudsim.examples.power.random.ThrMmt
