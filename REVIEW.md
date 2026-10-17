# REVIEW.md

Review instructions for pull requests into `dev`, for AI reviewers and humans alike.
Project rules live in `AGENTS.md` (root and per-service); a violation of them is a finding.

## Blocking (Important)

- Secrets: `.env`, tokens (`HF_TOKEN`, ...), keys, credentials, private URLs with auth in them.
- Large or generated files: model weights (`*.pth`, `*.safetensors`, `*.onnx`), datasets, CSV/NPY
  dumps, archives, `inference/generated/`, `build/`, `dist/`, `extension/release/`.
- An edited existing Flyway migration, or an entity/schema change without a new `V<n>__*.sql`.
- A `proto/` change that does not update both the backend and the inference servicer.
- `inference/` importing from `model/`; `extension/src/content.ts` calling the backend directly
  instead of going through the background proxy.
- Logic errors, unhandled exceptions that can crash the gRPC server or return a 500, leaked
  temp files or connections, models loaded per request.
- Analyzed content (user text, page content, images, videos) written to logs.
- New extension manifest permissions or host permissions without an explanation in the PR.
- PR targets anything other than `dev` (except maintainer `dev -> master` releases).

## Should fix (Normal)

- New rule, service method or endpoint without tests (backend); changed RPC without a way to verify it.
- Python function without full type hints (parameters and return type).
- New Java code using outdated idioms where a record, switch expression, pattern matching or
  immutable collection fits; field injection; `Optional` fields.
- A dependency added without its lockfile (`uv.lock`, `yarn.lock`) or outside the package manager.
- A new model, weights file or dataset without a download source and target path in the README.
- A new env var not added to `.env.example`.
- Extension/frontend: new `any`, HTTP calls outside `src/api/`, UI strings missing from
  `i18n/en.ts` or `i18n/ru.ts`, results shown as definitive instead of probabilistic.
- A version bump in only some of `backend/build.gradle`, `inference/pyproject.toml`,
  `extension/package.json`, `frontend/package.json`.

## Nit (at most 5 per review)

Naming, module placement, small readability improvements. Never block on a nit.

## Do not report

- What CI or tooling already enforces: compile errors caught by CI, ESLint/Prettier formatting.
- Lockfiles, the Gradle wrapper, generated stubs, `graphify-out/`.
- Exploratory code under `model/notebooks/` (still report secrets and large data there).
- Pre-existing issues the PR does not touch.
- Personal style preferences not backed by `AGENTS.md`.

## How to comment

- Cite `file:line` evidence for every claim; if you are not sure it is a real problem, do not post it.
- One comment per issue, with a concrete fix (a suggestion block for small fixes).
- Check the PR title: English, imperative, capitalized, no trailing period, no `#123`; one task per PR.
- Reply in the language of the PR description (Russian or English). Many authors are new to
  code review - be direct and explain the "why" briefly.
