// Neutral seeding primitives shared by the screenshots suite and the demo-video suite. Holds NO state values of its own:
// each suite owns its localStorage payloads (screenshots.setup.ts / demo/demoSetup.ts) and passes them in here.

import type { Page } from '@playwright/test';

// ─── localStorage keys ────────────────────────────────────────────────────────

const SETTINGS_KEY = 'bino_settingsStateV1';
const DASHBOARD_KEY = 'bino_dashboardStateV1';
const TABS_KEY = 'bino_tabsStateV1';
const AUTHORS_KEY = 'bino_authorsStateV1';
const PARAMETERS_KEY = 'bino_parametersStateV1';
const SPRINTS_KEY = 'bino_sprintsStateV1';

// Every field is a pre-stringified localStorage payload; `sprints` is omitted unless the caller wants pre-populated sprints.
export interface SeedState {
  settings: string;
  dashboard: string;
  tabs: string;
  authors: string;
  parameters: string;
  sprints?: string | null;
}

// ─── Dashboard builders ───────────────────────────────────────────────────────

export function buildDashboard(pluginName: string, width = 40, height = 22, settings?: object): string {
  const grid = Array.from({ length: 40 }, () => new Array(40).fill(0));
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      grid[y][x] = 1;
    }
  }
  return JSON.stringify({
    dashboardItems: [{ id: 1, pluginName, x: 0, y: 0, width, height, dataPluginId: 1, ...(settings ? { settings } : {}) }],
    dashboardItemCount: 1,
    popupCount: 0,
    dashboardState: grid,
    initialized: true,
  });
}

// An initialized but empty dashboard — for flows that add their visualization through the UI instead of having it pre-placed.
export function buildEmptyDashboard(): string {
  return JSON.stringify({
    dashboardItems: [],
    dashboardItemCount: 0,
    popupCount: 0,
    dashboardState: Array.from({ length: 40 }, () => new Array(40).fill(0)),
    initialized: true,
  });
}

export interface DashItemConfig {
  id: number;
  pluginName: string;
  x: number;
  y: number;
  width: number;
  height: number;
  settings?: object;
}

export function buildMultiDashboard(items: DashItemConfig[]): string {
  const grid = Array.from({ length: 40 }, () => new Array(40).fill(0));
  for (const item of items) {
    for (let r = item.y; r < Math.min(item.y + item.height, 40); r++) {
      for (let c = item.x; c < Math.min(item.x + item.width, 40); c++) {
        grid[r][c] = item.id;
      }
    }
  }
  return JSON.stringify({
    dashboardItems: items.map(({ id, pluginName, x, y, width, height, settings }) => ({
      id,
      pluginName,
      x,
      y,
      width,
      height,
      dataPluginId: 1,
      ...(settings ? { settings } : {}),
    })),
    dashboardItemCount: items.length,
    popupCount: 0,
    dashboardState: grid,
    initialized: true,
  });
}

// ─── Page loaders ─────────────────────────────────────────────────────────────

function seedLocalStorage(page: Page, state: SeedState) {
  return page.addInitScript(
    ({ settingsKey, dashboardKey, tabsKey, authorsKey, parametersKey, sprintsKey, settings, db, tabs, authors, parameters, sprints }) => {
      localStorage.setItem(settingsKey, settings);
      localStorage.setItem(dashboardKey, db);
      localStorage.setItem(tabsKey, tabs);
      localStorage.setItem(authorsKey, authors);
      localStorage.setItem(parametersKey, parameters);
      if (sprints) localStorage.setItem(sprintsKey, sprints);
    },
    {
      settingsKey: SETTINGS_KEY,
      dashboardKey: DASHBOARD_KEY,
      tabsKey: TABS_KEY,
      authorsKey: AUTHORS_KEY,
      parametersKey: PARAMETERS_KEY,
      sprintsKey: SPRINTS_KEY,
      settings: state.settings,
      db: state.dashboard,
      tabs: state.tabs,
      authors: state.authors,
      parameters: state.parameters,
      sprints: state.sprints ?? null,
    },
  );
}

export function stubRoutes(page: Page) {
  page.route('/api/**', (route) => route.fulfill({ status: 200, contentType: 'application/json', body: '{}' }));
  page.route('/graphQl', (route) => route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ data: {} }) }));
  page.route('/wsapi/**', (route) => route.abort());
}

// Seeds localStorage, stubs backend routes, and navigates once — the single choke point both suites' loaders build on.
export async function gotoSeededPage(page: Page, state: SeedState): Promise<void> {
  await seedLocalStorage(page, state);
  stubRoutes(page);
  await page.goto('/');
}

export async function waitForDashboardMounted(page: Page): Promise<void> {
  await page.waitForSelector('#tabBarTop', { state: 'visible' });
  await page.waitForSelector('#dashboardItem1', { state: 'visible' });
}

// Opens the Authors tab and waits for its author list to populate in Redux, then collapses it again unless keepOpen is set.
export async function revealAuthorList(page: Page, { keepOpen = false }: { keepOpen?: boolean } = {}): Promise<void> {
  const authorsTabBtn = page.locator('#tab_Authors');
  await authorsTabBtn.click();
  await page.waitForFunction(
    () => {
      try {
        const s = localStorage.getItem('bino_authorsStateV1');
        return s ? (JSON.parse(s).authorLists?.['1'] ?? []).length > 0 : false;
      } catch {
        return false;
      }
    },
    { timeout: 10_000 },
  );
  if (!keepOpen) {
    await authorsTabBtn.click();
  }
}
