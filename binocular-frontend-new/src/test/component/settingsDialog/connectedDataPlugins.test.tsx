import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';

// Mock DataPluginStorage before importing the component
vi.mock('../../../utils/dataPluginStorage.ts', () => ({
  default: {
    getDataPlugin: vi.fn(() => new Promise(() => {})), // never-resolving promise
  },
}));

// Mock the SCSS module
vi.mock('../../../components/settingsDialog/connectedDataPlugins/connectedDataPlugins.module.scss', () => ({
  default: { settingsButton: 'settingsButton' },
}));

import ConnectedDataPlugins from '../../../components/settingsDialog/connectedDataPlugins/connectedDataPlugins.tsx';
import SettingsReducer from '../../../redux/reducer/settings/settingsReducer.ts';
import FilesReducer from '../../../redux/reducer/data/filesReducer.ts';
import type { DatabaseSettingsDataPluginType } from '../../../types/settings/databaseSettingsType.ts';

const basePlugin: DatabaseSettingsDataPluginType = {
  id: 1,
  name: 'TestPlugin',
  color: '#aabbcc22',
  isDefault: false,
  parameters: {},
};

function createTestStore(plugins: DatabaseSettingsDataPluginType[] = []) {
  return configureStore({
    reducer: {
      settings: SettingsReducer,
      files: FilesReducer,
    },
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
    } as never,
  });
}

function renderWithStore(store: ReturnType<typeof createTestStore>, interactable = true) {
  return render(
    <Provider store={store}>
      <ConnectedDataPlugins interactable={interactable} />
    </Provider>,
  );
}

describe('ConnectedDataPlugins', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('C32.1 empty plugin list shows empty-state text', () => {
    const store = createTestStore([]);
    renderWithStore(store);
    expect(screen.getByText(/no database connections configured/i)).toBeInTheDocument();
  });

  it('C32.2 one plugin — plugin name visible in DOM', () => {
    const store = createTestStore([basePlugin]);
    renderWithStore(store);
    expect(screen.getByText(/TestPlugin/i)).toBeInTheDocument();
  });

  it('C32.3 plugin with isDefault: true shows "Default" badge', () => {
    const plugin: DatabaseSettingsDataPluginType = { ...basePlugin, isDefault: true };
    const store = createTestStore([plugin]);
    renderWithStore(store);
    expect(screen.getByText('Default')).toBeInTheDocument();
  });

  it('C32.4 plugin with id === 0 shows "pre-loaded" label/badge', () => {
    const plugin: DatabaseSettingsDataPluginType = { ...basePlugin, id: 0 };
    const store = createTestStore([plugin]);
    renderWithStore(store);
    expect(screen.getByText('pre-loaded')).toBeInTheDocument();
  });

  it('C32.5 interactable={true} shows "Set Default" button', () => {
    const store = createTestStore([basePlugin]);
    renderWithStore(store, true);
    expect(screen.getByRole('button', { name: /set default/i })).toBeInTheDocument();
  });

  it('C32.6 interactable={false} hides "Set Default" button', () => {
    const store = createTestStore([basePlugin]);
    renderWithStore(store, false);
    expect(screen.queryByRole('button', { name: /set default/i })).not.toBeInTheDocument();
  });

  it('C32.7 plugin with id === 0 and interactable={true} has no "Delete" button', () => {
    const plugin: DatabaseSettingsDataPluginType = { ...basePlugin, id: 0 };
    const store = createTestStore([plugin]);
    renderWithStore(store, true);
    expect(screen.queryByRole('button', { name: /delete/i })).not.toBeInTheDocument();
  });

  it('C32.8 clicking "Set Default" dispatches setDataPluginAsDefault and updates store', () => {
    const plugin: DatabaseSettingsDataPluginType = { ...basePlugin, id: 5, isDefault: false };
    const store = createTestStore([plugin]);
    renderWithStore(store, true);

    const setDefaultBtn = screen.getByRole('button', { name: /set default/i });
    fireEvent.click(setDefaultBtn);

    expect(store.getState().settings.database.defaultDataPluginItemId).toBe(5);
  });

  it('C32.9 clicking "Delete" on a non-file plugin dispatches removeDataPlugin — plugin gone from store', () => {
    // A plugin without parameters.fileName uses the non-async dispatch path
    const plugin: DatabaseSettingsDataPluginType = { ...basePlugin, id: 2, parameters: {} };
    const store = createTestStore([plugin]);
    renderWithStore(store, true);

    const deleteBtn = screen.getByRole('button', { name: /delete/i });
    fireEvent.click(deleteBtn);

    const remaining = store.getState().settings.database.dataPlugins;
    expect(remaining.find((p: DatabaseSettingsDataPluginType) => p.id === 2)).toBeUndefined();
  });
});
