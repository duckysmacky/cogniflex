# extension/AGENTS.md

Chrome extension (Manifest V3), the main Cogniflex client: highlights likely AI-generated text on
any page in real time and offers manual text/media analysis in the popup. Read the root `AGENTS.md` first.

## Stack

- React **19** with the **React Compiler**, TypeScript **5.9** (strict), Vite **8** + **CRXJS**
  (`@crxjs/vite-plugin`, manifest in `manifest.config.ts`)
- Tailwind CSS **4** (`@tailwindcss/vite`, `tailwind-merge`), `@tabler/icons-react`, `@floating-ui/dom`
- axios (HTTP), zustand + `zustand-chrome-storage` (state), react-hook-form + zod (forms), react-router-dom
- ESLint 9 + Prettier 3 (with `prettier-plugin-tailwindcss`)
- Node **24** (`.nvmrc`), package manager **yarn v1** (`yarn.lock`)

## Build and run

```bash
cd extension
nvm use                 # Node 24
yarn install            # never npm/pnpm - do not create package-lock.json
yarn start              # Vite dev build with HMR; load dist/ as an unpacked extension
yarn build              # dist/ + release/cogniflex-extension-<version>.zip
yarn lint               # yarn lint:fix to autofix
yarn format             # Prettier; yarn format:check in CI-style runs
yarn ts:check           # type check only
```

- Env is read from the repo root (`envDir: '..'`): `VITE_API_BASE_URL`. Only `VITE_`-prefixed vars
  are bundled, and everything bundled is public - never put secrets in them.
- The release workflow builds the zip and attaches it to the draft GitHub release; `dist/` and
  `release/` are build output and never committed.
- Add packages with `yarn add` / `yarn add -D` and commit `yarn.lock`. Ask before adding new
  frameworks or replacing an existing library.

## Layout

- `manifest.config.ts` - MV3 manifest (permissions, content script, service worker); version comes from `package.json`
- `src/background.ts` - service worker: receives `ChromeRuntimeMessage`s and performs API calls
- `src/content.ts` - content script: finds text candidates, sends them for analysis, renders highlights and tooltips
- `src/main.tsx`, `src/App.tsx` - popup UI; screens live in `src/sections/<name>/` (`analyze`, `settings`)
- `src/api/` - axios instance, `api.ts` endpoints, `types.ts` response types, `proxy/` for
  content-script -> background calls
- `src/components/` - shared UI (`ui/` primitives, `header/`, `footer/`), one folder per component with `index.tsx`
- `src/stores/` - zustand stores (`settings/`), persisted to `chrome.storage`
- `src/i18n/` - `en.ts` / `ru.ts` dictionaries and language resolution
- `src/entities/`, `src/types/`, `src/utils/` - domain types, shared types, helpers
- New files are `kebab-case` (`settings-section.tsx`, `settings.store.ts`); import through
  the `@/` alias and folder `index.ts` barrels.

## Rules

- Strict TypeScript: no new `any` (use `unknown` + narrowing), no `@ts-ignore`, no non-null `!`
  unless provably safe.
- Function components and hooks only. The React Compiler handles memoization, so do not sprinkle
  `useMemo` / `useCallback` / `memo` without a measured reason. Follow the Rules of Hooks.
- All HTTP goes through `src/api/`. The content script must call the backend only via
  `apiProxyRequest` (background service worker), never axios/`fetch` directly. New endpoints get a
  method in `api.ts` and an entry in `API_PROXY_METHODS_MAP`.
- Keep API types in `src/api/types.ts` in sync with backend DTOs (`backend/.../dto/`).
- Every user-facing string goes into **both** `i18n/en.ts` and `i18n/ru.ts`; no hardcoded UI text.
- Content script runs on every page: keep it light, debounce analysis, skip hidden/non-text nodes,
  never break page layout or scripts, and namespace every DOM attribute/id with `cogniflex`.
- Privacy: only send text the user's settings allow, never log page content, and do not add
  manifest permissions or host permissions without asking.
- Verdicts are probabilistic: show confidence, never present a result as a definitive claim.
- Style with Tailwind utility classes; merge conditional classes with `tailwind-merge`.
- Do not edit the Vite/TS/ESLint/Prettier configs or the manifest unless the task is about them.

## Checks before pushing

`yarn lint && yarn format:check && yarn ts:check && yarn build` must pass; load `dist/` in Chrome
and check the popup and an affected page manually.
