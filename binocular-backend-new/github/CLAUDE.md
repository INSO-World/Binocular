# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

See the root [CLAUDE.md](../CLAUDE.md) first for repo-wide conventions (KDoc requirements, ktlint-on-save, `mvn` commands, test tagging).

## Module Overview

`github` is a **library-grade** Maven module (Kotlin) that talks to the GitHub GraphQL API (`https://api.github.com/graphql`) to fetch assignable users and issues (with timeline events) for a repository. It depends on `core` (for `BinocularException`) and Spring WebFlux (reactive `WebClient`/`Mono`).

It is registered in the parent reactor (`../pom.xml` `<modules>`) but is **not yet wired into `cli` or `web`** — nothing in those modules imports from `github` yet.

### Key classes
- `client/GraphQlClient.kt` — thin reactive POST wrapper around the GitHub GraphQL endpoint; maps 4xx/5xx into `ServiceException`, 90s timeout.
- `service/GitHubService.kt` — builds the actual GraphQL queries (`loadAllAssignableUsers`, `loadIssuesWithEvents`) and paginates via `PageInfo`/cursor recursion until `hasNextPage` is false.
- `config/BinocularRcLoader.kt` — locates and parses the legacy `.binocularrc` JSON file (walking up parent directories, or an explicit path) to obtain the GitHub token. **This is known/marked-in-code as a stopgap**: it duplicates the old Node/TS backend's config format instead of a Spring `application*.yaml`. There is a `// TODO create github.yaml` in the file — do not extend this loader with new fields; a proper YAML-based config is the intended replacement.
- `config/WebClientConfig.kt` — provides the `WebClient` bean.
- `dto/` — Jackson data classes mirroring the GitHub GraphQL response shapes (`issue/` and `user/` subpackages), plus shared `PageInfo`.
- `exception/ServiceException.kt` — module's `BinocularException` subtype for GitHub API errors.

## Testing

Tests live under `src/test/kotlin/unit/` (note: package is `unit`, not mirroring `com.inso_world.binocular.github.*`, and does **not** extend the root `core` module's `BaseUnitTest`/`@Tag("unit")` convention — this module predates/bypasses that pattern). Fixtures are plain JSON files in `src/test/resources/response/` used to simulate paginated GraphQL responses (`*HasNextPage.json` / `*NoNextPage.json`).

Run just this module's tests:
```bash
mvn test --pl github
```

## Conventions specific to this module
- Follow native Kotlin style (per root CLAUDE.md — this module is not the `jgit` Java-style exception).
- Any new config values should go through a proper `application-github.yaml` + `*AppConfig.kt`, following the pattern used by `infrastructure-sql`/`infrastructure-arangodb` (see root CLAUDE.md "Infrastructure" section), rather than adding more fields to `BinocularRcLoader`'s `.binocularrc` parsing.