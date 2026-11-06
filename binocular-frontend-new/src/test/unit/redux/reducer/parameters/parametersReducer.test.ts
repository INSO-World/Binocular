import { describe, it, expect, beforeEach, vi } from 'vitest';
import reducer, {
  setParametersGeneral,
  setParametersDateRange,
  clearParametersStorage,
  parametersInitialState,
} from '../../../../../redux/reducer/parameters/parametersReducer';

beforeEach(() => {
  localStorage.clear();
});

describe('parametersReducer – setParametersGeneral', () => {
  it('U18.1 updates parametersGeneral', () => {
    const state = reducer(parametersInitialState, setParametersGeneral({ granularity: 'days', excludeMergeCommits: true }));
    expect(state.parametersGeneral.granularity).toBe('days');
    expect(state.parametersGeneral.excludeMergeCommits).toBe(true);
  });

  it('U18.2 persists to localStorage', () => {
    const spy = vi.spyOn(Storage.prototype, 'setItem');
    reducer(parametersInitialState, setParametersGeneral({ granularity: 'months', excludeMergeCommits: false }));
    expect(spy).toHaveBeenCalled();
    spy.mockRestore();
  });
});

describe('parametersReducer – setParametersDateRange', () => {
  it('U18.3 updates parametersDateRange', () => {
    const from = '2022-01-01T00:00:00';
    const to = '2022-12-31T00:00:00';
    const state = reducer(parametersInitialState, setParametersDateRange({ from, to }));
    expect(state.parametersDateRange.from).toBe(from);
    expect(state.parametersDateRange.to).toBe(to);
  });

  it('U18.4 persists to localStorage', () => {
    const spy = vi.spyOn(Storage.prototype, 'setItem');
    reducer(parametersInitialState, setParametersDateRange({ from: '2023-01-01T00:00:00', to: '2023-12-31T00:00:00' }));
    expect(spy).toHaveBeenCalled();
    spy.mockRestore();
  });
});

describe('parametersReducer – clearParametersStorage', () => {
  it('U18.5 calls localStorage.removeItem', () => {
    const spy = vi.spyOn(Storage.prototype, 'removeItem');
    reducer(parametersInitialState, clearParametersStorage());
    expect(spy).toHaveBeenCalled();
    spy.mockRestore();
  });
});
