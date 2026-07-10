In this folder we are building a research prototype for RDF and an ontology for MSR with Binocular.

Binocular is an ETL tool for software repository mining (MSR), which is able to collect data from VCS (Git), ITS (GitHub, GitLab), and related CI systems of these platforms.
The goal of Binocular-RDF is to provide a KG for `Holistic software project analysis`.
For now AST and static code analysis/mining is WIP and future work.
Keep the ontology extensible for such cases when it is integrated.

We are working `ontology-first, but keep it tiny` (small ontology → KG → iterate).

# What's in here:
- `msr-ontology.ttl` was a first try
- `msr-ontology-extended.ttl` is an extension
- `analysis-05-04-2026.md` is an analysis of the state of `msr-ontology.ttl`

## `OntoBinocular` folder
- `OntoBinocularOntology` is an ontology of one of my students.
- You can find summaries and analysis in this folder: `VALIDATION_REPORT.md`, `VALIDATION_SUMMARY.md`, `DATA_MODEL_MAPPING.md`, `ANALYSIS.md`, `ACTION_PLAN.md`.  
- In `FIXES_NEEDED.ttl` you can find some information about the fixes needed of `OntoBinocularOntology` and its gaps.


# Build a minimal ontology first
```
Don't aim for completeness. Define only what you know you need to query. Use an existing upper ontology fragment — don't reinvent. 
Validate it, load it, and show me the stats.
The goal is a shared vocabulary, not a logically complete axiomatisation.
```

You can find the arangodb dump in `../web/src/test/resources/realdata/db_dump/`.

## Technical requirements

Maven project, use super `../pom.xml` as parent if required.
Use Apache Jena for RDF.


