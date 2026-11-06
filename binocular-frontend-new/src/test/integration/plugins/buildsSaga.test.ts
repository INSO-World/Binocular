// I19 — builds/builds saga + MockData
//
// Mirrors the Changes saga flow (I6) wired to the builds visualization plugin.
// Trigger: setDateRange (takeEvery) — avoids REFRESH throttle for fast tests.

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { configureStore } from '@reduxjs/toolkit';
import createSagaMiddleware from 'redux-saga';

import MockData from '../../../plugins/dataPlugins/mockData/src/index.ts';
import buildsReducer, { DataState, setDateRange } from '../../../plugins/visualizationPlugins/builds/builds/src/reducer/index.ts';
import buildsSaga from '../../../plugins/visualizationPlugins/builds/builds/src/saga/index.ts';

const FROM = '2024-01-01T00:00:00.000Z';
const TO = '2024-12-31T23:59:59.000Z';

function createTestStore(plugin: MockData) {
  const sagaMiddleware = createSagaMiddleware();
  const store = configureStore({
    reducer: { plugin: buildsReducer },
    middleware: (gDM) => gDM({ thunk: false, serializableCheck: false }).concat(sagaMiddleware),
  });
  sagaMiddleware.run(buildsSaga, plugin);
  return store;
}

describe('I19 — builds saga + MockData', () => {
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

  // ── I19.1 — fresh store has EMPTY dataState ───────────────────────────────

  it('I19.1 — fresh plugin store has dataState: EMPTY', () => {
    const store = createTestStore(mockData);
    expect(store.getState().plugin.dataState).toBe(DataState.EMPTY);
  });

  // ── I19.2 — dataState reaches COMPLETE after setDateRange ────────────────

  it('I19.2 — dataState reaches COMPLETE after setDateRange', async () => {
    const store = createTestStore(mockData);
    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(() => expect(store.getState().plugin.dataState).toBe(DataState.COMPLETE), { timeout: 3000 });
  });

  // ── I19.3 — builds array is non-empty after saga ──────────────────────────

  it('I19.3 — builds array is non-empty after saga completes', async () => {
    const store = createTestStore(mockData);
    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(() => expect(store.getState().plugin.builds.length).toBeGreaterThan(0), { timeout: 3000 });
  });

  // ── I19.4 — each build has required fields ────────────────────────────────

  it('I19.4 — each build in state has id, status, and createdAt fields', async () => {
    const store = createTestStore(mockData);
    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(() => expect(store.getState().plugin.dataState).toBe(DataState.COMPLETE), { timeout: 3000 });

    for (const build of store.getState().plugin.builds) {
      expect(build).toHaveProperty('id');
      expect(build).toHaveProperty('status');
      expect(build).toHaveProperty('createdAt');
    }
  });
});
