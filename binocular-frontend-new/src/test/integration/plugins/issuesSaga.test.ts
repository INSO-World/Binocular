// I18 — issues/issues saga + MockData
//
// Mirrors the Changes saga flow (I6) wired to the issues visualization plugin.
// Trigger: setDateRange (takeEvery) — avoids REFRESH throttle for fast tests.

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { configureStore } from '@reduxjs/toolkit';
import createSagaMiddleware from 'redux-saga';

import MockData from '../../../plugins/dataPlugins/mockData/src/index.ts';
import issuesReducer, { DataState, setDateRange } from '../../../plugins/visualizationPlugins/issues/issues/src/reducer/index.ts';
import issuesSaga from '../../../plugins/visualizationPlugins/issues/issues/src/saga/index.ts';

const FROM = '2024-01-01T00:00:00.000Z';
const TO = '2024-12-31T23:59:59.000Z';

function createTestStore(plugin: MockData) {
  const sagaMiddleware = createSagaMiddleware();
  const store = configureStore({
    reducer: { plugin: issuesReducer },
    middleware: (gDM) => gDM({ thunk: false, serializableCheck: false }).concat(sagaMiddleware),
  });
  sagaMiddleware.run(issuesSaga, plugin);
  return store;
}

describe('I18 — issues saga + MockData', () => {
  let mockData: MockData;

  beforeEach(async () => {
    localStorage.clear();
    mockData = new MockData();
    await mockData.init();
  });

  afterEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  // ── I18.1 — fresh store has EMPTY dataState ───────────────────────────────

  it('I18.1 — fresh plugin store has dataState: EMPTY', () => {
    const store = createTestStore(mockData);
    expect(store.getState().plugin.dataState).toBe(DataState.EMPTY);
  });

  // ── I18.2 — dataState reaches COMPLETE after setDateRange ────────────────

  it('I18.2 — dataState reaches COMPLETE after setDateRange', async () => {
    const store = createTestStore(mockData);
    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(() => expect(store.getState().plugin.dataState).toBe(DataState.COMPLETE), { timeout: 3000 });
  });

  // ── I18.3 — issues array is non-empty after saga ─────────────────────────

  it('I18.3 — issues array is non-empty after saga completes', async () => {
    const store = createTestStore(mockData);
    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(() => expect(store.getState().plugin.issues.length).toBeGreaterThan(0), { timeout: 3000 });
  });

  // ── I18.4 — each issue has required fields ────────────────────────────────

  it('I18.4 — each issue in state has iid, title, and state fields', async () => {
    const store = createTestStore(mockData);
    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(() => expect(store.getState().plugin.dataState).toBe(DataState.COMPLETE), { timeout: 3000 });

    for (const issue of store.getState().plugin.issues) {
      expect(issue).toHaveProperty('iid');
      expect(issue).toHaveProperty('title');
      expect(issue).toHaveProperty('state');
    }
  });
});
