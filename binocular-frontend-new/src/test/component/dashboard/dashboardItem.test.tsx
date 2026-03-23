import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';

// ── Mocks (hoisted before all imports) ──────────────────────────────────────

vi.mock('../../../components/tabs/fileTree/fileList/fileListUtilities/fileTreeUtilities.tsx', () => ({
  loadFileList: vi.fn(),
  writeFileListToStorage: vi.fn(),
  generateFileTree: vi.fn(() => []),
  filterFileTree: vi.fn(),
  formatName: vi.fn(),
  clearStorage: vi.fn(),
  refreshFileList: vi.fn(),
}));

vi.mock('../../../utils/dataPluginStorage.ts', () => ({
  default: { getDataPlugin: vi.fn(() => new Promise(() => {})) },
}));

vi.mock('../../../plugins/pluginRegistry.ts', () => ({
  visualizationPlugins: [
    {
      name: 'TestPlugin',
      reducer: (s: Record<string, unknown> = {}) => s,
      saga: vi.fn(),
      dataConnectionName: 'test',
      chartComponent: undefined,
      settingsComponent: () => null,
      helpComponent: () => null,
      defaultSettings: {},
      capabilities: { export: false, popoutOnly: false },
      export: { getSVGData: vi.fn(() => '') },
    },
  ],
}));

// ── Actual imports (after mocks) ─────────────────────────────────────────────

import DashboardItem from '../../../components/dashboard/dashboardItem/dashboardItem';
import { DragResizeMode } from '../../../components/dashboard/resizeMode';
import DashboardReducer from '../../../redux/reducer/general/dashboardReducer';
import AuthorsReducer from '../../../redux/reducer/data/authorsReducer';
import FilesReducer from '../../../redux/reducer/data/filesReducer';
import SprintsReducer from '../../../redux/reducer/data/sprintsReducer';
import SettingsReducer from '../../../redux/reducer/settings/settingsReducer';
import ParametersReducer from '../../../redux/reducer/parameters/parametersReducer';
import ExportReducer from '../../../redux/reducer/export/exportReducer';
import ActionsReducer from '../../../redux/reducer/general/actionsReducer';
import type { DashboardItemType } from '../../../types/general/dashboardItemType';

// ── Store factory ────────────────────────────────────────────────────────────

function createTestStore() {
  return configureStore({
    reducer: {
      dashboard: DashboardReducer,
      authors: AuthorsReducer,
      files: FilesReducer,
      sprints: SprintsReducer,
      settings: SettingsReducer,
      parameters: ParametersReducer,
      export: ExportReducer,
      actions: ActionsReducer,
    },
  });
}

// ── Helpers ──────────────────────────────────────────────────────────────────

const defaultItem: DashboardItemType = {
  id: 1,
  x: 0,
  y: 0,
  width: 4,
  height: 4,
  pluginName: 'TestPlugin',
  dataPluginId: undefined,
};

const defaultProps = {
  item: defaultItem,
  cellSize: 50,
  colCount: 10,
  rowCount: 10,
  setDragResizeItem: vi.fn(),
  deleteItem: vi.fn(),
};

function renderItem(props = defaultProps) {
  const store = createTestStore();
  return {
    store,
    ...render(
      <Provider store={store}>
        <DashboardItem {...props} />
      </Provider>,
    ),
  };
}

beforeEach(() => {
  localStorage.clear();
  vi.clearAllMocks();
});

// ── Tests ────────────────────────────────────────────────────────────────────

describe('DashboardItem', () => {
  it('C12.1 renders nothing when item has no x or y', () => {
    const { container } = renderItem({
      ...defaultProps,
      item: { ...defaultItem, x: undefined, y: undefined },
    });
    expect(container.querySelector('#dashboardItem1')).toBeNull();
  });

  it('C12.2 renders the plugin name in the interaction bar', () => {
    renderItem();
    expect(screen.getByText('TestPlugin')).toBeInTheDocument();
  });

  it('C12.3 shows "No Data Plugin Selected" before data plugin loads', () => {
    renderItem();
    expect(screen.getByText('No Data Plugin Selected')).toBeInTheDocument();
  });

  it('C12.4 mouseDown on the interaction bar calls setDragResizeItem with drag mode', () => {
    const setDragResizeItem = vi.fn();
    renderItem({ ...defaultProps, setDragResizeItem });
    // The plugin name span is inside the interaction bar — mouseDown bubbles up to it
    const nameSpan = screen.getByText('TestPlugin');
    fireEvent.mouseDown(nameSpan);
    expect(setDragResizeItem).toHaveBeenCalledWith(1, DragResizeMode.drag);
  });

  it('C12.5 mouseDown on the top resize bar calls setDragResizeItem with resizeTop mode', () => {
    const setDragResizeItem = vi.fn();
    renderItem({ ...defaultProps, setDragResizeItem });
    // DOM child order inside #dashboardItem1:
    //   [0] content, [1] interaction bar,
    //   [2] resizeTopLeft, [3] resizeTop, [4] resizeTopRight, ...
    const item = document.getElementById('dashboardItem1') as HTMLElement;
    const resizeTopBar = item.children[3] as HTMLElement;
    fireEvent.mouseDown(resizeTopBar);
    expect(setDragResizeItem).toHaveBeenCalledWith(1, DragResizeMode.resizeTop);
  });

  it('C12.6 help panel is hidden by default and shown after clicking the help button', () => {
    renderItem();
    const helpPanel = document.getElementById('dashboardItem1_help') as HTMLElement;
    expect(helpPanel.style.display).toBe('none');

    // buttons rendered (capabilities.export=false, delete is display:none so excluded):
    // [0] popout, [1] help, [2] settings
    const buttons = screen.getAllByRole('button');
    fireEvent.click(buttons[1]);
    expect(helpPanel.style.display).toBe('block');
  });
});
