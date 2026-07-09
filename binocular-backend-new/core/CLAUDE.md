# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Scope

This directory is the **`core`** Maven module of the `binocular-parent` multi-module project at `../`. It is the **hexagonal port layer**: it defines the abstract ports (`*InfrastructurePort` interfaces) that adapter modules implement, plus the cross-cutting persistence-mapping machinery (mapping session AOP, lazy-loaded relationship proxies, identity-mapping `MappingContext`, generic `Page<T>`).

`core` depends on `domain` and is consumed by adapter modules (`infrastructure-sql`, `infrastructure-arangodb`, `ffi`, `jgit`, `cli`, `web`). It must **not** import any concrete adapter. Spring is allowed here (this is where DI wiring and the custom `"mapping"` scope live); database drivers, ORMs, and HTTP transports are not.

The companion `domain/CLAUDE.md` covers entity invariants — read it before changing port signatures, since ports are typed against `AbstractDomainObject<Iid, *>`.

## Toolchain

- **JDK 23**, **Kotlin 2.2.20**, **Spring Boot 3.5.6**, **Maven** (parent at `../pom.xml`)
- **AspectJ Weaver 1.9.24** — required for `@MappingSession` `@Around` interception
- **JUnit 5** + Spring Boot test starter; mockk (via parent dependency management)
- Tests grouped by JUnit tags: `unit`, `integration` (parent defines matching `unit-tests` / `integration-tests` profiles)

## Commands

Run from this `core/` directory unless noted. Most commands require dependencies (`domain` test-jar etc.) installed locally:

```sh
# First-time reactor build (run from ../)
mvn -f ../pom.xml install -DskipTests

# Compile this module
mvn compile

# Unit tests only
mvn test -Dgroups=unit

# Integration tests only (requires shell + git on PATH for fixture setup)
mvn verify -Dgroups=integration

# Single test class
mvn test -Dtest=MappingContextTest

# Single test method
mvn test -Dtest=MappingContextTest#findEntityByUniqueKey

# Package (also produces the test-jar consumed downstream)
mvn package

# Module-level convenience (from ../, uses Makefile)
make core
```

The `Makefile` at `../Makefile` is the canonical entry point for CI-like runs (`make core`, `make combo-core-infra-sql`, etc.). Use it when crossing module boundaries — `mvn --pl` invocations without `-am` will fail if the dependent modules' artifacts aren't installed.

## Architectural Invariants

These are load-bearing. Treat violations as bugs.

1. **`BinocularInfrastructurePort<T, Iid>` is the single CRUD port shape.** Every aggregate gets a sub-interface (`RepositoryInfrastructurePort`, `ProjectInfrastructurePort`, …). `findById(String)` is deprecated — new code uses `findByIid(iid: Iid)` with the typed value-class identifier. DELETE methods throw `UnsupportedOperationException` by default and are explicitly **not implemented yet** — do not silently override that without coordinating across all adapters.
2. **Aggregate ports over entity ports.** `CommitInfrastructurePort` is deprecated; commits are accessed through `RepositoryInfrastructurePort`. When adding a new query, decide which aggregate root owns it first.
3. **Mapping sessions are mandatory for adapter→domain mapping.** Beans scoped `"mapping"` (notably `MappingContext`) throw if accessed outside an active session. `MappingSessionAspect` opens/closes sessions via `@MappingSession` (function or class annotation) using AspectJ `@Around`. Sessions are reference-counted (nestable); outermost exit clears the identity map.
4. **Value-class `Iid` + `@MappingSession` requires the self-injection workaround.** Kotlin mangles JVM names for functions taking value classes (e.g. `Repository.Id`), which breaks Spring AOP's `@annotation` pointcut. Implementations must: inject `self` (the proxied bean), override `findByIid` to delegate to a non-private `findByIidInternal` via `self`, and annotate the internal method with `@MappingSession`. The kdoc on `BinocularInfrastructurePort.findByIid` documents this — copy that pattern for any new value-class-typed entry point. See [KT-31420](https://youtrack.jetbrains.com/issue/KT-31420).
5. **`EntityMapper<D, E>` is stateless; identity is per-session.** Use `MappingContext.findDomain` / `findEntity` / `remember` to dedupe object graphs. Mappers must keep `toDomain(toEntity(d)) ≡ d` and create lazy proxies (`RelationshipProxyFactory`) for relationships rather than eagerly loading them.
6. **Lazy proxies are thread-safe, initialize-once.** `LazyList`, `LazySet`, `LazyMutableSet`, `LazyReferenceImpl` use double-checked locking. `LazyMutableSet` has a `postProcessing` hook fired once after first load — use it to re-register elements with parent collections, never for side effects on every access.
7. **Exception hierarchy is split deliberately.** `BinocularException` → `BinocularInfrastructureException` (service/adapter failures) | `BinocularIndexerException` (VCS mining) | `PersistenceException` (mapper/DAO failures). There are **two** `NotFoundException`s — `service.exception.NotFoundException` for missing aggregates at the service boundary, `persistence.exception.NotFoundException` for the DAO layer. Throw the one matching your layer; don't merge them.
8. **`@Valid` annotations on port methods are real.** Jakarta validation runs at the AOP boundary on `@Validated` beans; constructor-level `init { require(...) }` is the domain's defensive duplicate. Do not strip `@Valid` to silence violations.

