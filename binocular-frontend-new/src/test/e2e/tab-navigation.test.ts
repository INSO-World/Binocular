import { test, expect } from './fixtures/appFixtures';

test.describe('E1 — Tab navigation', () => {
  test('E1.1 — top tab bar renders', async ({ initializedApp: page }) => {
    await expect(page.locator('#tabBarTop')).toBeVisible();
  });

  test('E1.2 — Parameters tab handle present', async ({ initializedApp: page }) => {
    await expect(page.locator('#tab_Parameters')).toBeVisible();
  });

  test('E1.3 — Visualizations tab handle present', async ({ initializedApp: page }) => {
    await expect(page.locator('#tab_Visualizations')).toBeVisible();
  });

  test('E1.4 — clicking Visualizations tab shows tab content', async ({ initializedApp: page }) => {
    await page.locator('#tab_Visualizations').click();
    await expect(page.getByText('Visualization Selector')).toBeVisible();
  });

  test('E1.5 — clicking active tab again hides its content', async ({ initializedApp: page }) => {
    // Select the tab first
    await page.locator('#tab_Visualizations').click();
    await expect(page.getByText('Visualization Selector')).toBeVisible();
    // Deselect by clicking again
    await page.locator('#tab_Visualizations').click();
    await expect(page.getByText('Visualization Selector')).not.toBeVisible();
  });

  test('E1.6 — right-side tab handles present', async ({ initializedApp: page }) => {
    await expect(page.locator('#tab_Authors')).toBeVisible();
    await expect(page.locator('[id="tab_File Tree"]')).toBeVisible();
    await expect(page.locator('#tab_Help')).toBeVisible();
  });

  test('E1.7 — clicking Authors opens right panel', async ({ initializedApp: page }) => {
    await page.locator('#tab_Authors').click();
    await expect(page.locator('#tabBarRight')).toBeVisible();
  });
});
