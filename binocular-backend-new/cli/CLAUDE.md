# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **Scope:** This file documents the `cli` Spring Shell module only. The parent `binocular-backend-new/CLAUDE.md` covers monorepo-wide concerns (hexagonal layering, port pattern, `MappingSession`, FFI/JGit rebuild flow, infrastructure test contract). Read it first — invariants documented there are assumed here and not repeated.

## What This Module Is

`cli` is one of two production-application-grade entry points (the other is `web`). It is a Spring Boot 3.5 + Spring Shell 3.4 app that wires together a Git adapter (`ffi` or `jgit`) and a persistence adapter (`infrastructure-sql` or `infrastructure-arangodb`) and exposes interactive shell commands to drive the indexers.

Entry point: `src/main/kotlin/com/inso_world/binocular/cli/BinocularCommandLineApplication.kt`. It is `WebApplicationType.NONE` by default — the only branch that flips it to `SERVLET` is the `h2` profile (legacy/debug). Do not assume an HTTP context exists.

`@CommandScan` is restricted to `com.inso_world.binocular.cli.commands` and `@ComponentScan` is explicitly listed (`cli` + `core.persistence` + `core.service`). When adding a new top-level package under `cli` that contains Spring beans, add it to the `@ComponentScan` list — auto-discovery does **not** cover sibling packages.

## Running

Invoke from the parent (`binocular-backend-new/`) so Maven resolves the reactor — running `mvn` inside `cli/` only works after the dependent modules are installed.

```bash
# Interactive shell (default profiles from application.yaml = postgres,gix)
../mvnw -pl cli spring-boot:run

# Headless one-shot command (Spring Shell parses trailing args as a command)
java -jar -Dspring.profiles.active=postgres,gix \
     -Djava.library.path=. \
     ./target/cli-0.0.1-SNAPSHOT.jar \
     index commits <repoPath> -b <branch> -n <projectName>

# Build the fat jar
../mvnw clean package -DskipTests -pl cli -am
```

`-Djava.library.path=.` is required when the FFI native libraries (`.dylib`/`.so`) are not on the default loader path. Skip it only when using the `jgit` profile.

## Spring Profiles — Required Combinations

This module composes orthogonal profile axes via the `@Profile`-gated configs in `cli/config/`. You **must** pick one from each axis or the app will start without an indexer or without a persistence backend (silently — there's no startup check).

| Axis        | Profile(s)         | Config class            | Effect                                                       |
|-------------|--------------------|-------------------------|--------------------------------------------------------------|
| Git adapter | `gix`              | `GixConfig`             | `@ComponentScan("com.inso_world.binocular.ffi")` — Rust FFI  |
| Git adapter | `jgit`             | `JGitConfig`            | `@ComponentScan("com.inso_world.binocular.jgit")` — pure JVM |
| Persistence | `sql` or `postgres`| `SqlConfig`             | `@Import(SqlAppConfig)` — Hibernate + Liquibase              |
| Persistence | `nosql` or `arangodb` | `ArangodbConfig`     | `@ComponentScan("…infrastructure.arangodb")`                 |

Note: `jgit` dependency is currently commented out in `pom.xml` (lines 77–81). Re-enable both the dependency block and `JGitConfig`'s profile registration when bringing JGit back.

The default in `src/main/resources/application.yaml` is `postgres,gix`. Tests use `test,gix,sql,postgres` (`src/test/resources/application.yaml`).

`application-postgres.yaml` has `liquibase.drop-first: true` — **every run with this profile wipes the schema**. This is fine for the indexer's "fresh load" use case but is the wrong default if you ever embed this module in a long-running process. Do not silently flip it.

## Commands

Only one command exists today: `index commits <repoPath> -b <branch> -n <projectName>` in `commands/Index.kt`. The class is `open` (not `final`) because Spring Shell's command proxying needs it; keep new command classes `open` for the same reason — the Kotlin `all-open` compiler plugin is configured but only opens Spring-stereotyped classes, and `@Command` is not in that set.

The path argument is resolved via `Paths.get(repoPath).toRealPath()` — symlinks are followed and the path must exist on disk before the command runs. There is no `--help` text customization beyond what `@Option(description = …)` provides.

To add a new command:
1. Create a class under `cli/commands/` annotated `@Command(command = ["<noun>"], group = "…")`. Keep it `open`.
2. Constructor-inject services from `cli/service/`. The shell's command discovery uses `@CommandScan`, so the class just needs to be on the classpath under that package.
3. Wire a method with `@Command(command = ["<verb>"])` and `@Option`-annotated parameters. Bean Validation (`@NotNull`, `@NotEmpty`) is honored.

## Service Layer

`cli/service/` contains thin orchestrators that sit *between* the shell command and the `core` ports. They are **not** the same as `core/service/*Service` — the latter are domain services. The CLI services translate CLI-level concerns (paths, project names, branch lookups) into `core` port calls.

- `VcsService` — coordinates `GitIndexer` (from the active Git profile) with `RepositoryService` to perform full or **incremental** indexing. Incremental traversal stops at the last known HEAD for a branch; see the KDoc on `VcsService.indexRepository` for the algorithm.
- `RepositoryService` — wraps `RepositoryInfrastructurePort`. Owns `transformCommits` which canonicalizes incoming commits by SHA and wires parent/child ID sets. With the recent refactor to ID-based references (commits `b57a544d6`..`c4f0b8d29`), commits hold `parentIds`/`childIds` rather than object references — do not reintroduce object-graph wiring here.
- `CommitService`, `ProjectService`, `UserService` — straight delegations to their respective ports plus convenience lookups (`getOrCreateProject`, etc.).
- `service/its/` — Issue and merge-request services. Currently unwired to any shell command; treat as scaffolding.

Two CLI-specific exception types (`CliException`, `ServiceException`) both extend `core.exception.BinocularException`. Wrap port/service failures in `ServiceException` inside services; convert to `CliException` at the command boundary (see `VcsService.findOrCreateRepository` for the pattern).

## Tests

Test packaging mirrors the parent's split:
- `unit/` — `@Tag("unit")`, fast, extend `BaseUnitTest` from the `core` test-jar.
- `integration/` — `@Tag("integration")`, Testcontainers-backed. Most extend `AbstractCliIntegrationTest` (loads the full Spring context with `SqlTestConfig.Initializer` for a Postgres container).
- `integration/shell/` — uses Spring Shell's `ShellTestClient` (`BinocularCommands` / `BuiltinCommands` helpers) to exercise commands as the shell would parse them.
- `performance/` — separate tree, not part of the standard `mvn test` / `mvn verify` flow; run by class name.

Run from the parent so reactor dependencies resolve:
```bash
../mvnw -pl cli test -Dgroups=unit
../mvnw -pl cli verify -Dgroups=integration
../mvnw -pl cli verify -Dgroups=integration -Dtest=VcsServiceTest#'specific test name'
```

The test `application.yaml` activates `test,gix,sql,postgres` — the `gix` profile means **the FFI native library must be loadable**, which requires `infrastructure-ffi`'s build to have run (`./build-all.sh` in `ffi/lib/` if the binaries aren't already in `ffi/src/main/resources/<platform>/`). If integration tests fail with `UnsatisfiedLinkError`, that's the cause — not a test bug.

`spring-shell-starter-test` is on the classpath with `mockito-core` and `junit-vintage-engine` **excluded** in `pom.xml`. Don't add Mockito back here; use hand-written fakes or MockK as the rest of the codebase does.