// Setup for the screenshot suite — owns the localStorage state its captures assume, plus the loaders and screenshot helpers,
// so screenshots.test.ts is only test cases. The demo-video suite owns its own equivalent in demo/demoSetup.ts; both build on seedState.ts.

import type { Page } from '@playwright/test';
import path from 'path';
import fs from 'fs';
import {
  buildDashboard,
  buildMultiDashboard,
  gotoSeededPage,
  revealAuthorList,
  waitForDashboardMounted,
  type DashItemConfig,
  type SeedState,
} from './seedState.ts';

export type { DashItemConfig };

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

// All 7 tabs stored with selected:false at the correct length (7) so TabController doesn't auto-select, giving a fully collapsed UI for screenshots.
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

// Wide date range so Change Frequency (which actually filters by date) covers the mock data; most other visualizations ignore it.
const MOCK_PARAMETERS_STATE = JSON.stringify({
  parametersGeneral: { granularity: 'days', excludeMergeCommits: false },
  parametersDateRange: { from: '2026-04-25T16:03:21', to: '2026-07-10T03:01:27' },
});

// DashboardItem only renders once `authors` (authorLists[dataPluginId] in Redux) is defined, so pre-seed an empty list to satisfy that without opening the tab.
const MOCK_AUTHORS_STATE = JSON.stringify({
  authorLists: { '1': [] },
  dragging: false,
  draggingSource: null,
  authorToEdit: undefined,
  dataPluginId: 1,
});

function screenshotsSeed(dashboard: string, sprints?: string): SeedState {
  return {
    settings: MOCK_DATA_SETTINGS,
    dashboard,
    tabs: ALL_TABS_CLOSED,
    authors: MOCK_AUTHORS_STATE,
    parameters: MOCK_PARAMETERS_STATE,
    sprints,
  };
}

// ─── Page loaders ─────────────────────────────────────────────────────────────

// Loads a single-item dashboard and closes the sidebar again, since screenshots.test.ts's captures assume a full-width chart with nothing occluding it.
export async function loadVis(page: Page, pluginName: string, itemSettings?: object, sprintsState?: string): Promise<void> {
  await gotoSeededPage(page, screenshotsSeed(buildDashboard(pluginName, 40, 22, itemSettings), sprintsState));
  await waitForDashboardMounted(page);
  await revealAuthorList(page);
}

// Loads a page with multiple dashboard items, then opens the requested tabs by display name (set keepAuthorsOpen for the Authors tab).
export async function loadDashboard(
  page: Page,
  items: DashItemConfig[],
  extraTabsToOpen: string[] = [],
  keepAuthorsOpen = false,
  sprintsState?: string,
): Promise<void> {
  await gotoSeededPage(page, screenshotsSeed(buildMultiDashboard(items), sprintsState));
  await waitForDashboardMounted(page);
  await revealAuthorList(page, { keepOpen: keepAuthorsOpen });
  for (const tabName of extraTabsToOpen) {
    await page.locator(`[id="tab_${tabName}"]`).click();
    await page.waitForTimeout(300);
  }
}

// ─── Screenshot helpers ───────────────────────────────────────────────────────

// Output goes to docs/assets/screenshots/visualizations/, matching the naming convention of manually captured screenshots.
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
