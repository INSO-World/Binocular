# Holistic Project Analysis Ontology — Implementation Guide

**Date:** 2026-04-05
**Status:** Design Phase (Ready for Implementation Planning)
**Scope:** Option B — Ambitious extension to support real project health analysis

---

## Quick Overview

You're building a **4-layer semantic knowledge graph** that transforms Binocular from a "tool event aggregator" into a genuine project analysis platform.

### The Layers (in order of implementation priority)

| Layer | Purpose | Est. Effort | ROI | Status |
|-------|---------|------------|-----|--------|
| **Layer 0** | Tool events (VCS, ITS, CI) | ✓ Done | Good | ✓ Current `msr-ontology.ttl` |
| **Layer 1** | Code structure, dependencies, team expertise | High | **Very High** | 📝 New `msr-ontology-extended.ttl` |
| **Layer 2** | Metrics, deployment, traceability | Medium | High | 📝 New (same file) |
| **Layer 3** | Governance, evolution, requirements | Medium | Medium | 📝 Nice to have (same file) |

---

## Layer 1: The Critical Foundation (Do This First)

### 1.1 Code Structure Mapping

**What to build:**
- Extract package/module hierarchy from source code
- Map files to modules
- Optionally extract classes, functions, types (requires LSP or AST parser)

**Data sources:**
```
Git repo
  ↓
Language-specific parser (LSP, AST, or simple analysis)
  ↓
code:Module, code:File, code:CodeEntity instances
```

**Implementation options (in order of effort):**

1. **Minimal (2 weeks):** File-based module inference
   - Java: Parse `package` declarations
   - Python: Infer from directory structure (packages are directories with `__init__.py`)
   - Go: Parse `package` statements
   - TypeScript: Parse `namespace` or infer from directory
   - **Tool:** Write language-specific regex/parser for each
   - **Output:** Populates `code:Module`, `code:File`, basic hierarchy

2. **Better (4 weeks):** Use Language Servers (LSP)
   - Leverage existing language servers (clangd, gopls, pylsp, tsserver)
   - Query for function/class definitions
   - **Tool:** LSP client in your ETL pipeline
   - **Output:** Adds `code:Class`, `code:Function`, `code:Type` entities

