# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Binocular is a software engineering data visualization tool. It indexes data from git repositories, GitHub/GitLab APIs, and CI systems into ArangoDB, then serves interactive D3-based visualizations via a web interface. It runs as a CLI from within a git repository.

## Monorepo Structure

The project has two main packages (no Lerna/Nx — just npm scripts with `postinstall`):

- **`binocular-backend/`** — Node.js/TypeScript CLI and Express server. Indexes VCS/ITS/CI data into ArangoDB, hosts a GraphQL proxy, and serves the frontend.
- **`binocular-frontend-new/`** — React 19 + Vite + TypeScript frontend. Uses Redux Toolkit + Redux-Saga for state, D3 for visualizations, Tailwind CSS + DaisyUI for styling. This is the active frontend.
- **`binocular-frontend/`** — Legacy React 18 frontend (is phased out). No root scripts point here anymore.
- **`foxx/`** — ArangoDB Foxx service providing the GraphQL interface. Auto-installed into ArangoDB at startup.

## Commands

### Install
```sh
npm install          # Installs root, backend, and frontend-new via postinstall hook
```

### Development
```sh
npm run dev                  # Backend + frontend (Unix, uses &)
npm run dev:concurrently     # Backend + frontend (Windows)
npm run dev:backend          # Backend only (tsx watch binocular.ts run)
npm run dev:frontend         # Frontend only (Vite dev server on port 8080)
```

### Lint
```sh
npm run check        # ESLint on backend + frontend-new
npm run fix          # ESLint auto-fix on backend + frontend-new
```

### Test
```sh
npm run test                 # Backend + frontend tests
npm run test:backend         # Backend only: mocha 'test/*test.ts'
npm run coverage             # NYC coverage for backend + frontend
```

Backend tests use Mocha + Chai + Sinon. To run a single backend test file:
```sh
cd binocular-backend && npx mocha --config=.mocharc.json --exit --reporter spec 'test/SPECIFIC_test.ts'
```

### Build
```sh
npm run build:offline        # Standalone offline build (PouchDB, outputs to dist/)
```

## Architecture

### Three-Component Design

1. **Backend** (`binocular-backend/binocular.ts`) — CLI entry point using Commander. Runs indexers that collect data from git (isomorphic-git), GitHub/GitLab APIs (Octokit), and CI systems, then stores it in ArangoDB.
2. **Foxx GraphQL Service** (`foxx/`) — Installed as an ArangoDB Foxx service at startup. Provides GraphQL queries over the indexed data.
3. **Frontend** — React/Redux app that queries data via GraphQL (proxied through the backend to avoid CORS). The backend proxies `/graphQl`, `/api`, and `/wsapi` (WebSocket via Socket.io) endpoints.

### Backend Key Directories
- `indexers/` — Data collectors: `vcs/` (git), `its/` (GitHub/GitLab issues), `ci/` (GitHub/GitLab/Travis CI)
- `models/` — ArangoDB document models (`Model.ts` base class) and edge connections
- `endpoints/` — Express route handlers (GraphQL proxy, progress, DB export)
- `cli/` — Commander CLI commands (run, build, setup, export)
- `core/db/` — Database connection management
- `utils/context.ts` — Application context (config, DB connection, repo info)

### Frontend Key Directories (binocular-frontend-new)
- `src/plugins/` — Plugin system for visualizations and data sources
  - `dataPlugins/` — Data source adapters
  - `visualizationPlugins/` — Individual visualization implementations
  - `pluginRegistry.ts` — Plugin registration
- `src/redux/` — Redux store with `reducer/` (parameters, data, export, settings) and `middleware/`
- `src/components/` — UI components (dashboard, tabs, settings, export dialog)
- `src/utils/databaseLoaders.ts` — Handles loading from ArangoDB (online) or PouchDB (offline)

### Offline Mode
The frontend supports offline builds using PouchDB instead of ArangoDB. Triggered by `PRE_CONFIGURE_DB=pouchdb` env var during build. Uses `vite-plugin-conditional-compiler` for compile-time switching. Exported JSON databases go in `binocular-frontend-new/db_export/`.

### Database
ArangoDB with collections: commits, files, issues, builds, users, accounts, branches, mergeRequests, milestones, modules, notes. Edge collections model relationships (commits-files, commits-users, issues-commits, etc.). See `docs/DATABASE.md` for the full schema.

## Prerequisites

- Node.js v22.21.1 (see `.nvmrc`)
- ArangoDB 3.12 — run via `docker compose up db` or `docker run -d --name binocular_db -p 8529:8529 -e ARANGO_NO_AUTH=1 arangodb:3.12`

## Configuration

Project config is in `.binocularrc` (JSON, read by the `rc` module). Key settings:
- `arango` — DB host/port/credentials
- `github.auth` / `gitlab` — API tokens
- `indexers.its` / `indexers.ci` — Which indexer to use (github/gitlab)
- `ignoreFiles` — Glob patterns for files to skip during indexing
- `fileRenameBranches` — Branches on which to track file renames

Run `binocular setup` for a configuration wizard.

## Code Style

- **Transitioning from JavaScript to TypeScript** — new code should be TypeScript
- Backend: ESLint + Prettier — single quotes, 140 char width, 2-space indent, unix line endings
- Frontend: ESLint 9 + Prettier + React plugins
- Use Prettier for formatting
