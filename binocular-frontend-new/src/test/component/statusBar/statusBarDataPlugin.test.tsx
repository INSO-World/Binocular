import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';

// ── Mocks (hoisted before all imports) ──────────────────────────────────────

vi.mock('../../../utils/dataPluginStorage.ts', () => ({
  default: { getDataPlugin: vi.fn(() => new Promise(() => {})) },
}));

vi.mock('../../../redux/middleware/socket/socketMiddleware.ts', () => ({
  default: vi.fn(() => () => (next: (action: unknown) => unknown) => (action: unknown) => next(action)),
}));

vi.mock('../../../redux/middleware/refresh/refreshMiddleware.ts', () => ({
  default: vi.fn(() => () => (next: (action: unknown) => unknown) => (action: unknown) => next(action)),
}));

// ── Actual imports (after mocks) ─────────────────────────────────────────────

import StatusBar from '../../../components/statusBar/statusBar.tsx';
import SettingsReducer from '../../../redux/reducer/settings/settingsReducer.ts';
import type { DatabaseSettingsDataPluginType } from '../../../types/settings/databaseSettingsType.ts';

// ── Store factory ────────────────────────────────────────────────────────────

function createTestStore(plugins: DatabaseSettingsDataPluginType[] = []) {
  return configureStore({
    reducer: {
      settings: SettingsReducer,
    },
    preloadedState: {
      settings: {
        general: { gridSize: 1 },
        initialized: false,
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
      <StatusBar />
    </Provider>,
  );
}

describe('StatusBarDataPlugin', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('C28.1 with one data plugin in the store, a loading element with the plugin color is rendered', () => {
    const plugins: DatabaseSettingsDataPluginType[] = [{ id: 1, name: 'MyPlugin', color: '#ff000022', isDefault: true, parameters: {} }];
    const store = createTestStore(plugins);
    renderWithStore(store);
    // getDataPlugin never resolves in tests, so the component shows the loading div with the plugin color
    const loadingDivs = screen.getAllByText('Loading Data Plugin');
    expect(loadingDivs).toHaveLength(1);
    expect(loadingDivs[0].style.background).toBe('rgba(255, 0, 0, 0.133)');
  });

  it('C28.2 with no data plugins in the store, renders without crashing and shows placeholder', () => {
    const store = createTestStore([]);
    renderWithStore(store);
    expect(screen.getByText(/No DataPlugins Configured/i)).toBeInTheDocument();
  });

  it('C28.3 when two plugins exist, two loading elements are rendered each with a distinct color', () => {
    const plugins: DatabaseSettingsDataPluginType[] = [
      { id: 1, name: 'Alpha', color: '#ff000022', isDefault: true, parameters: {} },
      { id: 2, name: 'Beta', color: '#00ff0022', isDefault: false, parameters: {} },
    ];
    const store = createTestStore(plugins);
    renderWithStore(store);
    const loadingDivs = screen.getAllByText('Loading Data Plugin');
    expect(loadingDivs).toHaveLength(2);
    const backgrounds = loadingDivs.map((el) => el.style.background);
    // Each plugin gets its own color — they must differ
    expect(backgrounds[0]).not.toBe(backgrounds[1]);
  });
});
