import { test, expect, mockBackendRoutes } from './fixtures/appFixtures';

// Reads the persisted dashboard state — the reducers write through to localStorage on
// every move/resize, so this is the authoritative post-interaction position
async function persistedItems(page: import('@playwright/test').Page) {
  return page.evaluate(
    () => JSON.parse(localStorage.getItem('bino_dashboardStateV1')!).dashboardItems as { x: number; y: number; width: number }[],
  );
}

// The drag handlers mix real layout pixels (indicator seeding) with the dashboard's
// cellSize state (delta-to-cell conversion). The state follows layout changes through a
// ResizeObserver, whose callback still lands a beat after the opening tab panels narrow
// the layout — wait until the state, mirrored verbatim into each td's inline width,
// agrees with the live dashboard width (20 visible columns on the medium grid).
async function appCellSize(page: import('@playwright/test').Page) {
  const measure = () =>
    page.evaluate(() => {
      const table = document.querySelector<HTMLElement>('[class*="dashboardBackground"]')!;
      const cell = parseFloat(table.querySelector('td')!.style.width);
      const dashboard = table.parentElement!.parentElement!;
      return { cell, drift: Math.abs(cell - dashboard.offsetWidth / 20) };
    });
  await expect.poll(async () => (await measure()).drift).toBeLessThan(0.5);
  return (await measure()).cell;
}

