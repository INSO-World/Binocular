// I10 — Offline saga + PouchDB plugin pipeline
//
// Mirrors the online Changes saga flow (I6) but wired to the real PouchDB
// plugin backed by an in-memory PouchDB instance instead of MockData.
// Confirms that the offline path produces the same Redux state transitions.
//
// Trigger used: setDateRange (takeEvery, no throttle) rather than REFRESH
// (throttle 5s), to keep tests fast and deterministic.

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { configureStore } from '@reduxjs/toolkit';
import createSagaMiddleware from 'redux-saga';
import PouchDB from 'pouchdb-browser';

// PouchDB plugins are self-registered by importing database.ts
import Database from '../../../plugins/dataPlugins/pouchDB/src/database.ts';
import PouchDbPlugin from '../../../plugins/dataPlugins/pouchDB/src/index.ts';
import CommitsCollection from '../../../plugins/dataPlugins/pouchDB/src/collections/commits.ts';
import changesReducer, { DataState, setDateRange } from '../../../plugins/visualizationPlugins/commits/changes/src/reducer/index.ts';
import changesSaga from '../../../plugins/visualizationPlugins/commits/changes/src/saga/index.ts';

const FROM = '2024-01-01T00:00:00.000Z';
const TO = '2024-12-31T23:59:59.000Z';
const OUTSIDE_FROM = '2020-01-01T00:00:00.000Z';
const OUTSIDE_TO = '2020-12-31T23:59:59.000Z';

function makeDatabase(): Database {
  const uid = `saga_${Date.now()}_${Math.random().toString(36).slice(2)}`;
  const database = new Database();
  database.documentStore = new PouchDB(`${uid}_docs`, { adapter: 'memory' });
  database.edgeStore = new PouchDB(`${uid}_edges`, { adapter: 'memory' });
  return database;
}

/**
 * Creates a PouchDbPlugin with its commits collection wired to the provided
 * in-memory database, bypassing PouchDbPlugin.init() to avoid Worker detection.
 */
function makePouchDbPlugin(database: Database): PouchDbPlugin {
  const plugin = new PouchDbPlugin();
  plugin.commits = new CommitsCollection(database);
  return plugin;
}

function createTestStore(plugin: PouchDbPlugin) {
  const sagaMiddleware = createSagaMiddleware();
  const store = configureStore({
    // The changes saga reads state via `yield select()` and expects the slice
    // to be registered under the key 'plugin'.
    reducer: { plugin: changesReducer },
    middleware: (getDefaultMiddleware) => getDefaultMiddleware({ thunk: false, serializableCheck: false }).concat(sagaMiddleware),
  });
  sagaMiddleware.run(changesSaga, plugin);
  return store;
}

