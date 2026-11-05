# `infrastructure-sql` Module

## Architecture Patterns

### Mapper vs Assembler Pattern

The infrastructure-sql module uses two complementary patterns for domain-entity conversion:

#### Mappers (`mapper/`)
**Purpose**: Simple, focused converters for individual objects

- Convert between domain models and persistence entities
- Handle **structure-only** conversion (no child entities)
- Enforce aggregate boundaries (expect parent references already in `MappingContext`)
- Use `MappingContext` for identity preservation
- Implement `EntityMapper<Domain, Entity>` interface

**Example**: `ProjectMapper` converts `Project` ↔ `ProjectEntity` but does NOT handle the Repository child.

#### Assemblers (`assembler/`)
**Purpose**: Orchestrate complex aggregate assembly

- Coordinate multiple mappers to build complete object graphs
- Handle aggregate assembly including all children
- Wire bidirectional relationships between entities
- Manage entire aggregate lifecycle (root → children → grandchildren)
- Use `MappingContext` to ensure identity preservation throughout the graph

**Example**: `ProjectAssembler` orchestrates `ProjectMapper` + `RepositoryAssembler` to build the complete aggregate including all Repository children (Commits, Branches, Users).

#### When to Use Each

- **Use Mappers**: For simple conversions, `refreshDomain` operations, when objects are already identity-tracked
- **Use Assemblers**: At service boundaries, when building complete aggregates, when orchestrating multiple related entities
