# CLAUDE.md

<role>
You are a senior Kotlin developer with deep expertise in Kotlin 2.2.20+ and its ecosystem, specializing in coroutines, Kotlin Multiplatform, Android development, and server-side applications with Ktor. Your focus emphasizes idiomatic Kotlin code, functional programming patterns, and leveraging Kotlin's expressive syntax for building robust applications.
</role>

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

When trying to access a file inside the project, try `idea_*` mcp server first.
    - Always pass `"projectPath": "/Users/manuel/Repository/Binocular-wasm/binocular-backend-new"` when calling MCP!
When trying to access a file outside of the project, e.g.
    - `/Users/manuel/Library/Caches/JetBrains/*/tmp/**`
use `read` tool.

When trying to access anything from Git, use `git_git_*` mcp server

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

## Project Overview

Binocular is a backend system for analyzing Git repositories. It uses Kotlin with Spring Boot and integrates Rust through FFI (Foreign Function Interface) for performance-critical Git operations.

**Key Technologies:**
- Kotlin 2.2.20, Java 23
- Spring Boot 3.5.6
- Rust (via UniFFI for FFI bindings)
- Maven (multi-module project)
- Database options: PostgreSQL (via Hibernate) or ArangoDB

This project is a new implementation of the existing nodejs/typescript version found in `../binocular-backend`. 

**Prerequisites:**
- JDK 23, Maven, Docker (running — required for any test that hits an `infrastructure-*` module or `infrastructure-test`; they spin up containers via Testcontainers)
- `ktlint` installed (`mvn ktlint:check` / `mvn ktlint:format`) — see [README.md](./README.md)

**Deeper, per-module guidance:** 
Before working in `core/`, `cli/`, `domain/`, `infrastructure-arangodb/`, `infrastructure-sql/`, `infrastructure-test/`, `integration-test/vcs-indexer/`, `jgit/`, `web/` or `ffi/`, read the local `CLAUDE.md` in those directories.
They document load-bearing invariants (mapping sessions, the value-class `Iid` self-injection workaround, UniFFI binding regeneration rules) that this root file only summarizes.

## Architecture
The architecture follows a DDD approach by Vaughn Vernon.

### Hexagonal Architecture (Ports & Adapters)

The codebase follows hexagonal/clean architecture principles with clear separation of concerns:

```
domain/          → Core domain models (Repository, Commit, Branch, File, etc.)
core/            → Application logic and port interfaces (*InfrastructurePort)
infrastructure-sql/       → PostgreSQL adapter (implements ports)
infrastructure-arangodb/  → ArangoDB adapter (implements ports)
infrastructure-test/  → Contract Tests for infrastructure-* adapter regarding their conformance with the interface implementation
ffi/             → Rust FFI integration for Git operations (xor with jgit)
jgit/             → JGit integration for Git operations (xor with ffi)
integration-test/vcs-indexer → Contract Tests for ffi & jgit regarding their conformance with the interface implementation
cli/             → Spring Shell CLI application
web/             → GraphQL web API
```

The following modules must be considered production-application-grade: `cli`, `web`.
All other modules must be treated as library-grade code/software.
Treat each module accordingly in the sense of robustness.

#### Infrastructure
Each infrastructure module configures itself via an `*AppConfig.kt`, e.g. `com.inso_world.binocular.infrastructure.sql.SqlAppConfig`.
These configs can then be used in the application layer `web`/`cli` via its own application config, e.g. `com.inso_world.binocular.cli.config.SqlConfig` where the following things are needed:
- `@Configuration` to define the class as configuration
- `@Profile("postgres")` to only activate this configuration class with `spring.profiles.active=postgres`
- `@Import(SqlAppConfig::class)` importing the infrastructure config from the module itself (infrastructure is configuring itself!)

**Port Pattern:** The `core` module defines port interfaces that infrastructure modules implement. `BinocularInfrastructurePort<T, Iid>` is the base interface defining standard CRUD operations (`findAll`, `findByIid`, `create`, `update`, `saveAll`). Specialized ports (e.g., `RepositoryInfrastructurePort`, `CommitInfrastructurePort`) extend it with domain-specific queries.

**DELETE is intentionally unimplemented.** Delete methods on `BinocularInfrastructurePort` throw `UnsupportedOperationException` by default and are not yet implemented in any adapter. Do not silently override that without coordinating across all adapters and tests. See `core/CLAUDE.md` invariant #1.

When changes in `infrastructure-*` are done, follow these steps (non-negotiable)
- First cover these changes with unit tests (if appropriate)
- **Always** cover these changes with integration tests in `infrastructure-test`
- Changes are considered as finished when tests in `infrastructure-test` *and* `infrastructure-*` (arangodb/sql, depending on where the changes were made) pass
- Tests which failed before doing changes may still fail afterwards (no first-class focus)

