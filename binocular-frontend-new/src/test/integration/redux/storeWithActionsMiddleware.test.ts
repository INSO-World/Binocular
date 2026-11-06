// I1 — Redux store + actionsMiddleware
//
// Verifies that actionsMiddleware intercepts every dispatched action (except
// setLastAction itself) and records it in actionsReducer.lastAction, does not
// recurse, and overwrites on subsequent dispatches.

import { describe, it, expect, beforeEach } from 'vitest';
import { createTestStore } from '../helpers.ts';
import { setLastAction } from '../../../redux/reducer/general/actionsReducer.ts';
import { addDataPlugin } from '../../../redux/reducer/settings/settingsReducer.ts';

describe('I1 — Redux store + actionsMiddleware', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  // ── I1.1 — fresh store has undefined lastAction ───────────────────────────

  it('I1.1 — fresh store has lastAction === undefined', () => {
    const store = createTestStore();
    expect(store.getState().actions.lastAction).toBeUndefined();
  });

  // ── I1.2 — dispatching any action records its type in lastAction ──────────

  it('I1.2 — dispatching an action updates lastAction to its type', () => {
    const store = createTestStore();
    store.dispatch({ type: 'test/foo', payload: 42 });
    expect(store.getState().actions.lastAction).toBe('test/foo');
  });

  // ── I1.3 — setLastAction itself does not recurse ──────────────────────────

  it('I1.3 — dispatching setLastAction directly does not recurse', () => {
    const store = createTestStore();
    store.dispatch({ type: 'test/first' });
    const beforeValue = store.getState().actions.lastAction;

    // Dispatching setLastAction should not trigger another setLastAction
    store.dispatch(setLastAction({ action: 'manual', payload: null }));

    // lastAction is updated to 'manual' (the direct dispatch),
    // but no infinite loop or extra dispatch occurred
    expect(store.getState().actions.lastAction).toBe('manual');
    expect(beforeValue).toBe('test/first');
  });

  // ── I1.4 — second dispatch overwrites lastAction ──────────────────────────

  it('I1.4 — second dispatch overwrites lastAction', () => {
    const store = createTestStore();
    store.dispatch({ type: 'first/action' });
    store.dispatch({ type: 'second/action' });
    expect(store.getState().actions.lastAction).toBe('second/action');
  });

  // ── I1.5 — object payload is preserved in actions.payload ────────────────

  it('I1.5 — object payload is preserved in actions.payload', () => {
    const store = createTestStore();
    store.dispatch({ type: 'test/withPayload', payload: { a: 1, b: 'hello' } });
    expect(store.getState().actions.payload).toEqual({ a: 1, b: 'hello' });
  });

  // ── I1.6 — RTK action creator produces the correct lastAction type ────────

  it('I1.6 — RTK slice action creator dispatch is recorded with its full type', () => {
    const store = createTestStore();
    store.dispatch(
      addDataPlugin({
        name: 'TestPlugin',
        color: '#ff0000',
        isDefault: false,
        parameters: { apiKey: undefined, endpoint: undefined, fileName: undefined, progressUpdate: undefined },
      }),
    );
    expect(store.getState().actions.lastAction).toBe('settings/addDataPlugin');
  });
});
