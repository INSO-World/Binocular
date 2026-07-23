import { test, expect, type Page } from './fixtures/appFixtures';

// The global settings button in the tab controller corner (aria-label "Settings").
// exact: true keeps the lowercase-titled authors settings button out of the match.
async function openSettings(page: Page) {
  await page.getByRole('button', { name: 'Settings', exact: true }).click();
  const dialog = page.locator('#settingsDialog');
  await expect(dialog).toBeVisible();
  return dialog;
}

test.describe('E14 — Settings & storage', () => {
  test('E14.1 — grid size setting changes the dashboard grid density and persists', async ({ mockDataApp: page }) => {
    const rows = page.locator('[class*="dashboardBackground"] tr');
    // medium (default) renders a 20×20 visible grid
    await expect(rows).toHaveCount(20);

    const dialog = await openSettings(page);
    // SettingsGeneralGridSize: small=0, medium=1, large=2
    await dialog.locator('select').selectOption('0');
    await page.keyboard.press('Escape');
    await expect(rows).toHaveCount(40);
    expect(await page.evaluate(() => JSON.parse(localStorage.getItem('bino_settingsStateV1')!).general.gridSize)).toBe(0);

    await openSettings(page);
    await dialog.locator('select').selectOption('2');
    await page.keyboard.press('Escape');
    await expect(rows).toHaveCount(10);
  });

  test('E14.2 — export → clear → import round-trips the whole configuration', async ({ mockDataApp: page }) => {
    const dialog = await openSettings(page);

    const downloadPromise = page.waitForEvent('download');
    await dialog.getByRole('button', { name: 'Export Storage' }).click();
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toBe('BinocularStateV1.json');
    const exportPath = await download.path();

    // Clear everything but keep the page alive — the Reload Page toggle is on by default
    await dialog.getByRole('button', { name: 'Clear Storage' }).click();
    const clearDialog = page.locator('#clearStorageDialog');
    await expect(clearDialog).toBeVisible();
    await clearDialog.locator('label', { hasText: 'Reload Page' }).locator('input').uncheck();
    await clearDialog.getByRole('button', { name: 'Clear Selected' }).click();

    // The plugin removal reaches the status bar, the cleared keys are gone, and the
    // deferred reload is offered as a button
    await expect(page.locator('[class*="statusLeft"]')).toHaveText('No DataPlugins Configured');
    await expect(dialog.getByRole('button', { name: 'Reload Page' })).toBeVisible();
    expect(await page.evaluate(() => localStorage.getItem('bino_dashboardStateV1'))).toBeNull();

    // Import the export back in
    await page.setInputFiles('#importStorageFilePicker', exportPath);
    await dialog.getByRole('button', { name: 'Import', exact: true }).click();
    await expect(dialog.getByText('Storage loaded successfully!')).toBeVisible();
    await expect
      .poll(() => page.evaluate(() => JSON.parse(localStorage.getItem('bino_settingsStateV1')!).database.dataPlugins.length))
      .toBe(1);
    await expect.poll(() => page.evaluate(() => JSON.parse(localStorage.getItem('bino_dashboardStateV1')!).dashboardItems.length)).toBe(1);
  });

  test('E14.3 — deleting the data plugin from database settings empties the status bar', async ({ mockDataApp: page }) => {
    await expect(page.locator('[class*="statusLeft"]')).toContainText('Mock Data #1');

    const dialog = await openSettings(page);
    await dialog.getByRole('tab', { name: 'Database' }).click();
    await expect(dialog.getByText('Configured Database Connections:')).toBeVisible();
    await dialog.getByRole('button', { name: 'Delete' }).click();

    await expect(page.locator('[class*="statusLeft"]')).toHaveText('No DataPlugins Configured');
  });
});
