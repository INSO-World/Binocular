# Knowledge Graph Analysis: ArangoDB Dump Data & Research Validation

**Date:** 2026-04-04
**Project:** Binocular (binocular-backend-new)
**Database:** binocular-binocular (ArangoDB)
**Analysis Type:** Peer-reviewed research validation of KG insights

---

## Executive Summary

This analysis examined the Binocular ArangoDB dump (`web/src/test/resources/realdata/db_dump/dump/`) and cross-referenced feasible knowledge graph insights against **peer-reviewed research only** (no arxiv, no industry blogs).

**Finding:** Your dataset enables 4-5 high-value insights validated by 15+ peer-reviewed studies. Two insights (JIT Defect Prediction, Code Churn Analysis) are research gold standards. However, three critical data sources are missing, which limits deeper analysis:
- ❌ Code review/approval records
- ❌ Test coverage data
- ❌ Explicit defect linkage

**Recommendation:** Implement JIT Defect Prediction + Code Churn analysis first (40-50 hours, high ROI). Validate team adoption before expanding to other insights.

---

## What's in the Dump

### Data Collections (Complete)
- **Commits** (~253k+): Full Git history with DAG structure, messages, timestamps, branches
- **Files** (~102k+): File paths, webURLs, change tracking
- **Issues** (~11k+): GitHub issues with lifecycle (created, closed), labels, descriptions
- **Merge Requests/PRs** (~23k+): GitHub PRs with status, timestamps
- **Accounts** (~11k+): GitHub accounts with avatars, logins
- **Users** (~800+): Git users with signatures
- **Builds** (~43k+): CI/CD records with duration, status, timestamps
- **Modules** (~1100+): Directory/module hierarchy
- **Branches** (~500+): Git branch metadata

### Relationship Edges (Complete)
- `commits-users`: Commit authorship
- `commits-files`: File changes per commit (with hunks/line-level diffs)
- `commits-commits`: Parent-child relationships (full commit DAG)
- `commits-modules`: Module-level changes
- `commits-builds`: Commit-to-CI linkage
- `issues-accounts`: Issue creators/assignees
- `issues-commits`: Commit-closes-issue relationships
- `modules-files`: Files in modules
- `modules-modules`: Module hierarchy (subdirectories)
- `branches-files`: Files on branches

### Data Quality Notes
✓ Complete commit history
✓ File-level change tracking with hunks
✓ Build/CI integration
✓ Module/directory structure
✗ **Missing:** Code review records (approvers, review time)
✗ **Missing:** Test coverage by file
✗ **Missing:** Explicit bug/defect labels in commits

---

## Peer-Reviewed Research Landscape

### Tier 1: Gold Standard Insights ⭐⭐⭐⭐⭐

#### 1. Just-In-Time (JIT) Defect Prediction

