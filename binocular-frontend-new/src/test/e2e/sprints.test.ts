import { test, expect, type Page } from './fixtures/appFixtures';

async function addSprint(page: Page, name: string) {
  await page.locator('#tab_Sprints').click();
  await page.getByRole('button', { name: 'Add Sprint' }).click();
  const dialog = page.locator('#addSprintDialog');
  await expect(dialog).toHaveAttribute('open');
  // From/To default to a valid 14-day range, only the name is required
  await dialog.getByPlaceholder('Type here').fill(name);
  await dialog.getByRole('button', { name: 'Add', exact: true }).click();
  await expect(dialog).not.toHaveAttribute('open');
}

test.describe('E13 — Sprints', () => {
  test('E13.1 — adding a sprint shows it on the timeline and survives a reload', async ({ mockDataApp: page }) => {
    await addSprint(page, 'Sprint Alpha');

    // Success toast appears and can be dismissed by clicking it
    const toast = page.locator('.alert-success', { hasText: 'Added Sprint: Sprint Alpha' });
    await expect(toast).toBeVisible();
    await toast.click();
    await expect(toast).not.toBeVisible();

    await expect(page.locator('[class*="tabContentTop"]').getByText('Sprint Alpha')).toBeVisible();

    await page.reload();
    await page.waitForSelector('#tabBarTop', { state: 'visible' });
    // The tab selection persists too, so Sprints is already the open tab after the
    // reload — clicking #tab_Sprints again would toggle the panel closed
    await expect(page.locator('[class*="tabContentTop"]').getByText('Sprint Alpha')).toBeVisible();
  });

  test('E13.2 — editing a sprint via the context menu renames it', async ({ mockDataApp: page }) => {
    await addSprint(page, 'Sprint Alpha');
    const sprintCard = page.locator('[class*="tabContentTop"]').getByText('Sprint Alpha');
    await sprintCard.click({ button: 'right' });
    await page.locator('#contextMenuContent').getByText('edit', { exact: true }).click();

    const dialog = page.locator('#addSprintDialog');
    await expect(dialog.getByText('Edit Sprint')).toBeVisible();
    await expect(dialog.getByPlaceholder('Type here')).toHaveValue('Sprint Alpha');
    await dialog.getByPlaceholder('Type here').fill('Sprint Beta');
    await dialog.getByRole('button', { name: 'Save', exact: true }).click();

    await expect(page.locator('[class*="tabContentTop"]').getByText('Sprint Beta')).toBeVisible();
    await expect(page.locator('[class*="tabContentTop"]').getByText('Sprint Alpha')).not.toBeVisible();
  });

  test('E13.3 — deleting a sprint via the context menu removes it', async ({ mockDataApp: page }) => {
    await addSprint(page, 'Sprint Alpha');
    const sprintCard = page.locator('[class*="tabContentTop"]').getByText('Sprint Alpha');
    await sprintCard.click({ button: 'right' });
    await page.locator('#contextMenuContent').getByText('delete', { exact: true }).click();

    await expect(page.locator('[class*="tabContentTop"]').getByText('Sprint Alpha')).not.toBeVisible();
  });
});
