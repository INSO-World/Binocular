import { test, expect, type Page } from './fixtures/appFixtures';

const rightTab = (page: Page) => page.locator('[class*="tabContentRight"]');

test.describe('E17 — Help tab', () => {
  test('E17.1 — general help sections and per-component help are reachable', async ({ mockDataApp: page }) => {
    await page.locator('#tab_Help').click();

    // General section renders its collapsible topics
    await expect(rightTab(page).getByText('Dashboard', { exact: true })).toBeVisible();
    await expect(rightTab(page).getByText('Zoom', { exact: true })).toBeVisible();

    // Components section lists every visualization plugin; selecting one opens its
    // dedicated help with a back button
    const changesHelp = rightTab(page).getByRole('button', { name: 'Changes', exact: true });
    await expect(changesHelp).toBeVisible();
    await changesHelp.click();
    const backButton = rightTab(page).getByRole('button', { name: 'back' });
    await expect(backButton).toBeVisible();
    await backButton.click();
    await expect(rightTab(page).getByRole('button', { name: 'Changes', exact: true })).toBeVisible();
  });
});
