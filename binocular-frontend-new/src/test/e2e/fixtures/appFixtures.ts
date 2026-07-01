/* eslint-disable react-hooks/rules-of-hooks */
import { test as base, type Page } from '@playwright/test';

// All keys use the 'bino_' prefix (introduced when multiple keys were namespaced).
// Pattern: `bino_${sliceName}StateV${Config.localStorageVersion}`
const SETTINGS_KEY = 'bino_settingsStateV1';
const DASHBOARD_KEY = 'bino_dashboardStateV1';
const TABS_KEY = 'bino_tabsStateV1';

// SettingsGeneralGridSize.medium = 1 (numeric enum, second member)
const INITIALIZED_SETTINGS = JSON.stringify({
  general: { gridSize: 1 },
  initialized: true,
  database: { currID: 0, dataPlugins: [] },
  localDatabaseLoadingState: 0,
  localDatabaseLoadingMessage: '',
});

const EMPTY_DASHBOARD = JSON.stringify({
  dashboardItems: [],
  dashboardItemCount: 0,
  popupCount: 0,
  dashboardState: Array.from({ length: 40 }, () => new Array(40).fill(0)),
  initialized: true,
});

// One "Repository Stats" item placed at grid (0,0), 12 wide × 8 tall
const DASHBOARD_WITH_REPO_STATS = JSON.stringify({
  dashboardItems: [{ id: 1, pluginName: 'Repository Stats', x: 0, y: 0, width: 12, height: 8, dataPluginId: undefined }],
  dashboardItemCount: 1,
  popupCount: 0,
  dashboardState: Array.from({ length: 40 }, () => new Array(40).fill(0)),
  initialized: true,
});

// tabList: [] is safe — tabController regenerates tabs from its children prop on first render
const EMPTY_TABS = JSON.stringify({ tabList: [] });

// Settings with the in-browser "Mock Data" plugin configured as the default data plugin.
// Mock Data needs no backend: all collections resolve from in-memory fixtures, so charts
// render real series without any network access. 8-digit hex color — DataPluginQuickSelect
// parses an alpha channel from positions 7-9.
const MOCK_DATA_SETTINGS = JSON.stringify({
  general: { gridSize: 1 },
  initialized: true,
  database: {
    currID: 1,
    defaultDataPluginItemId: 1,
    dataPlugins: [{ id: 1, name: 'Mock Data', color: '#66c2a525', isDefault: true, parameters: {} }],
  },
  localDatabaseLoadingState: 0,
  localDatabaseLoadingMessage: '',
});

// One "Changes" item at grid (0,0), 12 wide × 8 tall, wired to the Mock Data plugin (id 1).
// The 40×40 occupancy grid must mark the item's cells — overlap detection reads this grid,
// so leaving it all-zero would let other items be placed on top without a warning.
const CHANGES_GRID = Array.from({ length: 40 }, () => new Array(40).fill(0));
for (let y = 0; y < 8; y++) {
  for (let x = 0; x < 12; x++) {
    CHANGES_GRID[y][x] = 1;
  }
}
const DASHBOARD_WITH_CHANGES = JSON.stringify({
  dashboardItems: [{ id: 1, pluginName: 'Changes', x: 0, y: 0, width: 12, height: 8, dataPluginId: 1 }],
  dashboardItemCount: 1,
  popupCount: 0,
  dashboardState: CHANGES_GRID,
  initialized: true,
});

export async function mockBackendRoutes(page: Page) {
  await page.route('/api/**', (route) => route.fulfill({ status: 200, contentType: 'application/json', body: '{}' }));
  await page.route('/graphQl', (route) =>
    route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ data: {} }) }),
  );
  // Abort WebSocket upgrade — Socket.io logs a connection error but does not throw uncaught exceptions
  await page.route('/wsapi/**', (route) => route.abort());
}

type AppFixtures = {
  initializedApp: Page;
  freshApp: Page;
  appWithVisualization: Page;
  mockDataApp: Page;
};

export const test = base.extend<AppFixtures>({
  // App with settings + dashboard already initialized — setup dialog will NOT open
  initializedApp: async ({ page }, use) => {
    await page.addInitScript(
      ({ settingsKey, dashboardKey, tabsKey, settings, dashboard, tabs }) => {
        localStorage.setItem(settingsKey, settings);
        localStorage.setItem(dashboardKey, dashboard);
        localStorage.setItem(tabsKey, tabs);
      },
      {
        settingsKey: SETTINGS_KEY,
        dashboardKey: DASHBOARD_KEY,
        tabsKey: TABS_KEY,
        settings: INITIALIZED_SETTINGS,
        dashboard: EMPTY_DASHBOARD,
        tabs: EMPTY_TABS,
      },
    );
    await mockBackendRoutes(page);
    await page.goto('/');
    await page.waitForSelector('#tabBarTop', { state: 'visible' });
    await use(page);
  },

  // App with no localStorage — setup dialog WILL auto-open
  freshApp: async ({ page }, use) => {
    await mockBackendRoutes(page);
    await page.goto('/');
    await page.waitForSelector('#setupDialog[open]', { state: 'attached' });
    await use(page);
  },

  // App with one "Repository Stats" item pre-placed on the dashboard
  appWithVisualization: async ({ page }, use) => {
    await page.addInitScript(
      ({ settingsKey, dashboardKey, tabsKey, settings, dashboard, tabs }) => {
        localStorage.setItem(settingsKey, settings);
        localStorage.setItem(dashboardKey, dashboard);
        localStorage.setItem(tabsKey, tabs);
      },
      {
        settingsKey: SETTINGS_KEY,
        dashboardKey: DASHBOARD_KEY,
        tabsKey: TABS_KEY,
        settings: INITIALIZED_SETTINGS,
        dashboard: DASHBOARD_WITH_REPO_STATS,
        tabs: EMPTY_TABS,
      },
    );
    await mockBackendRoutes(page);
    await page.goto('/');
    await page.waitForSelector('#tabBarTop', { state: 'visible' });
    await use(page);
  },

  // App with the Mock Data plugin configured as default and one "Changes" chart on the
  // dashboard. Seeds set-if-absent so state the app persists itself (deletions, parameter
  // changes) survives page.reload() within a test — init scripts re-run on every navigation.
  mockDataApp: async ({ page }, use) => {
    await page.addInitScript(
      ({ settingsKey, dashboardKey, tabsKey, settings, dashboard, tabs }) => {
        if (!localStorage.getItem(settingsKey)) localStorage.setItem(settingsKey, settings);
        if (!localStorage.getItem(dashboardKey)) localStorage.setItem(dashboardKey, dashboard);
        if (!localStorage.getItem(tabsKey)) localStorage.setItem(tabsKey, tabs);
      },
      {
        settingsKey: SETTINGS_KEY,
        dashboardKey: DASHBOARD_KEY,
        tabsKey: TABS_KEY,
        settings: MOCK_DATA_SETTINGS,
        dashboard: DASHBOARD_WITH_CHANGES,
        tabs: EMPTY_TABS,
      },
    );
    await mockBackendRoutes(page);
    await page.goto('/');
    await page.waitForSelector('#tabBarTop', { state: 'visible' });
    await use(page);
  },
});

export { expect } from '@playwright/test';
export type { Page } from '@playwright/test';
