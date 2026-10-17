# PCIe/NVMe discovery progress

## Phase 1 — resource diagnosis

The first failures looked like a global BDF shortage. Testing separated two facts:

- 48 drives on one socket work when spread across parallel root paths.
- The same 48 fail behind one cascaded path.

That comparison identified the per-stack window as the relevant boundary. A two-segment build plus stack rebalance eliminated subordinate-bus overflow and made all expected bridge levels reachable.

## Phase 2 — intermittent endpoint failures

After allocation was fixed, some boots still exposed only 95 of 96 drives. The traces no longer showed a resource boundary violation. Instead they showed two endpoint states:

- Configuration identity reads as all ones, so no device object is created.
- Identity and class are valid, but BAR sizing returns a zero or otherwise invalid mask.

Both states occurred only below the second shelf, suggesting late readiness rather than insufficient address space.

## Recovery evolution

1. Bounded identity and BAR rereads recovered some events.
2. Larger passive budgets did not materially improve the failure rate.
3. Controller-driver polling was rejected after a crash and because it runs too late.
4. Link-gated leaf-port reset recovered one BAR failure but did not poll long enough after reset for missing identities.
5. Deferred recovery revisits failures once the initial tree walk completes, before MMIO assignment.

The current policy recognizes absent identity (`all ones`, `all zeroes`, and configuration-retry status) separately from invalid NVMe BAR state. It only targets expected endpoints under a configured cascade root. Generic PCI devices and empty ports keep stock behavior.

## Current result

The first deferred-recovery image produced 96/96 drives for 11 consecutive reboots. This is promising but below the 30-cold-boot minimum and does not cover warm boots, empty slots, or a disconnected second shelf.

