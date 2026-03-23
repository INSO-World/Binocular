import { describe, it, expect, beforeEach, vi } from 'vitest';
import reducer, {
  setSprints,
  addSprint,
  deleteSprint,
  sprintToEdit,
  saveSprint,
  clearSprintStorage,
} from '../../../../../redux/reducer/data/sprintsReducer';
import type { SprintsInitialState } from '../../../../../redux/reducer/data/sprintsReducer';
import type { SprintType } from '../../../../../types/data/sprintType';

const emptyState: SprintsInitialState = {
  sprintList: [],
  currID: 0,
  sprintToEdit: null,
};

function makeSprint(name: string, id?: number): SprintType {
  return { id, name, startDate: '2024-01-01', endDate: '2024-01-14' };
}

beforeEach(() => {
  localStorage.clear();
  // Reset dialog mock
  vi.restoreAllMocks();
});

function mockSprintDialog() {
  const showModal = vi.fn();
  vi.spyOn(document, 'getElementById').mockReturnValue({ showModal } as unknown as HTMLElement);
  return showModal;
}

describe('sprintsReducer – setSprints', () => {
  it('U25.1 replaces sprint list', () => {
    const sprints = [makeSprint('Sprint 1', 0), makeSprint('Sprint 2', 1)];
    const state = reducer(emptyState, setSprints(sprints));
    expect(state.sprintList).toHaveLength(2);
    expect(state.sprintList[0].name).toBe('Sprint 1');
  });

  it('U25.2 persists to localStorage', () => {
    const spy = vi.spyOn(Storage.prototype, 'setItem');
    reducer(emptyState, setSprints([makeSprint('Sprint 1', 0)]));
    expect(spy).toHaveBeenCalled();
  });
});

describe('sprintsReducer – addSprint', () => {
  it('U25.3 assigns currID as the sprint id', () => {
    const stateWith2: SprintsInitialState = { ...emptyState, currID: 5 };
    const state = reducer(stateWith2, addSprint(makeSprint('New Sprint')));
    expect(state.sprintList[0].id).toBe(5);
  });

  it('U25.4 increments currID', () => {
    const state = reducer(emptyState, addSprint(makeSprint('Sprint A')));
    expect(state.currID).toBe(1);
  });

  it('U25.5 persists to localStorage', () => {
    const spy = vi.spyOn(Storage.prototype, 'setItem');
    reducer(emptyState, addSprint(makeSprint('Sprint A')));
    expect(spy).toHaveBeenCalled();
  });
});

describe('sprintsReducer – deleteSprint', () => {
  const stateWithSprints: SprintsInitialState = {
    sprintList: [makeSprint('S1', 0), makeSprint('S2', 1)],
    currID: 2,
    sprintToEdit: null,
  };

  it('U25.6 removes matching sprint', () => {
    const state = reducer(stateWithSprints, deleteSprint(makeSprint('S1', 0)));
    expect(state.sprintList.find((s) => s.id === 0)).toBeUndefined();
  });

  it('U25.7 leaves other sprints intact', () => {
    const state = reducer(stateWithSprints, deleteSprint(makeSprint('S1', 0)));
    expect(state.sprintList.find((s) => s.id === 1)).toBeDefined();
  });
});

describe('sprintsReducer – sprintToEdit', () => {
  it('U25.8 sets state.sprintToEdit and calls showModal()', () => {
    const showModal = mockSprintDialog();
    const sprint = makeSprint('Edit Me', 3);
    const state = reducer(emptyState, sprintToEdit(sprint));
    expect(state.sprintToEdit).toEqual(sprint);
    expect(showModal).toHaveBeenCalledOnce();
  });
});

describe('sprintsReducer – saveSprint', () => {
  const stateWithSprint: SprintsInitialState = {
    sprintList: [{ id: 0, name: 'Old Name', startDate: '2024-01-01', endDate: '2024-01-14' }],
    currID: 1,
    sprintToEdit: { id: 0, name: 'Old Name', startDate: '2024-01-01', endDate: '2024-01-14' },
  };

  it('U25.9 updates the sprint in the list', () => {
    const updated = { id: 0, name: 'New Name', startDate: '2024-02-01', endDate: '2024-02-14' };
    const state = reducer(stateWithSprint, saveSprint(updated));
    expect(state.sprintList[0].name).toBe('New Name');
  });

  it('U25.10 clears state.sprintToEdit', () => {
    const updated = { id: 0, name: 'New Name', startDate: '2024-02-01', endDate: '2024-02-14' };
    const state = reducer(stateWithSprint, saveSprint(updated));
    expect(state.sprintToEdit).toBeNull();
  });
});

describe('sprintsReducer – clearSprintStorage', () => {
  it('U25.11 calls localStorage.removeItem', () => {
    const spy = vi.spyOn(Storage.prototype, 'removeItem');
    reducer(emptyState, clearSprintStorage());
    expect(spy).toHaveBeenCalled();
  });
});
