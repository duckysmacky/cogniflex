# NVMe endpoint-readiness investigation

## Observation boundary

This investigation begins after the bus-number fix. In the affected boots, every expected bridge in the cascaded topology received a legal primary/secondary/subordinate range. The missing drive therefore cannot be explained by a truncated bus walk.

## Failure signature A — identity absent

At the expected leaf location, a configuration identity read returns an absent/not-ready encoding. No PCI device object is created, so later BAR sizing and driver binding never run. Adjacent downstream ports on the same shelf switch enumerate normally.

## Failure signature B — identity valid, BAR invalid

The endpoint returns a valid identity and NVMe class code, and the link is active, but BAR sizing produces a zero or nonsensical mask. Treating that response as authoritative creates an endpoint with no usable register aperture; the NVMe driver subsequently cannot initialize it.

## Common pattern

Both signatures were observed below the second shelf, on different sockets and ports. A second-shelf endpoint is separated from the processor by the host-card switch, first-shelf switch, and second-shelf switch. Each stage must initialize and train downstream links before the drive can answer reliably. The working hypothesis is that the two signatures are different points in one readiness curve: configuration space may be fully absent first, then partially responsive before BAR logic is ready.

The captures establish correlation and timing, not the physical root cause. Drive firmware, switch firmware, power sequencing, and link training remain possible contributors.

## Timing implications

Inline delay per address scales badly across 96 endpoints and can perturb the race being measured. A single early retry also fails if the second shelf is still becoming ready. The preferred design records only expected cascade candidates and returns to them after the first topology walk, providing a natural delay while keeping the worst-case budget bounded per failed endpoint rather than per scanned address.

## Identity classification

Recovery distinguishes:

- normal identity: continue stock enumeration;
- configuration-retry status: poll within the selected budget;
- all ones or all zeroes with inactive link: treat as empty;
- absent identity with active link under a known cascade leaf: defer;
- any equivalent state outside the configured cascade: stock behavior.

## BAR classification

For a known NVMe endpoint under the cascade root, an invalid BAR is deferable only when identity and class remain stable. Reread both halves of a 64-bit BAR consistently. Never normalize a malformed mask into a fabricated resource size. If bounded recovery fails, quarantine the endpoint before driver binding.

## Measurement

Each recovery record should include candidate label, link state, identity class, BAR state, stage, elapsed milliseconds, and result. Aggregate boot time must be compared with recovery disabled.