describe('I10 — Offline saga + PouchDB plugin pipeline', () => {
  let database: Database;

  beforeEach(() => {
    localStorage.clear();
    database = makeDatabase();
  });

  afterEach(async () => {
    localStorage.clear();
    await database.documentStore?.destroy();
    await database.edgeStore?.destroy();
  });

  // ── I10.1 — REFRESH_PLUGIN + PouchDB → dataState COMPLETE ───────────────

  it('I10.1 — dataState reaches COMPLETE after setDateRange with seeded commits', async () => {
    await database.documentStore.bulkDocs([
      {
        _id: 'commits/aaa',
        sha: 'aaa',
        shortSha: 'aaa',
        messageHeader: 'fix: a',
        message: 'fix: a',
        date: '2024-06-01T00:00:00.000Z',
        stats: { additions: 3, deletions: 1 },
        webUrl: '',
      },
      { _id: 'users/u1', gitSignature: 'Alice <alice@example.com>' },
    ]);
    await database.edgeStore.bulkDocs([{ _id: 'commits-users/1', from: 'commits/aaa', to: 'users/u1' }]);

    const plugin = makePouchDbPlugin(database);
    const store = createTestStore(plugin);

    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(
      () => {
        expect(store.getState().plugin.dataState).toBe(DataState.COMPLETE);
      },
      { timeout: 3000 },
    );
  });

  // ── I10.2 — commits populated from PouchDB after saga runs ───────────────

  it('I10.2 — commits array is populated from PouchDB after saga runs', async () => {
    await database.documentStore.bulkDocs([
      {
        _id: 'commits/aaa',
        sha: 'aaa',
        shortSha: 'aaa',
        messageHeader: 'fix: a',
        message: 'fix: a',
        date: '2024-03-01T00:00:00.000Z',
        stats: { additions: 1, deletions: 0 },
        webUrl: '',
      },
      {
        _id: 'commits/bbb',
        sha: 'bbb',
        shortSha: 'bbb',
        messageHeader: 'feat: b',
        message: 'feat: b',
        date: '2024-06-01T00:00:00.000Z',
        stats: { additions: 4, deletions: 2 },
        webUrl: '',
      },
      {
        _id: 'commits/ccc',
        sha: 'ccc',
        shortSha: 'ccc',
        messageHeader: 'chore: c',
        message: 'chore: c',
        date: '2024-09-01T00:00:00.000Z',
        stats: { additions: 2, deletions: 1 },
        webUrl: '',
      },
      { _id: 'users/u1', gitSignature: 'Alice <alice@example.com>' },
    ]);
    await database.edgeStore.bulkDocs([
      { _id: 'commits-users/1', from: 'commits/aaa', to: 'users/u1' },
      { _id: 'commits-users/2', from: 'commits/bbb', to: 'users/u1' },
      { _id: 'commits-users/3', from: 'commits/ccc', to: 'users/u1' },
    ]);

    const plugin = makePouchDbPlugin(database);
    const store = createTestStore(plugin);

    store.dispatch(setDateRange({ from: FROM, to: TO }));

    await vi.waitFor(
      () => {
        expect(store.getState().plugin.commits).toHaveLength(3);
      },
      { timeout: 3000 },
    );
  });

  // ── I10.3 — setDateRange triggers a re-fetch from PouchDB ────────────────

  it('I10.3 — dispatching setDateRange triggers re-fetch and updates commits', async () => {
    await database.documentStore.bulkDocs([
      {
        _id: 'commits/old',
        sha: 'old',
        shortSha: 'old',
        messageHeader: 'old',
        message: 'old',
        date: '2022-06-01T00:00:00.000Z',
        stats: { additions: 1, deletions: 0 },
        webUrl: '',
      },
      {
        _id: 'commits/new',
        sha: 'new',
        shortSha: 'new',
        messageHeader: 'new',
        message: 'new',
        date: '2024-06-01T00:00:00.000Z',
        stats: { additions: 1, deletions: 0 },
        webUrl: '',
      },
      { _id: 'users/u1', gitSignature: 'Alice <alice@example.com>' },
    ]);
    await database.edgeStore.bulkDocs([
      { _id: 'commits-users/1', from: 'commits/old', to: 'users/u1' },
      { _id: 'commits-users/2', from: 'commits/new', to: 'users/u1' },
    ]);

    const plugin = makePouchDbPlugin(database);
    const store = createTestStore(plugin);

    // First fetch: only 2022 commit
    store.dispatch(setDateRange({ from: '2022-01-01T00:00:00.000Z', to: '2022-12-31T23:59:59.000Z' }));
    await vi.waitFor(() => expect(store.getState().plugin.dataState).toBe(DataState.COMPLETE), { timeout: 3000 });
    expect(store.getState().plugin.commits).toHaveLength(1);
    expect(store.getState().plugin.commits[0].sha).toBe('old');

    // Second fetch: only 2024 commit
    store.dispatch(setDateRange({ from: FROM, to: TO }));
    await vi.waitFor(() => expect(store.getState().plugin.commits[0]?.sha).toBe('new'), { timeout: 3000 });
    expect(store.getState().plugin.commits).toHaveLength(1);
  });

  // ── I10.4 — empty PouchDB yields commits: [] with COMPLETE state ─────────

  it('I10.4 — empty PouchDB yields commits: [] and dataState: COMPLETE', async () => {
    // No documents seeded — database is empty
    const plugin = makePouchDbPlugin(database);
    const store = createTestStore(plugin);

    store.dispatch(setDateRange({ from: OUTSIDE_FROM, to: OUTSIDE_TO }));

    await vi.waitFor(
      () => {
        expect(store.getState().plugin.dataState).toBe(DataState.COMPLETE);
      },
      { timeout: 3000 },
    );

    expect(store.getState().plugin.commits).toHaveLength(0);
  });
});
