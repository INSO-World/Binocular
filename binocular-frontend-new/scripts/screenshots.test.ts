// Captures screenshots using the in-browser Mock Data plugin — no backend required. Run: npm run screenshots.

import { test } from '@playwright/test';
import { VISUALIZATIONS, DashItemConfig, loadVis, loadDashboard, takeScreenshot, takeDashboardScreenshot } from './screenshots.setup.ts';

// ─── Per-visualization screenshots ───────────────────────────────────────────

for (const { pluginName, filename, waitFor, waitForText, waitForHidden, navigateTo, settings } of VISUALIZATIONS) {
  test(`screenshot: ${pluginName}`, async ({ page }) => {
    test.setTimeout(60_000);
    await loadVis(page, pluginName, settings);
    if (waitFor) {
      await page.locator(`#dashboardItem1 ${waitFor}`).first().waitFor({ state: 'visible', timeout: 20_000 });
    } else if (waitForText) {
      await page.locator('#dashboardItem1').getByText(waitForText).first().waitFor({ state: 'visible', timeout: 20_000 });
    } else if (waitForHidden) {
      await page.locator('#dashboardItem1').getByText(waitForHidden).first().waitFor({ state: 'hidden', timeout: 20_000 });
    }
    if (navigateTo?.length) {
      const dirBtn = page.locator('#dashboardItem1').getByRole('button', { name: 'Directory' });
      await dirBtn.click();
      await page.locator('#dashboardItem1 [class*="directoryDrawerOpen"]').waitFor({ state: 'visible' });
      for (const folder of navigateTo) {
        await page.locator('#dashboardItem1 [class*="directoryEntry"]').filter({ hasText: folder }).first().click();
        await page.locator('#dashboardItem1 [class*="breadcrumb"]').getByText(folder).first().waitFor({ state: 'visible', timeout: 5_000 });
      }
      await dirBtn.click();
      await page.locator('#dashboardItem1 [class*="directoryDrawerOpen"]').waitFor({ state: 'hidden' });
    }
    await page.waitForTimeout(800);
    await takeScreenshot(page, filename);
  });
}

// ─── Dashboard screenshots ────────────────────────────────────────────────────

test('screenshot: Dashboard Visualizations', async ({ page }) => {
  test.setTimeout(60_000);
  const items: DashItemConfig[] = [
    {
      id: 1,
      pluginName: 'Changes',
      x: 0,
      y: 0,
      width: 16,
      height: 12,
      settings: { visualizationStyle: 'curved', splitAdditionsDeletions: false, showSprints: false },
    },
    { id: 2, pluginName: 'Builds', x: 16, y: 0, width: 14, height: 12 },
    { id: 3, pluginName: 'Repository Stats', x: 30, y: 0, width: 10, height: 16 },
    {
      id: 4,
      pluginName: 'Issues Timeline',
      x: 0,
      y: 12,
      width: 14,
      height: 12,
      settings: { coloringMode: 'assignee', showSprints: false },
    },
    { id: 5, pluginName: 'Repository Activity', x: 14, y: 12, width: 14, height: 12 },
    {
      id: 6,
      pluginName: 'Issues',
      x: 28,
      y: 16,
      width: 14,
      height: 8,
      settings: { splitIssuesPerAuthor: true, breakdown: true, visualizationStyle: 'curved', showSprints: false },
    },
    { id: 7, pluginName: 'Code Ownership', x: 0, y: 24, width: 20, height: 10 },
    { id: 8, pluginName: 'Burndown', x: 20, y: 24, width: 20, height: 10 },
  ];
  await loadDashboard(page, items, ['Visualizations']);
  await page.locator('#dashboardItem1 svg g path').first().waitFor({ state: 'visible', timeout: 20_000 });
  // Click the grid icon button in the Visualization Selector to open the visualization overview overlay
  await page.locator('.btn.btn-square.btn-primary.btn-sm').click();
  await page.locator('#visualizationOverview').waitFor({ state: 'visible' });
  await page.waitForTimeout(500);
  await takeDashboardScreenshot(page, 'DashboardVisualizations.png');
});

