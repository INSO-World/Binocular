import { describe, it, expect } from 'vitest';
import reducer, { setLastAction } from '../../../../../redux/reducer/general/actionsReducer';

describe('actionsReducer – initial state', () => {
  it('U44.1 initial lastAction is undefined', () => {
    const state = reducer(undefined, { type: '@@INIT' });
    expect(state.lastAction).toBeUndefined();
  });
});

describe('actionsReducer – setLastAction', () => {
  it('U44.2 setLastAction sets the lastAction string', () => {
    const state = reducer(undefined, setLastAction({ action: 'myAction', payload: null }));
    expect(state.lastAction).toBe('myAction');
  });

  it('U44.3 setLastAction sets the payload', () => {
    const state = reducer(undefined, setLastAction({ action: 'myAction', payload: 42 }));
    expect(state.payload).toBe(42);
  });

  it('U44.4 dispatching again overwrites previous values', () => {
    let state = reducer(undefined, setLastAction({ action: 'first', payload: 1 }));
    state = reducer(state, setLastAction({ action: 'second', payload: 2 }));
    expect(state.lastAction).toBe('second');
    expect(state.payload).toBe(2);
  });
});