## Build & Development

### Basic Build Commands

```bash
# Build everything
mvn clean install

# Build skipping tests
mvn clean install -DskipTests

# Build specific module (requires dependent modules to be installed already)
mvn clean install --pl cli

# Build specific module and build dependent modules too
mvn clean install --pl cli -am
```

### Running Applications

**CLI Application:**
```bash
cd cli
mvn spring-boot:run
```

**Web Application:**
```bash
cd web
mvn spring-boot:run
```

### Testing

Tests are tagged with `@Tag("unit")` (via `src/test/kotlin/com/inso_world/binocular/core/unit/base/BaseUnitTest.kt`) or `@Tag("integration")` (via `src/test/kotlin/com/inso_world/binocular/core/integration/base/BaseIntegrationTest.kt`)

Running tests:

```bash
# Run all unit tests
mvn test -Dgroups=unit

# Run all integration tests
mvn verify -Dgroups=integration

# Run specific test class
mvn test -Dgroups=unit -Dtest=YourTestClass

# Run specific test method
mvn test -Dgroups=unit -Dtest=YourTestClass#testMethod

# Generate coverage report
mvn verify jacoco:report -Dgroups=unit
open target/site/jacoco/index.html
```

**Integration tests** use Testcontainers to spin up PostgreSQL or ArangoDB instances automatically. **Docker must be running** or every `infrastructure-sql` / `infrastructure-arangodb` / `infrastructure-test` integration test will fail at startup.

### Database Profiles

The infrastructure modules support different databases:

- **PostgreSQL**: Use `-Dspring.profiles.active=postgres` spring-profile or `application-postgres.yaml` (via `application.yaml`)
- **ArangoDB**: Use `-Dspring.profiles.active=arangodb` spring-profile or `application-arangodb.yaml` (via `application.yaml`)

Tests typically use `application.yaml` in `src/test/resources/` which configures Testcontainers.

## Module Dependencies

Reactor modules (from parent `pom.xml`): `domain`, `core`, `ffi`, `jgit`, `infrastructure-sql`, `infrastructure-arangodb`, `infrastructure-test`, `cli`, `web`, `integration-test/vcs-indexer`.

```
domain (pure domain models)
  ↑
core (business logic + port interfaces)
  ↑
infrastructure-sql / infrastructure-arangodb (port implementations)
  ↑
cli / web (application entry points)
```

- `ffi` module depends on `core` and provides Git operations, xor with jgit
    - Spring Profile `gix`
- `jgit` module depends on `core` and provides Git operations, xor with ffi
    - Spring Profile `jgit`
- `cli` uses Spring Shell for interactive commands
- `web` is the Spring Boot GraphQL API server. The bundled ArangoDB dump used by the `rdf/` research prototype lives at `web/src/test/resources/realdata/db_dump/`.
- `infrastructure-test` unified module which can integration-test all infrastructure layers based on the active spring profile
    - Spring Profile `arangodb` for `infrastructure-arangodb`, Spring Profile `postgres` for `infrastructure-sql`
- `integration-test/vcs-indexer` runs the `GitIndexer` contract against either Git adapter via `-Dspring.profiles.active=gix` (ffi) or `-Dspring.profiles.active=jgit`.
- `rdf/` exists on disk as an untracked research prototype (RDF/MSR ontology work with Apache Jena) with its own `pom.xml`, but is **commented out** in the parent reactor — it is not built or tested by default.

## Key Conventions

### General
- No Java-style except for `jgit` module. Java-style required here!
- Use native Kotlin-style for all other modules

Writing new functions or updating/refactoring existing ones one should write comprehensive KDoc to document especially the following elements:
- Short description
- Semantics
- Invariants & requirements
- Trade-offs & guidance
- Example section
  Depending on what is added/refactored this list is not exhaustive.
  See existing tests in e.g. the `domain` module for exhaustive documentation.

- If it is applicable use BDD testing. Follow best practices in BDD.
- If test files are getting very long (> 500 lines) ask to either split by operation (`SaveOperation`, `UpdateOperation`, `ToEntity`, `ToDomain`, ...).
  - a) by inner classes annotated with `@Nested`
  - b) by separate classes per operation or responsibility
- Use `assertAll()` to group assertions of the same concept to test, split if they are checking different things.
- Use `assertThrows<...>` to check an exception is thrown, e.g. `assertThrows<IllegalArgumentException>{}` to check a `require` or `requireNotNull` is activated
- Use `assertDoesNotThrow` to check the inverse of above.
- Use `@ParameterizedTest` to avoid writing duplicate code with similar inputs

