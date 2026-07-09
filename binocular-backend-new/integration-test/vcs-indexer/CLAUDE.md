# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> Read the parent `binocular-backend-new/CLAUDE.md` first — it covers the hexagonal architecture, the `GitIndexer` port, the `gix` vs `jgit` profiles, fixture conventions, and ktlint rules that this module relies on.

## Purpose

`integration-test/vcs-indexer` is a **test-only Maven module** (`vcs-indexer-test`). It contains no production code under `src/main` — only `src/test`. Its job is to run a single contract suite against the `core` module's `GitIndexer` port using either Git adapter (`ffi`/gix or `jgit`), selected at runtime via Spring profile.

If you find yourself adding production code here, you're in the wrong module — port implementations belong in `ffi/` or `jgit/`.

## Architecture

### How the adapter selection works

`VcsIndexerTestApplication` is a `@SpringBootApplication` whose `scanBasePackages` deliberately covers only `com.inso_world.binocular.indexer.vcs.config` and `com.inso_world.binocular.core` — **not** `ffi` or `jgit`. The active profile decides which adapter is loaded:

- Profile `gix` activates `config/GixTestConfig` → `@ComponentScan("com.inso_world.binocular.ffi")` → wires `GixIndexer`
- Profile `jgit` activates `config/JGitTestConfig` → `@ComponentScan("com.inso_world.binocular.jgit")` → wires `JGitGitIndexer`

Both classes implement `GitIndexer`; tests `@Autowired` the interface, so the same test code runs against both. Default profile in `src/test/resources/application.yaml` is `jgit`.

### Fixtures

Test repos (`simple`, `octo`, `advanced`, `mailmap`) are **not** in this module. They live in `core/src/test/resources/fixtures/` and arrive here via the `domain` and `core` `tests` classifiers (see `pom.xml`). `BaseFixturesIntegrationTest.FIXTURES_PATH` resolves them from the classpath and, when packaged as a JAR, extracts them to a temp directory and chmod's `*.sh` executable — relevant for cross-module runs where fixtures aren't loose on disk.

The repo SHAs/branch counts/parent counts asserted in `GitIndexerTest` are tied to those fixture build scripts. **Touching a fixture script in `core` will silently break the `@CsvSource` parameter rows here.** Update both.

### Spring lifecycle quirks

Every test class uses `@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)`. This is intentional — different classes set different `binocular.vcs.*` properties via `@TestPropertySource`, and without dirtying the context Spring would reuse a context bound to a previous flag value. Don't remove this annotation when copying a test class.

`GitIndexerTest.BranchOperations` is annotated `@Order(Int.MAX_VALUE)` and uses `@TestMethodOrder` because one of its tests calls `addCommit()` — it permanently mutates the `simple` fixture's working tree by appending a real commit. It must run last so earlier SHA assertions still hold.

### Tests that hit the host repository

`GitIndexerTest.Integration#complete workflow - Binocular` and the entire `CompareToGit` nested class run against `Path("./")` — the current working directory, expected to be the Binocular repo itself. They assert exact commit counts on `origin/main` / `origin/develop` (e.g. `2580 commits`) and shell out to `git log --use-mailmap` to cross-check committer/author groupings. These tests will fail outside a clone of Binocular with those remote refs fetched. They are **not** isolated fixtures — be aware before running locally.

## Commands

All commands assume you're at the repo root (`binocular-backend-new/`). The module isn't useful in isolation — it depends on `core`, `ffi`, `jgit`, and `domain` being installed first.

```bash
# First-time build of dependencies + this module
mvn install -DskipTests --pl integration-test/vcs-indexer -am

# Run the full contract suite against JGit (default profile)
mvn verify --pl integration-test/vcs-indexer -Dgroups=integration

# Run against the FFI (gix) adapter — switches the active profile
mvn verify --pl integration-test/vcs-indexer -Dgroups=integration -Dspring.profiles.active=gix

# Run a single test class against one adapter
mvn verify --pl integration-test/vcs-indexer -Dgroups=integration \
  -Dspring.profiles.active=jgit -Dtest=SkipMergesTest

# Single test method
mvn verify --pl integration-test/vcs-indexer -Dgroups=integration \
  -Dtest=GitIndexerTest#'complete workflow - find repo, traverse branch, find commits'

# After editing any .kt file in this module
mvn ktlint:format --pl integration-test/vcs-indexer
```

The suite must be green on **both** profiles before changes to `GitIndexer`, `GixIndexer`, or `JGitGitIndexer` are considered done. A change that passes on `jgit` but breaks `gix` (or vice versa) is a contract violation.

## Adding tests

- Extend `BaseFixturesIntegrationTest` (from `core`'s test-jar). Don't roll your own `@SpringBootTest` setup — it won't pick up the profile-driven adapter wiring.
- Inject `GitIndexer`, not a concrete adapter. The whole point of this module is adapter-agnostic tests.
- When asserting commit counts/SHAs, parameterize across fixtures (`SIMPLE_REPO`, `OCTO_REPO`, `ADVANCED_REPO`, `MAILMAP_REPO`) via `@ParameterizedTest` + `@CsvSource` — matches the existing style and makes adapter divergence obvious in the failure output.
- Toggle vcs flags with `@TestPropertySource(properties = ["binocular.vcs.skip-merges=true"])` (or `use-mailmap`); pair each flag-on test class with a `*Baseline` / `*Disabled` companion that asserts the inverse, like `SkipMergesTest` ↔ `MergeCommitsBaselineTest` and `MailmapTest` ↔ `MailmapDisabledTest`. This prevents silently passing when the flag has no effect.
- If the assertion is exception-type-specific, prefer `assertThrows<Exception>` over the concrete type — `gix` throws `UniffiException` (checked) while `jgit` throws `JGitException` (runtime), and tests have to cover both.