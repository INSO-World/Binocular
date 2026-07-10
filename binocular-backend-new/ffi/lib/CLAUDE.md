# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Scope

This directory is the **Rust workspace** that backs the sibling `ffi/` Maven module. Kotlin/Java never enters here — everything compiles to a single `cdylib` (`libgix_binocular.{dylib,so,dll}`) plus a UniFFI-generated Kotlin file. Both artifacts must be hand-copied into the Maven module after a rebuild; nothing in `mvn` invokes `cargo`.

Read `../CLAUDE.md` first for the FFI module's invariants (centralized lib loading, generated-bindings rules, error mapping across the boundary). This file documents the Rust side only.

## Workspace Layout

Cargo workspace root is `Cargo.toml` here. Crate graph:

```
gix-binocular (root, cdylib)         ← UniFFI #[uniffi::export] surface
 ├── src/ffi/*.rs                    ← FFI entry points (the only public Rust API consumed by Kotlin)
 ├── src/types/*.rs                  ← UniFFI Record/Enum/Object definitions + custom_type adapters
 ├── tests/ffi_integration_tests.rs  ← End-to-end tests against the public FFI surface
 └── uniffi-bindgen.rs               ← Bin target for `cargo run --bin uniffi-bindgen ...`

crates/
 ├── shared/    ← Sig (gix signature wrapper), tz_utils. Leaf crate, no deps on siblings.
 ├── commits/   ← Commit lookup + history traversal. Depends on `shared`.
 │   └── tests/ ← Separate `commits-test` crate (autotests=false on parent) — runs via `cargo test -p commits-test`.
 ├── diff/      ← `binocular-diff` — pair-wise diffing with rayon. Depends on `shared`, `commits`.
 │   └── tests/ ← Separate test crate with shell-script fixtures (`fixtures/make_*_repo.sh`).
 └── blame/     ← `binocular-blame` — blame computation. Depends on `shared`, `commits`.
```

Sub-crates set `autotests = false` and `include = ["src/**/*"]`, so their `tests/` dirs are wired in as standalone test crates (each with its own `Cargo.toml`), not normal integration tests. This keeps each crate's `cargo test -p <name>` fast and lets the test crates depend on `parameterized`, `pretty_assertions`, etc. without polluting the production crate's dependency tree.

## Commands

All from this `lib/` directory unless noted.

```sh
# Compile the workspace (debug)
cargo build

# Production build — produces target/release/libgix_binocular.{dylib,so,dll}
cargo build --release

# Run all tests across the workspace
cargo test

# Single crate
cargo test -p commits
cargo test -p binocular-diff
cargo test -p binocular-blame
cargo test -p shared

# The separate test crates (live under crates/<name>/tests/)
cargo test -p commits-test

# Root-crate integration tests only (tests/ffi_integration_tests.rs)
cargo test --test ffi_integration_tests

# Single test by name (substring match)
cargo test test_find_repo_with_valid_path

# Regenerate the Kotlin UniFFI bindings — overwrites
# ../src/main/kotlin/com/inso_world/binocular/ffi/internal/gix_binocular.kt
cargo build --release
cargo run --bin uniffi-bindgen -- generate \
    --library target/release/libgix_binocular.dylib \
    --language kotlin \
    --out-dir ../src/main/kotlin
```

Substitute the matching `.so` / `.dll` filename on Linux / Windows. UniFFI uses the library's embedded metadata to drive bindgen, so the bindings always match the just-built cdylib.

### Native library deployment

`cargo build --release` only writes to `target/release/`. To make tests in the Maven module pick the new lib up:

```sh
# macOS arm64 example — repeat for every triple under ../src/main/resources/ you support
cp target/release/libgix_binocular.dylib ../src/main/resources/aarch64-apple-darwin/
```

Supported triples currently checked in: `aarch64-apple-darwin`, `x86_64-apple-darwin`, `aarch64-unknown-linux-gnu`, `x86_64-unknown-linux-gnu`, `x86_64-pc-windows-gnu`. The Kotlin `Utils.loadPlatformLibrary` selects one of these at runtime — see `../CLAUDE.md` invariant #2.

## FFI Surface (the public Rust API)

These are the only `#[uniffi::export]` functions surfaced to Kotlin. Adding to or changing this list requires a binding regeneration + native-lib redeploy. Each function is wired up in `src/lib.rs` `pub use` statements; if a function isn't re-exported there, `tests/ffi_integration_tests.rs` won't see it.

