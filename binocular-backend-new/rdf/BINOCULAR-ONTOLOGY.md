# Binocular MSR Ontology

**Version:** 1.0.0
**Date:** 2026-04-05
**Status:** ✅ Clean (zero lint issues)

## Overview

`binocular.ttl` is a unified ontology for Mining Software Repositories (MSR) with Binocular. It consolidates the best parts of:
- **msr-ontology.ttl** (v0.2.0) — tool-integration architecture
- **OntoBinocularOntology.ttl** — code structure & ownership

**Design philosophy:** Small, focused, queryable. Emphasizes what matters for holistic project analysis.

---

## Statistics

| Metric | Value |
|--------|-------|
| **Classes** | 57 |
| **Object Properties** | 65 |
| **Triples** | 495 |
| **Lint Issues** | 0 ✓ |

---

## Architecture

### Three-Layer Design

1. **Abstract Layer** (`bio:` namespace)
   Tool-agnostic concepts: Project, Contributor, Repository, Issue, Pipeline, Module, etc.

2. **Domain Layer** (VCS, ITS, CI)
   Domain-specific abstractions: Revision (abstract) → git:Commit, svn:Revision, hg:Changeset, jj:Change

3. **System Layer** (GitHub, GitLab, Jira, Jenkins, etc.)
   Tool-specific subclasses: bio:Issue → its-gh:Issue, its-jira:Issue, etc.

### Namespace Organization

```
bio:              <https://ontology.binocular.example.org/>
├── vcs-git:      <https://ontology.binocular.example.org/vcs/git#>
├── vcs-svn:      <https://ontology.binocular.example.org/vcs/svn#>
├── vcs-hg:       <https://ontology.binocular.example.org/vcs/hg#>
├── vcs-jj:       <https://ontology.binocular.example.org/vcs/jj#>
├── its-gh:       <https://ontology.binocular.example.org/its/github#>
├── its-gl:       <https://ontology.binocular.example.org/its/gitlab#>
├── its-jira:     <https://ontology.binocular.example.org/its/jira#>
├── its-bb:       <https://ontology.binocular.example.org/its/bitbucket#>
├── its-linear:   <https://ontology.binocular.example.org/its/linear#>
├── ci-gha:       <https://ontology.binocular.example.org/ci/github-actions#>
├── ci-glci:      <https://ontology.binocular.example.org/ci/gitlab-ci#>
└── ci-jenkins:   <https://ontology.binocular.example.org/ci/jenkins#>
```

---

## Core Concepts

### 1. Project Management

- **bio:Project** — Top-level container
- **bio:Contributor** — Person or bot contributing to a project
- **bio:Forge** — Hosting platform (GitHub, GitLab, etc.)

### 2. Code Structure (NEW)

- **bio:Module** — Logical code unit (package, library, service)
- **bio:File** — Source file
- **bio:FileChange** — Modification within a revision
- **bio:Hunk** — Contiguous block of line changes

### 3. Code Ownership (NEW)

- **bio:CodeOwnership** — Establishes responsibility for code areas
- `bio:owner` → Contributor
- `bio:ownedModule` → Module

### 4. Version Control (VCS)

- **bio:Repository** — VCS repository
  - Subclasses: vcs-git:Repository, vcs-svn:Repository, etc.
- **bio:Revision** — Unit of recorded change
  - Subclasses: vcs-git:Commit, vcs-svn:Revision, vcs-hg:Changeset, vcs-jj:Change
- **bio:Label** — Named pointer (branch, tag)
  - Subclasses: bio:MutableLabel (branch/bookmark), bio:ImmutableLabel (tag)

**Key properties:**
- `bio:author` → Contributor
- `bio:committer` → Contributor
- `bio:hasParent` → Revision (DAG structure)
- `bio:modifiedFile` → FileChange
- `bio:changedModule` → Module

### 5. Issue Tracking (ITS)

- **bio:Issue** — Work item (bug, feature, task)
  - Subclasses: its-gh:Issue, its-jira:Issue, its-jira:Bug, its-jira:Story, etc.
- **bio:PullRequest** — Proposed change (GitHub PR, GitLab MR)
  - Subclasses: its-gh:PullRequest, its-gl:MergeRequest, etc.
- **bio:Comment** — Discussion on issue/PR/commit

**Key properties:**
- `bio:issueStatus` — open, closed, resolved, in-progress
- `bio:priority` — critical, high, medium, low
- `bio:severity` — critical, major, minor, trivial
- `bio:closedByRevision` → Revision (issue → code link)
- `bio:containsCommit` → Revision (commits mentioning issue)
- `bio:relatedModule` → Module (code areas affected)
- `bio:closes` → Issue (PR closes issues)

### 6. Continuous Integration (CI)

- **bio:Pipeline** — CI/CD pipeline
  - Subclasses: ci-gha:WorkflowRun, ci-glci:Pipeline, ci-jenkins:Build
- **bio:Job** — Individual job/stage
  - Subclasses: ci-gha:Job, ci-glci:Job
- **bio:TestResult** — Test execution results
  - Properties: bio:totalTests, bio:passedTests, bio:failedTests, bio:coverage

**Key properties:**
- `bio:triggeredBy` → Revision
- `bio:status` — success, failure, pending, running, skipped
- `bio:hasJob` → Job

### 7. Metrics & Quality

- **bio:Statistic** — Base class for metrics
  - Subclasses: bio:FileChurn, bio:ModuleChurn, bio:Complexity
