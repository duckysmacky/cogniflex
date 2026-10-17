# Decisions

## 2026-05-21 - Investigate platform bus allocation

**Status:** Accepted

**Decision:** Investigate platform bus allocation instead of treating the setup padding limit as the root cause.

**Rationale:** The per-root-port extra-bus setting did not resolve the reported identifier limit.

**Impact:** Focus investigation on platform allocation rather than setup padding.

## 2026-06-08 - Limit scope to cascaded single-slot topology

**Status:** Accepted

**Decision:** Narrow the issue to the cascaded single-slot topology; do not modify the working parallel topology.

**Rationale:** The parallel topology already enumerates 48 drives per socket without firmware changes; the failure occurs with two shelves cascaded behind one slot.

**Impact:** Exclude the working parallel topology from the implementation scope.

## 2026-06-08 - Reclaim capacity from lightly used I/O stacks

**Status:** Accepted

**Decision:** Reclaim bus capacity from lightly used neighbouring I/O stacks without physically disabling ports.

**Rationale:** Two neighbouring slot groups reserve capacity that is not needed by their current links.

**Impact:** Implement a forced policy first, then expose the behavior through setup controls.

## 2026-07-20 - Test shelf-side delay independently

**Status:** Accepted

**Decision:** Validate shelf-side delay independently from firmware endpoint recovery, then test both together so their effects remain distinguishable.

**Rationale:** Captures show intermittent endpoint loss only on the second cascade layer.

**Impact:** Run independent tests before validating the combined change.
