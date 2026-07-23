import { test, expect } from './fixtures/appFixtures';

const topTab = (page: import('@playwright/test').Page) => page.locator('[class*="tabContentTop"]');
const fromRow = (page: import('@playwright/test').Page) => topTab(page).locator('tr', { hasText: 'From:' });

const monthOf = (value: string) => new Date(value).getMonth();
const yearOf = (value: string) => new Date(value).getFullYear();

test.describe('E12 — Date range quick buttons', () => {
  test('E12.1 — +M / -M shift the From date by one month', async ({ mockDataApp: page }) => {
    const input = fromRow(page).locator('input');
    const before = await input.inputValue();

    await fromRow(page).getByRole('button', { name: '+M', exact: true }).click();
    let after = await input.inputValue();
    expect(monthOf(after)).toBe((monthOf(before) + 1) % 12);

    await fromRow(page).getByRole('button', { name: '-M', exact: true }).click();
    after = await input.inputValue();
    expect(monthOf(after)).toBe(monthOf(before));
  });

  test('E12.2 — holding Shift flips the buttons to ±Y and shifts by a year', async ({ mockDataApp: page }) => {
    const input = fromRow(page).locator('input');
    const before = await input.inputValue();

    await page.keyboard.down('Shift');
    const plusYear = fromRow(page).getByRole('button', { name: '+Y', exact: true });
    await expect(plusYear).toBeVisible();
    await plusYear.click();
    await page.keyboard.up('Shift');

    expect(yearOf(await input.inputValue())).toBe(yearOf(before) + 1);
    // Releasing Shift restores the month labels
    await expect(fromRow(page).getByRole('button', { name: '+M', exact: true })).toBeVisible();
  });

  test('E12.3 — T sets the date to today', async ({ mockDataApp: page }) => {
    // The From row starts one year in the past, so jumping to today is observable
    const input = fromRow(page).locator('input');
    const before = await input.inputValue();

    await fromRow(page).getByTitle('set date to today').click();
    const after = await input.inputValue();

    expect(after).not.toBe(before);
    // The app writes the current UTC timestamp
    expect(after.slice(0, 10)).toBe(new Date().toISOString().slice(0, 10));
  });
});
