import { test, expect, type Page } from './fixtures/appFixtures';

const rightTab = (page: Page) => page.locator('[class*="tabContentRight"]');

async function openFileTree(page: Page) {
  await page.locator('[id="tab_File Tree"]').click();
  // Mock Data indexes 536 files across backend/, e2e/, and frontend/
  await expect(rightTab(page).getByText('536 Files indexed')).toBeVisible({ timeout: 10_000 });
}

test.describe('E16 — File tree', () => {
  test('E16.1 — tree lists the indexed files; folders expand on click', async ({ mockDataApp: page }) => {
    await openFileTree(page);
    await expect(rightTab(page).getByText('.gitlab-ci.yml')).toBeVisible();

    // Folders start collapsed; clicking the name folds them out
    await expect(rightTab(page).getByText('cypress.config.js')).not.toBeVisible();
    await rightTab(page).getByText('e2e', { exact: true }).click();
    await expect(rightTab(page).getByText('cypress.config.js')).toBeVisible();
    await expect(rightTab(page).getByText('package.json')).toBeVisible();
  });

  test('E16.2 — search filters the tree by path', async ({ mockDataApp: page }) => {
    await openFileTree(page);
    await rightTab(page).getByText('e2e', { exact: true }).click();
    await expect(rightTab(page).getByText('cypress.config.js')).toBeVisible();

    // Search matches against the full path, so 'package' keeps only e2e/package.json + frontend/package.json
    await rightTab(page).getByPlaceholder('Search').fill('package');
    await expect(rightTab(page).getByText('.gitlab-ci.yml')).not.toBeVisible();
    await expect(rightTab(page).getByText('package.json')).toBeVisible();

    await rightTab(page).getByPlaceholder('Search').fill('');
    await expect(rightTab(page).getByText('.gitlab-ci.yml')).toBeVisible();
  });

  test('E16.3 — unchecking all files drains the Changes chart; checking restores it', async ({ mockDataApp: page }) => {
    const yAxis = page.locator('#dashboardItem1 svg g.yAxis');
    // Real per-file commit stats push the y-axis into 3-digit ticks (see E7.2)
    await expect(yAxis).toContainText(/[1-9]\d{2}/, { timeout: 15_000 });

    await openFileTree(page);
    await rightTab(page).getByTitle('Uncheck all files').click();
    await expect(yAxis).not.toContainText(/[1-9]\d{2}/, { timeout: 30_000 });

    // exact: true — substring matching would also hit "Uncheck all files"
    await rightTab(page).getByTitle('Check all files', { exact: true }).click();
    await expect(yAxis).toContainText(/[1-9]\d{2}/, { timeout: 10_000 });
  });
});
