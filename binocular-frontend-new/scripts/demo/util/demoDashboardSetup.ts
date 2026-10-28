// Demo dashboard bootstrapping for scripted demo-video recordings — adding/swapping/sizing the one visualization each scene records against.

import type { Locator, Page } from '@playwright/test';
import { buildEmptyDashboard, revealAuthorList } from '../../seedState.ts';
import { gotoDemoDashboard } from '../demoSetup.ts';
import { humanClickLocator, humanDrag, humanFill, openItemSettings, openTabIfClosed } from './demoInteractions.ts';
import { beginTitleCard, endTitleCard } from './demoTitleCard.ts';

export async function mockBackendRoutes(page: Page): Promise<void> {
  await page.route('/api/**', (route) => route.fulfill({ status: 200, contentType: 'application/json', body: '{}' }));
  await page.route('/graphQl', (route) =>
    route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ data: {} }) }),
  );
  await page.route('/wsapi/**', (route) => route.abort());
}

// Opens the Visualizations overview dialog, searches for the plugin and adds it — the on-camera path shared by loadDemoVis()
// and switchVisualization(); the item carries the plugin's default settings.
async function addVisualizationViaSearch(page: Page, pluginName: string, fast = false): Promise<Locator> {
  const pause = (ms: number) => page.waitForTimeout(fast ? Math.round(ms * 0.4) : ms);
  await humanClickLocator(page, page.locator('button.btn-square.btn-primary.btn-sm'));
  const overviewDialog = page.locator('#visualizationOverview');
  await overviewDialog.waitFor({ state: 'visible' });
  await pause(100);

  await humanFill(page, overviewDialog.getByPlaceholder('Search'), pluginName);
  await pause(200);

  await humanClickLocator(page, overviewDialog.locator(`button:has(img[alt="${pluginName}"])`));
  await page.keyboard.press('Escape');
  await pause(200);

  const newItem = page.locator('[id^="dashboardItem"]:not([id*="_"])').filter({ hasText: pluginName });
  await newItem.waitFor({ state: 'attached', timeout: 10_000 });
  await pause(100);
  return newItem;
}

// Walks up from a dashboard item to find the `.dashboard` scroll container by computed style, since a demo video shouldn't show scrolled-off content.
async function getViewableDashboardBox(page: Page): Promise<{ x: number; y: number; width: number; height: number } | null> {
  return page.evaluate(() => {
    let el: Element | null = document.querySelector('[id^="dashboardItem"]:not([id*="_"])');
    while (el && el !== document.body) {
      if (getComputedStyle(el).overflowY === 'scroll') break;
      el = el.parentElement;
    }
    if (!el || el === document.body) return null;
    const rect = el.getBoundingClientRect();
    return { x: rect.x, y: rect.y, width: rect.width, height: rect.height };
  });
}

// dashboard.tsx's `cellSize` updates asynchronously via ResizeObserver after a tab open/close, so poll a grid cell's rendered width until it settles.
async function waitForCellSizeStable(page: Page): Promise<void> {
  let previous: number | null = null;
  for (let i = 0; i < 15; i++) {
    const current = await page.evaluate(() => document.getElementById('cellY0X0')?.getBoundingClientRect().width ?? null);
    if (current !== null && previous !== null && Math.abs(current - previous) < 0.5) return;
    previous = current;
    await page.waitForTimeout(50);
  }
}

// Drags an item's resize handle to the dashboard's visible edge so it fills the screen; overshooting is safe since dashboardReducer.ts
// clamps it, and it retries once in case cellSize hadn't settled yet.
export async function resizeToViewableArea(page: Page, item: Locator): Promise<void> {
  for (let attempt = 0; attempt < 2; attempt++) {
    await waitForCellSizeStable(page);

    const viewableBox = await getViewableDashboardBox(page);
    if (!viewableBox) return;

    const resizeHandle = item.locator('[class*="dashboardItemResizeBarBottomRight"]');
    const handleBox = await resizeHandle.boundingBox();
    if (!handleBox) return;

    await humanDrag(
      page,
      { x: handleBox.x + handleBox.width / 2, y: handleBox.y + handleBox.height / 2 },
      { x: viewableBox.x + viewableBox.width, y: viewableBox.y + viewableBox.height },
      2,
    );
    await page.waitForTimeout(50);

    // Items are inset up to ~20px from the true grid edge, so compare the gap to viewableBox's edge (with slack) instead of raw width/height.
    const resultBox = await item.boundingBox();
    if (
      resultBox &&
      viewableBox.x + viewableBox.width - (resultBox.x + resultBox.width) < 30 &&
      viewableBox.y + viewableBox.height - (resultBox.y + resultBox.height) < 30
    ) {
      await page.waitForTimeout(100);
      return;
    }
  }
}

// Opens the Visualizations tab, adds the plugin via the overview search, then opens
// Authors and sizes the item to the visible dashboard area the state every scene records against.
async function prepareDemoDashboard(page: Page, pluginName: string): Promise<Locator> {
  await page.waitForSelector('#tabBarTop', { state: 'visible' });
  await openTabIfClosed(page, 'Visualizations');
  const item = await addVisualizationViaSearch(page, pluginName, true);
  await revealAuthorList(page, { keepOpen: true });
  await resizeToViewableArea(page, item);
  return item;
}

// Loads an empty dashboard and adds the plugin on camera — a scene's first visualization; later swaps use switchVisualization().
// Overview-added items always get plugin defaults, so a preset must be set through the settings panel like a user would.
export async function loadDemoVis(
  page: Page,
  pluginName: string,
  opts: { title?: string; sprintsState?: string; titleMs?: number } = {},
): Promise<Locator> {
  await gotoDemoDashboard(page, buildEmptyDashboard(), opts.sprintsState);
  // Raised only after the goto (a navigation discards it) and drops before the tab/search choreography, so callers await
  // readiness themselves instead of via endTitleCardWhenReady().
  if (opts.title) {
    await beginTitleCard(page, opts.title);
    // Hold for a readable minimum: on a warm run the app boots faster than the title takes to read.
    await Promise.all([page.waitForTimeout(opts.titleMs ?? 1200), page.waitForSelector('#tabBarTop', { state: 'visible' })]);
    await endTitleCard(page);
  }
  return prepareDemoDashboard(page, pluginName);
}

// Swaps the dashboard's item to a different plugin without a reload; the new item gets default settings, so callers needing a preset must re-apply it.
// `fast` scales the between-step pauses down (used to tighten a scene's pacing) without touching callers that don't opt in.
export async function switchVisualization(page: Page, pluginName: string, opts: { fast?: boolean } = {}): Promise<Locator> {
  const fast = opts.fast ?? false;
  const pause = (ms: number) => page.waitForTimeout(fast ? Math.round(ms * 0.4) : ms);

  await openTabIfClosed(page, 'Visualizations');
  await pause(200);

  const currentItem = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
  const currentItemId = await currentItem.getAttribute('id');

  const currentSettingsPanel = page.locator(`#${currentItemId}_settings`);
  await openItemSettings(page, currentItem, currentSettingsPanel);
  await pause(200);

  await humanClickLocator(page, currentSettingsPanel.getByRole('button', { name: 'Delete', exact: true }));
  await pause(50);

  const newItem = await addVisualizationViaSearch(page, pluginName, fast);

  await resizeToViewableArea(page, newItem);
  await pause(200);

  return newItem;
}
