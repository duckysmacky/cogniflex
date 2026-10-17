# Recovery failure investigation — 2026-07-24

## Test context

The tested image included link-state gating, aggressive passive retries, and one leaf-port secondary-bus reset. A serial capture covered five reboot attempts.

## Results

- At least one completed boot exposed only 95 of 96 drives.
- One failure showed a valid NVMe identity followed by invalid BAR sizing.
- Another showed absent identity at an expected second-shelf leaf despite an active upstream path.
- The reset path recovered an invalid BAR in one event.
- The absent-identity path still failed because the implementation waited for link and then performed only one configuration read.
- The final boot progressed beyond the affected bus range and stalled later in DXE/BDS, so it cannot be labeled a PCI bus-scan hang from the available evidence.

## Interpretation

The experiment disproved the idea that a single state-changing action followed by one read was sufficient. Reset completion and endpoint configuration readiness are separate events. A trained link only establishes that the physical/data-link path is available; it does not guarantee that endpoint configuration and BAR logic are ready.

It also reinforced that the bus-number problem remained solved. Enumeration continued through downstream buses beyond the failed leaf, and there was no bridge-window violation in the capture.

## Required changes

1. After reset, poll link state to a bounded deadline.
2. Then poll identity through configuration-retry, absent, and partial-ready states.
3. For known NVMe endpoints, rerun BAR sizing only after identity/class are stable.
4. Defer the complete sequence until the first tree walk finishes.
5. Quarantine a still-invalid BAR rather than allowing driver binding.
6. Log one line per state transition and one terminal line per candidate.

## Follow-up

The next image implemented a deferred second pass with optional power-cycle escalation. It produced the first 11-reboot 96/96 run, but did not close the issue because the sample was small and the reboot type/build identity were incompletely recorded.
