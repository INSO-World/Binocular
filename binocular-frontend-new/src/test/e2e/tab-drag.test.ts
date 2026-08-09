import { test, expect } from './fixtures/appFixtures';

test.describe('E6 — Tab drag to new alignment', () => {
  test('E6.1 — Parameters tab starts in the top bar', async ({ initializedApp: page }) => {
    await expect(page.locator('#tabBarTop').locator('[id="tab_Parameters"]')).toBeAttached();
  });

  test('E6.2 — dragging Parameters to the right bar moves it', async ({ initializedApp: page }) => {
    // Playwright's dragTo() fires dragstart → dragover → drop → dragend using the HTML5 DnD API
    await page.locator('[id="tab_Parameters"]').dragTo(page.locator('#tabBarRight'));

    await expect(page.locator('#tabBarRight').locator('[id="tab_Parameters"]')).toBeAttached({ timeout: 5_000 });
    await expect(page.locator('#tabBarTop').locator('[id="tab_Parameters"]')).not.toBeAttached();
  });

  test('E6.3 — dragging Authors from the right bar to the top bar moves it', async ({ initializedApp: page }) => {
    await page.locator('[id="tab_Authors"]').dragTo(page.locator('#tabBarTop'));

    await expect(page.locator('#tabBarTop').locator('[id="tab_Authors"]')).toBeAttached({ timeout: 5_000 });
    await expect(page.locator('#tabBarRight').locator('[id="tab_Authors"]')).not.toBeAttached();
  });

  test('E6.4 — drop zones disappear after tab is dropped', async ({ initializedApp: page }) => {
    // Drag Parameters to right bar — TabDropHint "Drop Here" zones should be gone after drop
    await page.locator('[id="tab_Parameters"]').dragTo(page.locator('#tabBarRight'));
    await expect(page.getByText('Drop Here')).not.toBeVisible();
  });
});
