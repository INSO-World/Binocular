# Frontend Test Plan

**Framework**: Vitest + jsdom
**Test ID convention**: `U{file_index}.{test_index}` for unit tests, `C{file_index}.{test_index}` for component tests, `I{file_index}.{test_index}` for integration tests.

---

## Test categories

| Category | File | IDs |
|---|---|---|
| Unit Tests | [docs/TEST_PLAN_UNIT.md](docs/TEST_PLAN_UNIT.md) | U1–U60 |
| Component Tests | [docs/TEST_PLAN_COMPONENT.md](docs/TEST_PLAN_COMPONENT.md) | C1–C42 |
| Integration Tests | [docs/TEST_PLAN_INTEGRATION.md](docs/TEST_PLAN_INTEGRATION.md) | I1–I21 |

---

## Quick reference

- **Unit**: Pure functions and helpers — no DOM, no Redux.
- **Component**: React Testing Library + Redux Provider — render, interact, assert DOM.
- **Integration**: Multi-reducer and saga flows — real in-memory state, pouchdb-memory where needed.
