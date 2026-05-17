# Holistic Project Analysis Architecture — Executive Summary

**Option B Decision:** You're committing to building a genuine project health analysis platform, not just a tool aggregator.

---

## The Strategic Shift

### From: Tool Event Aggregator
> "What happened in GitHub, Jira, and Jenkins?"
- Excellent for audit logs
- Good for multi-tool correlation
- **Silent on:** Code quality, architectural health, team bottlenecks, risk

### To: Project Health Analysis Platform
> "Is this project healthy? Where are the risks? What should we fix?"
- Answers strategic questions
- Guides technical decisions
- Enables data-driven architecture improvements

---

## Architecture at a Glance

```
┌─────────────────────────────────────────────────────────────────┐
│                    USER-FACING ANALYTICS                        │
│  "Which modules have bus factor = 1?"                           │
│  "What code is most complex and untested?"                      │
│  "Correlate commits to production incidents"                    │
│  Dashboards, reports, alerts                                    │
└────────────────────────────┬────────────────────────────────────┘
                             │
                    SPARQL Query Layer
                             │
┌────────────────────────────┴────────────────────────────────────┐
│          SEMANTIC KNOWLEDGE GRAPH (RDF Triple Store)            │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Layer 3: Governance & Evolution (Nice to Have)         │   │
│  │  - Architecture Decision Records (ADRs)                  │   │
│  │  - Refactoring events, churn patterns                    │   │
│  │  - Code ownership policies                               │   │
│  └──────────────────────────────────────────────────────────┘   │
│                             △                                     │
│                             ├─ links to                           │
│                             │                                     │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Layer 2: Metrics, Deployment, Traceability (High Value)│   │
│  │  - Code complexity, coverage, churn per module           │   │
│  │  - Deployment records, production incidents             │   │
│  │  - Issue → Code → Test lineage                          │   │
│  └──────────────────────────────────────────────────────────┘   │
│                             △                                     │
│                             ├─ links to                           │
│                             │                                     │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Layer 1: Code, Dependencies, Team (CRITICAL)           │   │
│  │  - Packages, modules, ownership                          │   │
│  │  - Internal & external dependencies, CVEs               │   │
│  │  - Team expertise distribution, bus factor detection     │   │
│  └──────────────────────────────────────────────────────────┘   │
│                             △                                     │
│                             ├─ links to                           │
│                             │                                     │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Layer 0: Tool Events (Already Have)                    │   │
│  │  - Git commits, branches, tags                          │   │
│  │  - GitHub/GitLab issues, PRs, reviews                   │   │
│  │  - CI builds, test results, artifacts                   │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  All stored as RDF triples (Subject-Predicate-Object)           │
└────────────────────────────┬────────────────────────────────────┘
                             │
        ┌────────────────────┴────────────────────┐
        │      ETL Data Extraction Pipeline       │
        │      (Your existing Binocular infra)    │
        │                                         │
        │  ┌─ Code structure extractors           │
        │  ├─ Dependency parsers (npm, pip, etc)  │
        │  ├─ Git history analyzers               │
        │  ├─ CI/CD system connectors             │
        │  ├─ Metrics aggregators (SonarQube)     │
        │  ├─ Deployment webhook receivers        │
        │  └─ CVE database queries                │
        └─────────────────┬──────────────────────┘
                          │
        ┌─────────────────┴──────────────────┐
        │    EXTERNAL DATA SOURCES          │
        │                                    │
        ├─ Git repositories                 │
        ├─ GitHub, GitLab, Bitbucket        │
        ├─ Jira, Linear, other issue trackers
        ├─ Jenkins, GitHub Actions, CI/CD   │
        ├─ SonarQube, CodeClimate           │
        ├─ NVD, Snyk, GitHub Advisories    │
        ├─ Deployment systems               │
        ├─ Incident tracking (PagerDuty)    │
        └─ APM (DataDog, New Relic)         │
```

---

## Data Flow Example: "Which Module is a Bus Factor Risk?"

1. **Extract** (ETL):
   - Scan git history: "Who committed to which files?"
   - Group commits by module (inferred from package structure)
   - Count unique contributors per module over last 3 months

