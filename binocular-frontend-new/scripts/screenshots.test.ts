/**
 * Release screenshots — runs as a separate Playwright project.
 *
 * Prerequisites:
 *   1. Backend + frontend running:  npm run dev:concurrently
 *   2. State file captured once:    npm run screenshots:setup
 *
 * Usage:
 *   npm run screenshots
 *
 * Output: docs/assets/screenshots/ (overview) and .../visualizations/ (per-plugin)
 */

import { test, type Page } from '@playwright/test';
import path from 'path';
import fs from 'fs';

// ---------------------------------------------------------------------------
// Paths
// ---------------------------------------------------------------------------

// process.cwd() is binocular-frontend-new/ when invoked via `npm run screenshots`
const STATE_FILE = path.join(process.cwd(), 'scripts/screenshot-state.json');
const OUT_DIR = path.join(process.cwd(), '../docs/assets/screenshots');
const VIZ_DIR = path.join(OUT_DIR, 'visualizations');
const EXTRA_WAIT = Number(process.env.SCREENSHOT_WAIT_MS ?? 3000);

const DASHBOARD_KEY = 'bino_dashboardStateV1';
const TABS_KEY = 'bino_tabsStateV1';
const SPRINTS_KEY = 'bino_sprintsStateV1';

// ---------------------------------------------------------------------------
// State helpers
// ---------------------------------------------------------------------------

type DashboardItem = { pluginName: string; x: number; y: number; width: number; height: number; settings?: Record<string, unknown> };

function makeDashboardState(items: DashboardItem[], dataPluginId: number): string {
  return JSON.stringify({
    dashboardItems: items.map((item, i) => ({ ...item, id: i + 1, dataPluginId, settings: item.settings ?? undefined })),
    dashboardItemCount: items.length,
    popupCount: 0,
    dashboardState: Array.from({ length: 40 }, () => new Array(40).fill(0)),
    initialized: true,
  });
}

