# Plan: RDF Knowledge Graph Builder MVP

## Context

Binocular collects MSR data (commits, issues, MRs, builds, files, modules) into ArangoDB. The `binocular.ttl` ontology defines an RDF vocabulary for this data. This MVP bridges the two: read ArangoDB -> produce an RDF knowledge graph as a Turtle file using Apache Jena.

## Approach

New standalone Maven module `rdf` -- a Spring Boot single-run app (no web server). Uses the raw ArangoDB Java driver for simple `FOR x IN collection RETURN x` queries, and Apache Jena to build an in-memory RDF model that gets serialized to Turtle.

## Files to Create

### 1. Module setup

**`rdf/pom.xml`** -- New Maven module
- Parent: `binocular-parent`
- Dependencies: `arangodb-java-driver:7.14.0`, `apache-jena-libs:5.3.0` (pom), `spring-boot-starter`, `kotlin-reflect`, `jackson-module-kotlin`
- Build: `spring-boot-maven-plugin` (skip=false), `kotlin-maven-plugin` with spring/allopen plugins
- Pattern: follows `cli/pom.xml`

**Parent POM** (`../pom.xml` line 65) -- Add `<module>rdf</module>` after `web`

**`rdf/src/main/resources/application.yaml`** -- ArangoDB connection config + output path

### 2. Application entry point

**`src/main/kotlin/com/inso_world/binocular/rdf/RdfApplication.kt`**
- `@SpringBootApplication` + `ApplicationRunner`
- `WebApplicationType.NONE`
- Calls `KnowledgeGraphBuilder.buildAndExport()`

### 3. Configuration

**`src/main/kotlin/com/inso_world/binocular/rdf/config/RdfConfig.kt`**
- `@ConfigurationProperties(prefix = "binocular.rdf")` for database + output settings
- `ArangoDatabase` bean via raw driver

### 4. ArangoDB reader

**`src/main/kotlin/com/inso_world/binocular/rdf/arango/ArangoReader.kt`**
- One method per collection: `readCommits()`, `readUsers()`, `readFiles()`, `readModules()`, `readIssues()`, `readMergeRequests()`, `readBuilds()`, `readBranches()`
- Edge readers: `readCommitsFiles()`, `readCommitsUsers()`, `readIssuesCommits()`, `readCommitsModules()`, `readCommitsBuilds()`, `readModulesFiles()`, `readModulesModules()`
- Returns `List<Map<String, Any?>>` from AQL cursors

### 5. RDF mapping

**`src/main/kotlin/com/inso_world/binocular/rdf/mapping/RdfNamespaces.kt`**
- Constants for `bio:`, `vcs-git:`, `its-gh:`, `ci-gha:`, `inst:` (instance data namespace)

**Entity mappers** (each a `@Component`, takes list of maps + Jena Model):

| Mapper | ArangoDB source | RDF type | Key properties |
|--------|----------------|----------|----------------|
| `CommitMapper` | commits | `vcs-git:Commit` | identifier, message, createdAt |
| `ContributorMapper` | users | `bio:Contributor` | name, email (parsed from gitSignature) |
| `FileMapper` | files | `bio:File` | name (path) |
| `ModuleMapper` | modules | `bio:Module` | name (path) |
| `IssueMapper` | issues | `its-gh:Issue` | issueId, title, description, issueStatus, createdAt, closedAt |
| `MergeRequestMapper` | mergeRequests | `its-gh:PullRequest` | prId, title, prStatus, createdAt |
| `BuildMapper` | builds | `ci-gha:WorkflowRun` + `ci-gha:Job` | pipelineId, status, hasJob, duration |
| `BranchMapper` | branches | `bio:MutableLabel` | name, pointsTo (via SHA lookup) |

**`EdgeMapper`** -- Maps all edge collections to relationship triples:
- commits-users -> `bio:author`
- commits-files -> `bio:FileChange` node + `bio:modifiedFile` + `bio:changedFile`
- issues-commits -> `bio:containsCommit` (+ `bio:closedByRevision` if closes=true)
- commits-modules -> `bio:changedModule`
- commits-builds -> `bio:triggeredBy` (reversed direction)
- modules-files -> extension property
- modules-modules -> `bio:containsModule`

### 6. Orchestrator

**`src/main/kotlin/com/inso_world/binocular/rdf/service/KnowledgeGraphBuilder.kt`**
- Creates Jena `Model`, sets namespace prefixes
- Reads all collections via `ArangoReader`
- Calls each mapper
- Writes Turtle to configured output path
- Logs triple count

## Instance URI scheme

```
https://data.binocular.example.org/commit/{_key}
https://data.binocular.example.org/user/{_key}
https://data.binocular.example.org/file/{_key}
...
```

Uses ArangoDB `_key` for deterministic, collision-free URIs.

## Implementation order

1. pom.xml + parent POM change + application.yaml + RdfApplication.kt + RdfConfig.kt
2. ArangoReader.kt + RdfNamespaces.kt
3. All entity mappers (CommitMapper through BranchMapper)
4. EdgeMapper
5. KnowledgeGraphBuilder orchestrator

## Verification

1. `mvn compile -pl rdf` -- builds without errors
2. Start ArangoDB locally (or via testcontainer), load dump data
3. `mvn spring-boot:run -pl rdf` -- runs, connects to ArangoDB, produces `output/knowledge-graph.ttl`
4. Verify output: check triple count, spot-check entity types and relationships
5. Optional: load output into Jena Fuseki or use `riot --validate` to verify Turtle syntax

## Key files to reference
- `../pom.xml` -- parent POM
- `../cli/pom.xml` -- reference for module POM structure
- `binocular.ttl` -- ontology
- `../web/src/test/resources/realdata/db_dump/` -- data model reference
