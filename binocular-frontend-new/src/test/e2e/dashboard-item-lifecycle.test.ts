import { test, expect } from './fixtures/appFixtures';

test.describe('E8 — Dashboard item lifecycle', () => {
  test('E8.1 — drag-placing a visualization onto an occupied area warns and adds nothing', async ({ mockDataApp: page }) => {
    const occupied = page.locator('#dashboardItem1');
    await occupied.waitFor();
    await page.locator('#tab_Visualizations').click();
    const source = page.locator('button:has(img[alt="Builds"])').first();

    // Drop inside the existing item's footprint — the new item centered there stays in
    // bounds but overlaps, so positionDashboardItem() must reject it.
    // Playwright's dragTo() cannot be used here: once dragstart fires, the dashboard's
    // drag-capture zone overlays everything, so the drop target never passes the
    // "receives pointer events" actionability check. Dispatch the HTML5 drag events
    // directly instead, sharing one DataTransfer across dragstart and drop.
    const box = (await occupied.boundingBox())!;
    const dropX = box.x + box.width * 0.75;
    const dropY = box.y + box.height * 0.75;

    await source.evaluate((el) => {
      const dt = new DataTransfer();
      (window as unknown as { __dragDt: DataTransfer }).__dragDt = dt;
      el.dispatchEvent(new DragEvent('dragstart', { bubbles: true, cancelable: true, dataTransfer: dt }));
    });
    // dragstart sets the placeable item in the store; the drag zone is displayed by an
    // effect afterwards — wait for it before delivering the drop
    const dragZone = page.locator('[class*="dragResizeZone"]');
    await dragZone.waitFor({ state: 'visible' });
    await dragZone.evaluate(
      (el, { x, y }) => {
        const dt = (window as unknown as { __dragDt: DataTransfer }).__dragDt;
        for (const type of ['dragenter', 'dragover', 'drop']) {
          el.dispatchEvent(new DragEvent(type, { bubbles: true, cancelable: true, dataTransfer: dt, clientX: x, clientY: y }));
        }
      },
      { x: dropX, y: dropY },
    );

    await expect(page.locator('.alert-warning').first()).toContainText(/overlap/);
    await expect(page.locator('[id^="dashboardItem"]:not([id*="_"])')).toHaveCount(1);
  });

  test('E8.2 — Shift reveals the delete button and clicking it removes the item', async ({ mockDataApp: page }) => {
    const item = page.locator('#dashboardItem1');
    await item.waitFor();
    const deleteButton = item.locator('[class*="deleteButton"]');
    await expect(deleteButton).toBeHidden();
    await page.keyboard.down('Shift');
    await deleteButton.click();
    await page.keyboard.up('Shift');
    await expect(item).toHaveCount(0);
  });

  test('E8.3 — deleted item stays deleted after reload', async ({ mockDataApp: page }) => {
    await page.locator('#dashboardItem1').waitFor();
    await page.keyboard.down('Shift');
    await page.locator('#dashboardItem1 [class*="deleteButton"]').click();
    await page.keyboard.up('Shift');
    await expect(page.locator('#dashboardItem1')).toHaveCount(0);
    await page.reload();
    await page.waitForSelector('#tabBarTop', { state: 'visible' });
    await expect(page.locator('#dashboardItem1')).toHaveCount(0);
  });
});
