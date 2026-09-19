# Energy-Aware VM Placement (RoundRobin vs PABFD vs MBFD) — CloudSim Implementation

Implementation of Beloglazov, Abawajy & Buyya (2012), *"Energy-aware resource
allocation heuristics for efficient management of data centers for Cloud
computing,"* Future Generation Computer Systems, 28(5), 755-768 — using
**CloudSim 3.0.3**, the actual simulation toolkit the authors used.

## How to run

Requires JDK 8+ (any recent JDK works). No Maven, no internet access needed —
`lib/cloudsim-3.0.3.jar` is a pre-built jar, bundled directly.

```bash
bash run.sh        # Linux / macOS / Git Bash
```

```bat
run.bat            REM Windows (double-click, or .\run.bat from PowerShell)
```

This compiles everything and runs all four experiments back to back, printing
results to the console. Takes under a minute.

## What this actually is

CloudSim ships official example code implementing exactly this paper's
algorithm (`org.cloudbus.cloudsim.power` package + the
`examples/power/random` example scenario). Rather than reimplementing the
framework's VM/Host/Datacenter machinery from scratch, I used CloudSim's own
reference classes directly and only changed the scenario **scale** to keep
runtime and complexity appropriate for a course assignment. Specifically:

- **`RandomConstants.java`** — the only file with parameters I changed:
  `NUMBER_OF_HOSTS` (50 → 10) and `NUMBER_OF_VMS` (50 → 16). Reduced from
  CloudSim's default 50/50 scenario. These particular numbers aren't
  arbitrary — the host/VM type MIPS values are fixed in `Constants.java`
  (real EC2 instance types vs. real HP ProLiant server specs), so the host
  and VM counts had to stay in a ratio where every VM can actually fit
  somewhere; smaller ratios (e.g. 8 hosts / 20 VMs) fail with "no suitable
  host" errors because too many large VM types compete for too few
  high-capacity hosts.
- **Every other file is unmodified CloudSim example source** —
  `Constants.java`, `Helper.java`, `RunnerAbstract.java`,
  `RandomHelper.java`, `RandomRunner.java`, `ThrMmt.java`,
  `NonPowerAware.java`.
- **New files written from scratch for this comparison** —
  `PowerVmAllocationPolicyRoundRobin.java` (cyclic placement, ignores power),
  `PowerVmAllocationPolicyPabfd.java` (places each VM where the estimated
  power increase `powerAfter − powerBefore` is minimal, skipping hosts that
  would exceed 100% utilization; no migration), plus the `RoundRobin.java`
  and `Pabfd.java` experiment mains that run them on a `PowerDatacenter`
  with migrations disabled. CloudSim 3.0.3 ships no PABFD policy
  (`PowerVmAllocationPolicySimple` is just first-fit), so PABFD had to be
  implemented — that part is genuinely your own code.

**Be upfront about this in your report and to your professor**: this is
CloudSim's own reference implementation of the paper's algorithm, with the
scenario scaled down — not code written from scratch. That's a legitimate
and common way to work with CloudSim (nobody reimplements the Datacenter/
Host/Vm/Cloudlet event machinery from scratch), but say so explicitly rather
than implying otherwise.

## The four experiments

| | Baseline (`NonPowerAware.java`) | Round-Robin (`RoundRobin.java`) | PABFD (`Pabfd.java`) | MBFD (`ThrMmt.java`) |
|---|---|---|---|---|
| VM allocation | `PowerVmAllocationPolicySimple` (first-fit) | `PowerVmAllocationPolicyRoundRobin` (cyclic, power-unaware) | `PowerVmAllocationPolicyPabfd` (min power increase, static) | `PowerVmAllocationPolicyMigrationStaticThreshold` (0.8 threshold) |
| VM selection (who to migrate) | n/a — migrations disabled | n/a — migrations disabled | n/a — migrations disabled | `PowerVmSelectionPolicyMinimumMigrationTime` |
| Consolidation | None — hosts stay at max power regardless of load | None — static cyclic placement | None — static power-aware placement | Dynamic — overloaded/underloaded hosts trigger migration |

`PowerVmAllocationPolicyMigrationStaticThreshold` + `MinimumMigrationTime` is
literally the paper's MBFD algorithm as CloudSim's own authors implemented
it: VM placement by least power increase, overload detection via a static
utilization threshold, and migrating the VM that takes least time to move
first when a host is overloaded.

## Results obtained (see `output_logs/` for full console output)

| Metric | Baseline | Round-Robin | PABFD | MBFD |
|---|---|---|---|---|
| Energy consumption | 30.14 kWh | 21.65 kWh (**28.2% saved**) | 15.50 kWh (**48.6% saved**) | 12.76 kWh (**57.7% saved**) |
| VM migrations | 0 | 0 | 0 | 1,298 |
| Overall SLA violation | 0.00% | 0.00% | 0.00% | 3.00% |
| Host shutdowns | 3 | 1 | 4 | 368 |

`cloudsim_results_summary.png` charts energy, migrations, and SLA side by side.

Reading the comparison in two steps for your report:

1. **Round-Robin → PABFD (28.2% → 48.6% saved)** is the pure *placement*
   win: identical setup (no migration, zero SLA violation), but best-fit by
   minimum power increase packs VMs onto efficient hosts instead of spreading
   them evenly.
2. **PABFD → MBFD (48.6% → 57.7% saved)** is the *consolidation* win: same
   placement logic, but static-threshold overload detection + minimum-migration-
   time selection migrates VMs off underloaded hosts so they can shut down
   (4 vs 368 shutdowns) — at the cost of 1,298 migrations and a ~3% SLA
   violation.

Worth noting for your report: this is close to, but not identical to, the
54.3% energy saving from the from-scratch Python simulation (if you built
that too) — two independent implementations of the same algorithm landing in
the same ballpark is a good cross-validation point. The small SLA violation
under MBFD (vs. 0% for baseline) is exactly the energy-vs-SLA trade-off the
paper describes: consolidating more aggressively saves energy but
occasionally overloads a host before the next migration cycle catches it.

## Files

```
lib/cloudsim-3.0.3.jar   Pre-built CloudSim jar (official release, no Maven needed)
src/                     Java source (see breakdown above)
output_logs/             Console output from all four experiment runs
cloudsim_results_summary.png   Comparison chart (Baseline vs RR vs PABFD vs MBFD)
run.sh                   Compiles and runs all four experiments (Linux/macOS)
run.bat                  Same as run.sh, for Windows (no bash/WSL needed)
Assignment/              Assignment report (.docx) + presentation (.pptx)
```