- Properties: `bio:name`, `bio:value`, `bio:timestamp`, `bio:appliesTo`

### 8. Traceability

Enables linking across layers:
- Issue → Commit/Revision (via `bio:closedByRevision`, `bio:containsCommit`)
- Revision → Code (via `bio:modifiedFile`, `bio:changedModule`)
- Code → Test (via `bio:hasTestResult` on Pipeline/Job)
- PR → Issues (via `bio:closes`)

---

## Key Features

### ✅ What This Ontology Covers

1. **Tool Integration** — Events across VCS, ITS, CI systems
2. **Code Structure** — Modules, files, ownership (NEW vs msr-ontology)
3. **Team Dynamics** — Contributors, code ownership, involvement
4. **Event Tracking** — Commits, issues, PRs, builds with timestamps
5. **Traceability** — Issue → Code → Test links
6. **Multi-Tool Support** — Abstract layer works across GitHub, GitLab, Jira, Jenkins, etc.

### ⏳ Future Extensions (Not in v1.0, but structure supports)

- **Deployment & Runtime** (Phase 2)
  - Environments, deployments, incidents, performance metrics

- **Requirements Traceability** (Phase 2)
  - Features, ADRs, user stories

- **Team & Knowledge** (Phase 2)
  - Team definitions, expertise profiles, bus factor

- **Architecture** (Phase 1)
  - Dependency graphs, service boundaries, layers

---

## Consolidation Notes

### From msr-ontology.ttl ✓
- Clean three-layer architecture (abstract → domain → system)
- Comprehensive tool support (GitHub, GitLab, Jira, Jenkins, etc.)
- Well-documented classes and properties
- Proper use of external ontologies (FOAF, DOAP, PROV)

### From OntoBinocularOntology.ttl ✓
- **Module** — Code structure representation
- **CodeOwnership** — Responsibility tracking
- **Hunk** — Fine-grained diff tracking
- **ChangeSet** — Detailed modification information

### Removed/Not Included
- Orphaned individuals (test data instances)
- Multiple namespace conflicts (de-duplicated to unified `bio:`)
- Deprecated SE-ON patterns (replaced with clean abstractions)
- Tool-specific quirks (prioritized cross-tool consistency)

---

## Usage Examples

### Query: All issues closed by commits

```sparql
PREFIX bio: <https://ontology.binocular.example.org/>

SELECT ?issue ?commit ?author
WHERE {
  ?issue bio:closedByRevision ?commit .
  ?commit bio:author ?author .
}
```

### Query: Modules with high churn

```sparql
SELECT ?module ?churnCount
WHERE {
  ?module a bio:Module .
  ?change bio:changedModule ?module ;
          prov:wasAssociatedWith ?revision .
}
GROUP BY ?module
HAVING (COUNT(?revision) > 10)
```

### Query: Code ownership by module

```sparql
SELECT ?module ?owner
WHERE {
  ?ownership bio:ownedModule ?module ;
             bio:owner ?owner .
}
```

---

## Standards & Dependencies

**External Ontologies Used:**
- **FOAF** (http://xmlns.com/foaf/0.1/) — Agent/Person modeling
- **DOAP** (http://usefulinc.com/ns/doap#) — Project description
- **PROV-O** (http://www.w3.org/ns/prov#) — Provenance (future)
- **RDF/OWL/RDFS** — W3C standards

**Format:** Turtle (RDF)

---

## Comparison: Before & After

| Aspect | msr-ontology.ttl | OntoBinocular | **binocular.ttl** |
|--------|------------------|---------------|-------------------|
| Classes | 81 | 39 | **57** |
| Properties | 135 | 81 | **65** |
| Triples | 648 | 539 | **495** |
| Code Structure | ❌ | ✓ | ✓ |
| Code Ownership | ❌ | ✓ | ✓ |
| Tool Support | ✓ | ❌ | ✓ |
| Lint Issues | 73 | 94 | **0** |
| Namespace | Consistent | Mixed | **Unified** |

---

## Migration Notes

If migrating from msr-ontology.ttl:
- Rename IRIs from `ontology.msr.example.org/` → `ontology.binocular.example.org/`
- Classes remain the same (e.g., `msr:Issue` → `bio:Issue`)
- Properties remain the same (e.g., `msr:author` → `bio:author`)
- Add new Module/CodeOwnership properties as data becomes available

If migrating from OntoBinocularOntology.ttl:
- Consolidate redundant concepts (e.g., multiple Issue subclasses → single bio:Issue)
- Use unified `bio:` namespace instead of mixed external IRIs
- Map tool-specific properties to abstract layer equivalents
- Align to three-layer architecture

---

## Next Steps

1. **Load into ArangoDB** — Import binocular.ttl alongside KG data
2. **Validate with SHACL** — Create shape constraints for data quality
3. **Run Inference** — Apply RDFS/OWL reasoning to derive facts
4. **Build Queries** — Develop SPARQL endpoints for analysis
5. **Phase 2** — Extend with deployment, requirements, team expertise layers

---

## Files

- **binocular.ttl** — The consolidated ontology (Turtle format)
- **BINOCULAR-ONTOLOGY.md** — This documentation
- **msr-ontology.ttl** — Original tool-integration layer (archived)
- **OntoBinocular/OntoBinocularOntology.ttl** — Original code-structure layer (archived)
- **analysis-05-04-2026.md** — Gap analysis that informed design