3. **Best (8 weeks):** Multi-language AST analysis
   - Full semantic analysis per language (Roslyn for C#, tree-sitter for 15 languages, rustc for Rust)
   - Enables call graphs, reference graphs
   - **Tool:** Polyglot AST framework (e.g., tree-sitter, Sourcetrail, or custom)
   - **Output:** Complete `code:CodeEntity` graph + call graph

**Recommendation:** Start with minimal (option 1), then upgrade to LSP (option 2) once working.

---

### 1.2 Dependency Extraction

**What to build:**
- Internal module dependencies (which modules import/depend on which)
- External package dependencies (npm, pip, Maven, Cargo, etc.)
- Vulnerability linking (CVEs)

**Data sources:**
```
Git repo (manifest files)
  ↓
Manifest parsers (package.json, requirements.txt, pom.xml, Cargo.toml, go.mod, …)
  ↓
dep:InternalDependency, dep:ExternalDependency instances

CVE databases (NVD, Snyk, GitHub Advisory)
  ↓
dep:Vulnerability instances
```

**Implementation options (in order of effort):**

1. **Minimal (2 weeks):** Parse lock files only
   - package-lock.json, yarn.lock, requirements.lock, go.sum, Cargo.lock
   - **Tool:** JSON/TOML/lock file parsers
   - **Output:** Exact pinned versions, vulnerability matching via version ranges

2. **Better (3 weeks):** Add manifest parsing + lock file reconciliation
   - Parse `package.json`, `requirements.txt`, `pom.xml`, `go.mod`
   - Compare against lock files for transitive analysis
   - **Tool:** Multi-format manifest parsers
   - **Output:** Distinguishes direct vs. transitive, understands constraints

3. **Best (6 weeks):** Add architecture layer validation
   - Define allowed dependency patterns per project (e.g., "data layer can't depend on UI layer")
   - Auto-detect violations
   - **Tool:** Dependency rule engine
   - **Output:** Architecture validation queries

4. **Integrate CVE data (1 week):**
   - Query NVD, Snyk, GitHub Advisory APIs for known vulnerabilities
   - Link to `dep:ExternalDependency` via package name + version range
   - **Tool:** Existing CVE APIs + caching
   - **Output:** `dep:Vulnerability` instances with scores, fix versions, etc.

**Recommendation:** Start with minimal (lock file parsing), ship quickly, iterate.

---

### 1.3 Team Expertise & Code Ownership

**What to build:**
- Infer code ownership from commit/review history
- Detect bus factor risks
- Identify expertise breadth/depth patterns

**Data sources:**
```
Git history (commits, authors)
  ↓
team:CodeOwnership instances (derived: "Alice authored X commits in auth module")

GitHub/GitLab PRs (reviewer data)
  ↓
team:CodeReviewQuality instances

CODEOWNERS file (optional explicit policy)
  ↓
Validate against actual history
```

**Implementation (1-2 weeks, computable from existing data):**

```sparql
# Example SPARQL query to derive code ownership:
SELECT ?contributor ?module (COUNT(?revision) as ?commitCount)
WHERE {
  ?revision msr:touches ?fileChange ;
            msr:authoredBy ?contributor ;
            dcterms:modified ?date .
  ?fileChange msr:path ?path .
  ?module code:hasFile ?file ;
          code:filePath ?path .
  FILTER(?date > NOW() - P3M)  # Last 3 months
}
GROUP BY ?contributor ?module
ORDER BY DESC(?commitCount)
```

**Bus factor detection (1 week):**
```
For each module:
  owners = contributors with commit > 20th percentile
  bus_factor = COUNT(owners)
  IF bus_factor == 1: CRITICAL RISK
  IF bus_factor == 2: HIGH RISK
  IF bus_factor >= 3: OK
```

**Recommendation:** Start simple (commit-based ownership), add review metrics later.

---

## Layer 2: The Multipliers (High Value, Do Next)

### 2.1 Code Metrics

**What to build:**
- Code complexity (cyclomatic complexity)
- Lines of code per module
- Maintainability index
- Test coverage per module

**Data sources:**
```
Code metrics tools (SonarQube, CodeFactor, CodeClimate)
  OR
Language-specific analyzers (Pylint, ESLint, go vet, Clippy)
  ↓
metric:CodeMetrics instances
```

**Implementation (2-3 weeks):**

1. **Minimal:** Integrate existing CI tool outputs
   - Many CI systems (GitHub Actions, GitLab CI) already run linters/metrics
   - Parse their output, correlate to modules
   - **Effort:** 1 week

2. **Better:** Run SonarQube or CodeClimate on every commit
   - Gets you complexity, duplication, smells
   - **Effort:** 2 weeks (set up in CI)

3. **Best:** Language-specific deep analysis
   - Per-function complexity, coverage details
   - **Effort:** 3+ weeks

**Recommendation:** Start by parsing existing CI output, add SonarQube later.

---

### 2.2 Deployment & Runtime Linking

**What to build:**
- Record deployments (which commit → which environment → when)
- Link to production incidents
- Track performance metrics

**Data sources:**
```
Deployment systems (ArgoCD, Spinnaker, CloudFormation, Kubernetes)
  ↓
msr:Deployment instances

Incident systems (PagerDuty, Opsgenie, internal)
  ↓
msr:Incident instances

APM systems (DataDog, New Relic, Prometheus)
  ↓
msr:PerformanceMetrics instances
```

**Implementation (2-3 weeks):**

1. **Minimal:** Webhook from CI/CD
   - When a build succeeds → record deployment with timestamp
   - Link to previous msr:BuildRun (already in ontology)
   - **Effort:** 1 week

2. **Better:** Correlate deployments to incidents
   - Incident started at time T, most recent deployment was at T-N
   - Probably related, create link
   - **Effort:** 1 week

3. **Best:** Add performance metrics
   - Poll APM systems, store p50/p99 latency, error rates
   - Detect regressions post-deployment
   - **Effort:** 2-3 weeks

**Recommendation:** Start with webhook (effort 1 week), ship immediately.

---

## Implementation Roadmap

### Phase 1: Tier 1 (Weeks 1–6)

```
Week 1–2:  Code structure extraction (minimal: files + packages)
Week 2–3:  Team expertise inference (from existing git history)
Week 3–4:  External dependency parsing (lock files)
Week 4–5:  Internal dependency detection + basic analysis
Week 5–6:  Integration testing + initial SPARQL queries

Output: Binocular can answer:
  ✓ "What modules exist and who owns them?"
  ✓ "What external libraries do we depend on?"
  ✓ "Who is a bottleneck (bus factor = 1)?"
  ✓ "Which modules have circular dependencies?"
```

### Phase 2: Tier 2 (Weeks 7–12)

```
Week 7–8:  Code metrics integration (SonarQube or existing CI output)
Week 8–9:  Deployment system webhook
Week 9–10: Incident → deployment correlation
Week 10–11: Churn pattern analysis
Week 11–12: Traceability (issue → code mapping)

Output: Binocular can answer:
  ✓ "What code is most complex / least tested?"
  ✓ "Which deployments caused incidents?"
  ✓ "Which modules are fire-fighting vs. development?"
  ✓ "What code was changed to implement feature X?"
```

### Phase 3: Tier 3 (Weeks 13+)

```
Weeks 13+: ADRs, refactoring event detection, governance rules, etc.

Output: Binocular becomes a true project health dashboard.
```

---

## Data Integration Pattern

All data flows through your ETL pipeline:

```
┌──────────────────┐
│  External Data   │
│  Sources:        │
│  - Git repos     │
│  - GitHub/GitLab │
│  - Jira          │
│  - Jenkins/CI    │
│  - SonarQube     │
│  - Snyk CVEs     │
│  - etc.          │
└────────┬─────────┘
         │
         ↓
┌──────────────────────────────┐
│  Binocular ETL Pipeline      │
│  (Your existing infra)       │
│                              │
│  - Fetch data from sources   │
│  - Parse/normalize           │
│  - Generate RDF triples      │
│  - Insert into RDF store     │
└────────┬─────────────────────┘
         │
         ↓
┌──────────────────────────────┐
│  RDF Triple Store            │
│  (Apache Jena, Virtuoso, etc)│
│                              │
│  - Layer 0: Tool events      │
│  - Layer 1: Code structure   │
│  - Layer 2: Metrics          │
│  - Layer 3: Governance       │
└────────┬─────────────────────┘
         │
         ↓
┌──────────────────────────────┐
│  Query Layer (SPARQL)        │
│  & Analytics Engine          │
│                              │
│  "Show bus factor risks"     │
│  "Find untested modules"     │
│  "Correlate commit → incident"
└──────────────────────────────┘
```

---

## Key Design Decisions

### 1. Keep Layer 0 (Tool Events) Unchanged

Your current `msr-ontology.ttl` is solid. Don't modify it. The new ontology (`msr-ontology-extended.ttl`) **extends** it via owl:ObjectProperty links.

Example linking:
```turtle
code:File code:touchedByRevision msr:Revision .
dep:ExternalDependency dep:usedByModule code:Module .
team:CodeOwnership team:ownedModule code:Module .
```

### 2. Timestamps Everywhere

All metrics are keyed by (subject, timestamp) to enable trend analysis:
```turtle
:AuthModule a code:Module ;
  code:moduleName "auth" .

:AuthMetrics2025Q1 a metric:CodeMetrics ;
  metric:subject :AuthModule ;
  metric:measuredAt "2025-01-01T00:00:00Z"^^xsd:dateTime ;
  metric:cyclomaticComplexity 8.3 ;
  metric:linesOfCode 2400 .

:AuthMetrics2025Q2 a metric:CodeMetrics ;
  metric:subject :AuthModule ;
  metric:measuredAt "2025-04-01T00:00:00Z"^^xsd:dateTime ;
  metric:cyclomaticComplexity 7.1 ;  # Improved!
  metric:linesOfCode 2100 .
```

This allows:
- Trend queries: "Is complexity trending down?"
- Regression detection: "Did quality drop after deployment X?"
- ROI analysis: "Did refactoring improve things?"

### 3. Derived vs. Explicit Data

**Derived:** Inferred from other data (e.g., bus factor from commit history)
- Regenerate periodically
- Store as separate instances with timestamps
- Don't overwrite source data

**Explicit:** Human-authored or system-recorded directly (e.g., ADRs, deployment records)
- Immutable once created
- Store with creation metadata (author, date)

### 4. Avoid Over-Normalization

Don't create entities for everything. Example:

**WRONG:**
```turtle
:AuthModuleComplexity a metric:ComplexityValue ;
  metric:value 8.3 .

:AuthMetricsSnapshot a metric:CodeMetrics ;
  metric:hasComplexity :AuthModuleComplexity .
```

**RIGHT:**
```turtle
:AuthMetrics a metric:CodeMetrics ;
  metric:subject :AuthModule ;
  metric:cyclomaticComplexity 8.3 .
```

Simpler triples, easier queries.

---

## SPARQL Query Examples (To Validate Design)

### Example 1: Find Bus Factor Risks

```sparql
PREFIX code: <https://ontology.msr.example.org/code#>
PREFIX team: <https://ontology.msr.example.org/team#>
PREFIX msr: <https://ontology.msr.example.org/msr#>

SELECT ?module (COUNT(DISTINCT ?owner) as ?ownerCount)
WHERE {
  ?ownership team:ownedModule ?module ;
             team:ownershipType "primary" ;
             team:owner ?owner .
}
GROUP BY ?module
HAVING (COUNT(DISTINCT ?owner) <= 1)
ORDER BY ?module
```

**Result:** Modules with only 1 owner (bus factor = 1).

---

### Example 2: Find Untested, Complex Modules

```sparql
PREFIX code: <https://ontology.msr.example.org/code#>
PREFIX metric: <https://ontology.msr.example.org/metrics#>

SELECT ?module ?complexity ?coverage
WHERE {
  ?module a code:Module .

  ?metrics metric:subject ?module ;
           metric:measuredAt ?time ;
           metric:cyclomaticComplexity ?complexity ;
           metric:hasCoverageMetrics ?coverage_metrics .

  ?coverage_metrics metric:lineCoverage ?coverage .

  FILTER (?complexity > 10)     # High complexity
  FILTER (?coverage < 0.5)      # Low coverage

  # Keep only the most recent metrics snapshot
  FILTER NOT EXISTS {
    ?metrics2 metric:subject ?module ;
              metric:measuredAt ?time2 .
    FILTER (?time2 > ?time)
  }
}
ORDER BY DESC(?complexity)
```

**Result:** Dangerous modules: high complexity + low test coverage.

---

### Example 3: Correlate Commits to Incidents

```sparql
PREFIX msr: <https://ontology.msr.example.org/msr#>
PREFIX code: <https://ontology.msr.example.org/code#>

SELECT ?module ?incident ?deployment
WHERE {
  # Find deployment → incident link
  ?deployment msr:sourceRevision ?revision ;
              msr:deployTime ?deployTime ;
              msr:linkedIncidents ?incident .

  ?incident msr:incidentStart ?incidentStart .

  # Find code changed in that revision
  ?revision msr:touches ?fileChange ;
            msr:committedAt ?commitTime .

  ?fileChange msr:path ?path .

  # Correlate to module
  ?module code:hasFile ?file ;
          code:filePath ?path .

  # Sanity check: incident started within 24 hours of deployment
  FILTER (?incidentStart < ?deployTime + "P1D"^^xsd:duration)
}
```

**Result:** "Deployment X touched auth module Y, and incident Z started 3 hours later."

---

## Testing Strategy

For each ontology layer, write SPARQL test queries:

```
tests/
├── layer1_code_structure.sparql
├── layer1_dependencies.sparql
├── layer1_team_expertise.sparql
├── layer2_metrics.sparql
├── layer2_deployment.sparql
├── layer3_governance.sparql
└── integration_cross_layer.sparql
```

Each test:
1. Inserts test data (turtle assertions)
2. Runs SPARQL query
3. Validates expected results

---

## Technology Stack Recommendations

### RDF Store
- **Lightweight:** Apache Jena (TDB2 backend), easy Java integration
- **Production-scale:** Virtuoso, AllegroGraph
- **Cloud:** AWS Neptune, Azure Cosmos DB

### ETL Framework
- Keep what you have (Binocular's existing pipeline)
- Add RDF generation layer
- Use libraries: `rdflib` (Python), Apache Jena (Java)

### Query API
- SPARQL endpoint (standard W3C)
- GraphQL wrapper (optional, for frontend)
- Custom REST API on top

### Visualization
- GraphQL + D3.js (for dependency graphs)
- Time-series dashboards (Grafana, Superset)
- Neo4j browser style (if using property graphs alongside RDF)

---

## Open Questions to Resolve Before Phase 1

1. **Code parsing:** Will you support all languages, or start with a subset (Java, Python, Go)?
2. **Data sources:** Which CI/CD systems, APM tools, and incident tracking systems must you support?
3. **Update frequency:** Real-time (stream), daily, weekly? (Affects ETL scheduling)
4. **Retention:** How long to keep historical metrics? (Affects storage strategy)
5. **Query complexity:** Simple dashboards, or power-user SPARQL queries?
6. **Team size:** Who's building this? Affects scope and timeline.

---

## Next Steps

1. **Review** this ontology design with stakeholders
2. **Pick Phase 1 components** to start with (recommend: code structure + team expertise)
3. **Create data sample** (one real project) to validate extraction/ingestion pipeline
4. **Build first SPARQL query** (bus factor detection)
5. **Iterate** on data quality until queries are useful

---

## Deliverables From This Design

✓ `msr-ontology-extended.ttl` — Full ontology (Layers 1–3)
✓ `ONTOLOGY-IMPLEMENTATION-GUIDE.md` — This document
📝 Data extraction modules (to be built)
📝 SPARQL test suite (to be built)
📝 Dashboard/API (to be built)

Good luck! You're building something genuinely useful.

