# Recovery algorithm analysis

## Placement

The recovery coordinator belongs between two firmware phases:

1. recursive PCI bus discovery, where bridges receive bus numbers and endpoint objects are created;
2. resource assignment, where MMIO apertures are calculated and programmed.

Running after discovery gives late endpoints additional time. Running before assignment allows a recovered endpoint to participate normally and prevents a malformed BAR from contaminating resource calculation.

## Scope gate

Recovery is enabled only when all conditions hold:

- setup policy explicitly enables cascade recovery;
- the segment/socket/root path matches the selected cascade slot;
- the parent is a leaf downstream port in the expected switched hierarchy;
- link state indicates a connected partner for absent-identity recovery, or a known NVMe endpoint has an invalid BAR;
- the per-endpoint and total boot budgets are not exhausted.

No vendor/device ID alone is sufficient. Generic devices, unrelated slots, and inactive empty ports retain stock behavior.

## State machine

```text
initial probe
  ├─ ready ───────────────────────────────> enumerate normally
  ├─ empty + link down ───────────────────> leave absent
  └─ candidate failure ─> defer
                           ├─ reread/poll ─> ready
                           ├─ leaf reset ──> link poll ─> identity/BAR poll ─> ready
                           ├─ power cycle ─> link poll ─> identity/BAR poll ─> ready
                           └─ exhausted ───> quarantine or leave absent
```

## Reset boundaries

A secondary-bus reset must target the last bridge immediately above the drive. Resetting a shelf-level or host-card bridge would disrupt already discovered siblings and invalidate the device tree. Optional slot power control is used only when the leaf port advertises the capability; otherwise that stage is skipped.

After either action, recovery must poll link completion before configuration identity. One immediate identity read after reset was a documented failed attempt.

## Quarantine

An endpoint with valid identity but invalid BAR must not remain bindable. Quarantine clears its pending resources, marks it excluded from protocol installation, and emits one terminal diagnostic. An endpoint that never returned identity has no device object and simply remains absent.

## Policy budgets

- Conservative: short passive polling, one leaf reset, no power cycle.
- Aggressive: longer polling, one leaf reset, optional power cycle.
- Disabled: no candidate recording and stock timing.

Budgets are constants in the example, but production values require measured hardware distributions. Use one total deadline as well as stage deadlines to prevent multiplicative stalls.

## Implementation boundaries

| Component | Responsibility |
|---|---|
| `PlatformPkg/Library/CascadePolicyLib` | Load policy and recognize configured topology |
| `PciBusDxe/Discovery` | Record identity-absent candidates |
| `PciBusDxe/BarSizing` | Record invalid-BAR candidates |
| `PciBusDxe/RecoveryCoordinator` | Run the deferred state machine before assignment |
| `PlatformPkg/Library/PortControlLib` | Leaf reset, link polling, optional power control |
