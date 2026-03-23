import { describe, it, expect, beforeEach, vi } from 'vitest';
import reducer, {
  setDragging,
  moveAuthorToOther,
  resetAuthor,
  setParentAuthor,
  checkAllAuthors,
  uncheckAllAuthors,
  switchAuthorSelection,
  clearAuthorsStorage,
} from '../../../../../redux/reducer/data/authorsReducer';
import type { AuthorsInitialState } from '../../../../../redux/reducer/data/authorsReducer';
import type { AuthorType } from '../../../../../types/data/authorType';

function makeAuthor(id: number, parent: number, selected = true): AuthorType {
  return {
    id,
    parent,
    selected,
    color: { main: '#ff0000', secondary: '#000000' },
    user: { id: `user-${id}`, gitSignature: `sig-${id}`, account: null },
  };
}

const author1 = makeAuthor(1, -1, true);
const author2 = makeAuthor(2, -1, false);

const emptyState: AuthorsInitialState = {
  authorLists: { 1: [author1, author2] },
  dragging: false,
  authorToEdit: undefined,
  dataPluginId: 1,
};

beforeEach(() => {
  localStorage.clear();
  vi.restoreAllMocks();
});

describe('authorsReducer – setDragging', () => {
  it('U26.1 updates dragging flag', () => {
    const state = reducer(emptyState, setDragging(true));
    expect(state.dragging).toBe(true);
  });
});

describe('authorsReducer – moveAuthorToOther', () => {
  it('U26.2 sets parent = 0 for the target author', () => {
    const state = reducer(emptyState, moveAuthorToOther(1));
    const a = (state.authorLists[1] as AuthorType[]).find((a) => a.id === 1);
    expect(a?.parent).toBe(0);
  });

  it('U26.3 also sets parent = 0 for children of the target', () => {
    const child = makeAuthor(3, 1);
    const stateWithChild: AuthorsInitialState = {
      ...emptyState,
      authorLists: { 1: [author1, author2, child] },
    };
    const state = reducer(stateWithChild, moveAuthorToOther(1));
    const c = (state.authorLists[1] as AuthorType[]).find((a) => a.id === 3);
    expect(c?.parent).toBe(0);
  });
});

describe('authorsReducer – resetAuthor', () => {
  it('U26.4 sets parent = -1 for the target author', () => {
    const stateWithParent: AuthorsInitialState = {
      ...emptyState,
      authorLists: { 1: [makeAuthor(1, 0), author2] },
    };
    const state = reducer(stateWithParent, resetAuthor(1));
    const a = (state.authorLists[1] as AuthorType[]).find((a) => a.id === 1);
    expect(a?.parent).toBe(-1);
  });
});

describe('authorsReducer – setParentAuthor', () => {
  it('U26.5 sets the parent relationship', () => {
    const state = reducer(emptyState, setParentAuthor({ author: 2, parent: 1 }));
    const a = (state.authorLists[1] as AuthorType[]).find((a) => a.id === 2);
    expect(a?.parent).toBe(1);
  });

  it('U26.6 ignores self-assignment (author === parent)', () => {
    const state = reducer(emptyState, setParentAuthor({ author: 1, parent: 1 }));
    const a = (state.authorLists[1] as AuthorType[]).find((a) => a.id === 1);
    expect(a?.parent).toBe(-1); // unchanged
  });
});

describe('authorsReducer – checkAllAuthors', () => {
  it('U26.7 sets all selected = true', () => {
    const state = reducer(emptyState, checkAllAuthors());
    const list = state.authorLists[1] as AuthorType[];
    expect(list.every((a) => a.selected === true)).toBe(true);
  });
});

describe('authorsReducer – uncheckAllAuthors', () => {
  it('U26.8 sets all selected = false', () => {
    const state = reducer(emptyState, uncheckAllAuthors());
    const list = state.authorLists[1] as AuthorType[];
    expect(list.every((a) => a.selected === false)).toBe(true);
  });
});

describe('authorsReducer – switchAuthorSelection', () => {
  it('U26.9 toggles selected on target and its children', () => {
    const child = makeAuthor(3, 1, true);
    const stateWithChild: AuthorsInitialState = {
      ...emptyState,
      authorLists: { 1: [makeAuthor(1, -1, true), author2, child] },
    };
    const state = reducer(stateWithChild, switchAuthorSelection(1));
    const list = state.authorLists[1] as AuthorType[];
    expect(list.find((a) => a.id === 1)?.selected).toBe(false);
    expect(list.find((a) => a.id === 3)?.selected).toBe(false);
    // author2 is unaffected
    expect(list.find((a) => a.id === 2)?.selected).toBe(false);
  });
});

describe('authorsReducer – clearAuthorsStorage', () => {
  it('U26.10 calls localStorage.removeItem', () => {
    const spy = vi.spyOn(Storage.prototype, 'removeItem');
    reducer(emptyState, clearAuthorsStorage());
    expect(spy).toHaveBeenCalled();
  });
});
