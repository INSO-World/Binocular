import { test, expect } from './fixtures/appFixtures';

test.describe('E9 — Setup wizard completion', () => {
  test('E9.1 — completing the wizard with Mock Data initializes the app', async ({ freshApp: page }) => {
    const dialog = page.locator('#setupDialog');
    await expect(dialog).toHaveAttribute('open');

    // Start → Database
    await dialog.getByRole('button', { name: 'Next' }).click();
    await expect(dialog.getByText('Setup Data Connection')).toBeVisible();
    await expect(dialog.getByText('No Database Connections configured!')).toBeVisible();

    // Connect the Mock Data plugin — first plugin becomes the default
    await dialog.locator('.card', { hasText: 'Mock Data' }).getByRole('button', { name: 'Add' }).click();
    await expect(dialog.getByText('Mock Data #1')).toBeVisible();
    await expect(dialog.getByText('Default', { exact: true })).toBeVisible();

    // Database → Authors (optional step) → Dashboard
    await dialog.getByRole('button', { name: 'Next' }).click();
    await expect(dialog.getByText('Manage Authors')).toBeVisible();
    await dialog.getByRole('button', { name: 'Next' }).click();

    // Pick the first recommended dashboard — its Select button disables once chosen
    const select = dialog.getByRole('button', { name: 'Select' }).first();
    await select.click();
    await expect(select).toBeDisabled();

    // Dashboard → Summary → Save (the app reloads itself after saving)
    await dialog.getByRole('button', { name: 'Next' }).click();
    await expect(dialog.getByRole('heading', { name: 'Summary' })).toBeVisible();
    const reloaded = page.waitForEvent('load');
    await dialog.getByRole('button', { name: 'Save' }).click();
    await reloaded;

    // After the reload the wizard stays closed, the plugin is connected and the
    // selected dashboard layout is on screen
    await page.waitForSelector('#tabBarTop', { state: 'visible' });
    await expect(page.locator('#setupDialog')).not.toHaveAttribute('open');
    await expect(page.locator('[class*="dataPluginElement"]').getByText('Mock Data #1')).toBeVisible();
    await expect(page.locator('[id^="dashboardItem"]:not([id*="_"])').first()).toBeAttached({ timeout: 10_000 });
  });

  test('E9.2 — database page warns when the default backend is unreachable; Retry recovers', async ({ freshApp: page }) => {
    // Later-registered routes win, overriding the fixture's 200 stub
    await page.route('/graphQl', (route) => route.fulfill({ status: 503, body: '' }));

    const dialog = page.locator('#setupDialog');
    await dialog.getByRole('button', { name: 'Next' }).click();
    await expect(dialog.getByText('The default binocular backend was not found')).toBeVisible();

    // Backend comes online — Retry flips the warning to the connect offer
    await page.route('/graphQl', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ data: {} }) }),
    );
    await dialog.getByRole('button', { name: 'Retry' }).click();
    await expect(dialog.getByText('It seems like you are using the default binocular backend.')).toBeVisible();
    await expect(dialog.getByRole('button', { name: 'Connect!' })).toBeVisible();
  });

  test('E9.3 — cancelling leaves the app uninitialized; wizard reopens on reload', async ({ freshApp: page }) => {
    await page.locator('#setupDialog').getByRole('button', { name: 'Cancel' }).click();
    await expect(page.locator('#setupDialog')).not.toHaveAttribute('open');
    await page.reload();
    await page.waitForSelector('#setupDialog[open]', { state: 'attached' });
  });
});
