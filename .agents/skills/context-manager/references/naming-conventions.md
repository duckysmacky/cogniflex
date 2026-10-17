# Naming conventions

Rules for naming issue folders and the files inside them. `SKILL.md` covers
*when* to create things; this covers *what to call them*.

## Issue folder names (`.context/<ISSUE-ID>/`)

**Ticket-backed issues** — use the external tracker's ID verbatim. Don't
reformat, lowercase, or abbreviate it.

```
.context/FW-1427/      # matches the tracker exactly
.context/fw-1427/      # don't lowercase it
.context/FW1427/       # don't "fix" its formatting
```

Preserving the ID exactly matters more than it looks: it's what lets the
folder name double as a lookup key back into the tracker, and what keeps a
single canonical spelling in use across every issue instead of several.

**Self-directed issues** - a naming you choose. Kebab-case, lowercase,
describing the *feature or system*, not the task:

```
.context/payment-system/           # names the system
.context/nvme-enumeration/         # names the feature/bug
.context/fix-payment-bug-2/        # names a task, and won't age well
.context/refactor/                 # too generic, will collide eventually
```

A useful test: if you'd still recognize what this issue is about by folder
name alone in six months, it's specific enough.

- Avoid slugs matching the `LETTERS-NUMBER` pattern (`payment-4`, `auth-2`).
- Once created, don't rename an issue folder after work has started.
- If the ID genuinely has to change (e.g. a ticket gets renumbered by the 
  tracker), rename the folder and add a line to the new `ISSUE.md`:
  ```markdown
  - redirected-from: FW-1427
  ```

## File names inside an issue folder

**The six core files are reserved names, exact casing, no exceptions:**

```
ISSUE.md  FINDINGS.md  ATTEMPTS.md  DECISIONS.md  CONNECTIONS.md  HANDOFF.md
```

Don't repurpose one of these names for something else, and don't create a
near-miss (`Findings.md`, `finding.md`).

## Project-level catalogue

`.context/ISSUES.md` is the reserved project-level issue catalogue. It is not
an issue folder and should contain rows only for managed `.context/<ISSUE-ID>/`
folders. Keep its exact uppercase name and do not use it for unrelated context
or project notes.

## Supporting folders

Use these exact lowercase names when an issue needs supporting material:

```text
notes/          # agent- or human-authored working documents
sources/         # textual material received from external sources
attachments/    # captured or native evidence and artifacts
```

`scripts/new_issue.sh` creates these folders empty when it scaffolds an issue;
they may remain empty until supporting material is needed. Do not create
alternate names such as `docs/`, `source/`, `files/`, or `artifacts/`.

**Files in `notes/`** - use lowercase-kebab-case `.md` names that describe
their actual content:

```
what-we-tried-before-reopening.md   # says why it exists
vendor-datasheet-notes.md           # says why it exists
notes.md                            # tells you nothing
misc.md                             # defeats the point of a structured system
```

**Files in `sources/`** - prefer lowercase-kebab-case names and preserve the
source format when it matters. Include the source or channel in the name when
useful, for example `ticket-description.md`, `customer-chat-2026-06-08.md`, or
`datasheet-section-7.txt`.

**Files in `attachments/`** - use descriptive lowercase-kebab-case names and
keep meaningful extensions, for example `cold-boot-07.log`,
`pcie-tree-before.txt`, or `recovery-flow.png`. If there are many files of one
type, group them one level deeper by type (`attachments/logs/`,
`attachments/screenshots/`, `attachments/patches/`) rather than by vague names
such as `misc/` or `old/`.

## Cross-referencing another issue

When referencing another issue in prose, like inside `DECISIONS.md`,
`ISSUE.md`, or a free-form note, always use its literal folder name, not
a paraphrase, so references stay greppable.

Structured issue-to-issue relationships still belong in `CONNECTIONS.md`
- this rule is about inline mentions elsewhere, not a substitute for it.
