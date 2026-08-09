import { test, expect } from './fixtures/appFixtures';

test.describe('E3 — Header button wiring', () => {
  test('E3.1 — Settings button visible in top bar', async ({ initializedApp: page }) => {
    // TabControllerButton now uses aria-label={name} on the <button> element
    await expect(page.locator('button[aria-label="Settings"]')).toBeVisible();
  });

  test('E3.2 — clicking Settings opens #settingsDialog', async ({ initializedApp: page }) => {
    await page.locator('button[aria-label="Settings"]').click();
    await expect(page.locator('#settingsDialog')).toHaveAttribute('open');
  });

  test('E3.3 — Export button visible in top bar', async ({ initializedApp: page }) => {
    await expect(page.locator('button[aria-label="Export"]')).toBeVisible();
  });

  test('E3.4 — clicking Export opens #exportDialog', async ({ initializedApp: page }) => {
    await page.locator('button[aria-label="Export"]').click();
    await expect(page.locator('#exportDialog')).toHaveAttribute('open');
  });

  test('E3.5 — theme switch renders in top bar', async ({ initializedApp: page }) => {
    // TabControllerButtonThemeSwitch renders a <label class="swap"> wrapping a hidden checkbox
    await expect(page.locator('label:has(input.theme-controller)')).toBeAttached();
  });

  test('E3.6 — clicking theme switch changes data-theme on root', async ({ initializedApp: page }) => {
    // data-theme is now set on document.documentElement (<html>)
    const html = page.locator('html');
    const before = await html.getAttribute('data-theme');
    const expectedAfter = before === 'binocularLight' ? 'binocularDark' : 'binocularLight';

    // DaisyUI swap hides the checkbox visually; click the wrapping label instead
    await page.locator('label:has(input.theme-controller)').first().click();

    // toHaveAttribute auto-waits for React to re-render after setTheme() fires
    await expect(html).toHaveAttribute('data-theme', expectedAfter);
  });
});
