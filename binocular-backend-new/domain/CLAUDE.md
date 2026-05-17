# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Scope

This directory is the **`domain`** Maven module of the `binocular-parent` multi-module project located at `../`. It contains **pure domain logic only** — Kotlin entities, value objects, and domain validation — with no framework or infrastructure coupling (no Spring, no Hibernate, no DB drivers).

Adapters live in sibling modules (`infrastructure-sql`, `infrastructure-arangodb`, `cli`, `web`, `ffi`, `jgit`, `core`). When a change requires touching persistence, services, or transports, expect to cross module boundaries — do not pull those concerns into `domain`.

The full architectural reference is in [`README.md`](./README.md) (DDD/hexagonal layout, identity model, collection semantics, examples). Read it before non-trivial work; this file deliberately stays terse.

## Toolchain

- **JDK 23**, **Kotlin 2.2.20**, **Maven** (multi-module, parent at `../pom.xml`)
- **JUnit 5** + **AssertJ** for tests; **Hibernate Validator** for Jakarta Bean Validation
- **JaCoCo** + **PIT (pitest)** configured at the parent level for coverage and mutation testing
- Tests are grouped via JUnit tags: `unit`, `integration` (parent pom defines `unit-tests` / `integration-tests` profiles)

## Commands

Run from this `domain/` directory unless noted. Most commands require building dependencies from the parent first if you've never built locally:

```sh
# First-time build of the whole reactor (run from ../)
mvn -f ../pom.xml install -DskipTests

# Compile this module
mvn compile

# Run all unit tests in this module
mvn test -Dgroups=unit

# Run a single test class
mvn test -Dgroups=unit -Dtest=CommitModelTest

# Run a single test method
mvn test -Dgroups=unit -Dtest=CommitModelTest#equalsRespectsIidAndUniqueKey

# Coverage report (JaCoCo) — opens target/site/jacoco/index.html
mvn verify jacoco:report -Dgroups=unit

# Mutation testing (PIT) — opens target/pit-reports/*/index.html
mvn org.pitest:pitest-maven:mutationCoverage

# Package, including the test-jar consumed by other modules
mvn package
```

The module publishes a **test-jar** classifier — `MockTestDataProvider` and `DummyTestData` are reused as test fixtures in sibling modules. Keep their public surface stable.

## Architectural Invariants (do not break)

These rules are enforced by code and tests. Treat violations as bugs, not style nits.

1. **Dual identity via `AbstractDomainObject<Iid, Key>`** — every entity has an immutable `iid` (used for `hashCode`) and a `uniqueKey` (domain-natural key used for collection deduplication). `equals` requires both to match plus exact runtime class.
2. **Add-only collections (`NonRemovingMutableSet`)** — `remove`, `clear`, `retainAll`, and iterator `remove` throw `UnsupportedOperationException`. Dedup is by `uniqueKey`, first-inserted wins. Backed by `ConcurrentHashMap`.
3. **Repository consistency** — every collection on `Repository` validates `element.repository == this` and throws `IllegalArgumentException` (often surfaced as `RepositoryMismatchException`) on mismatch. Never bypass this in new collections.
4. **Set-once parent references** — `repository`, `project`, `author`, `committer`, etc. cannot be set to `null` and cannot be reassigned to a different instance. Same-instance reassignment is a no-op.
5. **Validate in `init { require(...) }`** in addition to Jakarta annotations. Constructors must fail fast for invalid state; do not rely solely on bean validation, which only runs when a `Validator` is invoked.
6. **Auto-registration on construction** — `Commit`, `Branch`, etc. register themselves with their `Repository` inside `init`. New entities that own a parent-side collection should follow this pattern so caller code doesn't need to remember a second `parent.children.add(self)` step.
7. **Sub-classed `data class`es must re-delegate `equals`/`hashCode` to the parent** (`super.equals` / `super.hashCode`) to avoid Kotlin's generated component-based equality, which breaks the identity contract.

## Conventions for New Domain Models

- Extend `AbstractDomainObject<Id, Key>` with a `@JvmInline value class Id(val value: Uuid)` and a `data class Key(...)`.
- Override `uniqueKey` to compute the natural key; keep it stable for the entity's lifetime.
- Use `NonRemovingMutableSet` (often via an anonymous subclass overriding `add` to inject repository checks) for any 1:N or N:M collection.
- Add KDoc covering: identity & equality, validation rules, set-once relationships, thread-safety, and at least one usage example. Existing models (e.g. `Commit.kt`, `Repository.kt`) are the reference template.
- Coverage targets: **80%+ line coverage and 80%+ mutation coverage**. Add tests under `src/test/kotlin/com/inso_world/binocular/model/` (model tests) and `.../model/validation/` (Jakarta validation tests). Use `MockTestDataProvider` for pre-wired graphs.

## Notes on the Wider Repository

- The top-level `/Users/manuel/Repository/Binocular-wasm/CLAUDE.md` describes a **different, legacy JS/TS backend** (`binocular-backend/`). It does not apply here. The Kotlin backend you're working on lives entirely under `binocular-backend-new/`.
- The current branch is `feature/backend-new`; recent commits refactored entities (`Project`, `Repository`, `MockTestDataProvider`) toward **ID-based references** rather than holding direct object references. When extending models, follow that direction — prefer `Foo.Id` over `Foo` for cross-aggregate references where appropriate.