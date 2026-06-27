# Frontend Test Plan

**Frameworks**: Vitest + jsdom (unit/component/integration), Playwright + Chromium (E2E)
**Test ID convention**: `U{file_index}.{test_index}` for unit tests, `C{file_index}.{test_index}` for component tests, `I{file_index}.{test_index}` for integration tests, `E{file_index}.{test_index}` for E2E tests.

---

## Test categories

| Category | File | IDs |
|---|---|---|
| Unit Tests | [docs/TEST_PLAN_UNIT.md](docs/TEST_PLAN_UNIT.md) | U1–U56 |
| Component Tests | [docs/TEST_PLAN_COMPONENT.md](docs/TEST_PLAN_COMPONENT.md) | C1–C44 |
| Integration Tests | [docs/TEST_PLAN_INTEGRATION.md](docs/TEST_PLAN_INTEGRATION.md) | I1–I21 |
| E2E Tests | [docs/TEST_PLAN_E2E.md](docs/TEST_PLAN_E2E.md) | E1–E17 |

---

## Quick reference

- **Unit**: Pure functions and helpers — no DOM, no Redux.
- **Component**: React Testing Library + Redux Provider — render, interact, assert DOM.
- **Integration**: Multi-reducer and saga flows — real in-memory state, pouchdb-memory where needed.
- **E2E**: Playwright + Chromium — full app boot from localStorage, cross-component wiring in App.tsx, real SVG rendering.
