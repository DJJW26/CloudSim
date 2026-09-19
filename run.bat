@echo off
REM Windows equivalent of run.sh — compiles and runs all four experiments.
REM Usage: run.bat  (double-click, or run from cmd / PowerShell)
setlocal
cd /d %~dp0

echo Compiling...
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -cp lib/cloudsim-3.0.3.jar -d out @sources.txt
del sources.txt
if errorlevel 1 (
  echo COMPILATION FAILED
  exit /b 1
)

echo.
echo ==========================================
echo Running baseline (NonPowerAware)...
echo ==========================================
java -cp "lib/cloudsim-3.0.3.jar;out" org.cloudbus.cloudsim.examples.power.random.NonPowerAware

echo.
echo ==========================================
echo Running RoundRobin (power-unaware cyclic)...
echo ==========================================
java -cp "lib/cloudsim-3.0.3.jar;out" org.cloudbus.cloudsim.examples.power.random.RoundRobin

echo.
echo ==========================================
echo Running PABFD (power-aware best-fit, no migration)...
echo ==========================================
java -cp "lib/cloudsim-3.0.3.jar;out" org.cloudbus.cloudsim.examples.power.random.Pabfd

echo.
echo ==========================================
echo Running MBFD (ThrMmt: Static Threshold + Minimum Migration Time)...
echo ==========================================
java -cp "lib/cloudsim-3.0.3.jar;out" org.cloudbus.cloudsim.examples.power.random.ThrMmt
