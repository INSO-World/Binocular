import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
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

// Mock the PopoutController so it doesn't try to open a real browser window
vi.mock('../../../components/dashboard/dashboardItemPopout/popoutController/popoutController.tsx', () => ({
  default: (props: { children: React.ReactElement; title: string }) => (
    <div data-testid="popout-controller" data-title={props.title}>
      {props.children}
    </div>
  ),
}));

// ── Actual imports (after mocks) ─────────────────────────────────────────────

import DashboardItemPopout from '../../../components/dashboard/dashboardItemPopout/dashboardItemPopout.tsx';
import DashboardReducer from '../../../redux/reducer/general/dashboardReducer.ts';
import NotificationsReducer from '../../../redux/reducer/general/notificationsReducer.ts';

// ── Store factory ────────────────────────────────────────────────────────────

function createTestStore() {
  return configureStore({
    reducer: {
      dashboard: DashboardReducer,
      notifications: NotificationsReducer,
    },
  });
}

// ── Helpers ──────────────────────────────────────────────────────────────────

function renderPopout(name = 'MyVisualization', children = <div data-testid="chart-container">Chart</div>) {
  const store = createTestStore();
  return {
    store,
    ...render(
      <Provider store={store}>
        <DashboardItemPopout name={name} onClosing={vi.fn()} onResize={vi.fn()}>
          {children}
        </DashboardItemPopout>
      </Provider>,
    ),
  };
}

beforeEach(() => {
  localStorage.clear();
  vi.clearAllMocks();
});

// ── Tests ────────────────────────────────────────────────────────────────────

describe('DashboardItemPopout', () => {
  it('C29.1 renders without crashing given minimal valid props', () => {
    const { container } = renderPopout();
    expect(container).toBeTruthy();
  });

  it('C29.2 renders the plugin name in the popout title attribute', () => {
    renderPopout('MyVisualization');
    const controller = screen.getByTestId('popout-controller');
    expect(controller.getAttribute('data-title')).toContain('MyVisualization');
  });

  it('C29.3 the chart container element is present in the DOM', () => {
    renderPopout('MyVisualization', <div data-testid="chart-container">Chart Content</div>);
    expect(screen.getByTestId('chart-container')).toBeInTheDocument();
  });
});
