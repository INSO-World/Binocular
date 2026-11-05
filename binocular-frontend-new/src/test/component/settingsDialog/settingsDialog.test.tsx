import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';

// Mock complex sub-components
vi.mock('../../../components/settingsDialog/generalSettings/generalSettings.tsx', () => ({
  default: () => <div data-testid="general-settings-content">General Settings Content</div>,
}));
vi.mock('../../../components/settingsDialog/databaseSettings/databaseSettings.tsx', () => ({
  default: () => <div data-testid="database-settings-content">Database Settings Content</div>,
}));

import SettingsDialog from '../../../components/settingsDialog/settingsDialog.tsx';
import DashboardReducer from '../../../redux/reducer/general/dashboardReducer.ts';
import SettingsReducer from '../../../redux/reducer/settings/settingsReducer.ts';
import NotificationsReducer from '../../../redux/reducer/general/notificationsReducer.ts';
import AuthorsReducer from '../../../redux/reducer/data/authorsReducer.ts';
import AccountsReducer from '../../../redux/reducer/data/accountsReducer.ts';
import FilesReducer from '../../../redux/reducer/data/filesReducer.ts';
import ParametersReducer from '../../../redux/reducer/parameters/parametersReducer.ts';
import SprintsReducer from '../../../redux/reducer/data/sprintsReducer.ts';
import ExportReducer from '../../../redux/reducer/export/exportReducer.ts';
import TabsReducer from '../../../redux/reducer/general/tabsReducer.ts';
import ActionsReducer from '../../../redux/reducer/general/actionsReducer.ts';
import LayoutReducer from '../../../redux/reducer/general/layoutReducer.ts';
import actionsMiddleware from '../../../redux/middleware/actions/actionsMiddleware.ts';
import type { Middleware } from 'redux';

const reducerMap = {
  dashboard: DashboardReducer,
  layout: LayoutReducer,
  authors: AuthorsReducer,
  accounts: AccountsReducer,
  files: FilesReducer,
  settings: SettingsReducer,
  export: ExportReducer,
  parameters: ParametersReducer,
  sprints: SprintsReducer,
  notifications: NotificationsReducer,
  tabs: TabsReducer,
  actions: ActionsReducer,
};

function createTestStore() {
  return configureStore({
    reducer: reducerMap,
    middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(actionsMiddleware() as Middleware),
  });
}

function renderWithStore(store: ReturnType<typeof createTestStore>) {
  return render(
    <Provider store={store}>
      <SettingsDialog />
    </Provider>,
  );
}

describe('SettingsDialog', () => {
  let store: ReturnType<typeof createTestStore>;

  beforeEach(() => {
    store = createTestStore();
  });

  it('C3.1 General tab content is visible by default', () => {
    renderWithStore(store);
    expect(screen.getByTestId('general-settings-content')).toBeInTheDocument();
  });

  it('C3.2 Database tab content is hidden initially', () => {
    renderWithStore(store);
    expect(screen.queryByTestId('database-settings-content')).not.toBeInTheDocument();
  });

  it('C3.3 clicking Database tab shows Database content', () => {
    renderWithStore(store);
    const databaseTab = screen.getByRole('tab', { name: /database/i, hidden: true });
    fireEvent.click(databaseTab);
    expect(screen.getByTestId('database-settings-content')).toBeInTheDocument();
  });

  it('C3.4 clicking back to General tab hides Database content', () => {
    renderWithStore(store);
    // Switch to Database
    const databaseTab = screen.getByRole('tab', { name: /database/i, hidden: true });
    fireEvent.click(databaseTab);
    expect(screen.getByTestId('database-settings-content')).toBeInTheDocument();
    // Switch back to General
    const generalTab = screen.getByRole('tab', { name: /general/i, hidden: true });
    fireEvent.click(generalTab);
    expect(screen.queryByTestId('database-settings-content')).not.toBeInTheDocument();
    expect(screen.getByTestId('general-settings-content')).toBeInTheDocument();
  });

  it('C3.5 active tab has visual indicator (tab-active class on General initially)', () => {
    renderWithStore(store);
    const generalTab = screen.getByRole('tab', { name: /general/i, hidden: true });
    expect(generalTab.className).toContain('tab-active');
  });
});
