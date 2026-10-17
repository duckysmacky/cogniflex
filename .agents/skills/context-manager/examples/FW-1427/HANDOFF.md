# Handoff

**Last updated:** 2026-07-31 13:31 by firmware investigation agent

## Current state

The PCI bus-number shortage is solved with two segments and topology-aware I/O-stack rebalance. The latest deferred-recovery build detected 96/96 drives in 11 consecutive reboots, but the intermittent history means the issue remains blocked on broader hardware validation.

## Last thing tried

Added a deferred second pass before MMIO assignment, escalating from reread to leaf-port secondary-bus reset and optional slot power cycle, with invalid-BAR quarantine. See the 2026-07-31 entry in `ATTEMPTS.md` and [notes/recovery-solution-report.md](notes/recovery-solution-report.md).

## Next step

Request the exact build ID and reboot metadata for the successful 11-reboot run from the lab owner. Confirm that the corresponding capture contains the expected cascade-recovery markers. Then run at least 30 cold boots for the recovery-disabled baseline and each enabled recovery-budget configuration before exercising warm boots, empty slots, and a disconnected shelf. For every boot record all 96 PCI identities, valid BAR0 assignments, NVMe controllers/namespaces, recovery stage, and total boot time.

## Warning

Do not reopen bus allocation unless a new capture explicitly shows exhaustion; the remaining failure is endpoint readiness. Eleven clean boots are not enough to declare success because older builds exceeded ten before failing. The archive does not record the successful build's exact identity or whether those 11 runs were cold or warm; recover that information before comparing results. Map failures by full segment/BDF and a stable drive identifier rather than unstable operating-system names such as `nvme30`.