#### MappingSession / MappingContext (Identity Map Pattern)

This pattern will be deprecated, do not use it anymore.
Follow ID-based DDD by Vernon, hence mapping should be small and a thin-layer.
Ids in this case are special typed Kotlin-Value-classes.

To prevent duplicate entities and handle cyclic object graphs during mapping, the core module provides a custom Spring scope:

- **`@MappingSession`** annotation on a method/class activates an identity-map scope via AOP (`MappingSessionAspect`).
- **`MappingScope`** is a Spring custom scope that maintains a single application-wide identity map with a session-depth counter; the map is cleared when the outermost session ends.
- **`MappingContext`** is the per-session bidirectional cache: domain→entity keyed by `(domainClass, domain.uniqueKey)`, entity→domain keyed by `(entityClass, entity.id)`. First-write-wins; automatically promotes identity-based entries to ID-based entries once an entity is persisted.

Always annotate mapper or port methods that perform multi-entity mapping with `@MappingSession`.
Do not call `MappingScope.startSession()` directly; use the annotation.

### Test Organization

- Unit tests: Fast, isolated, no external dependencies (usually extend from `com.inso_world.binocular.core.unit.base.BaseUnitTest` if no other `Base*` class is used for the package)
- Integration tests: Use Testcontainers, test full infrastructure stack
    - usually extends from `com.inso_world.binocular.core.integration.base.BaseIntegrationTest` if no other `Base*` class is used for the package
    - can also extend from `com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest` (a subclass of `com.inso_world.binocular.core.integration.base.BaseIntegrationTest`) if there are some fixtures required for testing Git functionality
    - `BaseInfrastructurePortTest` provides contract tests verifying that port implementations conform to the port interface contract
- Test fixtures in domain module are exported as test-jar for reuse
- Use `MockTestDataProvider` (not the deprecated `TestDataProvider` singleton) to create fresh, isolated test data per test

#### Writing Tests
- Use mutation-testing via `pitest` to check for flaws
    - Maven command is `mvn test-compile org.pitest:pitest-maven:mutationCoverage` to run coverage for all modules. Add `--pl <modulename>` to just check a single module
    - No mutations may survive!
    - Adapt the configuration for kotlin specifics if required and it's not possible to kill mutants
- For complex code a C3/C4 coverage is the goal
- When writing tests, 80-85% coverage-range must be achieved.
- Add short 1-2 lines of KDoc for each test method giving more context about what, why, input, and expected output

## Docker & Deployment

```bash
# Start databases
docker-compose up postgres db

# The compose file defines services:
# - postgres (port 5432)
# - db (ArangoDB on port 8529)
# - binocular-init, binocular-backend containers
```

## Working with FFI

When modifying Rust code in `ffi/lib/`:

1. Make changes to Rust crates
2. Run `./build-all.sh` to compile for target platforms
3. Run `./uniffi-generate.sh` to regenerate Kotlin bindings
4. Update Kotlin wrapper code in `ffi/src/` if needed
5. Copy compiled `.dylib` or `.so` files to `ffi/src/main/resources/<platform>/`

The Rust workspace uses UniFFI's `#[uniffi::export]` macro to expose functions to Kotlin.

## Common Patterns
- DRY and SOLID principles must be followed strictly.
- If not stated or necessary otherwise keep classes kotlin `internal`.

### Modifying any files
After modifying a file, always run `ktlint -F <absolute file path>` on that touched file.
Any modified method or class must be updated with KDoc.
If a class/method has no KDoc so add it.
If the body of a class/method changed so update KDoc, it must always match the code.
This applies to all modules and must strictly be enforced

All changes must always be minimal git diff!

### Adding a New Domain Model

1. Create model in `domain/src/main/kotlin/com/inso_world/binocular/model/`
2. Define port interface in `core/src/main/kotlin/.../service/`
3. Implement entity in infrastructure module
4. Create mapper for domain ↔ entity conversion
5. Implement port in `*InfrastructurePortImpl.kt`

### Adding a New Test
Usually each module already has some other `Base*` classes which configure some environments already, e.g. `BaseServiceTest`.

#### Adding new Unit-Test
In case of adding a completely new Test class which does not rely on some other base class use:
```kotlin
class YourTest : BaseUnitTest() {
    @Test
    fun `test description`() {
        // test implementation
    }
}
```
#### Adding new Integration-Test
In case of adding a completely new Integration-Test class which does not rely on some other base class use:
```kotlin
class YourTest : BaseIntegrationTest() {
    @Test
    fun `test description`() {
        // test implementation
    }
}
```
