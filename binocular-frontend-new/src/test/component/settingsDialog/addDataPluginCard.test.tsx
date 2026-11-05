import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';

import AddDataPluginCard from '../../../components/settingsDialog/addDataPluginCard/addDataPluginCard.tsx';
import SettingsReducer from '../../../redux/reducer/settings/settingsReducer.ts';
import type { DataPlugin } from '../../../plugins/interfaces/dataPlugin.ts';

function createTestStore() {
  return configureStore({
    reducer: {
      settings: SettingsReducer,
    },
  });
}

function makeDataPlugin(overrides: Partial<DataPlugin> = {}): DataPlugin {
  return {
    name: 'TestPlugin',
    description: 'A test plugin',
    capabilities: ['commits', 'issues'],
    experimental: false,
    requirements: { apiKey: false, endpoint: false, file: false, progressUpdate: false },
    general: { getProgressUpdateConfig: vi.fn(() => ({ useAutomaticUpdate: false })) } as never,
    commits: {} as never,
    builds: {} as never,
    commitByFile: {} as never,
    issues: {} as never,
    mergeRequests: {} as never,
    notes: {} as never,
    users: {} as never,
    accounts: {} as never,
    files: {} as never,
    accountsIssues: {} as never,
    init: vi.fn(() => Promise.resolve(undefined)),
    clearRemains: vi.fn(() => Promise.resolve()),
    ...overrides,
  };
}

function renderWithStore(store: ReturnType<typeof createTestStore>, plugin: DataPlugin) {
  return render(
    <Provider store={store}>
      <AddDataPluginCard dataPlugin={plugin} />
    </Provider>,
  );
}

describe('AddDataPluginCard', () => {
  let store: ReturnType<typeof createTestStore>;

  beforeEach(() => {
    localStorage.clear();
    store = createTestStore();
  });

  it('C27.1 renders without crashing and shows a name input field when file requirement is set', () => {
    const plugin = makeDataPlugin({ requirements: { apiKey: false, endpoint: false, file: true, progressUpdate: false } });
    renderWithStore(store, plugin);
    const nameInput = screen.getByPlaceholderText('Name');
    expect(nameInput).toBeInTheDocument();
  });

  it('C27.2 renders the plugin name as a card title', () => {
    const plugin = makeDataPlugin();
    renderWithStore(store, plugin);
    expect(screen.getByText('TestPlugin')).toBeInTheDocument();
  });

  it('C27.3 Add button is present in the rendered output', () => {
    const plugin = makeDataPlugin();
    renderWithStore(store, plugin);
    const addButton = screen.getByRole('button', { name: /add/i });
    expect(addButton).toBeInTheDocument();
  });

  it('C27.4 clicking Add dispatches addDataPlugin and increases dataPlugins length', () => {
    const plugin = makeDataPlugin();
    renderWithStore(store, plugin);

    const initialCount = store.getState().settings.database.dataPlugins.length;
    const addButton = screen.getByRole('button', { name: /add/i });
    fireEvent.click(addButton);

    expect(store.getState().settings.database.dataPlugins.length).toBe(initialCount + 1);
  });

  it('C27.5 file input is shown when file requirement is set', () => {
    const plugin = makeDataPlugin({ requirements: { apiKey: false, endpoint: false, file: true, progressUpdate: false } });
    renderWithStore(store, plugin);
    const fileInput = document.getElementById('importStorageFilePicker');
    expect(fileInput).toBeInTheDocument();
  });
});
