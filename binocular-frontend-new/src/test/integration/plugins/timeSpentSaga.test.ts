// I21 — authorBehaviour/timeSpent saga + MockData
//
// Mirrors the Changes saga flow (I6) wired to the timeSpent visualization plugin.
// Trigger: setDateRange (takeEvery) — timeSpent uses throttle(5000) on REFRESH only.
//
// CRITICAL: The timeSpent saga uses a bare `yield select()` (no selector function),
// which returns the entire Redux root state typed as TimeSpentState.
// Therefore the store MUST be configured as:
//   reducer: timeSpentReducer          (NOT { plugin: timeSpentReducer })
// State is accessed directly: store.getState().dataState, store.getState().notes
//
// DataPluginNote has NO `id` field — check body, createdAt, author instead.

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { configureStore } from '@reduxjs/toolkit';
import createSagaMiddleware from 'redux-saga';

import MockData from '../../../plugins/dataPlugins/mockData/src/index.ts';
import timeSpentReducer, {
  DataState,
  setDateRange,
} from '../../../plugins/visualizationPlugins/authorBehaviour/timeSpent/src/reducer/index.ts';
import timeSpentSaga from '../../../plugins/visualizationPlugins/authorBehaviour/timeSpent/src/saga/index.ts';

const FROM = '2024-01-01T00:00:00.000Z';
const TO = '2024-12-31T23:59:59.000Z';

function createTestStore(plugin: MockData) {
  const sagaMiddleware = createSagaMiddleware();
  // Root reducer IS the timeSpent reducer — saga does `yield select()` which
  // returns the full root state and expects TimeSpentState shape directly.
  const store = configureStore({
    reducer: timeSpentReducer,
    middleware: (gDM) => gDM({ thunk: false, serializableCheck: false }).concat(sagaMiddleware),
  });
  sagaMiddleware.run(timeSpentSaga, plugin);
  return store;
}

describe('I21 — timeSpent saga + MockData', () => {
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

  // ── I21.1 — fresh store has EMPTY dataState ───────────────────────────────

  it('I21.1 — fresh store has dataState: EMPTY', () => {
    const store = createTestStore(mockData);
    expect(store.getState().dataState).toBe(DataState.EMPTY);
  });

  // ── I21.2 — dataState reaches COMPLETE after setDateRange ────────────────

  it('I21.2 — dataState reaches COMPLETE after setDateRange', async () => {
    const store = createTestStore(mockData);
    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(() => expect(store.getState().dataState).toBe(DataState.COMPLETE), { timeout: 3000 });
  });

  // ── I21.3 — notes array is non-empty after saga ───────────────────────────

  it('I21.3 — notes array is non-empty after saga completes', async () => {
    const store = createTestStore(mockData);
    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(() => expect(store.getState().notes.length).toBeGreaterThan(0), { timeout: 3000 });
  });

  // ── I21.4 — each note has required fields ────────────────────────────────
  // DataPluginNote has no `id` field — check body, createdAt, author instead.

  it('I21.4 — each note in state has body, createdAt, and author fields', async () => {
    const store = createTestStore(mockData);
    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(() => expect(store.getState().dataState).toBe(DataState.COMPLETE), { timeout: 3000 });

    for (const note of store.getState().notes) {
      expect(note).toHaveProperty('body');
      expect(note).toHaveProperty('createdAt');
      expect(note).toHaveProperty('author');
    }
  });
});