test.describe('E11 — Dashboard item tools', () => {
  test('E11.1 — dragging the header moves the item to a new grid position', async ({ mockDataApp: page }) => {
    await page.locator('#dashboardItem1').waitFor();
    const cell = await appCellSize(page);
    // Synthetic events because the move logic consumes mousemove.movementX/Y, which
    // CDP-synthesized input does not populate reliably. mousedown on the header arms
    // drag mode synchronously (ref-based, no re-render), mouseover on the capture zone
    // seeds the indicator at the item position, mousemove carries the delta.
    await page.evaluate((cell) => {
      const bar = document.querySelector('#dashboardItem1 [class*="dashboardItemInteractionBar"]')!;
      bar.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, cancelable: true }));
      const zone = document.querySelector('[class*="dragResizeZone"]')!;
      zone.dispatchEvent(new MouseEvent('mouseover', { bubbles: true }));
      zone.dispatchEvent(new MouseEvent('mousemove', { bubbles: true, movementX: Math.round(cell * 2), movementY: 0 }));
      zone.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
    }, cell);
    // 2 visible cells right = 4 underlying grid units (medium grid multiplier is 2)
    await expect.poll(async () => (await persistedItems(page))[0].x).toBe(4);
    expect((await persistedItems(page))[0].y).toBe(0);
  });

  test('E11.2 — dragging the right resize bar widens the item', async ({ mockDataApp: page }) => {
    await page.locator('#dashboardItem1').waitFor();
    const cell = await appCellSize(page);
    await page.evaluate((cell) => {
      const handle = document.querySelector('#dashboardItem1 [class*="dashboardItemResizeBarRight"]')!;
      handle.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, cancelable: true }));
      const zone = document.querySelector('[class*="dragResizeZone"]')!;
      zone.dispatchEvent(new MouseEvent('mouseover', { bubbles: true }));
      zone.dispatchEvent(new MouseEvent('mousemove', { bubbles: true, movementX: Math.round(cell * 2), movementY: 0 }));
      zone.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
    }, cell);
    // width grows from 12 to 16 underlying units; position stays put
    await expect.poll(async () => (await persistedItems(page))[0].width).toBe(16);
    expect((await persistedItems(page))[0].x).toBe(0);
  });

  test('E11.6 — cellSize follows a dashboard resize without the visualization debounce', async ({ mockDataApp: page }) => {
    await page.locator('#dashboardItem1').waitFor();
    await appCellSize(page);

    await page.setViewportSize({ width: 900, height: 720 });
    // Fixed wall-clock wait on purpose: the visualization-resize path is debounced by
    // 100ms, and cellSize must NOT ride on it — at 50ms the ResizeObserver has fired
    // but the debounce has not, so a cellSize still coupled to it would be ~19px stale
    await page.waitForTimeout(50);

    const drift = await page.evaluate(() => {
      const table = document.querySelector<HTMLElement>('[class*="dashboardBackground"]')!;
      const cell = parseFloat(table.querySelector('td')!.style.width);
      return Math.abs(cell - table.parentElement!.parentElement!.offsetWidth / 20);
    });
    expect(drift).toBeLessThan(0.5);
  });

  test('E11.3 — item settings switch the data plugin', async ({ page }) => {
    // Custom seed: two Mock Data plugins so there is something to switch between
    await page.addInitScript(() => {
      localStorage.setItem(
        'bino_settingsStateV1',
        JSON.stringify({
          general: { gridSize: 1 },
          initialized: true,
          database: {
            currID: 2,
            defaultDataPluginItemId: 1,
            dataPlugins: [
              { id: 1, name: 'Mock Data', color: '#66c2a525', isDefault: true, parameters: {} },
              { id: 2, name: 'Mock Data', color: '#fc8d6225', isDefault: false, parameters: {} },
            ],
          },
          localDatabaseLoadingState: 0,
          localDatabaseLoadingMessage: '',
        }),
      );
      const grid = Array.from({ length: 40 }, () => new Array(40).fill(0));
      for (let y = 0; y < 8; y++) for (let x = 0; x < 12; x++) grid[y][x] = 1;
      localStorage.setItem(
        'bino_dashboardStateV1',
        JSON.stringify({
          dashboardItems: [{ id: 1, pluginName: 'Changes', x: 0, y: 0, width: 12, height: 8, dataPluginId: 1 }],
          dashboardItemCount: 1,
          popupCount: 0,
          dashboardState: grid,
          initialized: true,
        }),
      );
      localStorage.setItem('bino_tabsStateV1', JSON.stringify({ tabList: [] }));
    });
    await mockBackendRoutes(page);
    await page.goto('/');
    await page.waitForSelector('#tabBarTop', { state: 'visible' });

    const item = page.locator('#dashboardItem1');
    await expect(item.getByText('(Mock Data #1)')).toBeVisible();

    await item.locator('[class*="settingsButton"]').click();
    const settingsPanel = page.locator('#dashboardItem1_settings');
    await expect(settingsPanel).toBeVisible();
    // The data plugin quick-select is a custom dropdown, not a native <select>:
    // a role="button" trigger showing the current plugin, then a list of buttons
    await settingsPanel.getByRole('button', { name: 'Mock Data #1 (default)' }).click();
    await settingsPanel.getByRole('button', { name: 'Mock Data #2' }).click();

    await expect(item.getByText('(Mock Data #2)')).toBeVisible();
  });

  test('E11.4 — item export button downloads the chart as SVG', async ({ mockDataApp: page }) => {
    await page.locator('#dashboardItem1 svg g.areas path').first().waitFor({ timeout: 15_000 });
    await page.locator('#dashboardItem1 [class*="exportButton"]').click();

    const dialog = page.locator('#exportDialog');
    await expect(dialog).toHaveAttribute('open');
    await expect(dialog.getByText('Image Export')).toBeVisible();

    const download = page.waitForEvent('download');
    await dialog.getByRole('button', { name: 'Export SVG' }).click();
    expect((await download).suggestedFilename()).toContain('ChangesExport');
  });

  test('E11.5 — popout shows the placeholder and closing it restores the chart', async ({ mockDataApp: page }) => {
    const item = page.locator('#dashboardItem1');
    await item.locator('svg g.areas path').first().waitFor({ timeout: 15_000 });

    const popup = page.waitForEvent('popup');
    await item.locator('[class*="popoutButton"]').click();
    await popup;
    await expect(item.getByText('Popped Out!')).toBeVisible();

    await item.getByRole('button', { name: 'Close Popout' }).click();
    await expect(item.getByText('Popped Out!')).not.toBeVisible();
    await expect(item.locator('svg g.areas path').first()).toBeVisible({ timeout: 15_000 });
  });
});