// Provides the full tab list so the tabController does NOT regenerate it
// (length matches the 7 Tab children in App.tsx → the regeneration condition
// `tabList.length !== newTabList.length` is false).
//
// contentID matches the auto-increment in generateTabs (id starts at 0, increments
// before assignment → Parameters=1, Visualizations=2, … Help=7).
// alignment uses the numeric enum (top=0, right=1).
// All selected:false — generateTabs would auto-select the FIRST tab of each
// alignment, so we must bypass regeneration to keep all panels closed.
function makeTabsAllClosed(): string {
  return JSON.stringify({
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
}

// Generate 10 consecutive 14-day sprints ending today, computed fresh on each run.
// Format matches the app's addSprint action: ISO string without milliseconds.
function makeSprintState(): string {
  const SPRINT_COUNT = 10;
  const SPRINT_DAYS = 14;
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const sprintList = Array.from({ length: SPRINT_COUNT }, (_, i) => {
    const daysToEnd = (SPRINT_COUNT - 1 - i) * SPRINT_DAYS;
    const endDate = new Date(today);
    endDate.setDate(today.getDate() - daysToEnd);
    const startDate = new Date(endDate);
    startDate.setDate(endDate.getDate() - SPRINT_DAYS);
    return {
      id: i,
      name: `Sprint ${i + 1}`,
      startDate: startDate.toISOString().split('.')[0],
      endDate: endDate.toISOString().split('.')[0],
    };
  });

  return JSON.stringify({ sprintList, currID: SPRINT_COUNT, sprintToEdit: null });
}

type LocalStorageEntry = { name: string; value: string };

function readSettings(): Record<string, unknown> {
  try {
    const raw = JSON.parse(fs.readFileSync(STATE_FILE, 'utf-8'));
    const origin = (raw.origins ?? []).find((o: { origin: string }) => o.origin.includes('localhost'));
    const entry = (origin?.localStorage ?? []).find((e: LocalStorageEntry) => e.name === 'bino_settingsStateV1');
    return entry ? JSON.parse(entry.value) : {};
  } catch {
    return {};
  }
}

// gridSize: 0=small (cellCount 40, ×1), 1=medium (20, ×2), 2=large (10, ×4).
// The physical grid is always 40 cells wide/tall (cellCount × gridMultiplier = 40).
// gridMultiplier converts visual grid boxes → logical (stored) units.
// Full width is always 40. "N grid boxes tall" = N × gridMultiplier logical units.
function readGridMultiplier(settings: Record<string, unknown>): number {
  const gridSize = (settings?.general as Record<string, unknown> | undefined)?.gridSize ?? 1;
  if (gridSize === 0) return 1; // small
  if (gridSize === 2) return 4; // large
  return 2; // medium (default)
}

function readDataPluginId(settings: Record<string, unknown>): number {
  const db = settings?.database as Record<string, unknown> | undefined;
  return (db?.defaultDataPluginItemId as number | undefined) ?? (db?.dataPlugins as Array<{ id: number }> | undefined)?.[0]?.id ?? 1;
}

// ---------------------------------------------------------------------------
// Layout / plugin definitions
// ---------------------------------------------------------------------------

// Per-plugin overrides:
//   settings         — override the plugin's defaultSettings (keys per its settings/settings.tsx)
//   waitMs           — extra wait on top of EXTRA_WAIT for D3 animations
//   spinnerTimeoutMs — override the 60 s spinner-disappear timeout for slow computations
//   selectedAuthorIds — restrict which authors are selected for this screenshot;
//                       use the numeric `id` from bino_authorsStateV1 (visible in screenshot-state.json)
//                       e.g. [1, 3, 12] — all others are deselected for that page load only
const VIZ_PLUGINS: {
  name: string;
  file: string;
  settings?: Record<string, unknown>;
  waitMs?: number;
  spinnerTimeoutMs?: number;
  selectedAuthorIds?: number[];
}[] = [
  { name: 'Changes', file: 'Changes' },
  { name: 'File Changes', file: 'File Changes' },
  { name: 'Commit By File', file: 'CommitByFile' },
  { name: 'Issues', file: 'Issues' },
  { name: 'Merge Requests', file: 'MergeRequests' },
  { name: 'Issues Timeline', file: 'IssuesTimeline' },
  { name: 'Burndown', file: 'Burndown' },
  { name: 'Builds', file: 'Builds' },
  { name: 'Code Ownership', file: 'CodeOwnership', spinnerTimeoutMs: 300_000 },
  { name: 'Time Spent', file: 'TimeSpent' },
  { name: 'Collaboration', file: 'Collaboration', waitMs: 300_000, spinnerTimeoutMs: 300_000 },
  { name: 'Repository Activity', file: 'RepositoryActivity', waitMs: 3000 },
  { name: 'Repository Stats', file: 'RepositoryStats' },
  { name: 'Code Expertise', file: 'CodeExpertise', spinnerTimeoutMs: 300_000, waitMs: 3000 },
  { name: 'Knowledge Radar', file: 'KnowledgeRadar' },
];

// ---------------------------------------------------------------------------
// Test setup — applied to all tests in this file
// ---------------------------------------------------------------------------

const stateFileExists = fs.existsSync(STATE_FILE);
const savedSettings = stateFileExists ? readSettings() : {};
const dataPluginId = stateFileExists ? readDataPluginId(savedSettings) : 1;
// Full dashboard grid is always 40×40. gridMultiplier converts visual boxes → logical units.
const GM = stateFileExists ? readGridMultiplier(savedSettings) : 2;

// ---------------------------------------------------------------------------
// Dashboard setups
// ---------------------------------------------------------------------------
// Each entry produces one screenshot: OUT_DIR/{file}.png
// DASHBOARD_SETUPS[0] is also used by the tab-cycling UI screenshots.
//
// Grid rules (full grid = 40×40):
//   - Full width  = 40
//   - 10 visual rows = GM * 10 logical rows (GM=2 for medium, =4 for large, =1 for small)
//   - Total height of items in any column must not exceed 40 logical rows
//
// Per-item settings override the plugin's defaultSettings — keys come from
// the plugin's own settings/settings.tsx file.

type DashboardSetup = { name: string; file: string; items: DashboardItem[] };

const DASHBOARD_SETUPS: DashboardSetup[] = [
  {
    name: 'Overview',
    file: 'Dashboard Overview',
    items: [
      { pluginName: 'Changes', x: 0, y: 0, width: 28, height: GM * 10 },
      { pluginName: 'Issues Timeline', x: 0, y: GM * 10, width: 28, height: GM * 10 },
      { pluginName: 'Repository Stats', x: 28, y: 0, width: 12, height: GM * 10 },
      { pluginName: 'Builds', x: 28, y: GM * 10, width: 12, height: GM * 10 },
    ],
  },
  {
    name: 'Issues',
    file: 'Dashboard Issues',
    items: [
      { pluginName: 'Issues', x: 0, y: 0, width: 20, height: GM * 10 },
      { pluginName: 'Burndown', x: 20, y: 0, width: 20, height: GM * 10 },
      { pluginName: 'Merge Requests', x: 0, y: GM * 10, width: 20, height: GM * 10 },
      { pluginName: 'Issues Timeline', x: 20, y: GM * 10, width: 20, height: GM * 10 },
    ],
  },
  {
    name: 'Code Quality',
    file: 'Dashboard Code Quality',
    items: [
      { pluginName: 'Code Ownership', x: 0, y: 0, width: 20, height: GM * 10 },
      { pluginName: 'Code Expertise', x: 20, y: 0, width: 20, height: GM * 10 },
      { pluginName: 'Collaboration', x: 0, y: GM * 10, width: 40, height: GM * 10 },
    ],
  },
];

// Seed the full user config (data plugins, settings) from the captured state file.
// Falls back to empty state if the file doesn't exist — tests will be skipped via beforeEach.
test.use({
  storageState: stateFileExists ? STATE_FILE : { cookies: [], origins: [] },
});

// ---------------------------------------------------------------------------
// Page helpers
// ---------------------------------------------------------------------------

async function loadAndWait(
  page: Page,
  items: DashboardItem[],
  extraWaitMs = 0,
  spinnerTimeoutMs = 60_000,
  selectedAuthorIds?: number[],
): Promise<void> {
  // Override dashboard layout, tabs, sprints, and optionally author selection.
  // Everything else (data plugin config, full author list, …) comes from storageState.
  await page.addInitScript(
    ({ dashboardKey, tabsKey, sprintsKey, authorsKey, dashboard, tabs, sprints, authorIds }) => {
      localStorage.setItem(dashboardKey, dashboard);
      localStorage.setItem(tabsKey, tabs);
      localStorage.setItem(sprintsKey, sprints);
      if (authorIds) {
        const raw = localStorage.getItem(authorsKey);
        if (raw) {
          const state = JSON.parse(raw) as { authorLists: Record<string, { id: number; selected: boolean }[]> };
          for (const pluginId of Object.keys(state.authorLists)) {
            state.authorLists[pluginId] = state.authorLists[pluginId].map((a) => ({
              ...a,
              selected: authorIds.includes(a.id),
            }));
          }
          localStorage.setItem(authorsKey, JSON.stringify(state));
        }
      }
    },
    {
      dashboardKey: DASHBOARD_KEY,
      tabsKey: TABS_KEY,
      sprintsKey: SPRINTS_KEY,
      authorsKey: 'bino_authorsStateV1',
      dashboard: makeDashboardState(items, dataPluginId),
      tabs: makeTabsAllClosed(),
      sprints: makeSprintState(),
      authorIds: selectedAuthorIds ?? null,
    },
  );

  await page.goto('/');
  // Wait for the tab bar container AND at least one tab button inside it.
  await page.waitForSelector('#tabBarTop', { state: 'visible', timeout: 30_000 });
  await page.waitForSelector('#tabBarTop [id^="tab_"]', { state: 'visible', timeout: 30_000 });

  if (items.length > 0) {
    await page.waitForSelector('[id^="dashboardItem"]', { state: 'visible', timeout: 20_000 });

    // Give React time to mount chart components and begin GraphQL fetches,
    // so the loading spinners appear before we start waiting for them to go.
    await page.waitForTimeout(1000);

    // Wait until every loading spinner has disappeared — means all visible
    // visualizations have finished fetching and rendered their charts.
    await page.waitForFunction(() => document.querySelector('.loading-spinner') === null, { timeout: spinnerTimeoutMs, polling: 300 });
  }

  // Wait for D3 enter/update animations to finish, plus any per-visualization extra.
  await page.waitForTimeout(EXTRA_WAIT + extraWaitMs);
}

async function clickTab(page: Page, tabId: string): Promise<void> {
  await page.locator(`[id="${tabId}"]`).click();
  await page.waitForTimeout(400); // let the panel slide animation finish
}

async function save(page: Page, filePath: string): Promise<void> {
  // Brief pause so panel content and any transition animations finish rendering.
  await page.waitForTimeout(800);
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
  await page.screenshot({ path: filePath, fullPage: false });
  console.log(`  ✓  ${path.relative(path.join(process.cwd(), '..'), filePath)}`);
}

// ---------------------------------------------------------------------------
// Dashboard setup screenshots — one per entry in DASHBOARD_SETUPS
// ---------------------------------------------------------------------------

test.describe('@screenshot — Dashboard setups', () => {
  // eslint-disable-next-line no-empty-pattern
  test.beforeEach(async ({}, testInfo) => {
    if (!stateFileExists) testInfo.skip(true, 'State file missing — run "npm run screenshots:setup" first');
  });

  for (const setup of DASHBOARD_SETUPS) {
    test(setup.name, async ({ page }) => {
      test.setTimeout(60_000 + EXTRA_WAIT + 60_000);
      await loadAndWait(page, setup.items);
      await save(page, path.join(OUT_DIR, `${setup.file}.png`));
    });
  }
});

// ---------------------------------------------------------------------------
// UI panel screenshots — tab cycling using DASHBOARD_SETUPS[0] as the base layout
// ---------------------------------------------------------------------------

test.describe('@screenshot — Dashboard UI panels', () => {
  // eslint-disable-next-line no-empty-pattern
  test.beforeEach(async ({}, testInfo) => {
    if (!stateFileExists) testInfo.skip(true, 'State file missing — run "npm run screenshots:setup" first');
  });

  test('ui-panels', async ({ page }) => {
    // loadAndWait can use up to spinnerTimeoutMs (60s) + EXTRA_WAIT.
    // Add 60s on top for all the tab clicks, dialog interactions, and screenshots.
    test.setTimeout(60_000 + EXTRA_WAIT + 60_000);
    await loadAndWait(page, DASHBOARD_SETUPS[0].items);

    await clickTab(page, 'tab_Visualizations');
    await save(page, path.join(OUT_DIR, 'Dashboard Visualizations.png'));

    await page.locator('[id="tab_File Tree"]').click();
    await page.waitForTimeout(400);
    await save(page, path.join(OUT_DIR, 'Dashboard Visualizations and FileTree.png'));

    // Close Visualizations + FileTree, open Parameters + Authors
    await clickTab(page, 'tab_Visualizations');
    await page.locator('[id="tab_File Tree"]').click();
    await page.waitForTimeout(400);
    await clickTab(page, 'tab_Parameters');
    await clickTab(page, 'tab_Authors');
    await save(page, path.join(OUT_DIR, 'Dashboard_Parameters and Authors.png'));

    // Close Parameters + Authors, open Sprints + Help
    await clickTab(page, 'tab_Parameters');
    await clickTab(page, 'tab_Authors');
    await clickTab(page, 'tab_Sprints');
    await clickTab(page, 'tab_Help');
    await save(page, path.join(OUT_DIR, 'Dashboard Sprints and Help.png'));

    // Close Sprints + Help, open Layouts
    await clickTab(page, 'tab_Sprints');
    await clickTab(page, 'tab_Help');
    await clickTab(page, 'tab_Layouts');
    await save(page, path.join(OUT_DIR, 'Dashboard Layouts.png'));
    await clickTab(page, 'tab_Layouts');

    // Database settings dialog
    await page.locator('button[aria-label="Settings"]').click();
    await page.waitForSelector('#settingsDialog', { state: 'visible' });
    await page.locator('#settingsDialog').getByRole('tab', { name: 'Database' }).click();
    await page.waitForTimeout(300);
    await save(page, path.join(OUT_DIR, 'Database Settings.png'));
    await page.locator('#settingsDialog').getByRole('button', { name: '✕' }).click();

    // Visualization item settings panel
    const firstItem = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
    await firstItem.waitFor({ state: 'visible' });
    await firstItem.getByRole('button').first().click();
    await page.waitForTimeout(300);
    await save(page, path.join(OUT_DIR, 'Visualization Settings.png'));
  });
});

// ---------------------------------------------------------------------------
// Individual visualization screenshots
// ---------------------------------------------------------------------------

test.describe('@screenshot — Individual visualizations', () => {
  // eslint-disable-next-line no-empty-pattern
  test.beforeEach(async ({}, testInfo) => {
    if (!stateFileExists) testInfo.skip(true, 'State file missing — run "npm run screenshots:setup" first');
  });

  for (const plugin of VIZ_PLUGINS) {
    test(plugin.name, async ({ page }) => {
      // Set the Playwright test timeout to cover the full budget for this plugin.
      // Default Playwright timeout (30 s) would kill slow visualizations before waitMs fires.
      const testBudget = 30_000 + (plugin.spinnerTimeoutMs ?? 60_000) + EXTRA_WAIT + (plugin.waitMs ?? 0);
      test.setTimeout(testBudget);

      await page.setViewportSize({ width: 1920, height: 1080 });
      page.on('pageerror', (err) => console.error(`  [${plugin.name}] ${err.message}`));

      await loadAndWait(
        page,
        [{ pluginName: plugin.name, x: 0, y: 0, width: 40, height: GM * 11, settings: plugin.settings }],
        plugin.waitMs,
        plugin.spinnerTimeoutMs,
        plugin.selectedAuthorIds,
      );
      await save(page, path.join(VIZ_DIR, `${plugin.file}.png`));
    });
  }
});
