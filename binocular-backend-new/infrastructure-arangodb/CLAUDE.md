# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> Scope: the `infrastructure-arangodb` module — the ArangoDB adapter that implements the `*InfrastructurePort` interfaces declared in `core/`. The repo-level [`../CLAUDE.md`](../CLAUDE.md) covers the big picture (hexagonal architecture, ports/adapters, mapping sessions, profile model). Read it first; this file documents only what's specific to or load-bearing for this module.

## Quick Commands

All commands assume CWD is `infrastructure-arangodb` unless noted; Docker must be running for any integration test.

```bash
# Build only this module (parent must already be installed)
mvn install --pl . -am

# Unit tests
mvn test -Dgroups=unit

# Integration tests — boots an ArangoDB 3.12 Testcontainer (reused across runs)
mvn verify -Dgroups=integration

# One test class / method
mvn test -Dgroups=unit -Dtest=DomainModelAlignmentTest
mvn verify -Dgroups=integration -Dtest=ContainerCheck#"check if arangodb container is running"

# Static analysis & format (from repo root)
mvn ktlint:check
mvn ktlint:format

# Mutation coverage — no mutations may survive
mvn test-compile org.pitest:pitest-maven:mutationCoverage --pl infrastructure-arangodb
```

Active profile for this adapter is `arangodb`. The test `application.yaml` activates `test,arangodb` and explicitly excludes SQL/JPA autoconfig so IDE classpaths that include `infrastructure-sql` don't break the boot.

## Layer Map

The naming gets confusing fast — there are three parallel hierarchies. Don't mix them up.

```
model/edge/                    → DB-AGNOSTIC edge domain types (data classes, no annotations).
                                 Used by edge DAO signatures and as the public surface for relationship objects.
                                 Example: CommitFileConnection.

persistence/entity/            → ArangoDB-ANNOTATED entities. @Document, @PersistentIndexed,
                                 @Ref, @Relations. Constructors are open via the all-open plugin
                                 (configured in pom.xml for @Document).
persistence/entity/edges/      → @Edge entities with @From/@To referencing document entities.

persistence/repository/        → Spring Data ArangoRepository interfaces (CRUD + @Query AQL).
persistence/repository/edges/  → ArangoRepository for edges, plus hand-written AQL @Query methods.

persistence/dao/interfaces/    → IDao<T, I> base + ICommitDao / IBranchDao… domain-facing.
persistence/dao/interfaces/node/  → node DAO interfaces
persistence/dao/interfaces/edge/  → edge DAO interfaces (return DOMAIN models, not entities)
persistence/dao/nosql/arangodb/             → MappedArangoDbDao base + per-node DAO impls
persistence/dao/nosql/arangodb/connection/  → per-edge DAO impls (carry the AQL-heavy logic)

persistence/mapper/            → EntityMapper<D, E> per type. Map a single domain↔entity pair.
assembler/                     → Coordinate multi-entity mapping for aggregates with cyclic
                                 refs (Project↔Repository). Use MappingContext to avoid duplicate
                                 entities — see core/CLAUDE.md.

service/                       → *InfrastructurePortImpl: the port implementations consumed by
                                 application modules (cli, web). Each impl extends
                                 AbstractInfrastructurePort<T, I> and wires its primary DAO in
                                 @PostConstruct so deleteAllEntities() (Extension.kt) works generically.
startup/ArangoCollectionInitializer → creates required document/edge collections at startup.
```

## Load-Bearing Invariants

1. **`MappedArangoDbDao<D, E, I>` is the canonical node DAO.** Subclasses are typically one-liners passing a `Spring Data ArangoRepository` and an `EntityMapper`. Its `create`/`update`/`saveAll`/`findAll(pageable)` methods are annotated `@MappingSession` — these open an identity-map scope so cyclic graphs (e.g. Commit↔Repository↔Project) don't produce duplicate entities. Don't add a new node DAO that bypasses this.

2. **Edge DAOs are hand-written.** Edge connection repositories carry the bulk of AQL (`@Query`) and pagination logic. When writing AQL:
   - Hyphenated collection names MUST be backtick-quoted: `` `commits-files` ``.
   - `_from`/`_to` are full identifiers (`collection/key`). Use `CONCAT('commits/', @commitId)` to build a filter; use `PARSE_IDENTIFIER(c._to).key` to extract the key for joining.
   - Pagination is offset/limit-based via `LIMIT @offset, @limit`; the corresponding total count uses `COLLECT WITH COUNT INTO length`.

3. **Spring Data ArangoDB does NOT auto-create collections.** `startup/ArangoCollectionInitializer` runs on `@PostConstruct` and ensures every document and edge collection used by entities exists. **When you add a new `@Document` or `@Edge` entity, register its collection name here** — otherwise integration tests fail with `Error: 1203 - collection or view not found`.

