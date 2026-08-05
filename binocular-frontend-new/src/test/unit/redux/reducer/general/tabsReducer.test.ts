import { describe, it, expect, beforeEach, vi } from 'vitest';
import reducer, { setTabList, clearTabsStorage } from '../../../../../redux/reducer/general/tabsReducer';
import type { TabsInitialState } from '../../../../../redux/reducer/general/tabsReducer';
import { TabAlignment } from '../../../../../types/general/tabType';
import type { TabType } from '../../../../../types/general/tabType';

const emptyState: TabsInitialState = { tabList: [] };

const sampleTab: TabType = {
  selected: false,
  contentID: 1,
  displayName: 'Overview',
  alignment: TabAlignment.top,
  position: 0,
};

beforeEach(() => {
  localStorage.clear();
});

describe('tabsReducer – setTabList', () => {
  it('U17.1 replaces tabList with the provided array', () => {
    const state = reducer(emptyState, setTabList([sampleTab]));
    expect(state.tabList).toHaveLength(1);
    expect(state.tabList[0].displayName).toBe('Overview');
  });

  it('U17.2 replaces a non-empty list with a new one', () => {
    const withOne = reducer(emptyState, setTabList([sampleTab]));
    const replaced = reducer(withOne, setTabList([]));
    expect(replaced.tabList).toHaveLength(0);
  });

  it('U17.3 persists the new state to localStorage', () => {
    const spy = vi.spyOn(Storage.prototype, 'setItem');
    reducer(emptyState, setTabList([sampleTab]));
    expect(spy).toHaveBeenCalled();
    spy.mockRestore();
  });
});

describe('tabsReducer – clearTabsStorage', () => {
  it('U17.4 calls localStorage.removeItem', () => {
    const spy = vi.spyOn(Storage.prototype, 'removeItem');
    reducer(emptyState, clearTabsStorage());
    expect(spy).toHaveBeenCalled();
    spy.mockRestore();
  });
});
