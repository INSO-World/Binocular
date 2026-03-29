import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';

// ── Mocks (hoisted before all imports) ──────────────────────────────────────

vi.mock('../../../components/tabs/sprints/addSprintDialog/addSprintDialog.tsx', () => ({
  default: () => <div data-testid="add-sprint-dialog" />,
}));

vi.mock('../../../redux/middleware/socket/socketMiddleware.ts', () => ({
  default: vi.fn(() => () => (next: (action: unknown) => unknown) => (action: unknown) => next(action)),
}));

vi.mock('../../../redux/middleware/refresh/refreshMiddleware.ts', () => ({
  default: vi.fn(() => () => (next: (action: unknown) => unknown) => (action: unknown) => next(action)),
}));

// ── Actual imports (after mocks) ─────────────────────────────────────────────

import AddSprint from '../../../components/tabs/sprints/addSprint/addSprint.tsx';
import SprintsReducer from '../../../redux/reducer/data/sprintsReducer.ts';

// ── Store factory ────────────────────────────────────────────────────────────

function createTestStore() {
  return configureStore({
    reducer: { sprints: SprintsReducer },
  });
}

function renderWithStore(store: ReturnType<typeof createTestStore>) {
  return render(
    <Provider store={store}>
      <AddSprint />
    </Provider>,
  );
}

// ── Tests ────────────────────────────────────────────────────────────────────

describe('AddSprint', () => {
  let dialog: HTMLDialogElement;

  beforeEach(() => {
    localStorage.clear();
    dialog = document.createElement('dialog') as HTMLDialogElement;
    dialog.id = 'addSprintDialog';
    dialog.showModal = vi.fn();
    document.body.appendChild(dialog);
  });

  afterEach(() => {
    document.body.removeChild(dialog);
    localStorage.clear();
  });

  it('C40.1 "Add Sprint" button is present in the DOM', () => {
    const store = createTestStore();
    renderWithStore(store);
    expect(screen.getByRole('button', { name: /add sprint/i })).toBeInTheDocument();
  });

  it('C40.2 clicking "Add Sprint" dispatches sprintToEdit(null) and calls showModal', () => {
    const store = createTestStore();
    renderWithStore(store);

    const button = screen.getByRole('button', { name: /add sprint/i });
    fireEvent.click(button);

    // Verify reducer processed the action — sprintToEdit state should be null
    expect(store.getState().sprints.sprintToEdit).toBeNull();

    // Verify showModal was called on the dialog element
    expect(dialog.showModal).toHaveBeenCalled();
  });
});
