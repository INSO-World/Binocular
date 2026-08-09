import { test, expect } from './fixtures/appFixtures';

// The Authors tab is the default-selected right tab, so its content renders on load.
// Scope queries to the right tab panel to avoid hidden duplicates elsewhere.
const rightTab = (page: import('@playwright/test').Page) => page.locator('[class*="tabContentRight"]');

// 10 total mock users, 4 alias pairs pre-merged → 6 parent rows visible.
// Parents are the users who have a GitLab account (canonical identity).
const PARENT_SIGNATURES = [
  'Vera Sion <vera.sion@example.com>',
  'Cliff Blame <cliff.blame@example.com>',
  'Ori Gina <ori.gina@example.com>',
  'Reed Me <reed.me@example.com>',
  'Bran Ching <bran.ching@example.com>',
  'Cole Myne <cole.myne@example.com>',
];

test.describe('E10 — Authors', () => {
  test('E10.1 — author list loads all mock authors, checked by default', async ({ mockDataApp: page }) => {
    // 6 parent rows render with a checkbox each; 4 alias children have no checkbox
    await expect(rightTab(page).locator('input.checkbox')).toHaveCount(6, { timeout: 10_000 });
    for (const signature of PARENT_SIGNATURES) {
      await expect(rightTab(page).getByText(signature)).toBeVisible();
    }
    await expect(rightTab(page).locator('input.checkbox:checked')).toHaveCount(6);
  });

  test('E10.2 — unchecking all authors empties the chart; checking all restores it', async ({ mockDataApp: page }) => {
    const yAxis = page.locator('#dashboardItem1 svg g.yAxis');
    // Real commit data pushes the y-axis into 3-digit ticks (see E7.2)
    await expect(yAxis).toContainText(/[1-9]\d{2}/, { timeout: 15_000 });

    await page.getByTitle('Uncheck all authors').click();
    await expect(yAxis).not.toContainText(/[1-9]\d{2}/, { timeout: 10_000 });

    // exact: true — substring matching would also hit "Uncheck all authors"
    await page.getByTitle('Check all authors', { exact: true }).click();
    await expect(yAxis).toContainText(/[1-9]\d{2}/, { timeout: 10_000 });
  });

  test('E10.3 — dragging one author onto another merges them; merge survives reload', async ({ mockDataApp: page }) => {
    const checkboxes = rightTab(page).locator('input.checkbox');
    await expect(checkboxes).toHaveCount(6, { timeout: 10_000 });

    // Same manual HTML5 DnD dispatch as E8.1 — the merge transfers the author id
    // through the DataTransfer, so dragstart and drop must share one instance
    const source = page.locator('div[draggable="true"]').filter({ hasText: 'Reed Me <reed.me@example.com>' });
    const target = page.locator('div[draggable="true"]').filter({ hasText: 'Vera Sion <vera.sion@example.com>' });
    await source.evaluate((el) => {
      const dt = new DataTransfer();
      (window as unknown as { __dragDt: DataTransfer }).__dragDt = dt;
      el.dispatchEvent(new DragEvent('dragstart', { bubbles: true, cancelable: true, dataTransfer: dt }));
    });
    await target.evaluate((el) => {
      const dt = (window as unknown as { __dragDt: DataTransfer }).__dragDt;
      el.dispatchEvent(new DragEvent('dragover', { bubbles: true, cancelable: true, dataTransfer: dt }));
      el.dispatchEvent(new DragEvent('drop', { bubbles: true, cancelable: true, dataTransfer: dt }));
    });

    // Reed Me is now a child of Vera Sion — child rows render without a checkbox
    await expect(checkboxes).toHaveCount(5);
    await expect(rightTab(page).getByText('Reed Me <reed.me@example.com>')).toBeVisible();

    await page.reload();
    await page.waitForSelector('#tabBarTop', { state: 'visible' });
    await expect(rightTab(page).locator('input.checkbox')).toHaveCount(5, { timeout: 10_000 });
  });

  test('E10.4 — "move to other" via context menu moves the author to the Other group', async ({ mockDataApp: page }) => {
    const checkboxes = rightTab(page).locator('input.checkbox');
    await expect(checkboxes).toHaveCount(6, { timeout: 10_000 });
    await expect(rightTab(page).getByText('No Authors in Other')).toBeVisible();

    await page.locator('div[draggable="true"]').filter({ hasText: 'Vera Sion <vera.sion@example.com>' }).click({ button: 'right' });
    await page.locator('#contextMenuContent').getByText('move to other').click();

    // Vera Sion left the main list (no checkbox anymore) and shows up under Other
    await expect(checkboxes).toHaveCount(5);
    await expect(rightTab(page).getByText('No Authors in Other')).not.toBeVisible();
    await expect(rightTab(page).getByText('Vera Sion <vera.sion@example.com>')).toBeVisible();
  });
});
