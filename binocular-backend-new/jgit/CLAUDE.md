# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Scope

This directory is the **`jgit`** Maven module of the `binocular-parent` multi-module project at `../`. It is an **adapter** implementing the `GitIndexer` port (defined in `core.index`) on top of [Eclipse JGit](https://www.eclipse.org/jgit/). Unlike its sibling `../ffi/`, this module is **pure Java** — there is no Rust workspace, no native library, and no UniFFI binding step.

The companion adapter is sibling `../ffi/` (gix/Rust-based implementation of the same port). They are **mutually exclusive at runtime** — selection happens through the Spring profile (`jgit` here, `gix` for `../ffi`). `../integration-test/vcs-indexer` runs the same `GitIndexer` contract under either profile.

`jgit` depends on `domain` + `core` (and `core`'s test-jar). It must **not** depend on the `ffi` adapter or any infrastructure module. Read `../core/CLAUDE.md` before changing port shapes and `../domain/CLAUDE.md` before changing how `Commit`/`Branch`/`Repository`/`Developer`/`Signature` are constructed.

## Toolchain

- **JDK 23**, **Java sources** (no Kotlin in this module), **Spring Boot 3.5.6**, **Maven** (parent at `../pom.xml`)
- **JGit 7.4.0.202509020913-r** (`org.eclipse.jgit:org.eclipse.jgit`)
- **JUnit 5** (`junit-jupiter-api` + `junit-jupiter-params`)
- Test bases come from `core`'s test-jar: `BaseUnitTest`, `BaseIntegrationTest`
- `ktlint` from the parent applies to Kotlin only — this module has no Kotlin sources, so `mvn ktlint:check` is a no-op here

Sources: `src/main/java`, tests: `src/test/java` (configured explicitly in `pom.xml`).

## Commands

Run from this `jgit/` directory unless noted. As with `ffi`, plain `mvn --pl jgit` without `-am` fails if `domain`/`core` (including their `tests` classifiers) are not installed locally.

```sh
# First-time reactor build (run from ../) — installs domain + core (and their test-jars)
mvn -f ../pom.xml install -DskipTests

# Module-only Maven invocations
mvn compile
mvn test                                # all unit tests in this module
mvn verify                              # includes integration tests (IT-suffixed classes)

# Single test class / method
mvn test -Dtest=JGitConfigTest
mvn test -Dtest=MailmapTest#'parse_emptyFile_returnsEmptyMailmap'

# Mutation coverage (pitest is wired in pom.xml, scoped to com.inso_world.binocular.jgit.*)
mvn test-compile org.pitest:pitest-maven:mutationCoverage --pl jgit -am

# Cross-module contract test against this adapter
mvn -f ../pom.xml verify --pl integration-test/vcs-indexer -am -Dspring.profiles.active=jgit
```

`*IT.java` classes (e.g., `GitDepsTreeOnCurrentRepoIT`) run during the `verify` phase via Failsafe (inherited from the parent). They typically operate on the current checkout via `user.dir`, gated with `Assumptions.assumeTrue(...)`, so they degrade to skips rather than fail outside a git working tree.

## Architectural Invariants

These are load-bearing.

1. **`JGitGitIndexer` is the sole port implementation.** It is a Spring `@Service` annotated `@Profile("jgit")` so it only registers when the `jgit` profile is active. The `ffi` module's `GixIndexer` plays the same role under `@Profile("gix")` — never have both active in the same context. Do not add a second `GitIndexer` bean in this module.
2. **Repository discovery mirrors gix.** `JGitGitIndexer.openRepository(Path)` reproduces gix's discovery semantics: it accepts a `.git` dir, a worktree dir, a worktree gitlink file, and a bare repo, walking parent directories until a match is found. This is intentional so the `vcs-indexer` contract tests pass identically on both adapters — keep it aligned with the FFI behavior when changing it.
3. **`Mailmap` is read once per top-level call, not per commit.** `read(jgitRepo)` runs at the entry of `traverseBranch`/`findAllBranches`/`findCommit`/`traverse` and the result is passed down. Do not push the `Mailmap.read` call into `createCommit` — that file-IO must not happen inside the per-commit loop.
4. **Identity dedup is the responsibility of `mapCommits`.** The two-pass mapping in `mapCommits` builds `commitsBySha` and `developersByKey` so that within a single traversal the same `Commit` / `Developer` instance is reused, and parent wiring goes through domain `iid` references (`Commit.Id`, `Repository.Id`, `Developer.Id`) — see `../core/CLAUDE.md` for the ID-based domain model invariant. Single-commit paths (`findCommit`, `findCommitInternal`) create a fresh developer cache; that is fine because callers do not chain them into a loop.
5. **`traverse` deliberately does not apply `skipMerges`.** Comment in `JGitGitIndexer.traverse` calls this out explicitly: range traversal must match the FFI implementation, which never filters merges. Only `traverseBranch` honors `binocular.vcs.skipMerges`.
6. **Mailmap mapping preserves original timestamps.** `createCommit` swaps the `PersonIdent` for name/email but keeps `rc.getAuthorIdent()` / `rc.getCommitterIdent()` as the timestamp source. Don't refactor this into a single mapped ident — `Mailmap.map` rebuilds the `PersonIdent` with `getWhenAsInstant()`, but the `gitSignature` string assembled afterwards must use the canonical name+email, not the original.
7. **Configuration goes through `JGitConfig`.** Extends `core.BinocularConfig` (which is `@ConfigurationProperties(prefix = "binocular")`). The flags exercised today are `binocular.vcs.skipMerges` and `binocular.vcs.useMailmap`. New flags get added to `BinocularConfig` in `core`, not here — `ffi` and `jgit` must read the same property names.
8. **Errors map onto `JGitException.*`.** The hierarchy (`DiscoverException`, `ReferenceException`, `TraversalException`, `OperationFailedException`) intentionally mirrors `UniffiException.*` from `ffi` so the `vcs-indexer` contract tests can assert the same failure modes for both adapters. When adding a failure path, throw the matching subtype rather than a bare `RuntimeException`.

## Subpackage: `tree/` (GitDepsTree visualization)

`tree/` contains a small helper that turns a `List<Commit>` into a lane-based `GitDepsTree` (`GitDepsTreeBuilder`) and an ASCII renderer (`GitDepsTreeAsciiGraphRenderer`) for human inspection — think `git log --graph`. It is **not** part of the `GitIndexer` port contract; it is auxiliary tooling used by the integration tests (`GitDepsTreeOnCurrentRepoIT`, `GitDepsTreeOnCurrentRepoGraphIT`) to dump visualizations under `cli/target/git-deps-trees/`.

These ITs are parameterized via system properties:

- `-Dbinocular.repoPath=/path/to/repo` (default `user.dir`)
- `-Dbinocular.branch=<branch>` (default: `main`, falling back to `master`, then the first branch returned)
- `-Dbinocular.commitLimit=<n>` (default `250`)

If you change the `GitDepsTree` model, the source of truth is `domain` (`com.inso_world.binocular.model.git.GitDepsTree`/`GitTreeNode`/`GitTreeEdge`/`EdgeType`); update `domain` first and let this builder follow.

## Test Layout

- `src/test/java/.../jgit/JGitConfigTest.java` — pure config getter/setter coverage (extends `BaseUnitTest`).
- `src/test/java/.../jgit/MailmapTest.java` — `.mailmap` parsing & mapping, uses `@TempDir`.
- `src/test/java/.../jgit/GitDepsTreeOnCurrentRepoIT.java` — integration test that builds a real tree from the surrounding checkout; uses `Assumptions.assumeTrue` to skip when no repo is found.
- `src/test/java/.../jgit/tree/` — builder + renderer tests, including a graph-rendering IT.

There is currently no `JGitGitIndexerTest` covering the indexer directly in this module — the cross-adapter contract is exercised by `../integration-test/vcs-indexer` under `-Dspring.profiles.active=jgit`. If you add jgit-specific unit tests for the indexer (e.g., to cover the discovery fallback in `openRepository`), follow the existing `@Nested` + `assertAll` style used in `JGitConfigTest` and `MailmapTest`.

## Conventions for Changes Here

1. New behaviors on the `GitIndexer` port are added in `core` first, then implemented in **both** `jgit` and `ffi`. Do not let the two implementations drift.
2. JGit calls that open a repo must go through `JGitGitIndexer.openRepository(Path)` (or be added to it) so worktree/bare-repo/gitlink discovery stays consistent.
3. Keep Java idioms here — this module deliberately stays Kotlin-free to keep the JGit surface uncluttered. Kotlin types from `domain` (e.g., `Commit.Id`, `kotlin.Pair`, `kotlin.uuid.Uuid`) are consumed directly; do not introduce wrapper classes.
4. When a test would require a fresh on-disk repo, prefer building one with JGit's `Git.init()` + commit helpers (see the disabled `GitDepsTreeBuilderTest.buildsTreeWithMergeEdgesAndBranchNames` for the pattern) over checking in fixture binaries.