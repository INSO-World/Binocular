import { describe, it, expect, beforeEach } from 'vitest';
import reducer, {
  addDashboardItem,
  moveDashboardItem,
  deleteDashboardItem,
  clearDashboard,
  setDashboardState,
  placeDashboardItem,
  updateDashboardItem,
} from '../../../../../redux/reducer/general/dashboardReducer';
import type { DashboardInitialState } from '../../../../../redux/reducer/general/dashboardReducer';
import type { DashboardItemType } from '../../../../../types/general/dashboardItemType';

// Use a small grid to keep tests fast
const GRID = 10;

function makeState(overrides: Partial<DashboardInitialState> = {}): DashboardInitialState {
  return {
    dashboardItems: [],
    placeableItem: undefined,
    dashboardItemCount: 0,
    popupCount: 0,
    dashboardState: Array.from(Array(GRID), () => new Array(GRID).fill(0)),
    initialized: false,
    ...overrides,
  };
}

function makeItem(overrides: Partial<DashboardItemType> = {}): DashboardItemType {
  return { id: 1, x: 0, y: 0, width: 2, height: 2, dataPluginId: undefined, pluginName: 'test', ...overrides };
}

beforeEach(() => {
  localStorage.clear();
});

describe('dashboardReducer – addDashboardItem', () => {
  it('U15.1 places the item and adds it to dashboardItems', () => {
    const state = reducer(makeState(), addDashboardItem(makeItem({ id: 0, x: undefined, y: undefined })));
    expect(state.dashboardItems).toHaveLength(1);
  });

  it('U15.2 increments dashboardItemCount', () => {
    const state = reducer(makeState(), addDashboardItem(makeItem({ id: 0, x: undefined, y: undefined })));
    expect(state.dashboardItemCount).toBe(1);
  });

  it('U15.3 marks the grid cells with the item id', () => {
    const state = reducer(makeState(), addDashboardItem(makeItem({ id: 0, x: 0, y: 0, width: 2, height: 2 })));
    const id = state.dashboardItems[0].id;
    expect(state.dashboardState[0][0]).toBe(id);
    expect(state.dashboardState[1][1]).toBe(id);
  });

  it('U15.4 clears placeableItem after placement', () => {
    const initial = makeState({ placeableItem: makeItem() });
    const state = reducer(initial, addDashboardItem(makeItem({ id: 0, x: undefined, y: undefined })));
    expect(state.placeableItem).toBeUndefined();
  });

  it('U15.5 sets initialized to true', () => {
    const state = reducer(makeState(), addDashboardItem(makeItem({ id: 0 })));
    expect(state.initialized).toBe(true);
  });
});

describe('dashboardReducer – moveDashboardItem', () => {
  it('U15.6 updates the item position when the target space is free', () => {
    const startState = reducer(makeState(), addDashboardItem(makeItem({ id: 0, x: 0, y: 0, width: 2, height: 2 })));
    const item = startState.dashboardItems[0];
    const moved = reducer(startState, moveDashboardItem({ ...item, x: 4, y: 4 }));
    const updatedItem = moved.dashboardItems.find((i) => i.id === item.id)!;
    expect(updatedItem.x).toBe(4);
    expect(updatedItem.y).toBe(4);
  });

  it('U15.7 keeps old cells cleared and fills new cells after move', () => {
    const startState = reducer(makeState(), addDashboardItem(makeItem({ id: 0, x: 0, y: 0, width: 2, height: 2 })));
    const item = startState.dashboardItems[0];
    const moved = reducer(startState, moveDashboardItem({ ...item, x: 4, y: 4 }));
    expect(moved.dashboardState[0][0]).toBe(0);
    expect(moved.dashboardState[4][4]).toBe(item.id);
  });

  it('U15.8 does not move when target space is occupied by another item', () => {
    let state = reducer(makeState(), addDashboardItem(makeItem({ id: 0, x: 0, y: 0, width: 2, height: 2 })));
    state = reducer(state, addDashboardItem(makeItem({ id: 0, x: 4, y: 4, width: 2, height: 2 })));
    const [item1, item2] = state.dashboardItems;
    // Try to move item1 to where item2 is
    const afterMove = reducer(state, moveDashboardItem({ ...item1, x: item2.x, y: item2.y }));
    const unchanged = afterMove.dashboardItems.find((i) => i.id === item1.id)!;
    expect(unchanged.x).toBe(item1.x);
    expect(unchanged.y).toBe(item1.y);
  });
});

