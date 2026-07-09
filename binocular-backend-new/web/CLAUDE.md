# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **Scope:** This file documents the `web` module only. The parent `binocular-backend-new/CLAUDE.md` covers monorepo-wide concerns (hexagonal layering, port pattern, `MappingSession`, FFI/JGit rebuild flow, infrastructure test contract). Read it first — invariants documented there are assumed here and not repeated. `cli/CLAUDE.md` is the sister production module; the two share several conventions (profile composition, exception-wrapping pattern, `@ComponentScan` discipline) — when in doubt, mirror what `cli` does.

## What This Module Is

`web` is one of two production-application-grade entry points (the other is `cli`). It is a Spring Boot 3.5 GraphQL server backed by an ArangoDB adapter. The schema-first GraphQL layer (`src/main/resources/graphql/schema.graphqls`) is the public API contract; everything in `graphql/` exists to fulfil it.

Entry point: `src/main/kotlin/com/inso_world/binocular/web/BinocularWebApplication.kt`. Default HTTP port is **48763** (`application.yaml`). GraphQL endpoint is `/graphQl`; GraphiQL UI is enabled by default in `application.yaml` (and disabled in `src/test/resources/application.yaml`).

`@SpringBootApplication.scanBasePackages` is explicitly listed (`com.inso_world.binocular.web`, `core.persistence`, `core.service`). The comment in the file is load-bearing: **these must match `CliApplication`'s `@ComponentScan`** — if you add a new top-level package under `core` that the web app needs to discover, update both entry points or one of them will break silently.

## Profile Wiring — ArangoDB Only Today

Unlike `cli`, this module only ships **one** persistence config: `config/ArangodbConfig.kt` (active under profile `nosql` *or* `arangodb`). `application-postgresql.yaml` exists in resources but there is **no `SqlConfig.kt` importing `SqlAppConfig`** — running with `spring.profiles.active=postgres` will start the app without an active persistence adapter and every port lookup will fail at runtime. Do not assume SQL works here just because the yaml is present. If you need SQL support, port the `SqlConfig` pattern from `cli/config/SqlConfig.kt`.

Default profiles are unset in `application.yaml`. Tests activate `test,arangodb` via `src/test/resources/application.yaml`.

`CorsConfig` registers `addMapping("/**").allowedOrigins("*")` — fully open CORS. Acceptable for the current research/dev posture; flag if hardening for deployment.

## Running

Invoke from the parent (`binocular-backend-new/`) so Maven resolves the reactor — running `mvn` inside `web/` only works after dependent modules are installed.

```bash
# Local run — requires an ArangoDB on localhost:8529 (see parent CLAUDE.md for `docker-compose up db`)
../mvnw -pl web spring-boot:run -Dspring-boot.run.profiles=arangodb

# Build the fat jar
../mvnw clean package -DskipTests -pl web -am
```

## GraphQL Layer Layout

The GraphQL stack is a strict three-tier separation. Adding a new domain type means touching all three (plus the schema file):

```
graphql/
├── config/        # TimestampScalar, SortScalar — register custom scalars via RuntimeWiringConfigurer
├── controller/    # @Controller + @SchemaMapping(typeName="X") — root queries (e.g. `commits`, `commit`)
├── resolver/      # @Controller + @SchemaMapping(typeName="X", field="y") — nested/lazy field resolution
├── mapper/        # GraphQlMapper aggregator + per-domain GraphQl<Type>Mapper.toDto(...) in mapper/impl/
├── model/         # *Dto, Paginated*, PageDto, Sort, Hunk — wire types returned to GraphQL
└── error/         # GraphQLExceptionHandler (DataFetcherExceptionResolverAdapter) + GraphQLValidationUtils
```

Conventions:
- **Controllers vs resolvers**: a `*Controller` owns top-level queries for one type (`@QueryMapping`); a `*Resolver` owns nested-field resolution (`@SchemaMapping(typeName=…, field=…)`). Keep them split — do not put nested resolvers on the controller. Each maps cleanly to one section of the schema.
- **Controllers are `internal`** by Kotlin visibility but still picked up by Spring (`@Controller` is on the kotlin-spring all-open list). Keep them `internal` to avoid accidental cross-module use.
- **Domain → DTO mapping** goes through the aggregator `GraphQlMapper` (`mapper.toDto(domainObject)`), which dispatches to per-type mappers in `mapper/impl/`. Do not call per-type mappers directly from controllers — keeps the dispatch site auditable.
- **Pagination**: every list query uses `PaginationUtils.createPageableWithValidation(page, size, sort, sortBy)`. Defaults are **1-based** (`page=1`), max 1000 perPage, max 10000 page. Invalid input throws `ValidationException` which the exception handler converts to a `VALIDATION_ERROR` GraphQL error.
- **Custom scalars** are wired via `RuntimeWiringConfigurer` beans in `graphql/config/`. `Timestamp` accepts epoch millis or ISO-8601, always serializes UTC ISO-8601 with millis. `Sort` is `ASC`/`DESC` case-insensitive (parses string or enum literal).

## Error Handling

`GraphQLExceptionHandler` extends `DataFetcherExceptionResolverAdapter` and maps four exception types onto GraphQL error envelopes with `code` extensions:

| Kotlin exception                  | GraphQL `errorType`     | `extensions.code`   |
|-----------------------------------|-------------------------|---------------------|
| `ValidationException`             | `ValidationError`       | `VALIDATION_ERROR`  |
| `GraphQLException`                | `ValidationError`       | (caller-provided)   |
| `NotFoundException`               | `DataFetchingException` | `NOT_FOUND`         |
| `ServiceException`                | `DataFetchingException` | `SERVICE_ERROR`     |
| anything else                     | `ExecutionAborted`      | `INTERNAL_ERROR`    |

