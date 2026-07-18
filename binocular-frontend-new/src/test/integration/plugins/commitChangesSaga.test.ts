// I6 — Visualization saga + reducer + MockData
//
// Tests the full REFRESH/setDateRange → saga → MockData.commits.getAll() →
// store update flow using createSagaMiddleware().
//
// MockData.commits.getAll() always returns the same 11 hardcoded commits
// (all dated June–August 2024) regardless of the date range passed.
// Date-range filtering is PouchDB's responsibility (tested in IW1.2).
//
// Trigger used for most tests: setDateRange (takeEvery, no throttle).
// I6.8 uses `{ type: 'REFRESH' }` to exercise the throttle.

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { configureStore } from '@reduxjs/toolkit';
import createSagaMiddleware from 'redux-saga';

import MockData from '../../../plugins/dataPlugins/mockData/src/index.ts';
import changesReducer, { DataState, setDateRange } from '../../../plugins/visualizationPlugins/commits/changes/src/reducer/index.ts';
import changesSaga from '../../../plugins/visualizationPlugins/commits/changes/src/saga/index.ts';

const FROM = '2024-01-01T00:00:00.000Z';
const TO = '2024-12-31T23:59:59.000Z';

function createTestStore(plugin: MockData) {
  const sagaMiddleware = createSagaMiddleware();
  const store = configureStore({
    // saga reads state via `yield select()` and expects key 'plugin'
    reducer: { plugin: changesReducer },
    middleware: (gDM) => gDM({ thunk: false, serializableCheck: false }).concat(sagaMiddleware),
  });
  sagaMiddleware.run(changesSaga, plugin);
  return store;
}

describe('I6 — Visualization saga + reducer + MockData', () => {
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

  // ── I6.1 — fresh store has EMPTY dataState ────────────────────────────────

  it('I6.1 — fresh plugin store has dataState: EMPTY', () => {
    const store = createTestStore(mockData);
    expect(store.getState().plugin.dataState).toBe(DataState.EMPTY);
  });

  // ── I6.2 — dataState is FETCHING before fetch completes ──────────────────

  it('I6.2 — dataState transitions to FETCHING before getAll resolves', async () => {
    let capturedMidState: DataState | undefined;

    vi.spyOn(mockData.commits, 'getAll').mockImplementationOnce(async (from, to) => {
      capturedMidState = store.getState().plugin.dataState;
      const original = new MockData();
      return original.commits.getAll(from, to);
    });

    const store = createTestStore(mockData);
    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(() => expect(store.getState().plugin.dataState).toBe(DataState.COMPLETE), { timeout: 3000 });

    expect(capturedMidState).toBe(DataState.FETCHING);
  });

  // ── I6.3 — dataState reaches COMPLETE after saga ─────────────────────────

  it('I6.3 — dataState is COMPLETE after saga finishes', async () => {
    const store = createTestStore(mockData);
    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(() => expect(store.getState().plugin.dataState).toBe(DataState.COMPLETE), { timeout: 3000 });
  });

  // ── I6.4 — commits array is populated ────────────────────────────────────

  it('I6.4 — commits array is non-empty after saga completes', async () => {
    const store = createTestStore(mockData);
    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(() => expect(store.getState().plugin.commits.length).toBeGreaterThan(0), { timeout: 3000 });
  });

  // ── I6.5 — each commit has required fields ────────────────────────────────

  it('I6.5 — each commit in state has sha, date, and stats fields', async () => {
    const store = createTestStore(mockData);
    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(() => expect(store.getState().plugin.dataState).toBe(DataState.COMPLETE), { timeout: 3000 });

    for (const commit of store.getState().plugin.commits) {
      expect(commit).toHaveProperty('sha');
      expect(commit).toHaveProperty('date');
      expect(commit).toHaveProperty('stats');
    }
  });

  // ── I6.6 — second setDateRange re-fetches data ────────────────────────────

  it('I6.6 — second setDateRange triggers another fetch and reaches COMPLETE again', async () => {
    const store = createTestStore(mockData);

    store.dispatch(setDateRange({ from: FROM, to: TO }));
    await vi.waitFor(() => expect(store.getState().plugin.dataState).toBe(DataState.COMPLETE), { timeout: 3000 });

    // Dispatch again — should re-enter FETCHING then COMPLETE
    store.dispatch(setDateRange({ from: '2023-01-01T00:00:00.000Z', to: '2023-12-31T23:59:59.000Z' }));

    await vi.waitFor(
      () => {
        expect(store.getState().plugin.dataState).toBe(DataState.COMPLETE);
      },
      { timeout: 3000 },
    );
  });

  // ── I6.7 — MockData loads all commits into the store ──────────────────────

  it('I6.7 — MockData.commits.getAll() loads all mock commits into the store', async () => {
    const store = createTestStore(mockData);
    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(() => expect(store.getState().plugin.dataState).toBe(DataState.COMPLETE), { timeout: 3000 });

    const expected = (await mockData.commits.getAll(FROM, TO)).length;
    expect(expected).toBeGreaterThan(0);
    expect(store.getState().plugin.commits).toHaveLength(expected);
  });

  // ── I6.8 — rapid REFRESH dispatches are throttled ─────────────────────────

  it('I6.8 — rapid REFRESH dispatches are throttled to at most one fetch per 5s', async () => {
    const spy = vi.spyOn(mockData.commits, 'getAll');
    const store = createTestStore(mockData);

    // Fire 3 REFRESH actions in quick succession
    store.dispatch({ type: 'REFRESH' });
    store.dispatch({ type: 'REFRESH' });
    store.dispatch({ type: 'REFRESH' });

    // Wait for the first saga run to complete
    await vi.waitFor(() => expect(store.getState().plugin.dataState).toBe(DataState.COMPLETE), { timeout: 3000 });

    // getAll should have been called exactly once — the others were throttled
    expect(spy).toHaveBeenCalledTimes(1);
  });
});
