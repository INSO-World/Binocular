import { test, expect } from './fixtures/appFixtures';

test.describe('E2 — App initialization', () => {
  test('E2.1 — fresh load auto-opens setup dialog', async ({ freshApp: page }) => {
    await expect(page.locator('#setupDialog')).toHaveAttribute('open');
  });

  test('E2.2 — seeded localStorage suppresses setup dialog', async ({ initializedApp: page }) => {
    await expect(page.locator('#setupDialog')).not.toHaveAttribute('open');
  });

  test('E2.3 — seeded localStorage: tab bar visible immediately, no loading overlay', async ({ initializedApp: page }) => {
    await expect(page.locator('#tabBarTop')).toBeVisible();
    // loadingLocalDatabaseOverlay renders dialog[open] only when localDatabaseLoadingState === loading
    await expect(page.locator('dialog[open]')).not.toBeVisible();
  });

  test('E2.4 — theme from localStorage applied to root element', async ({ page }) => {
    // Seed binocularDark theme before page load
    await page.addInitScript(() => {
      localStorage.setItem('bino_theme', 'binocularDark');
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
          dashboardItems: [],
          dashboardItemCount: 0,
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
    await page.waitForSelector('#tabBarTop', { state: 'visible' });
    // data-theme is now set on document.documentElement (<html>)
    await expect(page.locator('html')).toHaveAttribute('data-theme', 'binocularDark');
  });
});