4. **`InfrastructureConfig` is the binding root for `binocular.arangodb.*`** (host/port/name/user/password). It's annotated `@Configuration` + `@ComponentScan("com.inso_world.binocular.infrastructure.arangodb")` and extends `BinocularConfig`. `ArangodbAppConfig` is the importable Spring config — application modules (`cli`, `web`) `@Import(ArangodbAppConfig::class)` under `@Profile("arangodb")` (see repo root CLAUDE.md "Infrastructure" pattern). Spring Boot metadata for these keys lives in `src/main/resources/META-INF/additional-spring-configuration-metadata.json`.

5. **`AbstractInfrastructurePort<T, I>` holds a generic `dao: IDao<T, I>` field that each `*InfrastructurePortImpl` must wire in `@PostConstruct`.** This is what `Extension.kt::deleteAllEntities()` uses to tear down test fixtures generically. New port impls following this pattern: extend `AbstractInfrastructurePort<DomainType, IdType>`, wire `super.dao = primaryDao` in `init()`.

6. **Mappers self-inject `MappingContext` and use `@Lazy` for cross-mapper refs** to break circular DI (see `RepositoryAssembler` — `commitMapper`, `branchMapper`, `developerMapper` are all `@Lazy`). `BaseMapperTest` mirrors this pattern via reflection `ReflectionUtils.setField` because mappers are not constructor-injected — preserve this in new mapper unit tests.

7. **Domain-side `iid` injection via reflection.** `CommitMapper.toDomain` uses `ReflectionUtils.setField(domain.javaClass.superclass.getDeclaredField("iid"), domain, entity.iid)` because `iid` is a value-class property on the base in `core/`. This is the standard workaround documented in `core/CLAUDE.md`; don't refactor it away without coordinating.

8. **`IDao` exposes `delete` and `deleteById`, but per the repo-level invariant DELETE is intentionally not yet wired through the ports** (`BinocularInfrastructurePort.delete*` throws `UnsupportedOperationException` in `core/`). `IDao.deleteAll()` is only used by integration-test fixture teardown via the `Extension.kt` helpers — that's the only sanctioned caller.

9. **Many `*InfrastructurePortImpl` methods are still `TODO("Not yet implemented")`.** Before relying on a method, check the impl. The `findCommitsInternal` in `CommitInfrastructurePortImpl` is an in-memory fallback for `since`/`until` filtering and is explicitly marked TODO for moving to AQL — don't blindly copy this pattern; prefer pushing the filter into the AQL `@Query`.

10. **`DomainModelAlignmentTest` enforces structural alignment** between domain models in `domain/` and `*Entity` classes here. When you add a domain field, this test will likely warn (extra field) or fail (type mismatch). Update the test's `mappedClasses`, `allowedTypePairs`, `mappedProperties`, `deprecatedProperties`, or `internalModelProperties` for the affected pair — silencing the failure without thinking through the mapping is wrong.

## Testing

- Integration tests use **ArangoDB 3.12 Testcontainer** (`io.testcontainers.arangodb`, `goodforgod/arangodb-testcontainer`) — see `ArangodbTestConfig`. The container runs `withReuse(true)` `withoutAuth()`, so the first run is slow but subsequent runs reuse the container.
- The contract test suite that this adapter must satisfy lives in `../infrastructure-test/` and is run there with `-Dspring.profiles.active=arangodb`. **Per repo policy, changes to this module are not finished until both this module's tests and `infrastructure-test` pass.**
- Test fixtures come from `MockTestDataProvider` (preferred) or `TestDataProvider` (deprecated singleton). `ArangodbInfrastructureDataSetup` orchestrates fixture seeding — note the explicit ordering: projects → repositories → other nodes → edges. Replicate this order if you add new fixture types.
- Unit tests extend `BaseUnitTest`; integration tests extend `BaseIntegrationTest` (both come from the `core` test-jar). `BaseMapperTest` is the local base for mapper unit tests — extend it and add new mappers via `spyk` + reflection wiring.

## Maven Build Quirks

- `kotlin-maven-allopen` is configured to open classes annotated with `@com.arangodb.springframework.annotation.Document` (see `pom.xml`). Spring Data ArangoDB needs subclassable entities, so don't add `final` to `@Document` types and don't switch them away from `data class`.
- `kapt` runs `spring-boot-configuration-processor` over `src/main/kotlin` so `InfrastructureConfig` becomes a `@ConfigurationProperties` target with IDE-resolvable metadata.
- This module publishes a `test-jar` (see `maven-jar-plugin`), but at present nothing depends on it.