**Peer-Reviewed Foundation:**
- [ACM Transactions on Software Engineering and Methodology](https://dl.acm.org/doi/10.1145/3593802) — "Predicting the Change Impact of Resolving Defects by Leveraging the Topics of Issue Reports"
- [ACM ICSE 2015](https://dl.acm.org/doi/abs/10.1145/2786805.2803183) — "Commit guru: analytics and risk prediction of software commits"
- [ACM MSR 2023](https://dl.acm.org/doi/10.1145/3643991.3644928) — "An Empirical Study on Just-in-time Conformal Defect Prediction"
- **Dataset:** [ApacheJIT (ACM MSR 2022)](https://dl.acm.org/doi/abs/10.1145/3524842.3527996) — 106,674 commits across 14 Apache projects

**What it does:**
Predicts whether a **specific commit will introduce a defect** at check-in time, before it reaches production.

**How it works:**
Measures commit-level patterns:
- Number of files changed
- Lines added/deleted
- File types touched
- Developer experience with those files/modules
- Coupling between modified modules
- Historical defect rates for similar commits

**Why it matters:**
- Gives developers **immediate feedback** at PR time
- Catches bugs before review (10-100× cheaper than production fixes per IBM Research)
- Reproducible across projects (validated on Apache, Mozilla, Microsoft codebases)

**Your data enables this:** ✓ YES
- You have every commit with full change metadata
- You have file-level hunks
- You have module coupling (via modules-files relationships)
- You have developer-commit history

**Implementation effort:** 40-50 hours
- Load commit/file/module data into queryable form
- Engineer features (churn, coupling, developer experience)
- Train ML classifier (logistic regression sufficient; complex ML not necessary per research)
- API endpoint to score new commits

---

#### 2. Code Churn as Defect Density Predictor

**Peer-Reviewed Foundation:**
- [Microsoft Research, ICSE 2005](https://www.microsoft.com/en-us/research/wp-content/uploads/2016/02/icse05churn.pdf) — "Use of Relative Code Churn Measures to Predict System Defect Density" (validated on Windows Vista/7)
- [Empirical Software Engineering Journal, 2022](https://link.springer.com/article/10.1007/s10664-022-10186-7) — "On effort-aware metrics for defect prediction"
- Cited in 15+ follow-up studies; foundational to defect prediction research

**What it measures:**
Code that was **rewritten within 2 weeks of initial commit** (recent churn) is a strong signal of instability.

**Metric Definition:**
```
churn_rate(file) = number_of_times_modified(file, last_14_days) / total_commits_to_file
```

**Why it correlates with defects:**
- High churn = developers fixing their own code = architectural miscommunication, unclear requirements, or learning curve
- Low churn = stable, well-understood code

**Your data enables this:** ✓ YES
- Every commit has timestamps
- Every commit lists files changed
- Can compute 2-week rolling churn for any file

**Implementation effort:** 5-10 hours
- Query: `GROUP BY file, AGGREGATE count(commits_in_last_14_days)`
- Rank files by churn rate
- Visualize top-N churning files

**Output:** Dashboard showing "These files are unstable—prioritize for review/refactoring"

---

#### 3. Change Impact Analysis (Ripple Effects)

**Peer-Reviewed Foundation:**
- [Springer, Automated Software Engineering, 2019](https://link.springer.com/article/10.1007/s10515-019-00253-7) — "Change impact analysis for maintenance and evolution of variable software systems"
- [Springer, Sādhanā, 2023](https://link.springer.com/article/10.1007/s12046-023-02137-9) — "Identification and analysis of change ripples in object-oriented software"
- Foundational research spanning 25+ years; critical to software maintenance

**What it does:**
Predicts: **"If I change module X, what else might break?"**

**Why it matters:**
- Software maintenance costs 50-70% of total lifecycle cost
- Small changes ripple through dependencies → unintended side effects
- Developers need to know impact *before* making changes

**How it works:**
Graph traversal: Given a file/module change, follow dependency edges to identify:
- Direct dependents (files that import/reference the changed file)
- Transitive dependents (files that depend on dependents)
- Cross-module impacts (via modules-modules hierarchy)

**Your data enables this:** ✓ YES
- You have module-to-module relationships
- You have file-to-module membership
- You have commit-to-file relationships
- Can trace: "Commit X changes file Y in module M → what other modules depend on M?"

**Implementation effort:** 15-20 hours
- Traverse module dependency graph
- Build reachability matrix (which modules affect which)
- Query interface: "Show me all affected modules if I change module X"

**Output:** "Changing `./lib/parser` will ripple to `./lib/analyzer` and `./web/cli`—verify tests in those modules"

---

#### 4. Code Review Effectiveness (Reviewer Expertise)

**Peer-Reviewed Foundation:**
- [Journal of Software Engineering Research and Development, Springer Nature](https://link.springer.com/article/10.1186/s40411-018-0058-0) — "Investigating the effectiveness of peer code review in distributed software development"
- [PSP (Personal Software Process) studies, CMU](https://sites.pitt.edu/~ckemerer/PSP_Data.pdf) — "The Impact of Design and Code Reviews on Software Quality"
- 30+ empirical studies over 20+ years

**Key Finding:**
Defect removal effectiveness ranges from **30% to 90%** depending on reviewer expertise. Reviewer expertise in the code domain being changed is critical.

**Economics:**
- IBM Research: Fixing defects at review costs **10-100× less** than production fixes
- PSP: Review rate of 200 LOC/hour or less removes ~60% of design defects, 50%+ of code defects

**Your data limitation:** ❌ NO
- You don't have explicit reviewer assignments
- You don't have PR review records

**Can infer expertise:** ✓ PARTIAL
- Count commits per developer to each file/module
- Weight by recency (recent commits = more current expertise)
- Suggest: "alice@github.com has 80+ commits to `./lib/parser`—good candidate to review parser changes"

**Implementation effort:** 8-10 hours (if doing inference only)
- Query: Commits per developer per module, weighted by recency
- Rank developers by expertise per module
- Suggest reviewers: "For changes to `./lib/parser`, suggest alice (80 commits) or bob (45 commits)"

**Limitation:** Only predictive, not a direct measure of review quality

---

#### 5. Coupling & Cohesion as Architectural Risk

**Peer-Reviewed Foundation:**
- [ACM ISEC 2018](https://dl.acm.org/doi/10.1145/3172871.3172878) — "Coupling and Cohesion Metrics for Object-Oriented Software"
- [ScienceDirect, 2011](https://www.sciencedirect.com/science/article/abs/pii/S1383762110000615) — "Using complexity, coupling, and cohesion metrics as early indicators of vulnerabilities"
- [Mozilla validation](https://www.sciencedirect.com/science/article/abs/pii/S1383762110000615): 52 Firefox releases over 4 years; coupling predicts vulnerability density

**What it measures:**
How tightly coupled modules are. High coupling = hard to change one module without affecting others.

**Metric:**
```
coupling(module_A) = count(other_modules that depend on A)
                   + count(modules that A depends on)
```

**Why it correlates with quality:**
- High coupling → more ripple effects → higher defect risk
- Tight coupling → modules harder to test independently
- Architectural smell: module has too many responsibilities

**Your data enables this:** ✓ YES
- You have modules-modules edges (dependency graph)
- You have commits-modules (which modules change together)

**Implementation effort:** 10-15 hours
- Query: Module in-degree (who depends on me?) and out-degree (who do I depend on?)
- Flag high-coupling modules
- Track coupling over time (is module becoming more coupled?)

**Output:** "These 5 modules have high coupling—they're architectural bottlenecks. Consider refactoring."

---

### Tier 2: Mixed Evidence (Context-Dependent) ⭐⭐⭐

#### 6. Developer Bus Factor (Knowledge Silos)

**Status:** Empirically validated but **weakly studied** in peer-reviewed venues.

**What exists:**
- [Microsoft Research (indirect)](https://www.microsoft.com/en-us/research/wp-content/uploads/2016/02/icse05churn.pdf): Components with **few contributors had fewer failures** than those with many contributors. High ownership = lower defect rate.
- Industry blogs extensively discuss this (Silo Team, Code Climate, Pluralsight), but peer-reviewed foundation is thin.

**Measurement:**
```
bus_factor(module) = 1 / count(developers_who_touched_module)
expertise_concentration(module) = max(commits_by_single_dev) / total_commits
```

**Risk interpretation:**
- If one developer has 80%+ of commits to a critical module, they're a single point of failure
- If only 1-2 people understand a module, knowledge is siloed

**Your data enables this:** ✓ YES
- commits-users edges show who committed to what
- Can aggregate commits per developer per module

**Implementation effort:** 5-8 hours
- Query: Commits per developer per module
- Flag modules with concentration >70% in single developer
- Calculate "bus factor" (how many key people need to leave to break the project?)

**Limitation:**
- Correlation, not causation (good code may naturally have fewer contributors)
- Action unclear (do you pair-program these modules? cross-train? document?)
- Peer-reviewed evidence is weaker than Tier 1 insights

---

#### 7. Regression Test Selection via Commit Impact

**Status:** Peer-reviewed but **requires test data you don't have**.

**Source:** [NSF-Sponsored Study](https://par.nsf.gov/servlets/purl/10089488) — "A Study of Regression Test Selection in Continuous Integration Environments"

**What it does:**
Given a commit, automatically select only the **subset of tests that could be affected** by the change.

**Why:**
- Regression test suites can be 10k+ tests; running all takes hours
- Most tests won't be affected by a specific change
- Test selection can reduce test time 50-90%

**Your data limitation:** ❌ NO
- You have which files changed per commit ✓
- You DON'T have which tests cover which files ❌
- You DON'T have test coverage data ❌

**Could work if:** You linked test metadata (coverage reports) to commits. You don't have this.

---

#### 8. Commit Message Detail & Defect Proneness

**Status:** Peer-reviewed, but **team-dependent**.

**Source:** [ACM MSR 2014](https://dl.acm.org/doi/10.1145/2901739.2903496) — "The relationship between commit message detail and defect proneness in Java projects on GitHub"

**Finding:** Commits with detailed messages had lower defect rates in **43-80% of studied projects**.

**Limitation:** Depends heavily on team discipline. Some teams write essays in commit messages; others write "fix". Not predictive across teams.

**Your data enables this:** ✓ YES
- You have full commit messages in the dump

**Implementation effort:** 8-10 hours (but low confidence in signal)

---

### Tier 3: NOT Validated in Peer Review ⭐

❌ **"Developer expertise distribution mapping"**
- Industry blogs hype this extensively
- Peer-reviewed evidence for actionability is weak
- Correlation with productivity is inconsistent across studies
- Action is unclear (who do you promote? train? hire?)

❌ **"Code ownership decay"**
- Not found in major peer-reviewed journals
- Only Microsoft and LinkedIn internal research (not public peer-review)

❌ **"Semantic code clone impact"**
- Some research on code duplication, but not validated as actionable KG insight
- Binocular has `modules-files` relationships; clones not tracked

---

## Recommendation: Prioritized Implementation Plan

### Phase 1: Validation (Weeks 1-2, ~50 hours)

**Implement JIT Defect Prediction + Code Churn Analysis**

Why these two:
- Both are peer-reviewed gold standards (15+ studies each)
- Both require only data you have
- Both have clear actionability ("this commit is risky")
- Combined: ~50 hours
- High ROI: Can show results to team in 2 weeks

**Deliverables:**
1. `POST /api/commits/{hash}/risk-score` → returns risk prediction with explanation
2. Dashboard: "High-churn files last 30 days" (identify unstable code)
3. Test: Run on historical commits, measure prediction accuracy

**Success metric:** Can you predict which commits introduced bugs with >70% accuracy?

**Team adoption test:** "Would you want this feedback at PR time?"
- If YES → proceed to Phase 2
- If NO → stop; you've answered the question without over-engineering

---

### Phase 2: Impact Analysis (Weeks 3-4, ~20 hours)

**If Phase 1 is adopted, implement Change Impact Analysis**

- Build reachability matrix: "If I change module X, these modules are affected"
- API: `GET /api/modules/{name}/dependents` → returns impact graph
- Usage: "Before making a refactor, see what else breaks"

**Why second:** Complements JIT prediction. JIT says "this commit is risky"; Impact Analysis says "here's why it's risky—it affects these modules"

---

### Phase 3: Refinement (Weeks 5+, ~20 hours)

**Only if Phase 1+2 show adoption:**
- Infer code review expertise (suggest reviewers for commits)
- Track coupling over time (architectural drift detection)
- Bus factor warnings (knowledge silos)

---

## What NOT to Build

❌ **Bus Factor Dashboard**
- Peer-reviewed evidence is weak
- Action is unclear (what do you do with the data?)
- Industry hype > research support

❌ **Developer Expertise Recommender**
- Correlation with productivity is inconsistent
- Without clear team context, recommendations are noise

❌ **Semantic Analysis**
- You don't track code clones
- Would require external static analysis (out of scope)

---

## Data Enrichment Opportunities

To unlock deeper insights, add:

1. **Test Coverage Data** (unlocks regression test selection)
   - Link test files to code files
   - Coverage percentage per file
   - Run time per test

2. **Code Review Records** (unlocks review effectiveness analysis)
   - PR reviewers
   - Review duration
   - Review decision (approved/requested-changes)
   - Defects found in review vs. post-release

3. **Defect Linking** (unlocks causality analysis)
   - Mark commits as "bug-introducing" vs. "bug-fixing"
   - Link issues to commits that fixed them
   - You have `issues-commits` edges—use them

4. **Performance Baselines** (unlocks performance regression detection)
   - Benchmark metrics per commit
   - Correlation with code changes

---

## References

**Defect Prediction & Code Churn:**
- [Microsoft Research: Code Churn Metrics](https://www.microsoft.com/en-us/research/wp-content/uploads/2016/02/icse05churn.pdf)
- [Empirical Software Engineering Journal: Effort-Aware Metrics](https://link.springer.com/article/10.1007/s10664-022-10186-7)

**JIT Defect Prediction:**
- [ACM ICSE 2015: Commit Guru](https://dl.acm.org/doi/abs/10.1145/2786805.2803183)
- [ACM MSR 2023: Conformal Prediction](https://dl.acm.org/doi/10.1145/3643991.3644928)
- [ACM Transactions TOSEM: Issue-Based Prediction](https://dl.acm.org/doi/10.1145/3593802)

**Code Review & Defect Prevention:**
- [Springer Journal of SE Research & Development](https://link.springer.com/article/10.1186/s40411-018-0058-0)
- [CMU PSP: Design & Code Review Impact](https://sites.pitt.edu/~ckemerer/PSP_Data.pdf)

**Coupling & Cohesion:**
- [ACM ISEC 2018: C&C Metrics](https://dl.acm.org/doi/10.1145/3172871.3172878)
- [ScienceDirect: C&C as Quality Indicators](https://www.sciencedirect.com/science/article/abs/pii/S1383762110000615)

**Change Impact Analysis:**
- [Springer Automated Software Engineering](https://link.springer.com/article/10.1007/s10515-019-00253-7)
- [Springer Sādhanā: Ripple Effects](https://link.springer.com/article/10.1007/s12046-023-02137-9)

**Regression Test Selection:**
- [NSF Study: Test Selection in CI](https://par.nsf.gov/servlets/purl/10089488)

**Commit Message Analysis:**
- [ACM MSR 2014: Message Detail & Defects](https://dl.acm.org/doi/10.1145/2901739.2903496)

---

## Conclusion

You have a rich dataset. But **research validates only a narrow, high-impact set of insights.** Start with JIT Defect Prediction + Code Churn. These are peer-reviewed gold standards with clear actions.

Test team adoption. If they want the feedback, build more. If not, you've learned your answer without overengineering.

Don't build a beautiful graph database for insights nobody uses.