Throw these (defined in `exception/`) from controllers/resolvers — never leak port/core exceptions directly. Use `GraphQLValidationUtils.requireEntityExists(...)` to convert a null lookup result into a `NotFoundException` at the call site (see `CommitController.findById` for the pattern).

## REST Surface

There is **one** REST endpoint, `controller/DbExportController` mounted at `/api/db-export`. It wraps `DbExportPort` and is the only thing in `controller/` (not `graphql/controller/`). When adding REST routes, put them here and mount under `/api`. Errors are not yet routed through a global handler — the controller catches `RuntimeException` and maps to 500 manually.

## Tests

The test tree is split four ways. Pick the right base when adding tests:

| Directory                                | Base class                       | What it boots                                                          |
|------------------------------------------|----------------------------------|------------------------------------------------------------------------|
| `restcontroller/`                        | `RestControllerTest`             | REST-only slice tests (MockMvc).                                       |
| `graphql/resolver/`                      | `GraphQlControllerTest` → `BaseDbTest` → `AbstractWebIntegrationTest` | Full Spring context + ArangoDB Testcontainer + seeded test data + autowired `GraphQlTester`. Use for resolver/controller logic. |
| `graphql/controller/*WebTest`            | `GraphQlControllerTest` (often via `@Nested`) | Same as above; named `*WebTest` because they drive controllers through the GraphQL transport. |
| `graphql/integration/realdata/`          | `BaseGraphQlCompatibilityIT`     | Boots against a **prebuilt ArangoDB image** (`ghcr.io/inso-world/binocular-database:3.12.test-data`) — real-world data. |

Test data flow:
- `AbstractWebIntegrationTest` boots `BinocularWebApplication` and installs `ArangodbTestConfig.Initializer` (Testcontainers ArangoDB) via `@ContextConfiguration(initializers = …)`. Tests requiring data extend `BaseDbTest`, which `@BeforeEach` calls `testDataSetupService.clearAllData()` then `setupTestData()`. The seeded fixtures come from `core.integration.base.TestDataProvider` — same fixtures the infrastructure contract tests use, so what passes here also exercises the `core` test-jar.
- The real-data IT tree (`graphql/integration/realdata/`) is gated by the **`realdata` Maven profile** in `pom.xml` (lines 152–171). Without `-P realdata`, surefire only runs the standard `*Test` classes, not `*IT`. Activate the profile to run the real-data comparison tests:

```bash
# Standard unit + integration tests (Testcontainers ArangoDB)
../mvnw -pl web test -Dgroups=unit
../mvnw -pl web verify -Dgroups=integration

# Real-data IT tests only (pulls the inso-world prebuilt DB image)
../mvnw -pl web verify -P realdata

# Single test
../mvnw -pl web test -Dtest=CommitResolverTest
../mvnw -pl web test -Dtest=CommitResolverTest#'should retrieve commit with all fields'
```

### Real-data legacy comparison

`BaseGraphQlCompatibilityIT` supports two execution targets switched via `-Dgraphql.target`:
- `spring` (default) — drives the new server via the autowired `GraphQlTester`.
- `legacy` — fires the same queries at the legacy Binocular GraphQL endpoint (`http://[::1]:8080/graphQl` by default, override with `-Dgraphql.legacy.url=...`). Used to verify schema/result compatibility during the legacy → new migration.

Both targets implement `CompatibleGraphQlClient` so each test body is transport-agnostic. `LegacyReachabilityIT` skips (via `Assumptions.assumeTrue`) if no legacy endpoint is reachable — that's intentional, do not "fix" it to fail loudly.

The ArangoDB container in `BaseGraphQlCompatibilityIT` uses `Wait.forLogMessage(".*RECOVERY_COMPLETE_PROCEED_WITH_TESTS.*")` — that string is emitted by the prebuilt image's restore script. If you swap to a different DB image, update the wait strategy or startup will hang for 120s and time out.

## Adding a New GraphQL Domain Type

The repeatable recipe:

1. Add the type + queries to `src/main/resources/graphql/schema.graphqls`.
2. Add `<Type>Dto` (+ `Paginated<Type>` if listable) in `graphql/model/`.
3. Add `GraphQl<Type>Mapper` in `graphql/mapper/impl/` and register it as a constructor parameter on `GraphQlMapper`, plus a `toDto(domain: <Type>): <Type>Dto` overload.
4. Add `<Type>Controller` (root queries) in `graphql/controller/` — annotate with `@Controller` + `@SchemaMapping(typeName = "<Type>")`. Use `@QueryMapping(name = "...")` for each root query. Always go through `PaginationUtils` for list queries.
5. Add `<Type>Resolver` in `graphql/resolver/` if the type has nested fields requiring lazy resolution.
6. Wire whatever `core` port the controller needs (e.g. `<Type>InfrastructurePort`) — these are auto-discovered via the `core.service` component scan and need no extra config here.
7. Add a `<Type>ControllerWebTest` extending `GraphQlControllerTest` and (optionally) a `<Type>ResolverTest` for nested fields.

## Notes on the Realdata Fixture Tree

`src/test/resources/realdata/db_dump/dump/` contains a gzipped ArangoDB dump used to build the `binocular-database:3.12.test-data` image — it is the source of truth for the real-data tests when the prebuilt image is regenerated. Documentation for how the image is built lives in `src/test/resources/realdata/documentation/`. The dump files are large binary blobs; never edit them directly, regenerate from a fresh export per the docs.