test('screenshot: Dashboard Layouts', async ({ page }) => {
  test.setTimeout(90_000);
  const items: DashItemConfig[] = [
    { id: 1, pluginName: 'Collaboration', x: 0, y: 0, width: 14, height: 14 },
    {
      id: 2,
      pluginName: 'Changes',
      x: 14,
      y: 0,
      width: 14,
      height: 14,
      settings: { visualizationStyle: 'curved', splitAdditionsDeletions: false, showSprints: false },
    },
    { id: 3, pluginName: 'Knowledge Radar', x: 28, y: 0, width: 14, height: 18 },
    { id: 4, pluginName: 'Builds', x: 0, y: 14, width: 14, height: 14 },
    {
      id: 5,
      pluginName: 'Issues',
      x: 14,
      y: 14,
      width: 14,
      height: 14,
      settings: { splitIssuesPerAuthor: true, breakdown: true, visualizationStyle: 'curved', showSprints: false },
    },
    { id: 6, pluginName: 'Code Expertise', x: 28, y: 18, width: 14, height: 10 },
  ];
  await loadDashboard(page, items, ['Layouts']);
  await page.locator('#dashboardItem2 svg g path').first().waitFor({ state: 'visible', timeout: 20_000 });
  await page.locator('#dashboardItem1').getByText('Simulating graph layout...').waitFor({ state: 'hidden', timeout: 40_000 });
  // Click the grid icon button in the Layouts selector panel to open the layout overview overlay
  await page.locator('.btn.btn-square.btn-primary.btn-sm').click();
  await page.locator('#layoutOverview').waitFor({ state: 'visible' });
  await page.waitForTimeout(500);
  await takeDashboardScreenshot(page, 'DashboardLayouts.png');
});

test('screenshot: Dashboard Sprints and Help', async ({ page }) => {
  test.setTimeout(60_000);
  const sprintsState = JSON.stringify({
    sprintList: [
      { id: 0, name: 'Sprint 1', startDate: '2026-04-25', endDate: '2026-05-09' },
      { id: 1, name: 'Sprint 2', startDate: '2026-05-09', endDate: '2026-05-23' },
      { id: 2, name: 'Sprint 3', startDate: '2026-05-23', endDate: '2026-06-06' },
      { id: 3, name: 'Sprint 4', startDate: '2026-06-06', endDate: '2026-06-20' },
    ],
    currID: 5,
    sprintToEdit: null,
  });
  const items: DashItemConfig[] = [
    { id: 1, pluginName: 'Burndown', x: 0, y: 0, width: 13, height: 14, settings: { showSprints: true } },
    {
      id: 2,
      pluginName: 'Changes',
      x: 13,
      y: 0,
      width: 14,
      height: 14,
      settings: { visualizationStyle: 'curved', splitAdditionsDeletions: false, showSprints: true },
    },
    { id: 3, pluginName: 'Code Ownership', x: 27, y: 0, width: 13, height: 14, settings: { showSprints: true } },
    { id: 4, pluginName: 'Builds', x: 0, y: 14, width: 13, height: 14, settings: { showSprints: true } },
    {
      id: 5,
      pluginName: 'Issues',
      x: 13,
      y: 14,
      width: 14,
      height: 14,
      settings: { splitIssuesPerAuthor: true, breakdown: true, visualizationStyle: 'curved', showSprints: true },
    },
    {
      id: 6,
      pluginName: 'Issues Timeline',
      x: 27,
      y: 14,
      width: 13,
      height: 14,
      settings: { coloringMode: 'assignee', showSprints: true },
    },
  ];
  await loadDashboard(page, items, ['Sprints', 'Help'], false, sprintsState);
  await page.locator('#dashboardItem2 svg g path').first().waitFor({ state: 'visible', timeout: 20_000 });
  await page.waitForTimeout(800);
  await takeDashboardScreenshot(page, 'DashboardSprintsHelp.png');
});

test('screenshot: Dashboard Visualizations and FileTree', async ({ page }) => {
  test.setTimeout(60_000);
  const items: DashItemConfig[] = [
    {
      id: 1,
      pluginName: 'Changes',
      x: 0,
      y: 0,
      width: 16,
      height: 12,
      settings: { visualizationStyle: 'curved', splitAdditionsDeletions: false, showSprints: false },
    },
    { id: 2, pluginName: 'Builds', x: 16, y: 0, width: 14, height: 12 },
    { id: 3, pluginName: 'Repository Stats', x: 30, y: 0, width: 10, height: 16 },
    {
      id: 4,
      pluginName: 'Issues Timeline',
      x: 0,
      y: 12,
      width: 14,
      height: 12,
      settings: { coloringMode: 'assignee', showSprints: false },
    },
    { id: 5, pluginName: 'Repository Activity', x: 14, y: 12, width: 14, height: 12 },
    {
      id: 6,
      pluginName: 'Issues',
      x: 28,
      y: 16,
      width: 14,
      height: 8,
      settings: { splitIssuesPerAuthor: true, breakdown: true, visualizationStyle: 'curved', showSprints: false },
    },
    { id: 7, pluginName: 'Code Ownership', x: 0, y: 24, width: 20, height: 10 },
    { id: 8, pluginName: 'Burndown', x: 20, y: 24, width: 20, height: 10 },
  ];
  await loadDashboard(page, items, ['Visualizations', 'File Tree']);
  await page.locator('#dashboardItem1 svg g path').first().waitFor({ state: 'visible', timeout: 20_000 });
  await page.getByText('frontend', { exact: true }).waitFor({ state: 'visible', timeout: 15_000 });
  await page.locator('.flex.items-center.gap-1').filter({ hasText: 'frontend' }).first().locator('[class*="element"]').click();
  await page.waitForTimeout(400);
  await page.locator('.flex.items-center.gap-1').filter({ hasText: /^e2e$/ }).first().locator('input[type="checkbox"]').click();
  await page.locator('.flex.items-center.gap-1').filter({ hasText: '.gitlab-ci.yml' }).first().locator('input[type="checkbox"]').click();
  await page.waitForTimeout(800);
  await takeDashboardScreenshot(page, 'DashboardVisualizationsFileTree.png');
});