## Test Fixtures (consumed by adapter modules)

The `maven-jar-plugin` `test-jar` execution publishes the test classes (excluding `**/octo/**`). Downstream modules (`ffi`, `jgit`, `infrastructure-*`) pull this jar with `<classifier>tests</classifier>`. Stable surface:

- `BaseUnitTest` (`@Tag("unit")`, fixed `Clock`, ID constants)
- `BaseIntegrationTest` / `BaseFixturesIntegrationTest` (`@Tag("integration")`; the latter shells out to `src/test/resources/fixtures/*.sh` to construct real git repos under `simple`, `advanced`, `octo`, `mailmap` keys, transparently extracting them from the JAR when consumed from another module)
- `MockTestDataProvider` (**deprecated** in this module — use `com.inso_world.binocular.domain.data.MockTestDataProvider` from `domain`'s test-jar instead; this copy holds singleton state and leaks across tests)
- `TestDataProvider` (also deprecated, same reason)
- `Extensions.reset()` (test-only reflection helpers to clear `NonRemovingMutableSet` and `MappingContext` between tests)
- `InfrastructureDataSetup`, `BaseInfrastructurePortTest` (contracts adapter tests extend)

Keep the non-deprecated surface stable. Breaking these signatures cascades to every adapter test.

## Conventions for New Ports

- Sub-interface `BinocularInfrastructurePort<Aggregate, Aggregate.Id>` — always use the aggregate root and its value-class id.
- For value-class `Iid` methods that touch the mapping layer, apply the self-injection + `findByIidInternal` pattern (invariant #4).
- Domain-specific queries: name them by what they return, not what they filter (`findByName`, `findExistingCommits`, `findHeadForBranch`).
- `Page<T>` (from `core.persistence.model`) is the pagination wrapper, not Spring Data's `Page`. Construct it via the `(content, totalElements, pageable)` secondary constructor when adapting from Spring repositories.
- Spring `@ConfigurationProperties` lives under `core.config.*` and is exposed via `BinocularConfig` (`prefix = "binocular"`). When adding a property, update `src/main/resources/META-INF/additional-spring-configuration-metadata.json` so the IDE/Spring boot processor sees it.

## Wider Repository Notes

- The top-level `/Users/manuel/Repository/Binocular-wasm/CLAUDE.md` and the symlinked `binocular-backend-new/CLAUDE.md` describe a **legacy JS/TS backend** (`binocular-backend/`). They do not apply here. Kotlin work lives entirely under `binocular-backend-new/`.
- Current branch is `feature/backend-new`; recent commits refactored domain references to **ID-based** (e.g. `Project.repoId` instead of a direct `Repository` reference). Extend that direction in new ports: prefer accepting `Foo.Id` over `Foo` where the call site only needs the identifier.