2. **Model** (RDF Ontology):
   ```turtle
   :AuthModule a code:Module ;
     code:moduleName "auth" ;
     code:moduleLanguage "java" .

   :AliceAuthOwnership a team:CodeOwnership ;
     team:owner :Alice ;
     team:ownedModule :AuthModule ;
     team:ownershipType "primary" ;
     team:commitCount 47 ;
     team:lastActiveDate "2026-04-01"^^xsd:dateTime .

   :BobAuthOwnership a team:CodeOwnership ;
     team:owner :Bob ;
     team:ownedModule :AuthModule ;
     team:ownershipType "secondary" ;
     team:commitCount 3 ;
     team:lastActiveDate "2026-02-15"^^xsd:dateTime .

   :AuthBusFactor a team:BusFactorRisk ;
     team:atRiskModule :AuthModule ;
     team:busFactorValue 2 ;
     team:riskLevel "high" ;
     team:criticalPeople :Alice ;
     team:mitigationHint "Pair Alice with Bob for knowledge transfer" .
   ```

3. **Query** (SPARQL):
   ```sparql
   SELECT ?module ?riskLevel ?hint
   WHERE {
     ?risk a team:BusFactorRisk ;
           team:atRiskModule ?module ;
           team:riskLevel ?riskLevel ;
           team:mitigationHint ?hint .
     FILTER (?riskLevel = "critical" OR ?riskLevel = "high")
   }
   ORDER BY ?riskLevel
   ```

4. **Report** (User-facing):
   ```
   ⚠️  CRITICAL: auth module has only 1 owner (Alice)
       Action: Pair Alice with Bob for knowledge transfer

   ⚠️  HIGH: logging module has only 1 owner (Charlie)
       Action: Schedule architecture review
   ```

---

## Three Implementation Phases

### Phase 1: Foundation (Weeks 1–6)
**Goal:** Answer strategic questions about code structure & team

Deliverables:
- ✓ Module hierarchy extraction
- ✓ Team expertise inference
- ✓ External dependency parsing
- ✓ Bus factor detection
- ✓ First set of SPARQL queries

**Questions answered:**
- "What modules exist?"
- "Who understands what code?"
- "Who's a bottleneck?"
- "What are we depending on?"

---

### Phase 2: Insights (Weeks 7–12)
**Goal:** Add quality metrics, deployment linking, impact analysis

Deliverables:
- ✓ Code metrics integration (SonarQube, etc.)
- ✓ Deployment tracking
- ✓ Incident correlation
- ✓ Churn analysis
- ✓ Traceability (issue → code → test)

**Questions answered:**
- "What code is most complex/untested?"
- "Did that deployment cause the incident?"
- "Is this module under fire-fighting?"
- "What changed to implement feature X?"

---

### Phase 3: Decision Support (Weeks 13+)
**Goal:** Governance, evolution tracking, architectural health

Deliverables:
- ✓ ADR (Architecture Decision Record) management
- ✓ Refactoring ROI tracking
- ✓ Code ownership policy enforcement
- ✓ Approval workflow SLA monitoring

**Questions answered:**
- "Have we violated any architecture rules?"
- "Did that refactoring improve code health?"
- "Who should review this PR?"
- "Are we meeting SLA targets?"

---

## Core Design Principles

### 1. Immutable Data + Timestamped Snapshots
```
Never mutate; always version by timestamp.

WRONG:
  :AuthModule metric:complexity 8.5 .  ← Overwrites previous

RIGHT:
  :AuthModule code:moduleName "auth" .

  :AuthMetrics_Q1_2025 a metric:CodeMetrics ;
    metric:subject :AuthModule ;
    metric:measuredAt "2025-03-31T23:59:59Z"^^xsd:dateTime ;
    metric:cyclomaticComplexity 8.5 .

  :AuthMetrics_Q2_2025 a metric:CodeMetrics ;
    metric:subject :AuthModule ;
    metric:measuredAt "2025-06-30T23:59:59Z"^^xsd:dateTime ;
    metric:cyclomaticComplexity 7.2 .  ← Trend: improving!
```

This enables trend analysis, regression detection, ROI measurement.

### 2. Layer Independence + Forward Compatibility
Each layer is **independently queryable** but **cross-linkable**.

```
Layer 0 (Tool events) works standalone
  ↓ (can be used without Layer 1–3)

Layer 1 (Code structure) links to Layer 0
  ↓ (uses Layer 0 as foundation)

Layer 2 (Metrics) links to Layers 0–1
  ↓ (enriches Layer 1 with quality data)

Layer 3 (Governance) links to all layers
  ↓ (enforces policy across the graph)
```

**Benefit:** You can ship Phase 1 without Phase 2 or 3.

### 3. Derived Data Stored Separately
Computations (bus factor, churn patterns, complexity trends) are stored as separate RDF instances with timestamps, not mutated into source data.

