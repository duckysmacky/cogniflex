# Findings

## 2026-05-21 - Root-port padding cannot enlarge the stack window

The setup option only adds hot-plug padding inside the bus window already assigned to a root port. It cannot supply the roughly 120 bus numbers originally requested when the containing I/O stack has a smaller window.

**Evidence:** Ticket participants reported that raising the setting did not help; later source analysis placed the padding in DXE, after the early platform bus allocation. See [sources/ticket-conversation.md](sources/ticket-conversation.md) and [notes/pci-bus-allocation-findings.md](notes/pci-bus-allocation-findings.md).

## 2026-06-08 - Parallel and cascaded shelves consume bus windows differently

Forty-eight drives work on one socket when connected in parallel because their PCIe switch trees are distributed across several I/O stacks. The same drives fail in cascade because the host card, both shelf switches, their service endpoints, and all drives must fit behind one stack window.

**Evidence:** The customer storage team confirmed that 48 drives worked in parallel without firmware changes but not in cascade behind one slot. See [sources/customer-conversation.md](sources/customer-conversation.md) and [notes/bus-rebalance-solution-report.md](notes/bus-rebalance-solution-report.md).

## 2026-06-26 - A precompiled initialization component overwrites the supplied socket ratio

During memory initialization, a precompiled silicon-initialization component reinstalls the inter-socket policy with embedded defaults before the platform consumer reads it. Editing unused source files does not affect the packaged binary. A synchronous firmware-side policy notification can modify the live policy instance before consumption.

**Evidence:** Call-path and build-consumer analysis, followed by a working policy-notification override. See [notes/pci-bus-allocation-findings.md](notes/pci-bus-allocation-findings.md).

## 2026-07-17 - The bus-number shortage is resolved

Enabling a second PCI segment and redistributing unused bus numbers from neighbouring I/O stacks gives each selected cascade stack the required window. Subsequent full-topology captures do not show enumeration exceeding bridge limits or exhausting PCI bus numbers.

**Evidence:** The topology-aware rebalance and the before/after captures in [attachments/logs](attachments/logs), analyzed in [notes/pcie-nvme-discovery-progress.md](notes/pcie-nvme-discovery-progress.md).

## 2026-07-20 - Remaining losses have two endpoint-readiness signatures

After bus allocation was fixed, separate 95/96 boots showed one target NVMe endpoint present with an invalid zero-sized BAR and another absent during configuration-space discovery. Both failures occurred on the second cascaded shelf, on different sockets and downstream ports.

**Evidence:** [attachments/logs/endpoint-recovery-validation.log](attachments/logs/endpoint-recovery-validation.log), analyzed in [notes/nvme-timing-investigation.md](notes/nvme-timing-investigation.md).

## 2026-07-24 - Aggressive passive retry and the first reset escalation were insufficient

In a five-boot capture, one completed boot lost an endpoint and the final boot contained both a permanently absent identity and an invalid BAR after the configured retries and one secondary-bus reset. PCI enumeration continued beyond the affected subtree; the final capture stalled later in DXE/BDS rather than during bus scanning.

**Evidence:** [notes/recovery-failure-investigation-2026-07-24.md](notes/recovery-failure-investigation-2026-07-24.md).

## 2026-07-31 - Deferred recovery produced the first 11-boot clean run

Moving recovery to a second pass after bus discovery but before MMIO assignment, with reread, leaf-port reset, optional power cycle, and invalid-BAR quarantine, produced 96/96 drives in 11 consecutive reboots. Earlier builds had also produced runs of ten or more successes before failing, so this does not satisfy the validation target.

**Evidence:** [notes/recovery-solution-report.md](notes/recovery-solution-report.md) and [attachments/logs/endpoint-recovery-validation.log](attachments/logs/endpoint-recovery-validation.log).
