import { describe, it, expect, beforeEach, vi } from 'vitest';
import reducer, { addCustomLayout, saveChanges, deleteCustomLayout } from '../../../../../redux/reducer/general/layoutReducer';
import type { LayoutsInitialState } from '../../../../../redux/reducer/general/layoutReducer';
import { DashboardLayoutCategory } from '../../../../../types/general/dashboardLayoutType';
import type { DashboardLayout } from '../../../../../types/general/dashboardLayoutType';

const emptyState: LayoutsInitialState = {
  customLayouts: [],
  customLayoutCount: 0,
};

function makeLayout(name: string, id?: number): DashboardLayout {
  return { id, name, category: DashboardLayoutCategory.CUSTOM, items: [] };
}

beforeEach(() => {
  localStorage.clear();
});

describe('layoutReducer – addCustomLayout', () => {
  it('U28.1 appends layout to customLayouts', () => {
    const state = reducer(emptyState, addCustomLayout(makeLayout('My Layout')));
    expect(state.customLayouts).toHaveLength(1);
    expect(state.customLayouts[0].name).toBe('My Layout');
  });

  it('U28.2 auto-assigns id from customLayoutCount', () => {
    const stateWithCount: LayoutsInitialState = { ...emptyState, customLayoutCount: 3 };
    const state = reducer(stateWithCount, addCustomLayout(makeLayout('Layout')));
    expect(state.customLayouts[0].id).toBe(3);
  });

  it('U28.3 increments customLayoutCount', () => {
    const state = reducer(emptyState, addCustomLayout(makeLayout('Layout')));
    expect(state.customLayoutCount).toBe(1);
  });

  it('U28.4 persists to localStorage', () => {
    const spy = vi.spyOn(Storage.prototype, 'setItem');
    reducer(emptyState, addCustomLayout(makeLayout('Layout')));
    expect(spy).toHaveBeenCalled();
    spy.mockRestore();
  });
});

describe('layoutReducer – saveChanges', () => {
  const stateWithLayouts: LayoutsInitialState = {
    customLayouts: [makeLayout('Original', 0), makeLayout('Other', 1)],
    customLayoutCount: 2,
  };

  it('U28.5 updates layout with matching id', () => {
    const updated = makeLayout('Updated', 0);
    const state = reducer(stateWithLayouts, saveChanges(updated));
    expect(state.customLayouts.find((l) => l.id === 0)?.name).toBe('Updated');
  });

  it('U28.6 does not affect other layouts', () => {
    const updated = makeLayout('Updated', 0);
    const state = reducer(stateWithLayouts, saveChanges(updated));
    expect(state.customLayouts.find((l) => l.id === 1)?.name).toBe('Other');
  });
});

describe('layoutReducer – deleteCustomLayout', () => {
  const stateWithLayouts: LayoutsInitialState = {
    customLayouts: [makeLayout('Layout A', 0), makeLayout('Layout B', 1)],
    customLayoutCount: 2,
  };

  it('U28.7 removes layout with matching id', () => {
    const state = reducer(stateWithLayouts, deleteCustomLayout(0));
    expect(state.customLayouts.find((l) => l.id === 0)).toBeUndefined();
  });

  it('U28.8 persists to localStorage', () => {
    const spy = vi.spyOn(Storage.prototype, 'setItem');
    reducer(stateWithLayouts, deleteCustomLayout(0));
    expect(spy).toHaveBeenCalled();
    spy.mockRestore();
  });
});
