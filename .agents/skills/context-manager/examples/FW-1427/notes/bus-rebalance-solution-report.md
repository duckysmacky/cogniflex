---
tags: [work, ticket, solution]
---

# Bus-rebalance solution report

## Problem statement

The platform does not lack PCI bus numbers globally. The shortage exists at one boundary. When two 24-drive shelves are cascaded behind one host card, the host-card switch, both shelf switches, service endpoints, and 48 drives must fit in the continuous bus window assigned to one I/O stack. The default window is too small, while neighbouring stacks reserve bus ranges they barely use.

Rebalancing transfers unused bus numbers from those neighbours to the cascade stack. It does not invent addresses or disable hardware; it changes boundaries inside the socket's fixed bus-number pool.

## Why the reported numbers were misleading

The original ticket estimated 30 BDFs per shelf and described a limit of 40. BDF count is not the resource the allocator partitions. Each bridge consumes a secondary bus, and a switched hierarchy needs room for intermediate bridge buses as well as endpoints. Root-port hot-plug padding is also not an independent pool: padding must fit inside the stack window that early firmware already assigned.

The useful sizing input is therefore the deepest complete topology behind the cascade root, plus margin for service functions and implementation variance. Test traces indicated that a target window near 96 bus numbers per cascade stack was sufficient, whereas the default layout was not.

## Parallel versus cascade

In the working parallel layout, shelf trees enter through multiple root ports and therefore multiple stack windows. No single window carries the entire topology. In the failing cascade layout, every downstream bridge and endpoint shares one root path, so the aggregate fits globally but not locally.

## Two allocation levels

The firmware makes two decisions:

1. Divide the segment's bus-number space among sockets.
2. Divide each socket's pool among its I/O stacks.

Changing only the first level produced a useful experiment but could not support equivalent cascades on both sockets. The final design enables one PCI segment per socket, giving each socket a full 256-number namespace, then changes the second-level stack boundaries.

## Rebalance algorithm

For each socket:

1. Read the platform-generated stack map.
2. Resolve the configured cascade slot to a stack.
3. Calculate the cascade target end from its existing base and requested size.
4. Shrink only explicitly eligible neighbouring stacks to documented minima.
5. Move boundaries while keeping every active stack continuous and non-overlapping.
6. Validate ordering, segment limits, minimum reservations, and total conservation.
7. Commit the complete map only if every check passes; otherwise retain the original map.

Conceptually:

```text
before: [firmware][small cascade][large neighbour A][large neighbour B]
after:  [firmware][       large cascade       ][min A][min B]
```

The cascade stack must grow by moving shared boundaries, not by assigning independent ranges that can overlap.

## Safety and controls

- Disabled by default.
- Applied only to supported sockets and configured slots.
- No port is hidden or electrically disabled.
- A complete validation pass precedes mutation.
- Failure leaves the original allocator output intact.
- Diagnostics report old/new ranges and the reason for fallback without exposing board identifiers.

## Result and remaining issue

With two segments and the validated rebalance, later captures showed no bridge subordinate-bus overflow or bus exhaustion. The investigation then exposed a separate intermittent readiness problem: second-shelf endpoints sometimes returned no identity or an invalid BAR even though sufficient buses existed. Bus rebalancing is a prerequisite, not the complete reliability fix.
