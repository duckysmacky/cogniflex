# frontend/AGENTS.md

Cogniflex website: manual analysis interface and project landing. Early stage: only the Vite app
shell exists, the API integration and Docker setup do not yet. Read the root `AGENTS.md` first.

## Stack

- React **19** with the **React Compiler** (`babel-plugin-react-compiler`), TypeScript **5.9** (strict)
- Vite **8** (`@vitejs/plugin-react`), `normalize.css`
- ESLint 9 (flat config, `typescript-eslint`, `react-hooks`, `react-refresh`) + Prettier 3
- Node **24** (`.nvmrc`), package manager **yarn v1** (`yarn.lock`)

## Build and run

```bash
cd frontend
nvm use                 # Node 24
yarn install            # never npm/pnpm - do not create package-lock.json
yarn dev                # Vite dev server
yarn build              # tsc -b && vite build -> dist/
yarn lint               # yarn lint:fix to autofix
yarn format             # Prettier; yarn format:check in CI-style runs
yarn ts:check           # type check only
```

- API base URL comes from `VITE_API_BASE_URL` (root `.env`). Only `VITE_`-prefixed vars reach the
  browser, so never put secrets in them.
- Add packages with `yarn add` / `yarn add -D` and commit `yarn.lock`. Ask before adding UI
  frameworks, state managers, routers or CSS libraries - the stack is not settled yet. When in
  doubt, reuse what `extension/` already uses (Tailwind, axios, zustand, react-hook-form + zod).

## Layout

- `src/main.tsx` - entry; `src/App.tsx` - root component
- Grow it with conventional folders as needed: `src/api/` (HTTP client + backend calls),
  `src/components/` (reusable UI), `src/pages/` (screens), `src/hooks/`, `src/types/`, `src/utils/`.
  One component per file, `PascalCase.tsx` for components, `camelCase.ts` for the rest.

## Rules

- Strict TypeScript: no `any` (use `unknown` + narrowing), no `@ts-ignore`, no non-null `!`
  unless provably safe. Prefer `type` unions and literal types.
- Function components and hooks only. The React Compiler handles memoization, so do not sprinkle
  `useMemo` / `useCallback` / `memo` without a measured reason. Follow the Rules of Hooks.
- Keep API types in sync with backend DTOs (`backend/.../dto/`); all HTTP calls go through `src/api/`,
  never `fetch` directly inside components. Handle loading and error states.
- Verdicts are probabilistic: show confidence, never present a result as a definitive claim.
- User-facing text is Russian and English; code, identifiers and comments are English.
- Accessible markup: semantic elements, labels for inputs, `alt` for images.
- Do not edit the Vite/TS/ESLint/Prettier configs unless the task is about tooling.

## Checks before pushing

`yarn lint && yarn format:check && yarn build` must pass.
