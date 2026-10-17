# AGENTS.md

Instructions for AI agents working in the Cogniflex repository. `CLAUDE.md` is a symlink to this file.
Each service has its own `AGENTS.md` with stack, build and style rules - read it before touching that directory.

## What Cogniflex is

Cogniflex detects AI-generated content - text, images and videos - and gives a probabilistic
verdict instead of an absolute claim. The main client is a browser extension that analyzes page
content in real time and highlights likely AI-generated fragments; manual analysis (paste text,
upload media) is available in the extension and on the website. The project is early-stage:
video analysis is a stub and the website frontend is a bare shell.

Every item goes through two analysis paths whose results are fused into one verdict:
- **Static** - deterministic rules over the content (text heuristics, image/video checks) -> evidence + score.
- **Dynamic** - ML models served by the inference service over gRPC -> class + confidence.

## Repository map

| Path | What | Stack |
|---|---|---|
| `backend/` | REST API, analysis pipeline, persistence | Java 25, Spring Boot 4, Gradle |
| `inference/` | gRPC model-serving service | Python 3.12, uv, just, PyTorch (CPU) |
| `model/` | Research, training, evaluation (not deployed) | Python, Jupyter |
| `extension/` | Chrome extension (Manifest V3): real-time and manual analysis | React 19, TypeScript, Vite, CRXJS, Tailwind 4, yarn |
| `frontend/` | Website (early shell, not in Docker Compose yet) | React 19, TypeScript, Vite, yarn |
| `proto/cogniflex/inference/` | gRPC contract, single source of truth | Protobuf 3 |
| `docs/` | Documentation and research for **humans** (mostly Russian) | Markdown |
| `.context/` | Additional context for **agents** | Markdown |
| `.github/workflows/` | CI tests, release image builds and deploy, Claude review | GitHub Actions |
| `deploy.sh` | Server-side deploy script used by the release workflow | Bash |

## Architecture

```
extension / frontend --REST--> backend --gRPC(:50051)--> inference
                                  |
                          PostgreSQL 18 + Redis 8
```

- `backend` preprocesses input, runs static and dynamic analyzers in parallel, fuses scores,
  stores history in PostgreSQL (Flyway migrations) and rate-limits requests.
- `inference` loads model weights from `inference/weights/` at startup and answers `AnalyzeText` /
  `AnalyzePhoto` / `AnalyzeVideo` RPCs. It never calls the backend.
- `extension` content scripts never call the backend directly: they message the background service
  worker, which performs the HTTP request (`src/api/proxy/`).
- Both backend and inference generate code from `proto/` at build time; generated code is never
  committed. A proto change must keep backend and inference compiling in the same PR.
- `model/` produces weights; the result is shipped to `inference/weights/` manually, never via imports.
- Full stack: `docker compose up --build` (config from `.env`, template in `.env.example`).

## Tools and skills

- there is a `.agents/skills` directory present with predefined skills for this project. it is symlinked
  to `.claude/skills` and `.codex/skills` for those specific agents to be able to use those skills
- **graphify**: if a code graph exists, query it (report/graph) before broad grep/glob exploration,
  and use it to find callers and dependencies across services. Rebuild it after large structural changes.
- **context-manager**: use it for complex or multi-session work (large features, refactors,
  investigations) to keep plans and progress in `.context/` so the next session can resume.
- Put agent-facing notes in `.context/`, never in `docs/`. Edit `docs/` only when asked.

## Git workflow

Never commit to `master` or `dev` directly. All work goes through a PR into `dev`;
`dev -> master` is done only by Maintainers.

1. Start from fresh `dev`: `git fetch origin && git switch dev && git pull --ff-only`.
2. Create a branch: `git switch -c <prefix>/<short-kebab-description>` (2-5 lowercase words).
   Prefixes: `feature/`, `fix/`, `refactor/`, `chore/` (deps, build), `docs/`, `release/` (maintainers).
3. If already on a branch, check it is the right one and up to date: `git fetch origin` and
   `git rebase origin/dev` when behind. Use `git push --force-with-lease` only on your own branch.
4. Commits: small, one logical change each. Title in English, imperative, capitalized, no period,
   <= 50 chars (`Add em dash density rule`, `Fix crash on empty image`). Body explains "why".
5. Stage files by name, not `git add .`; check `git status` and `git diff --staged` first.
6. PR into `dev`: same title rules (it becomes the squash commit), description with what/why/how
   to test, related GitHub issue in the body (`Closes #123`, not `#123` in the title). One PR = one task.
7. Run the affected service's build, tests and linters before pushing.
8. Do not merge PRs, approve your own work, delete remote branches or push release tags - humans do that.

When working with worktrees, keep them in `.worktrees/` directory with directory name derived from
branch's name (e.g.: `feature/auth` branch will be in `.worktrees/feature-auth` worktree)

## Guardrails

- **No secrets.** Never commit `.env`, tokens (`HF_TOKEN`, ...), keys or credentials. Config goes through env vars.
- **No large files.** Never commit model weights (`*.pth`, `*.safetensors`, ...), datasets, CSV
  dumps, archives or build output. Document the download source and target path in that
  service's `README.md` instead. If you do not know where a file came from, ask the user.
- **No generated code or build output** in git: `inference/generated/`, `backend/build/`,
  `frontend/dist/`, `extension/dist/`, `extension/release/`.
- **Agent instruction files** (`AGENTS.md`, `CLAUDE.md`) are edited only after a **critical**
  change to the repo (new service, new build tool, changed workflow), or when explicitly asked.
- **CI, release, deploy and Docker** (`.github/workflows/`, `deploy.sh`, `Dockerfile`s,
  `docker-compose.yml`, `.dockerignore`) are edited only when the task is about them or a critical
  change requires it.
- **Versions.** The project version lives in `backend/build.gradle`, `inference/pyproject.toml`,
  `extension/package.json` and `frontend/package.json`. Bump them together and only when asked.
- **Organization.** Keep the existing clean, explicit layout (like `proto/cogniflex/inference/`):
  clear module and package names, one responsibility per module, no junk dirs (`utils2`, `new`).
- **Scope.** Change only what the task needs. No repo-wide reformatting, no unrelated renames.
- **Dependencies.** Add them through the service's package manager (Gradle, uv, yarn) and commit
  the lockfile. Do not add new tools, frameworks or services without asking.
- **Destructive actions** (history rewrites on shared branches, deleting files or data, dropping
  DB tables, resetting volumes) require explicit user confirmation.
- Investigate before changing: read the code and existing tests, reproduce bugs, then fix.
- Code, comments, identifiers, commits and PR titles are in English.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
