# File schemas

This document defines the exact structure expected inside each file in a
`.context/<ISSUE-ID>/` folder. `SKILL.md` tells an agent *when* to write to
which file; this document defines *what goes in it*.

## File kinds

Every file below is one of two kinds, and the kind determines how it's edited:

- **Living document** - reflects current state. Edited in place. (`ISSUE.md`, `HANDOFF.md`, `CONNECTIONS.md`)
- **Append-only log** - a record of what happened, in order. Never edit or
  delete a past entry - if something turns out to be wrong, add a new entry
  saying so; don't rewrite history. (`FINDINGS.md`, `ATTEMPTS.md`,
  `DECISIONS.md`)

Two running examples are used throughout: `FW-1427`, a ticket-backed issue,
and `payment-system`, a self-directed one.

---

## `.context/ISSUES.md` — living document

**Purpose:** the project-level catalogue of issue folders managed by this
system. It makes managed issues discoverable without treating every directory
under `.context/` as an issue.

Keep one Markdown-table row per issue, using this exact header:

```markdown
| Name | Short description | Status |
| --- | --- | --- |
| [FW-1427](FW-1427/ISSUE.md) | NVMe drives fail after S3 resume | open |
```

`Name` links to the issue's `ISSUE.md`. `Short description` is a concise,
current summary. `Status` mirrors the status in that issue's `ISSUE.md`.
Create the initial row with `scripts/new_issue.sh`; update the description or
status whenever the issue changes. Do not list unrelated `.context/`
directories.

---

## `ISSUE.md` — living document

**Purpose:** what this issue is, right now. The one file every session must
read first.

**Required header block** at the very top:

```markdown
---
id: <ISSUE-ID>
status: open | blocked | done
kind: feature | bug | refactor | investigation (can change)
created: <YYYY-MM-DD>
updated: <YYYY-MM-DD>
---

# <Issue title>


```

**Required sections**, in order:

- `## Summary` - one or two sentences. What this issue is, in plain language.
- `## Scope` - what's explicitly in and out. This is the section most likely
  to change over time; when it does, the *change itself* (and why) goes in
  `DECISIONS.md`, but `ISSUE.md` always reflects the current scope, not
  the history of how it got there.
- `## Acceptance criteria` - a checklist. Concrete and testable, not
  aspirational.
- `## Links` - non-issue references: design docs, PRs, datasheets. (Links to
  *other issues* belong in `CONNECTIONS.md`, not here.)

### Examples

#### Example of ticket-backed issue

```markdown
---
id: FW-1427
status: open
kind: bug
created: 2026-08-12
updated: 2026-08-14
---

# NVMe drive fails to re-enumerate after S3 resume

## Summary

On rev-B boards, the primary NVMe drive intermittently fails to
re-enumerate after resuming from S3 sleep, requiring a cold reboot.

## Scope

In scope: rev-B boards only, S3 resume path. Out of scope: S0ix (modern
standby) - tracked separately if it turns out to be related.

## Acceptance criteria

- [ ] Root cause identified and confirmed with a repeatable trace
- [ ] Fix does not regress S3 resume time by more than 20ms
- [ ] Verified across 100 consecutive resume cycles on rev-B

## Links

- Datasheet: [NVMe controller rev-B errata]
- Original bug report: FW-1427
```

#### Example of self-directed issue

```markdown
---
id: payment-system
status: open
kind: feature
created: 2026-08-01
updated: 2026-08-14
---

# Payment system: webhook retry handling

## Summary

Add retry-with-backoff for outbound payment provider webhooks, since
provider-side outages currently cause silent drops.

## Scope

In scope: outbound webhook delivery and retry logic. Out of scope:
inbound webhook handling (already reliable, not touching it).

## Acceptance criteria

- [ ] Failed webhook deliveries retry with exponential backoff, max 5 tries
- [ ] Retries are idempotent (no duplicate charges on provider side)
- [ ] Dead-letter queue for deliveries that exhaust retries

## Links

- Design note: [retry-strategy.md in project docs]
```

---

## `FINDINGS.md` — append-only log

**Purpose:** durable technical facts. Signal, not narrative - a root cause,
a constraint, how a subsystem actually behaves. If it's still a guess, it
belongs in `ATTEMPTS.md`, not here.

**Entry format**, appended at the end of the file (oldest first):

```markdown
## <YYYY-MM-DD> - <short factual title>

<Description of the finding. State the fact. No hedging language if it's confirmed.>

**Evidence:** <How this was confirmed - trace, repro steps, log line, datasheet reference, commit hash>

```

**Example:**

```markdown
## 2026-08-14 — NVMe controller drops PCIe link after S3 resume

Root cause confirmed: the ASPM L1.2 exit latency programmed by the BIOS is
shorter than this NVMe model's advertised exit latency. The controller
times out negotiating the link back up before the OS driver retries.

**Evidence:** PCIe analyzer trace during resume; link training retries 3 times then reports 
DL_Down before recovering ~400ms later.

```

