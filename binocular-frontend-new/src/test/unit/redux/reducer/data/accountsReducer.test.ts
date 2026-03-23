import { describe, it, expect, beforeEach, vi } from 'vitest';
import reducer, { setAccountList, setAccountsDataPluginId, clearAccountsStorage } from '../../../../../redux/reducer/data/accountsReducer';
import type { AccountsInitialState } from '../../../../../redux/reducer/data/accountsReducer';
import type { AccountType } from '../../../../../types/data/accountType';

const emptyState: AccountsInitialState = {
  accountLists: {},
  dataPluginId: undefined,
};

function makeAccount(id: string, name: string): AccountType {
  return { localId: 0, id, name, user: null, platform: 'github' };
}

beforeEach(() => {
  localStorage.clear();
});

describe('accountsReducer – setAccountList', () => {
  it('U24.1 adds new accounts to the list for the given dataPluginId', () => {
    const accounts = [makeAccount('a1', 'Alice'), makeAccount('a2', 'Bob')];
    const state = reducer(emptyState, setAccountList({ dataPluginId: 1, accounts }));
    expect(state.accountLists[1]).toHaveLength(2);
  });

  it('U24.2 auto-assigns incrementing localId to new accounts', () => {
    const accounts = [makeAccount('a1', 'Alice'), makeAccount('a2', 'Bob')];
    const state = reducer(emptyState, setAccountList({ dataPluginId: 1, accounts }));
    const list = state.accountLists[1] as AccountType[];
    expect(list[0].localId).toBe(1);
    expect(list[1].localId).toBe(2);
  });

  it('U24.3 removes accounts no longer in payload', () => {
    const initial: AccountsInitialState = {
      accountLists: {
        1: [makeAccount('a1', 'Alice'), makeAccount('a2', 'Bob')] as unknown as AccountsInitialState['accountLists'][number],
      },
      dataPluginId: 1,
    };
    // Only keep Alice
    const state = reducer(initial, setAccountList({ dataPluginId: 1, accounts: [makeAccount('a1', 'Alice')] }));
    expect((state.accountLists[1] as AccountType[]).find((a) => a.id === 'a2')).toBeUndefined();
  });

  it('U24.4 persists to localStorage', () => {
    const spy = vi.spyOn(Storage.prototype, 'setItem');
    reducer(emptyState, setAccountList({ dataPluginId: 1, accounts: [makeAccount('a1', 'Alice')] }));
    expect(spy).toHaveBeenCalled();
    spy.mockRestore();
  });
});

describe('accountsReducer – setAccountsDataPluginId', () => {
  it('U24.5 updates dataPluginId', () => {
    const state = reducer(emptyState, setAccountsDataPluginId(42));
    expect(state.dataPluginId).toBe(42);
  });

  it('U24.6 persists to localStorage', () => {
    const spy = vi.spyOn(Storage.prototype, 'setItem');
    reducer(emptyState, setAccountsDataPluginId(42));
    expect(spy).toHaveBeenCalled();
    spy.mockRestore();
  });
});

describe('accountsReducer – clearAccountsStorage', () => {
  it('U24.7 calls localStorage.removeItem', () => {
    const spy = vi.spyOn(Storage.prototype, 'removeItem');
    reducer(emptyState, clearAccountsStorage());
    expect(spy).toHaveBeenCalled();
    spy.mockRestore();
  });
});
