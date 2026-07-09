# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Scope

This directory is the **`ffi`** Maven module of the `binocular-parent` multi-module project at `../`. It is an **adapter** implementing the `GitIndexer` port (defined in `core.index`) by delegating to a Rust library built on top of [`gix`](https://crates.io/crates/gix). Kotlin code is a thin marshalling layer over UniFFI-generated bindings; all real git work happens in the Rust crates under `lib/`.

The companion adapter is sibling `jgit/` (JGit-based implementation of the same port). They are mutually exclusive at runtime — `integration-test/vcs-indexer` runs the same contract under Spring profile `gix` (this module) or `jgit`.

`ffi` depends on `domain` + `core` (and their test-jars). It must **not** depend on other adapters. Read `core/CLAUDE.md` before changing port shapes and `domain/CLAUDE.md` before changing entity construction patterns (this module's extension functions construct `Commit`, `Branch`, `Repository`, `Developer`).

## Toolchain

- **JDK 23**, **Kotlin 2.2.20**, **Spring Boot 3.5.6**, **Maven** (parent at `../pom.xml`)
- **Rust 2021 edition**, **gix 0.75.0**, **UniFFI 0.30** (Cargo workspace at `lib/`)
- **JNA 5.17.0** as the runtime native loader
- **JUnit 5** + AssertJ + Spring Boot test starter
- `ktlint` (parent-level plugin) — `mvn ktlint:check` / `mvn ktlint:format`

Sources: `src/main/kotlin`, tests: `src/test/kotlin` (configured in `pom.xml`, not Maven defaults).

## Commands

Run from this `ffi/` directory unless noted.

```sh
# First-time reactor build (run from ../) — required because ffi depends on domain + core test-jars
mvn -f ../pom.xml install -DskipTests

# Build + verify this module (preferred entry point)
make -C .. ffi                  # == mvn clean verify --pl ffi (from parent)

# Module-only Maven invocations
mvn compile
mvn test                        # all Kotlin tests
mvn verify                      # full module gate, includes integration tests

# Single Kotlin test class / method
mvn test -Dtest=FfiIntegrationTest
mvn test -Dtest=FfiIntegrationTest#'hello should execute without errors'

# Rust workspace (under lib/)
cd lib && cargo build --release           # builds libgix_binocular for the host triple
cd lib && cargo test                      # runs Rust integration + per-crate tests
cd lib && cargo test -p commits           # single crate

# Cross-module verification using the gix profile
make -C .. vcs-indexer-ffi      # runs integration-test/vcs-indexer with -Dspring.profiles.active=gix
```

The `Makefile` at `../Makefile` is the canonical entry point. Plain `mvn --pl ffi` without `-am` fails if `domain`/`core` artifacts (including their `tests` classifiers) are not installed locally.

### Native library build & placement

The compiled native library is **checked in** under `src/main/resources/{target-triple}/` (currently `aarch64-apple-darwin`, `x86_64-apple-darwin`, `x86_64-unknown-linux-gnu`, `aarch64-unknown-linux-gnu`, `x86_64-pc-windows-gnu`). After Rust changes, rebuild and copy the artifact into the matching resource directory before running JVM tests — Maven does not invoke `cargo` for you.

UniFFI Kotlin bindings are generated into `src/main/kotlin/com/inso_world/binocular/ffi/internal/gix_binocular.kt` (~3.4k lines). Generation is driven by `lib/uniffi.toml` (`package_name = "com.inso_world.binocular.ffi.internal"`) and the `uniffi-bindgen` binary defined in `lib/uniffi-bindgen.rs`. **Do not hand-edit `gix_binocular.kt`** — regenerate it.

## Architectural Invariants

These are load-bearing.

1. **`GixIndexer` is the sole public entry point.** It is a Spring `@Service` implementing `core.index.GitIndexer`. All Rust calls go through `com.inso_world.binocular.ffi.internal.*` (UniFFI-generated). Other Kotlin classes must not call `internal.*` directly except for the extension/`pojos` helpers and tests.
2. **Native library loading is centralized.** `util.Utils.loadPlatformLibrary("gix_binocular")` detects OS/arch, maps to a target triple, and sets `uniffi.component.gix_binocular.libraryOverride` to the resource path **before** UniFFI tries to load. It throws `IllegalStateException` if the resource is absent. Tests must call this from a `@BeforeAll` (see `FfiIntegrationTest.loadLibrary`).
3. **Generated bindings live in `internal/`.** `internal/gix_binocular.kt` is regenerated from Rust and must not be edited. If you need to adjust a type, change the Rust side (`lib/src/types/*.rs`, `#[derive(uniffi::Record|Enum|Object)]`) and regenerate.
4. **Translation happens in `extensions/` + `pojos/`.** UniFFI POJOs (`GixCommit`, `GixBranch`, `GixRepository`, …) are converted to domain entities via `toDomain` extension functions. Domain → UniFFI conversion lives in the `pojos` package (`Repository.toFfi()`, `GixRepository.toModel(project)`). Keep this layering: domain entities never carry UniFFI types, and Rust-facing code never imports `com.inso_world.binocular.model.*`.
5. **SHA strings are validated where they cross the boundary.** `GixCommit.toDomain` calls `String.validateSha()` (40 hex chars) on both `oid` and every parent SHA. Do not skip the validation when adding new commit-producing paths.
6. **Identity dedup is the caller's responsibility.** The collection-form `Collection<GixCommit>.toDomain(...)` accepts a `shaIndex` and `developerRegistry` so the same `Commit` / `Developer` instance is reused across a traversal. Parent/child wiring uses domain `iid` references (see invariant #4 in `core/CLAUDE.md` on the ID-based domain model). Single-commit conversions should pass through a shared registry whenever called in a loop.
7. **Configuration goes through `GixModuleConfig`.** Extends `core.BinocularConfig` (which is `@ConfigurationProperties(prefix = "binocular")`). The two flags exercised today are `binocular.vcs.skipMerges` and `binocular.vcs.useMailmap` — both threaded through to every Rust call. New flags get added to `BinocularConfig` in `core`, not here.
8. **Errors map across FFI as `UniffiException.*`.** Rust returns `Result<_, UniffiError>` (`lib/src/types/error.rs`) which surfaces in Kotlin as a sealed hierarchy: `GixDiscoverException`, `ReferenceException`, `RevisionParseException`, `ObjectException`, `CommitLookupException`, `GixException`. Catch these specifically; do not wrap in `FfiException` unless you are at the service boundary translating to `BinocularIndexerException`.

## Rust Workspace Layout (`lib/`)

- `lib/src/lib.rs` — re-exports the FFI surface and calls `uniffi::setup_scaffolding!()`. The `#[uniffi::export]` functions are split across `lib/src/ffi/{repository,branch,commit,diff,blame,utils}.rs`.
- `lib/src/types/*.rs` — UniFFI record/enum/object definitions. Edits here change the Kotlin POJO shape.
- `lib/crates/commits` — commit lookup + history traversal logic. Has its own integration test crate at `lib/crates/commits/tests/` with `git/` fixtures.
- `lib/crates/diff`, `lib/crates/blame` — per-feature crates, each with their own `tests/` sub-crate.
- `lib/crates/shared` — common helpers (`signature`, `tz_utils`). Reused across `commits`/`diff`/`blame`.
- `lib/tests/ffi_integration_tests.rs` — end-to-end Rust tests of the public FFI surface.
- `lib/uniffi-bindgen.rs` — entry point for the bindgen binary (`cargo run --bin uniffi-bindgen ...`).

## Test Layout

- `src/test/kotlin/com/inso_world/binocular/ffi/unit/extensions/` — pure `toDomain` / `toModel` conversion tests (no native lib).
- `src/test/kotlin/com/inso_world/binocular/ffi/unit/lib/` — unit-level FFI tests over `BaseLibraryUnitTest`.
- `src/test/kotlin/com/inso_world/binocular/ffi/integration/` — `FfiIntegrationTest` (full UniFFI surface), `GitIndexerTest` (`GixIndexer` ↔ domain), `PerformanceTest`.
- `FfiIntegrationTest` extends `core.integration.base.BaseFixturesIntegrationTest` from `core`'s test-jar. Fixtures (`SIMPLE_REPO`, `OCTO_REPO`, `ADVANCED_REPO`) are real git repos built by shell scripts shipped inside the `core` test-jar; the base class extracts them to `FIXTURES_PATH`.
- Spring context for tests is `BinocularFfiTestApplication` (`scanBasePackages = ["com.inso_world.binocular.ffi"]`). Test config defaults live in `src/test/resources/application.yaml` (sets `skip_merges: false`, `use_mailmap: true`, trace logging for the `ffi` package).

## Conventions for New FFI Functions

1. Define the Rust side first: types in `lib/src/types/`, the `#[uniffi::export]` function in `lib/src/ffi/{module}.rs`, errors as variants of `UniffiError`.
2. `cargo build --release` and copy `target/release/libgix_binocular.{dylib,so,dll}` into every relevant `src/main/resources/{triple}/` directory you support.
3. Regenerate the Kotlin bindings — overwrite `internal/gix_binocular.kt`.
4. Add a `toDomain` extension under `extensions/` if the new POJO becomes a domain entity; add a `toFfi`/`toModel` under `pojos/` if a domain object flows into Rust.
5. Wire it through `GixIndexer` (or extend `GitIndexer` in `core` if it is a new operation — that affects `jgit` too).
6. Cover: a Rust unit/integration test in the appropriate crate, a Kotlin unit test in `unit/`, and a `FfiIntegrationTest` case using the fixture repos.

## Wider Repository Notes

- The repo-root `Binocular-wasm/CLAUDE.md` and the symlinked `binocular-backend-new/CLAUDE.md` describe the **legacy JS/TS backend** (`binocular-backend/`). They do not apply here — Kotlin/Rust work lives entirely under `binocular-backend-new/`.
- Current branch `feature/backend-new` — domain refactor to ID-based references is in progress. New conversion code should accept/return `Foo.Id` (e.g. `Repository.Id`, `Project.Id`) rather than full entity references where the call site only needs the identifier. See `core/CLAUDE.md` and recent commits on this branch.