describe('dashboardReducer – deleteDashboardItem', () => {
  it('U15.9 removes the item from dashboardItems', () => {
    const state = reducer(makeState(), addDashboardItem(makeItem({ id: 0, x: 0, y: 0, width: 2, height: 2 })));
    const item = state.dashboardItems[0];
    const after = reducer(state, deleteDashboardItem(item));
    expect(after.dashboardItems).toHaveLength(0);
  });

  it('U15.10 clears the item cells in dashboardState', () => {
    const state = reducer(makeState(), addDashboardItem(makeItem({ id: 0, x: 0, y: 0, width: 2, height: 2 })));
    const item = state.dashboardItems[0];
    const after = reducer(state, deleteDashboardItem(item));
    expect(after.dashboardState[0][0]).toBe(0);
    expect(after.dashboardState[1][1]).toBe(0);
  });

  it('U15.11 leaves sibling items untouched', () => {
    let state = reducer(makeState(), addDashboardItem(makeItem({ id: 0, x: 0, y: 0, width: 2, height: 2 })));
    state = reducer(state, addDashboardItem(makeItem({ id: 0, x: 5, y: 5, width: 2, height: 2 })));
    const [item1, item2] = state.dashboardItems;
    const after = reducer(state, deleteDashboardItem(item1));
    expect(after.dashboardItems.find((i) => i.id === item2.id)).toBeDefined();
    expect(after.dashboardState[5][5]).toBe(item2.id);
  });
});

describe('dashboardReducer – clearDashboard', () => {
  it('U15.12 resets dashboardItems to empty', () => {
    const state = reducer(makeState(), addDashboardItem(makeItem({ id: 0 })));
    const after = reducer(state, clearDashboard());
    expect(after.dashboardItems).toHaveLength(0);
  });

  it('U15.13 resets dashboardState to all zeros', () => {
    const state = reducer(makeState(), addDashboardItem(makeItem({ id: 0, x: 0, y: 0, width: 2, height: 2 })));
    const after = reducer(state, clearDashboard());
    const allZero = after.dashboardState.every((row) => row.every((cell) => cell === 0));
    expect(allZero).toBe(true);
  });

  it('U15.14 resets dashboardItemCount to 0', () => {
    const state = reducer(makeState(), addDashboardItem(makeItem({ id: 0 })));
    const after = reducer(state, clearDashboard());
    expect(after.dashboardItemCount).toBe(0);
  });
});

describe('dashboardReducer – setDashboardState', () => {
  it('U15.15 assigns sequential ids starting at 1', () => {
    const items: DashboardItemType[] = [
      makeItem({ id: 0, x: 0, y: 0, width: 2, height: 2 }),
      makeItem({ id: 0, x: 3, y: 3, width: 2, height: 2 }),
    ];
    const state = reducer(makeState(), setDashboardState(items));
    expect(state.dashboardItems[0].id).toBe(1);
    expect(state.dashboardItems[1].id).toBe(2);
  });

  it('U15.16 fills grid cells with the item ids', () => {
    const items: DashboardItemType[] = [makeItem({ id: 0, x: 0, y: 0, width: 2, height: 2 })];
    const state = reducer(makeState(), setDashboardState(items));
    expect(state.dashboardState[0][0]).toBe(1);
    expect(state.dashboardState[1][1]).toBe(1);
  });

  it('U15.17 sets initialized to true', () => {
    const state = reducer(makeState(), setDashboardState([]));
    expect(state.initialized).toBe(true);
  });
});

describe('dashboardReducer – placeDashboardItem / updateDashboardItem', () => {
  it('U15.18 placeDashboardItem sets placeableItem and initialized', () => {
    const item = makeItem();
    const state = reducer(makeState(), placeDashboardItem(item));
    expect(state.placeableItem).toEqual(item);
    expect(state.initialized).toBe(true);
  });

  it('U15.19 updateDashboardItem replaces matching item in the list', () => {
    let state = reducer(makeState(), addDashboardItem(makeItem({ id: 0 })));
    const item = state.dashboardItems[0];
    state = reducer(state, updateDashboardItem({ ...item, pluginName: 'updated' }));
    expect(state.dashboardItems[0].pluginName).toBe('updated');
  });
});
