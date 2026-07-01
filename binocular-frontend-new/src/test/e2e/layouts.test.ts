import { test, expect, type Page } from './fixtures/appFixtures';

const topTab = (page: Page) => page.locator('[class*="tabContentTop"]');

async function persistedItems(page: Page) {
  return page.evaluate(() => JSON.parse(localStorage.getItem('bino_dashboardStateV1')!).dashboardItems as { pluginName: string }[]);
}

test.describe('E15 — Dashboard layouts', () => {
  test('E15.1 — saving the dashboard as a custom layout adds a card that survives a reload', async ({ mockDataApp: page }) => {
    await page.locator('#tab_Layouts').click();
    // BUG (app): layoutSelector.tsx renders the Save Dashboard button twice
    await topTab(page).getByRole('button', { name: 'Save Dashboard' }).first().click();
    await page.getByPlaceholder('Layout name').fill('My Layout');
    await page.getByRole('button', { name: 'Save', exact: true }).click();
    await expect(topTab(page).getByRole('button', { name: 'My Layout' })).toBeVisible();

    await page.reload();
    await page.waitForSelector('#tabBarTop', { state: 'visible' });
    // The Layouts tab selection persisted, so its content is already open
    await expect(topTab(page).getByRole('button', { name: 'My Layout' })).toBeVisible();
  });

  test('E15.2 — applying the Default layout replaces the dashboard after confirmation', async ({ mockDataApp: page }) => {
    await page.locator('#tab_Layouts').click();
    await topTab(page).getByRole('button', { name: 'Default', exact: true }).click();

    const menu = page.locator('#contextMenuContent');
    await expect(menu).toContainText('This will remove the current dashboard layout');
    await menu.getByRole('button', { name: 'Yes' }).click();

    // The Default layout holds two items: Changes and Builds, both wired to the
    // default data plugin
    await expect.poll(async () => (await persistedItems(page)).length).toBe(2);
    expect((await persistedItems(page)).map((item) => item.pluginName).sort()).toEqual(['Builds', 'Changes']);
    await expect(page.getByText('Builds (Mock Data #1)')).toBeVisible();
  });

  test('E15.3 — declining the confirmation leaves the dashboard untouched', async ({ mockDataApp: page }) => {
    await page.locator('#tab_Layouts').click();
    await topTab(page).getByRole('button', { name: 'Default', exact: true }).click();

    const menu = page.locator('#contextMenuContent');
    await expect(menu).toContainText('This will remove the current dashboard layout');
    await menu.getByRole('button', { name: 'No' }).click();

    await expect(page.locator('#dashboardItem1')).toBeAttached();
    expect((await persistedItems(page)).map((item) => item.pluginName)).toEqual(['Changes']);
  });
});