test('screenshot: Dashboard Parameters and Authors', async ({ page }) => {
  test.setTimeout(90_000);
  const items: DashItemConfig[] = [
    { id: 1, pluginName: 'Collaboration', x: 0, y: 0, width: 14, height: 14 },
    {
      id: 2,
      pluginName: 'Changes',
      x: 14,
      y: 0,
      width: 14,
      height: 14,
      settings: { visualizationStyle: 'curved', splitAdditionsDeletions: false, showSprints: false },
    },
    { id: 3, pluginName: 'Knowledge Radar', x: 28, y: 0, width: 14, height: 18 },
    { id: 4, pluginName: 'Builds', x: 0, y: 14, width: 14, height: 14 },
    {
      id: 5,
      pluginName: 'Issues',
      x: 14,
      y: 14,
      width: 14,
      height: 14,
      settings: { splitIssuesPerAuthor: true, breakdown: true, visualizationStyle: 'curved', showSprints: false },
    },
    { id: 6, pluginName: 'Code Expertise', x: 28, y: 18, width: 14, height: 10 },
  ];
  await loadDashboard(page, items, ['Parameters'], true);
  await page.locator('#dashboardItem2 svg g path').first().waitFor({ state: 'visible', timeout: 20_000 });
  await page.locator('#dashboardItem1').getByText('Simulating graph layout...').waitFor({ state: 'hidden', timeout: 40_000 });
  await page.waitForTimeout(800);
  await takeDashboardScreenshot(page, 'DashboardParametersAuthors.png');
});

test('screenshot: Database Settings', async ({ page }) => {
  test.setTimeout(60_000);
  const items: DashItemConfig[] = [
    {
      id: 1,
      pluginName: 'Changes',
      x: 0,
      y: 0,
      width: 40,
      height: 24,
      settings: { visualizationStyle: 'curved', splitAdditionsDeletions: false, showSprints: false },
    },
  ];
  await loadDashboard(page, items);
  await page.locator('#dashboardItem1 svg g path').first().waitFor({ state: 'visible', timeout: 20_000 });
  await page.evaluate(() => {
    window.dispatchEvent(new CustomEvent('openSettingsTab', { detail: { tab: 'Database' } }));
  });
  await page.locator('#settingsDialog').waitFor({ state: 'visible', timeout: 5_000 });
  await page.waitForTimeout(400);
  await takeDashboardScreenshot(page, 'DatabaseSettings.png');
});

test('screenshot: Visualization Settings', async ({ page }) => {
  test.setTimeout(60_000);
  const items: DashItemConfig[] = [
    {
      id: 1,
      pluginName: 'Issues',
      x: 0,
      y: 0,
      width: 20,
      height: 12,
      settings: { splitIssuesPerAuthor: true, breakdown: true, visualizationStyle: 'curved', showSprints: false },
    },
    {
      id: 2,
      pluginName: 'Changes',
      x: 20,
      y: 0,
      width: 20,
      height: 12,
      settings: { visualizationStyle: 'curved', splitAdditionsDeletions: false, showSprints: false },
    },
    { id: 3, pluginName: 'Burndown', x: 0, y: 12, width: 20, height: 12 },
    {
      id: 4,
      pluginName: 'Issues Timeline',
      x: 20,
      y: 12,
      width: 20,
      height: 12,
      settings: { coloringMode: 'assignee', showSprints: false },
    },
  ];
  await loadDashboard(page, items, ['Visualizations', 'Help']);
  await page.locator('#dashboardItem1 svg g path').first().waitFor({ state: 'visible', timeout: 20_000 });
  await page.evaluate(() => {
    const el = document.getElementById('dashboardItem1_settings');
    if (el) el.style.display = 'block';
  });
  await page.locator('#dashboardItem1_settings').waitFor({ state: 'visible', timeout: 5_000 });
  await page.waitForTimeout(400);
  await takeDashboardScreenshot(page, 'VisualizationSettings.png');
});
