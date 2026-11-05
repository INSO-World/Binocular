# Domain Module

**Pure domain logic for the Binocular Git repository analysis system.**

This module contains the core domain models and business logic for Binocular. It is completely framework-agnostic, has
no external infrastructure dependencies, and follows Domain-Driven Design (DDD) principles.

## Table of Contents

- [Overview](#overview)
- [Architecture & Design Principles](#architecture--design-principles)
- [Core Domain Models](#core-domain-models)
- [Key Concepts](#key-concepts)
- [Domain Model Relationships](#domain-model-relationships)
- [Validation](#validation)
- [Testing](#testing)
- [Usage Examples](#usage-examples)
- [Building](#building)

---

## Overview

The domain module provides:

- **Pure domain models** representing Git repositories, commits, branches, files, users, and projects
- **Business logic** encapsulated within domain entities
- **Validation rules** enforcing domain invariants
- **Collection semantics** for managing entity relationships with consistency guarantees
- **Identity management** via dual identity system (technical ID + business key)

**Key characteristics:**

- Zero framework dependencies (no Spring, no Hibernate, no database concerns)
- Immutable identifiers with value-based business keys
- Add-only collections with repository consistency checks
- Comprehensive KDoc documentation
- Extensive unit test coverage (80%+ with mutation testing)

---

## Architecture & Design Principles

### Hexagonal Architecture

The domain module sits at the **core** of a hexagonal (ports & adapters) architecture:

```
┌─────────────────────────────────────────────┐
│  Application Layer (cli, web)              │
│  - Spring Boot entry points                │
│  - GraphQL API, Shell commands             │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│  Core Module                                │
│  - Application services                     │
│  - Port interfaces (InfrastructurePorts)    │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│  Domain Module ← YOU ARE HERE               │
│  - Pure domain models                       │
│  - Business logic                           │
│  - Domain validation                        │
└─────────────────────────────────────────────┘
                    ↑
┌─────────────────────────────────────────────┐
│  Infrastructure Adapters                    │
│  - infrastructure-sql (PostgreSQL/Hibernate)│
│  - infrastructure-arangodb (ArangoDB)       │
└─────────────────────────────────────────────┘
```

### Design Principles

1. **Framework Independence**: No external dependencies except Kotlin stdlib, Jakarta validation, and SLF4J
2. **Immutable Identifiers**: Technical IDs (`iid`) are immutable and stable
3. **Business Keys**: Natural keys (`uniqueKey`) enable domain-driven deduplication
4. **Set-Once Relationships**: Parent references cannot be reassigned once set
5. **Add-Only Collections**: History-preserving collections prevent data loss
6. **Repository Consistency**: Cross-repository relationships are prevented via runtime checks
7. **Comprehensive Documentation**: Every public API has detailed KDoc with semantics, invariants, and examples

---

## Core Domain Models

### Primary Entities

These entities are to root aggregates which in turn own secondary entities.
Although `Repository` is owned by the `Project` it serves as a root aggregate for all Source Code Management (SCM)
related data.
`Project` on the other hand is the root aggregate for everything.
Especially for the ITS/CI data, since it is not _directly_ related to the `Repository`, just indirectly based on the
platform.

| Entity         | Description                     | Identity               | Business Key               |
|----------------|---------------------------------|------------------------|----------------------------|
| **Project**    | Top-level organizational unit   | `Project.Id` (UUID)    | `name`                     |
| **Repository** | Git repository within a project | `Repository.Id` (UUID) | `(project.iid, localPath)` |

### Secondary Entities

Effectively all others.

| Entity     | Description                 | Identity              | Business Key             |
|------------|-----------------------------|-----------------------|--------------------------|
| **Commit** | Git commit snapshot         | `Commit.Id` (UUID)    | `sha` (40-char hex)      |
| **Branch** | Named Git reference         | `Reference.Id` (UUID) | `(repository.iid, name)` |
| **User**   | Git commit author/committer | `User.Id` (UUID)      | `(repository.iid, name)` |
| **File**   | File tracked in repository  | `File.Id` (UUID)      | `(repository.iid, path)` |

### Supporting Entities

- **CommitDiff**: Represents diff between two commits
- **FileDiff**: Represents changes to a file within a commit
- **FileState**: Snapshot of a file at a specific commit
- **Issue**, **MergeRequest**, **Build**, **Job**: CI/CD and issue tracking entities (future/legacy)

### Collections

- **NonRemovingMutableSet**: Add-only set backed by `ConcurrentHashMap`, keyed by `uniqueKey`

---

## Key Concepts

### 1. Dual Identity System

Every domain entity inherits from `AbstractDomainObject<Iid, Key>` and exposes two forms of identity:

#### Technical Identity (`iid`)

- Immutable, stable identifier generated at construction time
- Typically a UUID wrapped in a value class (e.g., `Commit.Id`, `Repository.Id`)
- Used for `hashCode()` calculation and entity identity in collections

#### Business Key (`uniqueKey`)

- Domain-level natural key that's unique within its scope
- Used for deduplication in `NonRemovingMutableSet`
- Examples:
    - `Commit.uniqueKey = sha`
    - `Branch.uniqueKey = Key(repository.iid, name)`
    - `User.uniqueKey = Key(repository.iid, name)`

**Equality Semantics:**

```kotlin
override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as AbstractDomainObject<*, *>

    if (iid != other.iid) return false
    if (uniqueKey != other.uniqueKey) return false

    return true
}

override fun hashCode(): Int = iid.hashCode()
```

Two entities are equal **only if** they have the same runtime class **and** both `iid` and `uniqueKey` match.

### 2. Add-Only Collections

Domain entities use **`NonRemovingMutableSet`** for relationship collections:

**Characteristics:**

- ✅ **Add operations**: `add()`, `addAll()`, `contains()`, `containsAll()`
- ❌ **Remove operations**: `remove()`, `removeAll()`, `retainAll()`, `clear()`, iterator `remove()` → throw
  `UnsupportedOperationException`
- 🔑 **Deduplication**: By `uniqueKey`, not object identity
- 🔒 **Canonical instance**: First instance added for a given `uniqueKey` is retained
- ⚡ **Thread-safe**: Backed by `ConcurrentHashMap` for concurrent reads/writes
- 🔄 **Weakly consistent iteration**: Iterator may not reflect concurrent modifications

**Why add-only?**

1. **History preservation**: Git data is immutable and shouldn't be deleted
2. **Consistency**: Prevents accidentally breaking the commit graph or relationships
3. **Audit trail**: Domain maintains complete record of all added entities

**Example:**

```kotlin
val repository = Repository(localPath = "/path/to/repo", project = myProject)
val commit1 = Commit(sha = "a".repeat(40), ..., repository = repository)
val commit2 = Commit(sha = "a".repeat(40), ..., repository = repository) // Same SHA

repository.commits.add(commit1) // true - added
repository.commits.add(commit2) // false - duplicate uniqueKey, commit1 is canonical

repository.commits.contains(commit1) // true
repository.commits.contains(commit2) // true (checks uniqueKey, not identity)
repository.commits.size // 1
```

### 3. Repository Consistency

Entities belonging to different repositories **cannot** be linked:

```kotlin
val repoA = Repository(localPath = "/repoA", project = projectA)
val repoB = Repository(localPath = "/repoB", project = projectB)

val commitA = Commit(sha = "abc...", repository = repoA)

// ❌ This will throw IllegalArgumentException:
repoB.commits.add(commitA)

// ❌ This will also throw:
val branchB = Branch(name = "main", repository = repoB, head = commitA)
```

All add operations validate `element.repository == this@Repository`.

### 4. Set-Once Relationships

Parent references use **set-once** semantics:

```kotlin
var repo: Repository? = null
set(value) {
    requireNotNull(value) { "Cannot set repo to null" }
    if (value == this.repo) return // Idempotent: same value is no-op
    if (this.repo != null) {
        throw IllegalArgumentException("Repository already set")
    }
    field = value
}
```

**Rules:**

- Cannot be set to `null`
- Cannot be reassigned to a different instance
- Re-assigning the same instance is a no-op

This prevents:

- Breaking relationships after initialization
- Accidentally moving entities between aggregates
- Null reference errors

---

## Domain Model Relationships

### Project ↔ Repository (1:1)

```kotlin
val project = Project(name = "MyProject")
val repository = Repository(localPath = "/path", project = project)

// Bidirectional link established in Repository.init:
assert(repository.project === project)
assert(project.repo === repository)
```

### Repository → Commits/Branches/Users (1:N)

```kotlin
val repository = Repository(localPath = "/path", project = project)

// Commits auto-register on construction:
val commit = Commit(sha = "abc...", repository = repository)
assert(commit in repository.commits)

// Branches auto-register on construction:
val branch = Branch(name = "main", repository = repository, head = commit)
assert(branch in repository.branches)

// Users are explicitly added:
val user = User(name = "Alice", email = "alice@example.com", repository = repository)
assert(user in repository.user)
```

### Commit ↔ Author/Committer (N:1)

```kotlin
val commit = Commit(sha = "abc...", repository = repository)
val author = User(name = "Alice", email = "alice@example.com", repository = repository)

commit.author = author // Set-once, bidirectional
assert(commit in author.authoredCommits)

commit.committer = author
assert(commit in author.committedCommits)
```

### Commit → Parents/Children (DAG)

```kotlin
val parent = Commit(sha = "aaa...", repository = repository)
val child = Commit(sha = "bbb...", repository = repository)

// Bidirectional link:
child.parents.add(parent)
assert(parent in child.parents)
assert(child in parent.children)
```

### Branch → Commits (1:N)

```kotlin
val branch = Branch(name = "main", repository = repository, head = headCommit)

// Access complete commit history via lazy property:
val allCommits: List<Commit> = branch.commits
// Returns [head, parent1, parent2, ..., root] in topological order
```

---

## Validation

### Jakarta Validation Annotations

Domain models use Jakarta Validation (JSR-380) annotations:

```kotlin
@field:NotBlank
val name: String

@field:Size(min = 40, max = 40)
val sha: String

@field:PastOrPresent
val commitDateTime: LocalDateTime
```

### Runtime Validation

In addition to annotations, constructors enforce invariants with `require()`:

```kotlin
init {
    require(name.isNotBlank()) { "name must not be blank" }
    require(sha.length == 40) { "SHA must be 40 hex chars" }
    require(sha.all { it.isHex() }) { "SHA-1 must be hex [0-9a-fA-F]" }
    require(commitDateTime.isBefore(LocalDateTime.now())) {
        "commitDateTime must be past or present"
    }
}
```

### Repository Consistency Checks

Collections validate repository ownership:

```kotlin
override fun add(element: Commit): Boolean {
    require(element.repository == this@Repository) {
        "$element cannot be added to a different repository."
    }
    return super.add(element)
}
```

---

## Testing

### Test Organization

```
domain/src/test/kotlin/
├── com/inso_world/binocular/
│   ├── data/
│   │   ├── MockTestDataProvider.kt      # Test fixtures for integration tests
│   │   └── DummyTestData.kt             # Parameterized test data
│   └── model/
│       ├── *ModelTest.kt                # Unit tests for domain models
│       ├── NonRemovingMutableSetTest.kt # Unit tests for collection
│       ├── validation/                  # Jakarta Validation tests
│       │   ├── *ValidationTest.kt
│       │   └── base/ValidationTest.kt
│       └── utils/
│           └── ReflectionUtils.kt       # Test utilities
```

### Running Tests

```bash
# Run all unit tests
mvn test -Dgroups=unit

# Run specific test class
mvn test -Dtest=CommitModelTest -Dgroups=unit

# Run with coverage
mvn verify jacoco:report -Dgroups=unit
open target/site/jacoco/index.html
```

### Mutation Testing

The domain module uses [PIT (pitest)](https://pitest.org/) for mutation testing to ensure test quality:

```bash
# Run mutation tests
mvn org.pitest:pitest-maven:mutationCoverage

# View report
open target/pit-reports/*/index.html
```

**Coverage Goals:**

- Line coverage: **80%+**
- Mutation coverage: **80%+**
- C3/C4 (branch/path) coverage for complex logic

### Test Fixtures

The domain module exports a **test-jar** for reuse in other modules:

```xml

<dependency>
    <groupId>com.inso-world.binocular</groupId>
    <artifactId>domain</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <classifier>tests</classifier>
    <type>test-jar</type>
    <scope>test</scope>
</dependency>
```

**Available fixtures:**

- `MockTestDataProvider`: Creates pre-wired graphs of commits, branches, users
- `DummyTestData`: Provides parameterized test data (blank strings, valid strings, etc.)

---

## Usage Examples

### Creating a Project and Repository

```kotlin
@OptIn(ExperimentalUuidApi::class)
val project = Project(name = "Binocular")
val repository = Repository(
    localPath = "/path/to/binocular/.git",
    project = project
)

// Bidirectional link is established automatically
assert(project.repo === repository)
```

### Creating Commits and Building History

```kotlin
val user = User(
    name = "Alice",
    email = "alice@example.com",
    repository = repository
)

val commit1 = Commit(
    sha = "a".repeat(40),
    message = "Initial commit",
    commitDateTime = LocalDateTime.now().minusDays(2),
    authorDateTime = LocalDateTime.now().minusDays(2),
    repository = repository
)
commit1.author = user
commit1.committer = user

val commit2 = Commit(
    sha = "b".repeat(40),
    message = "Second commit",
    commitDateTime = LocalDateTime.now().minusDays(1),
    authorDateTime = LocalDateTime.now().minusDays(1),
    repository = repository
)
commit2.author = user
commit2.committer = user
commit2.parents.add(commit1) // Establishes parent-child relationship

assert(commit1 in commit2.parents)
assert(commit2 in commit1.children)
```

### Creating Branches

```kotlin
val main = Branch(
    name = "main",
    repository = repository,
    head = commit2 // Points to latest commit
)

// Branch automatically registers with repository
assert(main in repository.branches)

// Access complete commit history
val history: List<Commit> = main.commits
assert(history == listOf(commit2, commit1))
```

### Working with Collections

```kotlin
// Add commits (idempotent, deduplicates by uniqueKey)
repository.commits.add(commit1) // true - added
repository.commits.add(commit1) // false - already present

// Check membership (by uniqueKey)
val probe = Commit(sha = "a".repeat(40), ..., repository = repository)
assert(repository.commits.contains(probe)) // true - same sha

// Removal is not allowed
repository.commits.remove(commit1) // Throws UnsupportedOperationException
```

### Handling Validation Errors

```kotlin
// ❌ Blank project name
assertThrows<IllegalArgumentException> {
    Project(name = "  ")
}

// ❌ Invalid SHA
assertThrows<IllegalArgumentException> {
    Commit(sha = "invalid", ..., repository = repository)
}

// ❌ Cross-repository reference
val repoA = Repository(localPath = "/repoA", project = projectA)
val repoB = Repository(localPath = "/repoB", project = projectB)
val commitA = Commit(sha = "abc...", repository = repoA)

assertThrows<IllegalArgumentException> {
    repoB.commits.add(commitA)
}
```

---

## Best Practices

### When Adding New Domain Models

1. **Extend `AbstractDomainObject<Iid, Key>`**
   ```kotlin
   data class MyEntity(val name: String)
       : AbstractDomainObject<MyEntity.Id, MyEntity.Key>(Id(Uuid.random())) {

       @JvmInline
       value class Id(val value: Uuid)
       data class Key(val name: String)

       override val uniqueKey: Key get() = Key(name)

       // Override equals/hashCode to use parent implementation
       override fun equals(other: Any?) = super.equals(other)
       override fun hashCode(): Int = super.hashCode()
   }
   ```

2. **Add comprehensive KDoc**
    - Short description
    - Identity & equality semantics
    - Construction & validation rules
    - Relationships & mutability
    - Thread-safety guarantees
    - Examples

3. **Write comprehensive tests**
    - Unit tests covering all public methods
    - Validation tests for all constraints
    - Edge cases and error conditions
    - Aim for 80%+ line and mutation coverage

4. **Use add-only collections for relationships**
   ```kotlin
   val children: MutableSet<MyChild> =
       object : NonRemovingMutableSet<MyChild>() {
           override fun add(element: MyChild): Boolean {
               require(element.parent == this@MyEntity)
               return super.add(element)
           }
       }
   ```

5. **Validate at construction time**
   ```kotlin
   init {
       require(name.isNotBlank()) { "name must not be blank" }
       require(value >= 0) { "value must be non-negative" }
   }
   ```

### When Modifying Existing Models

1. **Never change `iid` after construction** (breaks hashCode contract)
2. **Keep `uniqueKey` stable** (used for deduplication)
3. **Add tests for new behavior** (maintain >80% coverage)
4. **Update KDoc** to reflect changes
5. **Run mutation tests** to verify test quality
