// ── Mocks (hoisted before all imports) ──────────────────────────────────────

vi.mock('../../../utils/dataPluginStorage.ts', () => ({
  default: { getDataPlugin: vi.fn(() => new Promise(() => {})) },
}));

vi.mock('../../../redux/middleware/socket/socketMiddleware.ts', () => ({
  default: () => () => (next: (action: unknown) => unknown) => (action: unknown) => next(action),
}));

vi.mock('../../../redux/middleware/refresh/refreshMiddleware.ts', () => ({
  default: () => () => (next: (action: unknown) => unknown) => (action: unknown) => next(action),
}));

// ── Actual imports (after mocks) ─────────────────────────────────────────────

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';

import DashboardItemSettings from '../../../components/dashboard/dashboardItemSettings/dashboardItemSettings.tsx';
import SettingsReducer from '../../../redux/reducer/settings/settingsReducer.ts';
import type { DashboardItemType } from '../../../types/general/dashboardItemType.ts';
import type { DatabaseSettingsDataPluginType } from '../../../types/settings/databaseSettingsType.ts';

// ── Store factory ────────────────────────────────────────────────────────────

function createTestStore() {
  return configureStore({
    reducer: {
      settings: SettingsReducer,
    },
    preloadedState: {
      settings: {
        general: { gridSize: 1 },
        initialized: false,
        database: {
          currID: 0,
          dataPlugins: [],
          defaultDataPluginItemId: undefined,
        },
        localDatabaseLoadingState: 0,
        localDatabaseLoadingMessage: '',
      },
    },
  });
}

// ── Fixtures ─────────────────────────────────────────────────────────────────

const item: DashboardItemType = {
  id: 42,
  pluginName: 'TestViz',
  dataPluginId: 1,
  width: 4,
  height: 4,
  x: 0,
  y: 0,
};

const defaultProps = {
  selectedDataPlugin: undefined as DatabaseSettingsDataPluginType | undefined,
  onSelectDataPlugin: vi.fn(),
  item,
  settingsComponent: <div>custom-settings</div>,
  onClickDelete: vi.fn(),
  onClickRefresh: vi.fn(),
  ignoreGlobalParameters: false,
  setIgnoreGlobalParameters: vi.fn(),
  doAutomaticUpdate: false,
  setDoAutomaticUpdate: vi.fn(),
  parametersGeneral: { granularity: 'weeks', excludeMergeCommits: false },
  setParametersGeneral: vi.fn(),
  parametersDateRange: { from: '2024-01-01T00:00:00.000Z', to: '2024-12-31T00:00:00.000Z' },
  setParametersDateRange: vi.fn(),
};

function renderComponent(props = defaultProps) {
  const store = createTestStore();
  return {
    store,
    ...render(
      <Provider store={store}>
        <DashboardItemSettings {...props} />
      </Provider>,
    ),
  };
}

// ── Tests ────────────────────────────────────────────────────────────────────

beforeEach(() => {
  localStorage.clear();
  vi.clearAllMocks();
});

describe('DashboardItemSettings', () => {
  it('C35.1 heading contains "TestViz (#42)"', () => {
    renderComponent();
    expect(screen.getByRole('heading', { level: 2 })).toHaveTextContent('TestViz (#42)');
  });

  it('C35.2 clicking "Refresh" button calls onClickRefresh', () => {
    const onClickRefresh = vi.fn();
    renderComponent({ ...defaultProps, onClickRefresh });
    fireEvent.click(screen.getByRole('button', { name: /refresh/i }));
    expect(onClickRefresh).toHaveBeenCalledTimes(1);
  });

  it('C35.3 clicking "Delete" button calls onClickDelete', () => {
    const onClickDelete = vi.fn();
    renderComponent({ ...defaultProps, onClickDelete });
    fireEvent.click(screen.getByRole('button', { name: /delete/i }));
    expect(onClickDelete).toHaveBeenCalledTimes(1);
  });

  it('C35.4 toggling the "Ignore Global Parameters" checkbox calls setIgnoreGlobalParameters with true', () => {
    const setIgnoreGlobalParameters = vi.fn();
    renderComponent({ ...defaultProps, setIgnoreGlobalParameters });
    // Find the checkbox associated with "Ignore Global Parameters"
    const label = screen.getByText(/ignore global parameters/i).closest('label');
    const checkbox = label!.querySelector('input[type="checkbox"]') as HTMLInputElement;
    fireEvent.click(checkbox);
    expect(setIgnoreGlobalParameters).toHaveBeenCalledWith(true);
  });

  it('C35.5 "Automatic Update" toggle is shown when selectedDataPlugin.parameters.progressUpdate.useAutomaticUpdate === true; absent when selectedDataPlugin is undefined', () => {
    // When selectedDataPlugin is undefined — no Automatic Update toggle
    renderComponent({ ...defaultProps, selectedDataPlugin: undefined });
    expect(screen.queryByText(/automatic update/i)).toBeNull();

    // When selectedDataPlugin has useAutomaticUpdate=true — toggle is shown
    const dataPluginWithAutoUpdate = {
      id: 1,
      name: 'MockPlugin',
      color: '#ff000022',
      isDefault: true,
      parameters: {
        progressUpdate: {
          useAutomaticUpdate: true,
        },
      },
    } as unknown as DatabaseSettingsDataPluginType;

    renderComponent({ ...defaultProps, selectedDataPlugin: dataPluginWithAutoUpdate });
    expect(screen.getByText(/automatic update/i)).toBeInTheDocument();
  });
});
