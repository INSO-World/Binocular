import { test, expect } from './fixtures/appFixtures';

// Scope for the global Parameters widgets: the per-item settings panel renders the same
// "Granularity:" / "Exclude Merge Commits:" / date inputs inside a hidden sub-window, so
// unscoped getByLabel() would resolve to multiple elements.
const topTabContent = (page: import('@playwright/test').Page) => page.locator('[class*="tabContentTop"]');

test.describe('E7 — Mock Data visualization', () => {
  test('E7.1 — status bar shows the configured Mock Data plugin', async ({ mockDataApp: page }) => {
    await expect(page.locator('[class*="dataPluginElement"]').getByText('Mock Data #1')).toBeVisible();
    await expect(page.getByText('No DataPlugins Configured')).not.toBeVisible();
  });

  test('E7.2 — Changes chart renders stacked-area series from mock commits', async ({ mockDataApp: page }) => {
    const item = page.locator('#dashboardItem1');
    await expect(item.getByText('Changes')).toBeVisible();
    // Series paths appear once authors and commits arrive through the data plugin
    await expect(item.locator('svg g.areas path').first()).toBeVisible({ timeout: 15_000 });
    // A 3-digit y-axis tick proves real commit stats were plotted — with no data the
    // converter only emits ±0.001 placeholders and the axis never reaches 100
    await expect(item.locator('svg g.yAxis')).toContainText(/[1-9]\d{2}/, { timeout: 15_000 });
  });

  test('E7.3 — changing the From date refetches commits with the new range', async ({ mockDataApp: page }) => {
    await page.locator('#dashboardItem1 svg g.areas path').first().waitFor({ timeout: 15_000 });
    // The Mock Data plugin logs "Getting Commits from <from> to <to>" on every fetch,
    // which proves the new range travelled parameters → chart → saga → data plugin
    const refetch = page.waitForEvent('console', {
      predicate: (msg) => msg.text().includes('Getting Commits from 2023-01-01T00:00'),
      timeout: 10_000,
    });
    await topTabContent(page).locator('input[type="datetime-local"]').first().fill('2023-01-01T00:00');
    await refetch;
  });

  test('E7.4 — switching granularity re-buckets the x-axis', async ({ mockDataApp: page }) => {
    const xAxis = page.locator('#dashboardItem1 svg g.xAxis');
    await page.locator('#dashboardItem1 svg g.areas path').first().waitFor({ timeout: 15_000 });
    const before = await xAxis.textContent();
    await topTabContent(page).getByLabel('Granularity:').selectOption('years');
    await expect.poll(() => xAxis.textContent(), { timeout: 10_000 }).not.toBe(before);
  });

  test('E7.5 — granularity and merge-commit toggle persist across reload', async ({ mockDataApp: page }) => {
    await topTabContent(page).getByLabel('Granularity:').selectOption('months');
    await topTabContent(page).getByLabel('Exclude Merge Commits:').check();
    await page.reload();
    await page.waitForSelector('#tabBarTop', { state: 'visible' });
    await expect(topTabContent(page).getByLabel('Granularity:')).toHaveValue('months');
    await expect(topTabContent(page).getByLabel('Exclude Merge Commits:')).toBeChecked();
  });
});
