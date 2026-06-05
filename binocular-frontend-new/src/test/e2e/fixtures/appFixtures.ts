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

async function mockBackendRoutes(page: Page) {
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
});

export { expect } from '@playwright/test';