---

## `ATTEMPTS.md` - append-only log

**Purpose:** every approach tried, and its outcome — successful or not. The
whole point of this file is preventing repeated dead ends.

**Entry format:**

```markdown
## <YYYY-MM-DD> - <short description of the approach>

**Hypothesis:** <what you expected to happen, and why>
**Result:** worked | partial | failed
**Details:** <what actually happened; error output if relevant>
**Next:** <follow-up idea, or "abandoned - see FINDINGS.md">
```

**Example:**

```markdown
## 2026-08-14 09:40 - Disable ASPM L1.2 globally in BIOS setup

**Hypothesis:** If L1.2 exit latency is the problem, disabling L1.2
entirely should remove the timeout.
**Result:** worked
**Details:** Resume timeout gone across 50 consecutive resume cycles.
Resume time regressed by ~15ms - within the 20ms budget in ISSUE.md.
**Next:** Confirm no regression in S0ix idle power draw before calling
this done - see FINDINGS.md for the follow-up measurement.
```

---

## `DECISIONS.md` - append-only log

**Purpose:** a durable, chronological record of choices that affect the
issue's scope, priority, design, or execution. A decision can be informed by
people, agents, experiments, or reviews; this file records the resulting
choice and rationale, not a transcript of the discussion.

**Entry format:**

```markdown
## <YYYY-MM-DD> - <short decision title>

**Status:** <proposed | accepted | superseded | rejected>

**Decision:** <state the choice plainly>

**Rationale:** <why this was chosen; link supporting evidence when useful>

**Impact:** <what changes in scope, priority, design, or next steps>

**References:** <optional links to sources, findings, attempts, or notes>
```

**Example:**

```markdown
## 2026-08-13 - Limit scope to rev-B boards

**Status:** Accepted

**Decision:** Limit this issue to rev-B boards; rev-A is out of scope.

**Rationale:** The failure reproduces only on rev-B, which uses a different
NVMe vendor and ASPM timing.

**Impact:** Update `ISSUE.md` and exclude rev-A from validation.

**References:** [Platform-lead Jira comment](sources/ticket-comment-2026-08-13.md)
```

---

## `CONNECTIONS.md` - living document

**Purpose:** relationships to other issues. Kept as a table, not prose,
so it stays scannable and greppable as it grows.

Use a fixed, small vocabulary for the relationship type - don't invent new
ones per entry:

| Relationship | Meaning |
|---|---|
| `depends on` | this issue can't be finished until the other one is |
| `blocks` | the other issue can't be finished until this one is |
| `related to` | relevant context, no hard ordering dependency |
| `duplicates` | this is the same underlying problem as the other |
| `supersedes` | this issue replaces the other, which should be closed |

**Format:**

```markdown
| Issue | Relationship | Notes |
|---|---|---|
| <ISSUE-ID> | <one of the above> | <why, in a few words> |
```

**Example:**

```markdown
| Issue | Relationship | Notes |
|---|---|---|
| FW-1419 | depends on | needs the DMA controller fix landed first |
| power-management | related to | same board rev, might share root cause |
```

---

## `HANDOFF.md` - living document

**Purpose:** the single file a cold-start session or a different agent
should be able to read and immediately know what to do next. [SKILL.md](../SKILL.md)
defines when to read and write it; this section defines its shape.

Overwritten every time a session ends, not appended to.

**Required sections:**

```markdown
# Handoff

**Last updated:** <YYYY-MM-DD HH:MM> by <agent/tool, e.g. "Claude Code">

## Current state

<1–3 sentences: where things stand right now, plainly.>

## Last thing tried

<Pointer to the relevant ATTEMPTS.md entry, or restated in one line.>

## Next step

<The concrete next action. Specific enough that a cold-start agent with
zero other context can act on it immediately.>

## Warning

<Gotchas, ruled-out red herrings, fragile state - anything that would
waste the next session's time to rediscover.>
```

**Example:**

```markdown
# Handoff — FW-1427

**Last updated:** 2026-08-14 18:02 by Claude Code

## Current state

Root cause confirmed (ASPM L1.2 exit latency). Fix (disabling L1.2)
verified to work but its power-draw impact hasn't been measured yet.

## Last thing tried

Disabled ASPM L1.2 globally - fixed the timeout. See ATTEMPTS.md,
2026-08-14 09:40 entry.

## Next step

Measure S0ix idle power draw with L1.2 disabled, compare against the
baseline in FINDINGS.md. If the regression is under 5%, this is done.

## Warning

Don't re-enable L1.2 to "double check" - 50-cycle resume test takes real
board time and this was already confirmed. If power regression is too
large, the next approach is a per-device L1.2 exception, not a global
re-enable (see ATTEMPTS.md for why global enable was ruled out in the
first place).
```
