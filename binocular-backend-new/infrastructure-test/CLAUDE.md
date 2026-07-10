# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Purpose of this module

`infrastructure-test` is a **test-only module** that runs the same contract test suite against both infrastructure adapters (`infrastructure-sql` and `infrastructure-arangodb`). It has no `src/main` — only `src/test`. Production code lives in the adapter modules; this module exists so a single set of integration tests proves both adapters honour the port contracts defined in `core`.

The root `binocular-backend-new/CLAUDE.md` covers the surrounding hexagonal architecture, port pattern, mapping sessions, and global commands. Read it first; this file only documents the things specific to running and extending the contract tests.

### Test constraints
- It is absolutely forbidden to use `@Transactionl` for tests methods and classes.

## Running the tests

Tests pick which adapter to exercise from `src/test/resources/application.yaml`:

```yaml
spring:
  profiles:
    active: test,gix,arangodb   # swap arangodb ↔ postgres to test the other adapter
```

Profiles in play:

| Profile    | Activates                                                          |
|------------|--------------------------------------------------------------------|
| `arangodb` | `ArangodbTestConfig` via `LocalArangodbConfig` (Testcontainers)    |
| `postgres` | `SqlTestConfig` via `LocalPostgresConfig` (Testcontainers + Liquibase) |
| `gix`     | `LocalGixConfig` — scans `com.inso_world.binocular.ffi` so `GitIndexer` is wired up for fixture tests |
| `test`     | Standard Spring test marker                                        |

The two adapter profiles are **mutually exclusive**. Override on the CLI when you need to exercise the other one without editing the file:

```bash
# Run against PostgreSQL
mvn -pl infrastructure-test verify -Dgroups=integration -Dspring.profiles.active=test,gix,postgres

# Run against ArangoDB (matches the committed default)
mvn -pl infrastructure-test verify -Dgroups=integration

# Single test class / method
mvn -pl infrastructure-test test -Dgroups=integration -Dtest=RepositoryTest
mvn -pl infrastructure-test test -Dgroups=integration -Dtest=RepositoryTest#'create repository and find by iid'
```

**Docker must be running** — the active profile spins up either a PostgreSQL or ArangoDB container via Testcontainers. `ContainerChecks` validates this at startup with profile-guarded `@EnabledIf` assertions.

CI parity: the active profiles list in `application.yaml` is the single source of truth. If you add or rename a required profile, update `.github/workflows/backend-fast.yml` to match — there is a comment in `application.yaml` reminding you of this.

## Base test class hierarchy

Pick the right base class for what your test needs:

```
BaseInfrastructureSpringTest          ← TestDataProvider-backed data set up & torn down per test
   ├─ ContainerChecks                  (verifies the Testcontainer is up under its profile)
   └─ <most *Test classes>             (RepositoryTest, CommitTest, IssueTest, …)

BasePortNoDataTest                     ← Same wiring as above but tears the data down in @BeforeEach
                                         (use when you need an empty database)

BasePortWithDataTest                   ← Extends BaseFixturesIntegrationTest, adds GitIndexer + ProjectInfrastructurePort.
   │                                     `prepare(path, projectName, branchName)` indexes a fixture repo on disk
   │                                     and returns the persisted Project. Requires the `gix` profile.
   ├─ BasePortSimpleDataTest           pre-indexes the `simple` fixture into `simpleRepo`
   └─ BasePortOctoDataTest             pre-indexes the `octo`  fixture into `octoRepo`
```

`FIXTURES_PATH`, `SIMPLE_REPO`, and `OCTO_REPO` are inherited from `core`'s `BaseFixturesIntegrationTest` — that class lazily creates real Git repos on disk before any fixture-dependent test runs.

## Adding a new contract test

1. **No data needed** → extend `BaseInfrastructureSpringTest` and write tests that call the port directly. The mock data set up by `InfrastructureDataSetup` (driven by `MockTestDataProvider`) is already present.
2. **Empty database** → extend `BasePortNoDataTest`.
3. **Real Git history** → extend `BasePortSimpleDataTest` (simple linear history) or `BasePortOctoDataTest` (octopus merge fixture). Use `simpleRepo` / `octoRepo` as the entry point.

Conventions inherited from the project (also see root `CLAUDE.md`):

- Mark tests with `@Tag("integration")`; `BaseInfrastructureSpringTest` is wired for `@SpringBootTest`.
- Use `MockTestDataProvider` for fresh, isolated data per test — **not** the deprecated `TestDataProvider` singleton.
- Group long test classes with `@Nested` per operation (`SaveOperation`, `UpdateOperation`, etc.) — see the `commit/`, `project/`, `repository/` subpackages for examples.
- Use `assertAll {}` to cluster related assertions and `assertThrows<UnsupportedOperationException>` to verify the intentionally-unimplemented `delete*` and `clear()` port methods.

## Important invariants

- **DELETE is intentionally unimplemented.** Several tests assert `UnsupportedOperationException` from `delete*` / mutating-collection operations. Do not "fix" these — they enforce the contract documented in `core/CLAUDE.md` invariant #1. If you ever implement delete, change adapters, ports, and tests together.
- **Test classes are `internal`.** The whole module is package-private to its own test classpath; this is deliberate so the test wiring (e.g. `internal class TestApplication`) cannot leak into other modules.
- **`scanBasePackages` is explicit.** `TestApplication` scans `infrastructure.test` + `core`. Adapter beans come in through `LocalArangodbConfig` / `LocalPostgresConfig` `@Import`s, not through component-scan. Don't broaden the scan to `com.inso_world.binocular` or both adapters will fight for primary beans.
- **`NoteTestPostgresConfig` is dormant** (`@Profile("never-load-this")`, `@Deprecated`). The real SQL `NoteInfrastructurePort` replaced it; keep the file as reference only or delete in a dedicated cleanup PR.

## Where to look when something breaks

| Symptom                                                | Likely cause                                                                     |
|--------------------------------------------------------|----------------------------------------------------------------------------------|
| `Connection refused` to localhost:5432 / :8529         | Docker not running, or the wrong adapter profile is active                       |
| `No qualifying bean of type GitIndexer`                | Test needs the `gix` profile (only present in fixture-based tests)               |
| `DataSourceAutoConfiguration` errors under `arangodb`  | IDE put both adapters on classpath; `application-arangodb.yaml` already excludes them — re-import the project |
| Tests pass on one adapter, fail on the other           | Adapter divergence — fix the adapter, not the test. Both must satisfy the same port contract. |
