// Shared setup helpers for the screenshot test suite.
// All constants, state seeds, builder functions, and page-load helpers live here
// so screenshots.test.ts contains only test cases.

import type { Page } from '@playwright/test';
import path from 'path';
import fs from 'fs';

// ─── localStorage keys ────────────────────────────────────────────────────────

const SETTINGS_KEY = 'bino_settingsStateV1';
const DASHBOARD_KEY = 'bino_dashboardStateV1';
const TABS_KEY = 'bino_tabsStateV1';
const AUTHORS_KEY = 'bino_authorsStateV1';
const PARAMETERS_KEY = 'bino_parametersStateV1';
const SPRINTS_KEY = 'bino_sprintsStateV1';

// ─── Seeded localStorage values ───────────────────────────────────────────────

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

// All 7 tabs (Parameters, Visualizations, Sprints, Layouts, Authors, File Tree, Help) stored
// with selected:false. TabController only auto-selects tabs when the stored tabList length
// differs from the number of Tab children in App.tsx — providing the correct length (7)
// preserves the stored selection and gives a fully collapsed UI for screenshots.
// TabAlignment: top=0, right=1. contentID and position follow App.tsx declaration order.
const ALL_TABS_CLOSED = JSON.stringify({
  tabList: [
    { displayName: 'Parameters', alignment: 0, selected: false, contentID: 1, position: 0 },
    { displayName: 'Visualizations', alignment: 0, selected: false, contentID: 2, position: 1 },
    { displayName: 'Sprints', alignment: 0, selected: false, contentID: 3, position: 2 },
    { displayName: 'Layouts', alignment: 0, selected: false, contentID: 4, position: 3 },
    { displayName: 'Authors', alignment: 1, selected: false, contentID: 5, position: 4 },
    { displayName: 'File Tree', alignment: 1, selected: false, contentID: 6, position: 5 },
    { displayName: 'Help', alignment: 1, selected: false, contentID: 7, position: 6 },
  ],
});

// Wide date range so Change Frequency (which actually filters by date) covers the mock data.
// Most other visualizations ignore the date range in their mock data implementations.
const MOCK_PARAMETERS_STATE = JSON.stringify({
  parametersGeneral: { granularity: 'days', excludeMergeCommits: false },
  parametersDateRange: { from: '2026-04-25T16:03:21', to: '2026-07-10T03:01:27' },
});

// DashboardItem renders the visualization only when `authors` is not undefined.
// `authors` is set from `authorLists[dataPluginId]` in Redux, which is only populated
// when the Authors component mounts (normally via the Authors tab being open).
// Pre-seeding an empty authorList for dataPluginId=1 satisfies the truthiness check
// without needing the Authors tab open.
const MOCK_AUTHORS_STATE = JSON.stringify({
  authorLists: { '1': [] },
  dragging: false,
  draggingSource: null,
  authorToEdit: undefined,
  dataPluginId: 1,
});

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

function seedLocalStorage(page: Page, dashboard: string, sprintsState?: string | null) {
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
      settings: MOCK_DATA_SETTINGS,
      db: dashboard,
      tabs: ALL_TABS_CLOSED,
      authors: MOCK_AUTHORS_STATE,
      parameters: MOCK_PARAMETERS_STATE,
      sprints: sprintsState ?? null,
    },
  );
}

function stubRoutes(page: Page) {
  page.route('/api/**', (route) => route.fulfill({ status: 200, contentType: 'application/json', body: '{}' }));
  page.route('/graphQl', (route) => route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ data: {} }) }));
  page.route('/wsapi/**', (route) => route.abort());
}

export async function loadVis(page: Page, pluginName: string, itemSettings?: object): Promise<void> {
  await seedLocalStorage(page, buildDashboard(pluginName, 40, 22, itemSettings));
  stubRoutes(page);
  await page.goto('/');
  await page.waitForSelector('#tabBarTop', { state: 'visible' });
  await page.waitForSelector('#dashboardItem1', { state: 'visible' });
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
  await authorsTabBtn.click();
}

// Loads a page with multiple dashboard items. After authors load, opens the requested tabs
// by display name (e.g. 'Visualizations', 'File Tree'). Set keepAuthorsOpen=true when the
// Authors tab should remain open for the screenshot. Pass sprintsState to pre-seed sprints.
export async function loadDashboard(
  page: Page,
  items: DashItemConfig[],
  extraTabsToOpen: string[] = [],
  keepAuthorsOpen = false,
  sprintsState?: string,
): Promise<void> {
  await seedLocalStorage(page, buildMultiDashboard(items), sprintsState);
  stubRoutes(page);
  await page.goto('/');
  await page.waitForSelector('#tabBarTop', { state: 'visible' });
  await page.waitForSelector('#dashboardItem1', { state: 'visible' });
  const authorsTabBtn = page.locator('[id="tab_Authors"]');
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
  if (!keepAuthorsOpen) {
    await authorsTabBtn.click();
  }
  for (const tabName of extraTabsToOpen) {
    await page.locator(`[id="tab_${tabName}"]`).click();
    await page.waitForTimeout(300);
  }
}