```
Source:    :Alice :committedTo :AuthModule
Derived:   :AuthBusFactor :busFactorValue 1

This way:
- If you improve computation, you regenerate derived data
- Source data remains immutable audit trail
- Easy to validate correctness
```

### 4. Language-Agnostic Core, Tool-Specific Extensions
```
core:Module (generic)
  ↑
  ├─ java:Package
  ├─ python:Module
  ├─ go:Package
  └─ typescript:Namespace

All queryable uniformly, but tool-specific subclasses available for detailed analysis.
```

---

## Integration with Existing Binocular

Your current `msr-ontology.ttl` stays **exactly as-is**. New ontology **extends** it.

**Current structure (Layer 0):**
```
msr:Project ↔ msr:Repository ↔ msr:Revision ↔ msr:Contributor
msr:Project ↔ msr:IssueTracker ↔ msr:Issue ↔ msr:ChangeProposal
msr:Project ↔ msr:Pipeline ↔ msr:BuildRun ↔ msr:TestResult
```

**New connections (Layers 1–3):**
```
msr:Revision → touches → msr:FileChange ← path ← code:File ← hasFile ← code:Module
msr:Contributor ← team:owner ← team:CodeOwnership ← team:ownedModule ← code:Module
msr:Issue ← relatesIssue ← msr:CodeTraceability ← relatesModule ← code:Module
msr:BuildRun → linkedIncidents → msr:Incident → rootCauseModule → code:Module
code:Module ← hasVulnerability ← dep:ExternalDependency ← dep:Vulnerability
```

**Result:** Single unified knowledge graph, all layers queryable together.

---

## Data Volume & Storage Estimates

For a mid-size project (500K LOC, 10 years history, 50 contributors, 3 programming languages):

| Layer | Triple Count | Storage | Update Freq |
|-------|--------------|---------|------------|
| Layer 0 (Tool events) | 500K–1M | ~500 MB | Daily |
| Layer 1 (Code structure) | 50K–200K | ~100 MB | Per commit |
| Layer 1 (Dependencies) | 5K–50K | ~20 MB | Weekly |
| Layer 1 (Team expertise) | 10K–100K | ~50 MB | Daily |
| Layer 2 (Metrics) | 10K–100K | ~100 MB | Weekly/Monthly |
| Layer 2 (Deployment) | 5K–50K | ~20 MB | Per deployment |
| Layer 3 (Governance) | 1K–10K | ~10 MB | As changed |
| **Total** | ~600K–1.5M | **~700 MB–1 GB** | **Mixed** |

**RDF Store Options:**
- **Development:** Apache Jena TDB2 (local SSD, single machine)
- **Production:** Virtuoso (scales to billions of triples)
- **Cloud:** AWS Neptune (managed, multi-region)

---

## Risk Mitigation

### Risk 1: Code Structure Extraction is Language-Specific
**Mitigation:** Start with 1–2 languages (Java, Python), generalize later. Use existing tools (AST parsers, LSP).

### Risk 2: Dependency Resolution is Complex (Transitive, versions, etc.)
**Mitigation:** Start with lock files only (fast, accurate). Add manifest parsing later for more detail.

### Risk 3: Team Expertise Inference from Git is Imperfect
**Mitigation:** Use git history as starting point, allow manual override via CODEOWNERS file. Surface confidence scores in UI.

### Risk 4: Data Quality Varies Across Tools
**Mitigation:** Each data source has extraction module with quality checks. Validate via SPARQL test suite.

---

## Success Criteria

### Phase 1 Success:
- [ ] Can extract module hierarchy from 3+ languages
- [ ] Can infer team expertise from git history
- [ ] Can identify bus factor risks accurately
- [ ] SPARQL queries run in <1 second on 600K triples

### Phase 2 Success:
- [ ] Can correlate commits to production incidents
- [ ] Can identify high-complexity, low-coverage modules
- [ ] Can show code churn trends
- [ ] Dashboard shows top 5 risk areas

### Phase 3 Success:
- [ ] Can auto-validate architecture rules
- [ ] Can measure refactoring ROI
- [ ] Can surface all strategic risks in single view
- [ ] Stakeholders use it for decision-making

---

## Next Immediate Action

1. **Socialize this design** (share with team, leadership)
2. **Pick one data source** to start with (recommend: git history + code structure)
3. **Extract sample data** from one real project
4. **Write one SPARQL query** (bus factor detection)
5. **Validate correctness** of extracted data
6. **Plan Phase 1 sprint** (Week 1: design, Week 2–5: implementation, Week 6: testing)

You're building something that will genuinely improve project health visibility. Good luck!