| Function | File | Purpose |
|---|---|---|
| `hello` | `src/ffi/utils.rs` | Connectivity smoke test |
| `find_repo(path)` | `src/ffi/repository.rs` | Discover repo, collect remotes |
| `find_all_branches(repo)` | `src/ffi/repository.rs` | Enumerate local + remote branches |
| `find_commit(repo, hash, use_mailmap)` | `src/ffi/commit.rs` | Single commit lookup by revspec |
| `traverse_history(repo, source, target?, use_mailmap)` | `src/ffi/commit.rs` | Walk commits source → target (or root) |
| `traverse_branch(repo, branch, skip_merges, use_mailmap)` | `src/ffi/branch.rs` | Branch + its commits as `BranchTraversalResult` |
| `diffs(repo, pairs, max_threads, algorithm?)` | `src/ffi/diff.rs` | Parallel pair-wise diffs |
| `blames(repo, defines, algorithm?, max_threads)` | `src/ffi/blame.rs` | Blame for `{commit → [paths]}` map |

## Type System Rules

- **`#[uniffi::Record|Enum|Object]`** lives in `src/types/`. Edits here propagate to Kotlin only after `uniffi-bindgen generate` is rerun.
- **`uniffi::custom_type!` adapters** in `src/types/` lift/lower foreign Rust types (`gix::ObjectId` ↔ `String`, `gix::bstr::BString` ↔ `String`, `gix::refs::FullName` ↔ `BString`, `PathBuf` ↔ `String`). Every gix-native type that crosses the FFI needs one of these; otherwise UniFFI codegen fails.
- **`#[uniffi::remote(...)]`** in `src/lib.rs` / `src/types/signature.rs` is how foreign-crate types (`anyhow::Error`, `log::Level`, `gix::date::Time`, `gix::actor::Signature`) get UniFFI treatment without owning them.
- **Errors** all flow through `types::error::UniffiError` (sealed enum with `#[derive(thiserror::Error, uniffi::Error)]`). When adding a gix error origin, also add a `From<...> for UniffiError` impl in `src/types/error.rs` — that's what lets `?` work in the FFI functions. The `commits::CommitLookupError` impl pattern-matches into specific `UniffiError` variants; preserve that granularity.
- **`ProcErrorInterface`** in `src/types/error.rs` is a `uniffi::Object` left for compatibility; do not extend it.

## Conventions

- Keep `#[uniffi::export]` functions in `src/ffi/`; keep type/marshalling code in `src/types/`. The pure business logic lives in the sub-crates (`commits`, `diff`, `blame`) and should be reusable from non-FFI Rust callers (it is — by their own `tests/` test crates).
- `ThreadSafeRepository::try_from(gix_repo)?.to_thread_local()` is the standard entry pattern in `src/ffi/*` — every FFI function that needs to do gix work goes through it (see `repository.rs`, `commit.rs`, `branch.rs`, `diff.rs`, `blame.rs`).
- SHA-string validation does **not** happen here on the Rust side — `gix::ObjectId::from_hex` enforces format on lower. Kotlin-side validation (`String.validateSha()`) is an additional check; see `../CLAUDE.md` invariant #5.
- Logging uses `log` + `env_logger`. Tests in `crates/commits/tests/` initialize a logger; the workspace's own tests do not — add `env_logger::try_init()` if you need trace output.
- `thiserror` is the workspace's only error-derive crate. `anyhow` is allowed inside the sub-crates' internals, but the FFI surface must return `UniffiError`.

## Adding a New FFI Function

1. Add the Rust types in `src/types/` (`#[derive(uniffi::Record|Enum|Object)]`). Add `custom_type!` adapters for any gix-native field type.
2. Add the `#[uniffi::export]` function in the appropriate `src/ffi/<module>.rs`.
3. Add the `pub use` line in `src/lib.rs` next to the existing re-exports.
4. If a new gix error variant can surface, add a `From<...> for UniffiError` impl in `src/types/error.rs`.
5. `cargo test` — at minimum extend `tests/ffi_integration_tests.rs`. If the logic is non-trivial, also add a crate-level test in `crates/<name>/tests/`.
6. `cargo build --release`, copy the lib to every triple under `../src/main/resources/`, regenerate `gix_binocular.kt`.
7. Wire the Kotlin side per `../CLAUDE.md` (extension/POJO conversion, `GixIndexer` integration).

## Troubleshooting

- **`uniffi-bindgen generate` produces an empty / stale file** — make sure you rebuilt the cdylib first (`cargo build --release`); bindgen reads metadata from the library binary, not from source.
- **Kotlin tests fail with `UnsatisfiedLinkError`** — the native lib in `../src/main/resources/<triple>/` is out of sync with `gix_binocular.kt`. Rebuild and recopy.
- **`commits` crate compile errors after gix bump** — check `gix-object` is on a version compatible with the new `gix` (it's pinned separately in `crates/commits/Cargo.toml`).
- **Test crate not running** — the per-crate `tests/` are separate Cargo packages (`commits-test`, etc.). Plain `cargo test -p commits` does **not** run them; use `cargo test -p commits-test` or `cargo test` from the workspace root.