// ─── Screenshot helpers ───────────────────────────────────────────────────────

// Output goes to docs/assets/screenshots/visualizations/ at the repo root,
// matching the existing naming convention used by manually captured screenshots.
export const SCREENSHOTS_DIR = path.resolve(process.cwd(), '..', 'docs', 'assets', 'screenshots', 'visualizations');

export async function takeScreenshot(page: Page, filename: string): Promise<void> {
  fs.mkdirSync(SCREENSHOTS_DIR, { recursive: true });
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, filename), animations: 'disabled' });
}

export const DASHBOARD_SCREENSHOTS_DIR = path.resolve(process.cwd(), '..', 'docs', 'assets', 'screenshots');

export async function takeDashboardScreenshot(page: Page, filename: string): Promise<void> {
  fs.mkdirSync(DASHBOARD_SCREENSHOTS_DIR, { recursive: true });
  await page.screenshot({ path: path.join(DASHBOARD_SCREENSHOTS_DIR, filename), animations: 'disabled' });
}

// ─── Visualization registry ───────────────────────────────────────────────────

// waitFor: CSS selector inside #dashboardItem1 that must be visible before screenshotting.
// waitForText: plain text inside #dashboardItem1 to wait for (for HTML-only or text-only states).
// waitForHidden: text that must DISAPPEAR before screenshotting (e.g. loading indicators).
// Omit all three for visualizations whose final state renders synchronously.
export const VISUALIZATIONS: Array<{
  pluginName: string;
  filename: string;
  waitFor?: string;
  waitForText?: string;
  waitForHidden?: string;
  navigateTo?: string[];
  settings?: object;
}> = [
  {
    pluginName: 'Changes',
    filename: 'Changes.png',
    waitFor: 'svg g.areas path',
    settings: { visualizationStyle: 'stepped', splitAdditionsDeletions: false, showSprints: false },
  },
  { pluginName: 'Sum Commits', filename: 'SumCommits.png', waitFor: 'svg g rect' },
  {
    pluginName: 'File Changes',
    filename: 'File Changes.png',
    waitFor: 'svg g path',
    settings: {
      file: 'frontend/src/app/app-routing.module.ts',
      splitAdditionsDeletions: true,
      visualizationStyle: 'curved',
      showSprints: false,
      showExtraMetrics: false,
    },
  },
  { pluginName: 'Commit By File', filename: 'CommitByFile.png', waitForHidden: 'No Data' },
  { pluginName: 'Builds', filename: 'Builds.png', waitFor: 'svg g path' },
  {
    pluginName: 'Issues',
    filename: 'Issues.png',
    waitFor: 'svg g path',
    settings: { splitIssuesPerAuthor: true, breakdown: true, visualizationStyle: 'curved', showSprints: false },
  },
  {
    pluginName: 'Merge Requests',
    filename: 'MergeRequests.png',
    waitFor: 'svg g path',
    settings: { splitMergeRequestsPerAuthor: true, breakdown: true, visualizationStyle: 'curved', showSprints: false },
  },
  {
    pluginName: 'Issues Timeline',
    filename: 'IssuesTimeline.png',
    waitFor: 'svg rect',
    settings: { coloringMode: 'assignee', showSprints: false },
  },
  { pluginName: 'Burndown', filename: 'Burndown.png', waitFor: 'svg g path' },
  {
    pluginName: 'Time Spent',
    filename: 'TimeSpent.png',
    waitFor: 'svg g path',
    settings: { breakdown: true, visualizationStyle: 'linear', splitTimePerIssue: false, splitSpentRemoved: false, showSprints: false },
  },
  { pluginName: 'Collaboration', filename: 'Collaboration.png', waitForHidden: 'Simulating graph layout...' },
  { pluginName: 'Repository Activity', filename: 'RepositoryActivity.png', waitFor: 'svg rect' },
  { pluginName: 'Repository Stats', filename: 'RepositoryStats.png', waitForText: 'Contributors' },
  { pluginName: 'Code Ownership', filename: 'CodeOwnership.png', waitFor: 'svg g path' },
  { pluginName: 'Code Expertise', filename: 'CodeExpertise.png', waitFor: 'svg text' },
  { pluginName: 'Knowledge Radar', filename: 'KnowledgeRadar.png', waitFor: 'svg text' },
  {
    pluginName: 'Change Frequency',
    filename: 'ChangeFrequency.png',
    waitFor: 'svg circle',
    navigateTo: ['frontend', 'src', 'app', 'components'],
  },
];
