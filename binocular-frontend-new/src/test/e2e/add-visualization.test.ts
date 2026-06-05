import { test, expect } from './fixtures/appFixtures';

test.describe('E5 — Add visualization via click', () => {
  test('E5.1 — clicking Visualizations tab shows recommended plugin buttons', async ({ initializedApp: page }) => {
    await page.locator('#tab_Visualizations').click();
    // Two matches exist: one in the visible selector panel, one in the always-present
    // #visualizationOverviewPositionController dialog. Use .first() to target the panel.
    await expect(page.locator('button:has(img[alt="Builds"])').first()).toBeVisible();
  });

  test('E5.2 — clicking a viz button places item on dashboard', async ({ initializedApp: page }) => {
    await page.locator('#tab_Visualizations').click();
    await page.locator('button:has(img[alt="Builds"])').first().click();
    // Each DashboardItem also renders #dashboardItemN_settings and #dashboardItemN_help sub-panels.
    // Exclude those by filtering out IDs containing "_".
    await expect(page.locator('[id^="dashboardItem"]:not([id*="_"])')).toBeAttached({ timeout: 5_000 });
  });

  test('E5.3 — dashboard item shows the clicked plugin name', async ({ initializedApp: page }) => {
    await page.locator('#tab_Visualizations').click();
    await page.locator('button:has(img[alt="Changes"])').first().click();
    await page.waitForSelector('[id^="dashboardItem"]:not([id*="_"])', { timeout: 5_000 });
    await expect(page.locator('[id^="dashboardItem"]:not([id*="_"])').getByText('Changes')).toBeVisible();
  });

  test('E5.4 — clicking two different viz buttons adds two items', async ({ initializedApp: page }) => {
    await page.locator('#tab_Visualizations').click();
    await page.locator('button:has(img[alt="Builds"])').first().click();
    await page.waitForSelector('[id^="dashboardItem"]:not([id*="_"])', { timeout: 5_000 });

    await page.locator('button:has(img[alt="Issues"])').first().click();
    await expect(page.locator('[id^="dashboardItem"]:not([id*="_"])')).toHaveCount(2, { timeout: 5_000 });
  });
});
