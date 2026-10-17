# Firmware setup controls handoff

The bus-rebalance logic already works with a compile-time cascade-stack choice. Production integration needs two NVRAM-backed controls: an enable switch and the physical slot that contains the cascade host card.

## Control 1 — cascade bus allocation

- Field: `CascadeBusAllocationEnabled` in the platform I/O configuration structure
- Default: disabled, preserving stock behavior
- Effect: when disabled, the early allocation hook returns without modifying the platform map
- Dependency: recovery policy must not silently enable bus rebalance

## Control 2 — cascade slot

Store a platform-defined stack identifier rather than a UI ordinal. The board mapping should live in a small lookup table so setup choices remain physical-slot labels while early boot consumes a stack index.

```c
typedef struct {
  UINT8 CascadeBusAllocationEnabled;
  UINT8 CascadeSlot;
} PLATFORM_IO_CONFIGURATION;

STATIC CONST CASCADE_SLOT_MAP mCascadeSlots[] = {
  { CascadeSlotPrimaryX16, StackPrimary },
  { CascadeSlotSecondaryX8, StackSecondary },
  { CascadeSlotTertiaryX8, StackTertiary },
};
```

The changes span the platform I/O configuration, setup form, setup strings, and early bus-policy library:

| Location | Change |
|---|---|
| `PlatformPkg/Include/PlatformIoConfig.h` | Add the two one-byte configuration fields |
| `PlatformPkg/Setup/IoSetup.vfr` | Add enable and slot-selection questions; hide the slot unless enabled |
| `PlatformPkg/Setup/IoSetupStrings.uni` | Add English prompt/help/choice strings |
| `PlatformPkg/Library/BusPolicyLib/BusPolicyLib.c` | Read and validate configuration; map slot to stack |

Invalid or unavailable slot values must select the documented default and emit one concise diagnostic. A malformed variable, missing variable service, or impossible stack map must leave the vendor allocation untouched.

## Validation

- Disabled: byte-for-byte equivalent resource map and normal boot.
- Enabled, primary slot: the primary stack receives the cascade window and exposes 48 drives.
- Enabled, alternate slot: the selected alternate stack receives the window; no boot loop.
- Wrong slot selected: the selected stack grows and the actual cascade may be incomplete; document this as operator error.
- Reset setup defaults: control returns to disabled.
- Confirm UI strings render correctly and changes require reset.
