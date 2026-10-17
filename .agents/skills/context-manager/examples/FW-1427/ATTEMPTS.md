# Attempts

## 2026-05-21 - Increase root-port extra-bus reservation

**Hypothesis:** Reserving more buses for the cascade root port would allow all shelf bridges and drives to enumerate.
**Result:** failed
**Details:** The customer confirmed that the setting did not solve the problem. The padding is allocated inside the existing I/O-stack window and cannot enlarge it.
**Next:** Change the earlier stack-window allocation instead of DXE hot-plug padding.

## 2026-06-08 - Force a 2:6 bus ratio between sockets

**Hypothesis:** Giving socket 1 three quarters of the platform's original bus-number space would provide enough capacity for the cascade.
**Result:** partial
**Details:** A forced test-policy build demonstrated the intended inter-socket split, but this addressed the socket pools rather than the decisive distribution among stacks. It also left socket 0 unable to host an equivalent cascade. The reliable runtime interception mechanism was not established until 2026-06-26.
**Next:** Find where the packaged initialization component replaces the test policy, then add a second PCI segment and redistribute bus numbers within each socket.

## 2026-06-08 - Override the ratio in source corresponding to a packaged binary

**Hypothesis:** Editing the policy-update routine before allocation would preserve the desired ratio.
**Result:** failed
**Details:** The normal firmware build packages a precompiled silicon-initialization binary and does not rebuild the inspected source. Both variants were absent at runtime.
**Next:** Intercept the live policy instance from a firmware-compiled early-boot module.

## 2026-06-26 - Override the reinstalled policy from a notification callback

**Hypothesis:** A synchronous firmware-side notification could update the exact policy instance reinstalled by the precompiled component before the allocator reads it.
**Result:** worked
**Details:** Using the callback's supplied policy instance produced the intended socket split. A later hardcoded reset of the ratio was also removed.
**Next:** Solve the within-socket cascade-stack allocation and support both sockets.

## 2026-07-17 - Enable two PCI segments and rebalance stack windows

**Hypothesis:** One 256-bus segment per socket plus topology-aware reclamation from lightly used neighbouring stacks would fit two cascaded shelves per socket.
**Result:** worked
**Details:** The selected cascade stack received the target window and later captures no longer showed bus-window exhaustion. The implementation validates the complete map and falls back to the firmware allocation if it is invalid.
**Next:** Investigate intermittent 95/96 endpoint losses separately from bus allocation.

## 2026-07-20 - Add bounded identity and BAR retries during enumeration

**Hypothesis:** A temporarily unready endpoint would recover if configuration identity or BAR sizing were retried with bounded delays.
**Result:** partial
**Details:** The retries recovered some cases, but losses remained. Increasing the budget from conservative to aggressive did not materially change the failure rate across roughly twenty boots.
**Next:** Gate retries on link state and add a bounded escalation that changes device state.

## 2026-07-21 - Retry controller-register reads in the NVMe driver

**Hypothesis:** If the endpoint and BAR were valid but controller registers were late, the NVMe driver could wait for capability and version registers to become readable.
**Result:** failed
**Details:** The test image raised a general protection fault after controller initialization began. Driver-specific recovery was removed.
**Next:** Keep recovery in PCI discovery and resource assignment, before NVMe driver binding.

## 2026-07-24 - Gate on link state and issue one leaf-port secondary-bus reset

**Hypothesis:** Avoiding retries on empty ports and resetting an unready endpoint's leaf port would recover both missing-identity and invalid-BAR failures.
**Result:** partial
**Details:** The reset recovered a zero BAR in one capture, but the first implementation performed only one identity read shortly after reset and failed to recover absent endpoints. A later five-boot capture still showed both signatures and one late DXE/BDS stall.
**Next:** Perform bounded post-reset link and identity polling, and do not leave an invalid-BAR NVMe endpoint bindable.

## 2026-07-31 - Add deferred second-pass recovery and quarantine

**Hypothesis:** Returning to failed endpoints after the rest of bus discovery would give second-shelf drives tens of seconds to become ready without adding that delay per endpoint.
**Result:** partial
**Details:** The second pass runs before resource assignment and escalates through reread, leaf-port reset, and optional slot power cycle; unrecoverable invalid-BAR endpoints are quarantined. The latest image achieved 96/96 drives in 11 consecutive reboots, but that sample is below the acceptance target.
**Next:** Run the full cold/warm, policy, empty-slot, and disconnected-shelf validation matrix.
