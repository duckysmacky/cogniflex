---
id: FW-1427
status: blocked
kind: bug
created: 2026-05-21
updated: 2026-07-31
---

# Reliable discovery of cascaded external NVMe shelves

## Summary

Modify the firmware of a dual-socket server so that two cascaded 24-drive NVMe shelves per socket enumerate reliably, providing 96 usable drives across the system.

## Scope

In scope: PCI bus-number allocation for the cascaded topology, per-socket host-card slot selection, endpoint-readiness recovery during PCI enumeration and resource assignment, and full-topology hardware validation.

Out of scope: the already-working parallel shelf topology, fixed MMIO addresses, unbounded retries across arbitrary PCI devices, and unrelated slots.

## Acceptance criteria

- [x] Provide enough PCI bus numbers for two cascaded shelves on each socket
- [x] Keep bus rebalance and endpoint recovery behind dedicated setup controls
- [x] Preserve normal PCI behavior when cascade support is disabled
- [ ] Detect 96/96 PCI endpoints, NVMe controllers, and namespaces across at least 30 cold boots per configuration
- [ ] Complete warm-boot, empty-slot, and disconnected-shelf validation
- [ ] Show no bus exhaustion, BAR overlap, duplicate enumeration, watchdog expiry, or late DXE/BDS stall
- [ ] Measure and document the bounded boot-time impact of recovery

## Links

- Original task context: [sources/starting-prompt.md](sources/starting-prompt.md)
- Bus-rebalance report: [notes/bus-rebalance-solution-report.md](notes/bus-rebalance-solution-report.md)
- Recovery report: [notes/recovery-solution-report.md](notes/recovery-solution-report.md)
- Verification procedure: [notes/verification-commands.md](notes/verification-commands.md)
- Boot captures: [attachments/logs](attachments/logs)
