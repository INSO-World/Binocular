# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Scope

This file covers the `infrastructure-sql` Maven module only. For the cross-module architecture (hexagonal layout, port pattern, Spring profiles, FFI vs JGit), the `@MappingSession` rule, and the global `core/CLAUDE.md` invariants, read `../CLAUDE.md` first. Do not duplicate that content here — extend it.

## Module Purpose

`infrastructure-sql` is the **PostgreSQL adapter** for the hexagonal `core` ports. It is library-grade (not application-grade), activated by the `postgres` Spring profile and configured by `SqlAppConfig`. Persistence uses Spring Data JPA + Hibernate; schema is owned by **Liquibase** (`ddl-auto: none` is non-negotiable — Hibernate must never auto-create tables).

Counterpart adapter: `../infrastructure-arangodb/`. Contract tests for both live in `../infrastructure-test/` and must pass after any port behavior change here. See parent CLAUDE.md → "Infrastructure" for the workflow.

## Build & Test

All commands assume the working directory is the module root (`infrastructure-sql/`). Docker must be running for any integration test — Testcontainers spins up a real `postgres:18-alpine`.

```bash
# Build this module (assumes parent/dependent modules already installed via `mvn install` at repo root)
mvn clean install --pl . -am

# Unit tests only (mapper tests, no container)
mvn test -Dgroups=unit

# Integration tests — spins up Testcontainers Postgres
mvn verify -Dgroups=integration

# Single test class / method
mvn test -Dgroups=unit -Dtest=ProjectMapperTest
mvn verify -Dgroups=integration -Dtest=ProjectInfrastructurePortImplTest#someMethod

# Mutation testing (no surviving mutants allowed for changed code — see parent CLAUDE.md)
mvn test-compile org.pitest:pitest-maven:mutationCoverage --pl .

# Coverage report
mvn verify jacoco:report -Dgroups=unit
open target/site/jacoco/index.html
```

After changing port behavior here, the corresponding contract tests in `../infrastructure-test/` MUST be run with the `postgres` profile and pass. Treat that as the definition of done.

## Local Architecture

```
src/main/kotlin/.../infrastructure/sql/
├── SqlAppConfig.kt          Spring config: @EnableJpaRepositories, @EntityScan, @ComponentScan, @EnableAspectJAutoProxy
├── assembler/               Aggregate-root orchestrators (ProjectAssembler, RepositoryAssembler)
├── mapper/                  Per-entity domain↔entity converters implementing core's EntityMapper<D,E>
├── persistence/
│   ├── entity/              JPA entities; all extend AbstractEntity<Id, Key>
│   ├── repository/          Spring Data JPA repositories
│   ├── dao/                 IDao-based wrappers (used by AbstractInfrastructurePort)
│   └── converter/           AttributeConverters (e.g. KotlinUuidConverter for kotlin.uuid.Uuid ↔ java.util.UUID)
├── service/                 *InfrastructurePortImpl — the port implementations
└── exception/               Module-local exceptions (e.g. IllegalMappingStateException)

src/main/resources/db/changelog/   Liquibase changelogs, organized by YYYY/MM and included via db.changelog-master.yaml
src/test/resources/                application.yaml (active profiles=test,postgres; drop-first=true) + application-postgres.yaml
```

### Mapper vs Assembler (load-bearing distinction)

Documented in `README.md`; restating the rule because it controls how a change should be split:

- **Mappers** are **structure-only**. A mapper converts one domain object ↔ one entity. It does NOT recurse into child aggregates and assumes any parent reference is already present in `MappingContext`. Mappers belong at the leaves of aggregate assembly.
- **Assemblers** are **aggregate orchestrators**. An assembler walks the aggregate root (Project → Repository → Commits/Branches/Users), calls the right mappers in the right order, wires bidirectional links, and is the place where `MappingContext`-based identity preservation is enforced.

When a relation crosses an aggregate boundary, change the assembler. When only one entity's columns change, change the mapper. Never make a mapper recurse — that is the assembler's job.

### Identity Preservation

`AbstractEntity<Id, Key>` defines a `uniqueKey` field that is the basis for `equals`/`hashCode` (id is a fallback). This pairs with the per-session `MappingContext` (from `core`) keyed by `(domainClass, domain.uniqueKey)` and `(entityClass, entity.id)`. Any new port or assembler method that maps a non-trivial graph MUST be annotated `@MappingSession` (see parent `../CLAUDE.md` → "MappingSession / MappingContext"). Do not call `MappingScope.startSession()` directly.

### Port Base Class

`AbstractInfrastructurePort<D, E, I>` is the shared base for `*InfrastructurePortImpl`. Note the explicit invariants encoded there:

- `findById(I)` throws `UnsupportedOperationException` — use `findByIid` (the domain-side stable identity). `id` is the JPA surrogate and must not leak to callers.
- `delete(...)` / `deleteByEntityId(...)` throw `UnsupportedOperationException`. Delete is intentionally unimplemented across ALL adapters (see parent CLAUDE.md → core invariant #1). Do not implement it in this module alone.

### UUIDs

Domain models use Kotlin's `kotlin.uuid.Uuid` (experimental). Persistence uses `java.util.UUID`. `KotlinUuidConverter` (auto-applied) bridges the two — do not introduce ad-hoc conversion at call sites.

## Liquibase Conventions

- Master file: `src/main/resources/db/changelog/db.changelog-master.yaml`. It uses `includeAll` per month folder (`2025/09`, `2025/11`, `2025/12`, …).
- To add a migration: create a new YAML file under the current month directory (`db/changelog/<YYYY>/<MM>/<DD>-<NN>-<description>.yaml`). It is picked up automatically by `includeAll`; do not edit the master.
- Triggers / raw SQL live under `db/changelog/trigger/` (e.g. `prevent_cycles_trigger.sql`).
- Tests run with `drop-first: true` and `ddl-auto: none` (see `src/test/resources/application.yaml`) — every schema change must be expressed as a changelog, never as a JPA annotation that relies on auto-DDL.

## Testing Patterns Specific to This Module

- **Integration tests** extend `BaseRepositoryTest` (for repository/DAO tests) or `BaseServiceTest` (for port tests). Both bootstrap `SqlTestConfig`, which starts the Testcontainers `PostgreSQLContainer` via `SqlTestConfig.Initializer` and only when the `postgres` profile is active. The container is a `companion object` — reused across the test class but reset between classes via `@DirtiesContext(BEFORE_CLASS)`.
- **Per-test cleanup** is done in `@AfterEach` via `SqlInfrastructureDataSetup.teardown()` inside a `TransactionTemplate` block (flush + clear + teardown). Do not rely on Spring transaction rollback for cleanup — JPA's first-level cache must be drained explicitly to avoid stale-entity false positives.
- **Mapper unit tests** extend `BaseMapperTest` and run under `@Tag("unit")` without a container.
- **Contract conformance** lives in `../infrastructure-test/` — when you add a port method, the contract test in that module is what proves both SQL and ArangoDB adapters behave identically. A green test here is necessary but not sufficient.

## Common Pitfalls

- Forgetting `@MappingSession` on a new port method that maps an aggregate → produces duplicate child entities or `IllegalStateException("... must be in context")` from the assemblers.
- Adding a JPA relation expecting `ddl-auto` to create the column — schema is Liquibase-owned. Add a changelog.
- Implementing `delete*` in this adapter only — breaks the cross-adapter contract. Coordinate via parent CLAUDE.md → core invariant #1 before touching it.
- Calling `findById` instead of `findByIid` — the former throws.
- Mapping a child aggregate inside a `*Mapper` — push that into the corresponding `*Assembler` instead.