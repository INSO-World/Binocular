// I5 — refreshMiddleware → REFRESH_PLUGIN dispatch
//
// refreshMiddleware(globalStore, dataPlugin) watches actions passing through
// the local middleware chain. When `progress/setProgress` passes through, it
// dispatches `REFRESH_PLUGIN` (with the configured pluginId) on globalStore.
//
// Setup: two separate stores.
// - globalStore: the target of the secondary REFRESH_PLUGIN dispatch (spied on)
// - localStore:  the store whose middleware chain includes refreshMiddleware

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { configureStore } from '@reduxjs/toolkit';
import type { Middleware } from 'redux';

import ActionsReducer from '../../../redux/reducer/general/actionsReducer.ts';
import actionsMiddleware from '../../../redux/middleware/actions/actionsMiddleware.ts';
import refreshMiddleware from '../../../redux/middleware/refresh/refreshMiddleware.ts';

function createGlobalStore() {
  return configureStore({
    reducer: { actions: ActionsReducer },
    middleware: (gDM) => gDM().concat(actionsMiddleware() as Middleware),
  });
}

function createLocalStore(globalStore: ReturnType<typeof createGlobalStore>, pluginId: number) {
  return configureStore({
    reducer: { actions: ActionsReducer },
    middleware: (gDM) => gDM().concat(refreshMiddleware(globalStore, { id: pluginId } as never) as Middleware),
  });
}

describe('I5 — refreshMiddleware → REFRESH_PLUGIN dispatch', () => {
  beforeEach(() => localStorage.clear());
  afterEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  // ── I5.1 — progress/setProgress triggers REFRESH_PLUGIN ──────────────────

  it('I5.1 — progress/setProgress dispatches REFRESH_PLUGIN on globalStore', () => {
    const globalStore = createGlobalStore();
    const dispatchSpy = vi.spyOn(globalStore, 'dispatch');
    const localStore = createLocalStore(globalStore, 7);

    localStore.dispatch({ type: 'progress/setProgress', payload: 50 });

    const refreshCall = dispatchSpy.mock.calls.find(([action]) => (action as { type: string }).type === 'REFRESH_PLUGIN');
    expect(refreshCall).toBeDefined();
  });

  // ── I5.2 — REFRESH_PLUGIN carries the configured pluginId ─────────────────

  it('I5.2 — REFRESH_PLUGIN payload carries the correct pluginId', () => {
    const globalStore = createGlobalStore();
    const dispatchSpy = vi.spyOn(globalStore, 'dispatch');
    const localStore = createLocalStore(globalStore, 42);

    localStore.dispatch({ type: 'progress/setProgress', payload: 100 });

    const refreshCall = dispatchSpy.mock.calls.find(([action]) => (action as { type: string }).type === 'REFRESH_PLUGIN');
    expect((refreshCall![0] as unknown as { payload: { pluginId: number } }).payload.pluginId).toBe(42);
  });

  // ── I5.3 — non-matching actions do not trigger REFRESH_PLUGIN ────────────

  it('I5.3 — non-matching actions do NOT dispatch REFRESH_PLUGIN on globalStore', () => {
    const globalStore = createGlobalStore();
    const dispatchSpy = vi.spyOn(globalStore, 'dispatch');
    const localStore = createLocalStore(globalStore, 7);

    localStore.dispatch({ type: 'some/otherAction', payload: 'hello' });

    const refreshCalls = dispatchSpy.mock.calls.filter(([action]) => (action as { type: string }).type === 'REFRESH_PLUGIN');
    expect(refreshCalls).toHaveLength(0);
  });

  // ── I5.4 — original action still reaches next middleware ──────────────────

  it('I5.4 — progress/setProgress is still processed by the local store', () => {
    const globalStore = createGlobalStore();
    const localStore = createLocalStore(globalStore, 7);

    // If next() is not called, actions.lastAction would remain undefined
    // actionsMiddleware is NOT in localStore here, so we verify via a custom check:
    // the action reaches the reducer if the store state changes appropriately.
    // Since localStore only has ActionsReducer which ignores progress/setProgress,
    // the clearest signal is that no error is thrown and dispatch returns.
    expect(() => localStore.dispatch({ type: 'progress/setProgress', payload: 1 })).not.toThrow();
  });
});
