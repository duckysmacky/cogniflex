---
name: context-manager
description: Maintain a persistent `.context/ISSUE/` directory for complex engineering work spanning sessions or coding agents. Use for long-running features, bugs, investigations, or tickets, when resuming prior work or a handoff, or when asked to record findings, attempts, decisions, or issue context.
---

# Context Manager

Preserve engineering context in the repository so another session or agent can resume without relying on chat history.

## Core model

> An "issue" in this document always means something with its own `.context/`
> folder - not an arbitrary request or sub-step within a session. 

- An **issue** is a feature, bug, investigation, or ticket substantial enough to need its own `.context/<ISSUE-ID>/` folder.
- The core principle of this skill is to retain information only relevant to the issue being worked on to keep the context clear and concise.
- Issues provide information only related to the given issue, don't span across multiple issues, have a main problem statement or a concrete feature request.
- Context for an issue is isolated and scoped only to issue-related information, and not to the general feature or system being worked on.

Every issue is usually of two kinds:
- **Ticket-backed** - comes from an external tracker (Jira, GitHub Issues, etc.)
  and already has an ID, e.g. `FW-1427`. The folder name is that ID, verbatim.
  If user mentions a ticket ID, the agent should always use it as the main 
  identifier for the issue, and in all other references to the issue, prefer that ID. 
- **Self-directed** - you're splitting the project into a separate issue for 
  the main goal and your own tracking purposes, e.g. `payment-system`, 
  `nvme-enumeration`, etc. The folder name is a slug you choose.
Read [naming conventions](references/naming-conventions.md) when choosing or changing names.

## Layout

```text
.context/
├── ISSUES.md                 # catalogue of managed issues and statuses
└── <ISSUE-ID>/
    ├── ISSUE.md              # current description, scope, and acceptance criteria
    ├── FINDINGS.md           # confirmed technical facts
    ├── ATTEMPTS.md           # approaches tried and their outcomes
    ├── DECISIONS.md          # durable choices, rationale, and impact
    ├── CONNECTIONS.md        # relationships to other issues
    ├── HANDOFF.md            # current state and next action
    ├── notes/                # detailed agent- or human-authored analysis
    ├── sources/              # textual material received from external sources
    └── attachments/          # logs, screenshots, patches, and other evidence
```

`ISSUES.md` lists only issues managed by this system. The six files inside each issue folder are the source of truth; supporting folders hold detail or evidence, not conclusions that belong in a core file.

## Guidelines

- One `.context/<ISSUE-ID>/` directory per issue
- Never share a folder across issues, even closely related ones
- Don't duplicate the same fact across files
- If you're not sure to which core file something belongs in, see `references/file-schemas.md`
- Treat `notes/`, `sources/`, and `attachments/` as optional supporting material, not replacements for the six core files
- Keep each issue self-contained. Track related issues separately and cross-reference them through `CONNECTIONS.md`.

## Workflow

### Locate or create

1. Consult `.context/ISSUES.md` when it exists, then check `.context/<ISSUE-ID>/`.
2. If the issue exists, resume it before changing anything.
3. Otherwise run `scripts/new_issue.sh` when available; if not, copy `assets/templates/`. Populate `ISSUE.md` from the available issue context and add the catalogue row—never leave the issue blank.
4. If `.context/` is not covered by `.gitignore`, ask whether it should be committed or kept local.

### Resume

Read `ISSUE.md`, `FINDINGS.md`, `ATTEMPTS.md`, then `HANDOFF.md`; read other files as needed. Summarize the known state and last attempt to the user. Do not silently re-derive recorded context or retry a ruled-out approach without explaining why.

### Record work

- Append every tried approach, reason, outcome, and useful next step to `ATTEMPTS.md`.
- Append only confirmed, durable technical facts and their evidence to `FINDINGS.md`.
- Append durable scope, design, priority, or trade-off choices with rationale and impact to `DECISIONS.md`.
- Update `CONNECTIONS.md` when issue relationships change.
- Keep the matching `ISSUES.md` row synchronized when an issue's short description or status changes.
- Put external text in `sources/`, captured or native evidence in `attachments/`, and detailed analysis or procedures in `notes/`. Link supporting material from the relevant core entry and state what it establishes.

Date-stamp additions to `FINDINGS.md`, `ATTEMPTS.md`, and `DECISIONS.md`; these logs are append-only. `ISSUE.md`, `CONNECTIONS.md`, and `HANDOFF.md` are living documents edited in place. Avoid duplication and use relative links so issue folders remain portable.

### Handoff

Only prepare a handoff when the user explicitly ends the session or requests a handoff. Overwrite `HANDOFF.md` with the current state, last attempt, concrete next action, and important warnings. Never infer that a session has ended.

## References

- Read [file schemas](references/file-schemas.md) before creating or restructuring context files, or when entry placement is unclear.
- Read [naming conventions](references/naming-conventions.md) when naming issue folders or supporting files.
- Use `assets/templates/` for manual scaffolding when the helper script is unavailable.
- Consult `examples/FW-1427/` only when a worked example would materially clarify the workflow.
