import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';

// ── Mocks (hoisted before all imports) ──────────────────────────────────────

vi.mock('../../../utils/dataPluginStorage.ts', () => ({
  default: {
    getDataPlugin: vi.fn(() => new Promise(() => {})),
    addDataPlugin: vi.fn(() => Promise.resolve()),
  },
}));

vi.mock('../../../redux/middleware/socket/socketMiddleware.ts', () => ({
  default: vi.fn(() => () => (next: (action: unknown) => unknown) => (action: unknown) => next(action)),
}));

vi.mock('../../../redux/middleware/refresh/refreshMiddleware.ts', () => ({
  default: vi.fn(() => () => (next: (action: unknown) => unknown) => (action: unknown) => next(action)),
}));

// Stub ConnectedDataPlugins to avoid its DataPluginStorage dependency
vi.mock('../../../components/settingsDialog/connectedDataPlugins/connectedDataPlugins.tsx', () => ({
  default: () => <div data-testid="connected-data-plugins" />,
}));

// Stub AddDataPluginCard to avoid its complexity
vi.mock('../../../components/settingsDialog/addDataPluginCard/addDataPluginCard.tsx', () => ({
  default: ({ dataPlugin }: { dataPlugin: { name: string } }) => <div data-testid="add-data-plugin-card">{dataPlugin.name}</div>,
}));

// ── Actual imports (after mocks) ─────────────────────────────────────────────

import { combineReducers } from '@reduxjs/toolkit';
import DatabaseSettings from '../../../components/settingsDialog/databaseSettings/databaseSettings.tsx';
import SettingsReducer from '../../../redux/reducer/settings/settingsReducer.ts';
import FilesReducer from '../../../redux/reducer/data/filesReducer.ts';
import DataPluginStorage from '../../../utils/dataPluginStorage.ts';
import type { DatabaseSettingsDataPluginType } from '../../../types/settings/databaseSettingsType.ts';

// ── Store factory ────────────────────────────────────────────────────────────

const testReducer = combineReducers({ settings: SettingsReducer, files: FilesReducer });

function createTestStore(plugins: DatabaseSettingsDataPluginType[] = []) {
  return configureStore({
    reducer: testReducer,
    preloadedState: {
      settings: {
        general: { gridSize: 1 },
        initialized: true,
        database: {
          currID: plugins.length,
          dataPlugins: plugins,
          defaultDataPluginItemId: plugins.find((p) => p.isDefault)?.id,
        },
        localDatabaseLoadingState: 0,
        localDatabaseLoadingMessage: '',
      },
    },
  });
}

function renderWithStore(store: ReturnType<typeof createTestStore>) {
  return render(
    <Provider store={store}>
      <DatabaseSettings />
    </Provider>,
  );
}

// ── Tests ────────────────────────────────────────────────────────────────────

describe('DatabaseSettings', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('C41.1 "Add Database Connection:" heading is rendered', () => {
    const store = createTestStore([]);
    renderWithStore(store);
    expect(screen.getByText('Add Database Connection:')).toBeInTheDocument();
  });

  it('C41.2 when store contains one plugin, DataPluginStorage.addDataPlugin is called once on mount with that plugin', () => {
    const plugin: DatabaseSettingsDataPluginType = {
      id: 1,
      name: 'MyPlugin',
      color: '#aabbcc',
      isDefault: true,
      parameters: {},
    };
    const store = createTestStore([plugin]);
    renderWithStore(store);

    expect(DataPluginStorage.addDataPlugin).toHaveBeenCalledOnce();
    expect(DataPluginStorage.addDataPlugin).toHaveBeenCalledWith(plugin);
  });
});
