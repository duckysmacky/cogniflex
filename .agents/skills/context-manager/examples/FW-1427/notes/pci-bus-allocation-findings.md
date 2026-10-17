# PCI bus-allocation findings

## Allocation model

The platform owns 256 bus numbers per PCI segment. In the original single-segment design, an early policy first divided that namespace between sockets, then a silicon allocator divided each socket's share among several I/O stacks. Every stack received one continuous window.

The cascade needs a large continuous window on each socket. A socket-level ratio can make one side larger, but cannot give both sockets equivalent cascades, and it does not control how the winning socket's pool is distributed among stacks.

## Why root-port padding failed

The setup option for extra buses is consumed later by hot-plug resource code. It reserves subordinate buses under a root port but remains constrained by the parent stack's earlier window. Increasing the field's UI maximum cannot cross that boundary.

## Build-consumer discovery

Several plausible edits had no runtime effect. The inspected source corresponded to a precompiled silicon-initialization binary that the normal firmware build merely packaged. The live sequence was:

1. Main firmware installs a policy initialized from setup.
2. The packaged component begins memory/silicon initialization.
3. It reinstalls the policy using embedded defaults.
4. The topology allocator consumes that new instance.

The decisive experiment registered a synchronous notification in a main-firmware early-boot module. When the packaged component reinstalled the policy, the callback modified the supplied live instance before the allocator consumed it. This proved both the call path and the distinction between inspected source and built artifact.

Policy callback:

```c
EFI_STATUS
PlatformPolicyNotifyCallback (
  IN CONST EFI_PEI_NOTIFY_DESCRIPTOR *Notify,
  IN VOID *PolicyInstance
  )
{
  PLATFORM_TOPOLOGY_POLICY *Policy = PolicyInstance;

  Policy->SocketBusWeight[0] = 2;
  Policy->SocketBusWeight[1] = 6;
  Policy->ResourceMapHook = ApplyCascadeBusRebalance;
  return EFI_SUCCESS;
}
```

## Final allocation direction

The ratio override was useful proof but not the product solution. The final design:

- Enables a separate PCI segment for each socket.
- Lets the stock allocator produce an initial per-stack map.
- Invokes a board-owned resource-map hook before enumeration.
- Enlarges only the configured cascade stack by reclaiming documented slack from neighbours.
- Validates all starts/ends, ordering, minimum windows, and segment boundaries.
- Falls back atomically to the original map on any inconsistency.

## Evidence that allocation is fixed

The rebalance build reaches every bridge level in the full topology, and captures no longer contain subordinate-bus clipping or bus-exhaustion diagnostics. Later 95/96 events occur after the full path has a legal bus number and therefore belong to endpoint readiness, not bus allocation.
