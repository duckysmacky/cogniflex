---
applyTo: "**"
excludeAgent: "coding-agent"
---

# Copilot code review instructions

The canonical review rules are in `REVIEW.md` at the repository root; project rules are in the
root `AGENTS.md`. This file repeats the essentials because Copilot does not read the per-service
`AGENTS.md` files. Keep it in sync with `REVIEW.md`.

## Blocking

- Secrets, `.env` files, credentials, `HF_TOKEN`.
- Model weights, datasets, CSV/NPY dumps, archives, generated code or build output in the diff.
- An edited existing Flyway migration; a schema change without a new migration.
- A `proto/` change without matching backend and inference updates.
- `inference/` importing `model/`; the extension content script calling the backend directly.
- Logic errors, unhandled exceptions, leaked resources, analyzed content in logs.
- New extension permissions or host permissions without an explanation.
- Unexplained changes to CI workflows, `deploy.sh`, Dockerfiles, `docker-compose.yml` or agent
  instruction files.

## Per-service checks

- `backend/` (Java 25, Spring Boot 4): records for DTOs and config, switch expressions and pattern
  matching, constructor injection, no field `@Autowired`, no Lombok, no `Optional` fields,
  explicit imports. New rules, services and endpoints need tests (`@WebMvcTest` + `@MockitoBean`).
- `inference/` (Python 3.12, uv): full type hints on every function; models loaded once at
  startup; gRPC errors via `context.set_code`; `logging`, not `print`; deps via `uv add` with `uv.lock`.
- `model/`: type hints in `.py` files; no data or weights committed; no hardcoded personal paths;
  new datasets and models documented in the README with a download source.
- `extension/` (React 19, TS strict, CRXJS, yarn): no new `any`; HTTP only through `src/api/`,
  content script only via the background proxy; every UI string in both `i18n/en.ts` and
  `i18n/ru.ts`; no `package-lock.json`.
- `frontend/` (React 19, TS strict, yarn): no `any`; HTTP calls only through `src/api/`;
  no `package-lock.json`.

## Do not report

Formatting and lint issues, lockfiles, the Gradle wrapper, generated stubs, `graphify-out/`,
notebook outputs, pre-existing issues and personal style preferences. Report at most 5 nits.

## Comments

Cite concrete lines, propose a fix, skip anything you are not confident about, and reply in the
language of the PR description.
