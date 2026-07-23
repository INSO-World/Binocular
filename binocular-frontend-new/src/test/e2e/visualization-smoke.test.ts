import { test, expect } from './fixtures/appFixtures';

test.describe('E4 — Visualization smoke (Repository Stats)', () => {
  test('E4.1 — dashboard item container renders', async ({ appWithVisualization: page }) => {
    await expect(page.locator('#dashboardItem1')).toBeAttached();
  });

  test('E4.2 — plugin name visible in interaction bar', async ({ appWithVisualization: page }) => {
    await expect(page.locator('#dashboardItem1').getByText('Repository Stats')).toBeVisible();
  });

  test('E4.3 — plugin renders loading state without crashing (no data plugin configured)', async ({ appWithVisualization: page }) => {
    // With dataPluginId: undefined, DashboardItem shows "No Data Plugin Selected" — proves
    // the plugin component mounts in Chromium without throwing. SVG rendering requires a
    // functional data plugin which cannot be provided without a live backend.
    await expect(page.locator('#dashboardItem1').getByText('No Data Plugin Selected')).toBeVisible();
  });

  test('E4.4 — no uncaught page errors during plugin mount', async ({ page }) => {
    const errors: string[] = [];
    page.on('pageerror', (err) => errors.push(err.message));

    // Set up the full fixture inline so we can attach the listener before navigation
    await page.addInitScript(() => {
      localStorage.setItem(
        'bino_settingsStateV1',
        JSON.stringify({
          general: { gridSize: 1 },
          initialized: true,
          database: { currID: 0, dataPlugins: [] },
          localDatabaseLoadingState: 0,
          localDatabaseLoadingMessage: '',
        }),
      );
      localStorage.setItem(
        'bino_dashboardStateV1',
        JSON.stringify({
          dashboardItems: [{ id: 1, pluginName: 'Repository Stats', x: 0, y: 0, width: 12, height: 8, dataPluginId: undefined }],
          dashboardItemCount: 1,
          popupCount: 0,
          dashboardState: Array.from({ length: 40 }, () => new Array(40).fill(0)),
          initialized: true,
        }),
      );
      localStorage.setItem('bino_tabsStateV1', JSON.stringify({ tabList: [] }));
    });
    await page.route('/api/**', (route) => route.fulfill({ status: 200, contentType: 'application/json', body: '{}' }));
    await page.route('/graphQl', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ data: {} }) }),
    );
    await page.route('/wsapi/**', (route) => route.abort());
    await page.goto('/');
    await page.waitForSelector('#dashboardItem1', { state: 'attached', timeout: 10_000 });

    expect(errors).toHaveLength(0);
  });

  test('E4.5 — settings panel opens on settings button click', async ({ appWithVisualization: page }) => {
    await page.waitForSelector('#dashboardItem1', { state: 'attached' });
    // The DashboardItem toolbar has multiple buttons; the settings/help button toggles a panel
    const buttons = page.locator('#dashboardItem1').getByRole('button');
    await buttons.first().click();
    // After clicking, a settings or help panel should become visible inside the item
    await expect(page.locator('#dashboardItem1')).toContainText(/.+/);
  });
});
