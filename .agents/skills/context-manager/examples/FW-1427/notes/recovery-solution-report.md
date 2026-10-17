---
tags: [work, ticket, solution]
---

# Deferred endpoint-recovery solution report

## Summary

After PCI bus allocation was corrected, rare boots still lost one drive from the second shelf in a cascade. The solution moves recovery out of the linear discovery hot path and into a deferred pass after topology discovery but before MMIO assignment.

The pass escalates from passive reread to a leaf-port secondary-bus reset and, when supported and enabled, a slot power cycle. A recovered endpoint is inserted back into normal enumeration. A present endpoint whose BAR remains invalid is quarantined so the NVMe driver cannot bind to an unusable register aperture.

## Why the second shelf is vulnerable

The second shelf starts behind three switch stages: host-card switch, first-shelf switch, and second-shelf switch. Its drives cannot begin their final link and controller initialization until the upstream chain is ready. Firmware walks the PCI tree in a deterministic order, so a late drive can be probed at the edge of its readiness distribution. The first shelf has one less cascade stage and was consistently ready earlier.

## Two failure modes

### Identity absent

The expected leaf returns an absent/not-ready identity. The ordinary enumerator treats the address as empty and creates no endpoint object. Recovery is eligible only when the leaf link is active and the path is under the configured cascade root.

### Invalid BAR

The endpoint returns stable NVMe identity and class but an invalid BAR-sizing response. This is later than identity absence but still indicates incomplete readiness in this topology. The endpoint object exists, so failure must end in either successful rescan or explicit quarantine.

## Deferred sequence

1. During discovery, record a compact candidate containing parent leaf, path label, failure type, and timestamp.
2. Finish the initial recursive bus walk.
3. Before resource assignment, revisit each candidate under a total recovery deadline.
4. Reread link, identity, class, and BAR as appropriate.
5. If still unready, reset only the leaf port and poll link plus endpoint state.
6. If policy permits and the port supports power control, power-cycle that leaf and repeat bounded polling.
7. On success, resume ordinary device creation/BAR sizing.
8. On terminal invalid BAR, quarantine; on terminal absent identity, leave the endpoint absent.

## Correctness constraints

- Never reset a bridge that contains already enumerated sibling drives.
- Never apply recovery based only on a vendor/device ID.
- Never retry inactive empty ports.
- Never fabricate a BAR size from an invalid response.
- Never exceed the per-stage or whole-boot budget.
- Never let recovery policy implicitly change the bus-allocation policy.
- Preserve stock enumeration when either feature is disabled.

## Result

The first image with the complete deferred sequence reported 96 PCI endpoints, 96 NVMe controllers, and 96 namespaces in 11 consecutive reboots. The previous history included success streaks longer than ten followed by failure, so this is evidence of improvement rather than proof of reliability.

## Validation required before closure

- At least 30 cold boots for disabled, conservative, and aggressive policies.
- At least 30 warm boots using the selected production policy.
- Empty supported slot and disconnected second-shelf cases to prove bounded behavior.
- Negative tests with recovery enabled outside the configured topology.
- Per-boot controller, namespace, BAR, quarantine, reset, and timing records.
- Confirmation that no watchdog expiry, duplicate enumeration, BAR overlap, or late boot stall is introduced.
