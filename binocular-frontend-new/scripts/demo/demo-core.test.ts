// Records the "core" scene (setup wizard + tabs tour) from demo-core-v3.md; offline via Mock Data. Run: npm run demo:record; render via render-demo-videos.mjs.
// Parameters/Authors tabs are open by default, so this file interacts with their contents directly instead of clicking their tab handles.

import { test } from '@playwright/test';
import {
  installCursorOverlay,
  humanClickLocator,
  humanFill,
  beat,
  showTitleCard,
  mockBackendRoutes,
  settingsControl,
  humanHoverLocator,
  humanMove,
  humanDrag,
} from './demoHelpers.ts';

test.describe('Demo video — core', () => {
  test('Binocular core', async ({ page }) => {
    test.setTimeout(5 * 60_000);

    await page.addInitScript(installCursorOverlay);
    await mockBackendRoutes(page);
    await page.goto('/');

    await showTitleCard(page, 'Binocular');

    // ─── Setup wizard — connect Mock Data, pick a recommended dashboard ────────────────────
    const dialog = page.locator('#setupDialog');
    await dialog.waitFor({ state: 'attached' });
    await beat(page, 1000);

    await humanClickLocator(page, dialog.getByRole('button', { name: 'Next' })); // Start -> Database
    await beat(page, 800);

    await humanClickLocator(page, dialog.locator('.card', { hasText: 'Mock Data' }).getByRole('button', { name: 'Add' }));
    await beat(page, 1000);

    await humanClickLocator(page, dialog.getByRole('button', { name: 'Next' })); // Database -> Authors
    await beat(page, 800);
    await humanClickLocator(page, dialog.getByRole('button', { name: 'Next' })); // Authors -> Dashboard
    await beat(page, 1000);

    await humanClickLocator(page, dialog.getByRole('button', { name: 'Select' }).first());
    await beat(page, 800);

    await humanClickLocator(page, dialog.getByRole('button', { name: 'Next' })); // Dashboard -> Summary
    await beat(page, 1200);

    const reloaded = page.waitForEvent('load');
    await humanClickLocator(page, dialog.getByRole('button', { name: 'Save' }));
    await reloaded;
    // Shown right after 'load' (not after dashboard-ready) so its hold time covers the blank mounting gap.
    await showTitleCard(page, 'Around the Dashboard', 2500);
    await page.waitForSelector('#tabBarTop', { state: 'visible' });
    await page.waitForSelector('[id^="dashboardItem"]:not([id*="_"])', { state: 'attached', timeout: 10_000 });
    await beat(page, 1500);

    // ─── Tabs tour ──────────────────────────────────────────────────────────────────────────

    // Parameters (top) — already open by default, no tab click.
    const excludeMergeCommits = settingsControl(page.locator('body'), 'Exclude Merge Commits:');
    await excludeMergeCommits.waitFor({ state: 'visible' });
    await humanClickLocator(page, excludeMergeCommits);
    await beat(page, 700);

    // Date range left untouched — Mock Data ignores from/to on almost every collection's getAll(), so it wouldn't show anyway.

    // Visualizations (top) — click to open, closes Parameters.
    await humanClickLocator(page, page.locator('#tab_Visualizations'));
    await beat(page, 700);

    // Delete the existing item, add Time Spent via the full plugin selector, then grow it into the freed space.
    const existingItem = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
    if (await existingItem.count()) {
      // Captured before deletion so the new item's resize drag later can't overlap a neighbor.
      const freedBox = await existingItem.boundingBox();
      const existingItemId = await existingItem.getAttribute('id');

      await humanClickLocator(page, existingItem.locator('[class*="settingsButton"]'));
      const existingSettingsPanel = page.locator(`#${existingItemId}_settings`);
      await existingSettingsPanel.waitFor({ state: 'visible' });
      await beat(page, 600);

      await humanClickLocator(page, existingSettingsPanel.getByRole('button', { name: 'Delete', exact: true }));
      await beat(page, 800);

      await humanClickLocator(page, page.locator('button.btn-square.btn-primary.btn-sm'));
      const overviewDialog = page.locator('#visualizationOverview');
      await overviewDialog.waitFor({ state: 'visible' });
      await beat(page, 600);

      // Search narrows to a single result, avoiding a scroll in the mousemove-sensitive overview dialog.
      await humanFill(page, overviewDialog.getByPlaceholder('Search'), 'Time Spent');
      await beat(page, 500);

      await humanClickLocator(page, overviewDialog.locator('button:has(img[alt="Time Spent"])'));
      await beat(page, 800);

      // The click adds the item but leaves the modal open — Escape is a more deterministic close than a mousemove.
      await page.keyboard.press('Escape');
      await beat(page, 400);

      const newItem = page.locator('[id^="dashboardItem"]:not([id*="_"])').filter({ hasText: 'Time Spent' });
      await newItem.waitFor({ state: 'attached', timeout: 10_000 });
      await beat(page, 600);

      if (freedBox) {
        const resizeHandle = newItem.locator('[class*="dashboardItemResizeBarBottomRight"]');
        const handleBox = await resizeHandle.boundingBox();
        if (handleBox) {
          // Fewer steps than humanDrag's default — a simple straight-line drag doesn't need as many.
          await humanDrag(
            page,
            { x: handleBox.x + handleBox.width / 2, y: handleBox.y + handleBox.height / 2 },
            { x: freedBox.x + freedBox.width - 10, y: freedBox.y + freedBox.height - 10 },
            4,
          );
          await beat(page, 600);
        }
      }
    }

    // Sprints (top) — click to open, closes Visualizations.
    await humanClickLocator(page, page.locator('#tab_Sprints'));
    await beat(page, 700);
    const addSprintButton = page.getByRole('button', { name: 'Add Sprint', exact: true });
    if (await addSprintButton.count()) {
      await humanClickLocator(page, addSprintButton);
      const sprintDialog = page.locator('#addSprintDialog');
      await sprintDialog.waitFor({ state: 'visible' });
      await beat(page, 500);

      // 7 two-week sprints starting April 1st, via the dialog's "Multiple Sprints" tab.
      await humanClickLocator(page, sprintDialog.getByRole('tab', { name: 'Multiple Sprints' }));
      await beat(page, 500);

      // datetime-local's OS-drawn sub-fields garble a typed ISO string, so fill() sets it directly (hover is just for the camera).
      const sprintFromInput = sprintDialog.locator('label', { hasText: 'From:' }).locator('input');
      await humanHoverLocator(page, sprintFromInput);
      await sprintFromInput.fill('2026-04-01T00:00');
      await beat(page, 600);

      // Pre-populated fields reject humanFill's clear-then-type (onChange guards on Number(value) > 0), so fill() replaces the value in one shot.
      const sprintLengthInput = sprintDialog.locator('label', { hasText: 'Sprint Length (Days):' }).locator('input');
      await humanHoverLocator(page, sprintLengthInput);
      await sprintLengthInput.fill('14');
      await beat(page, 500);

      const sprintAmountInput = sprintDialog.locator('label', { hasText: 'Amount:' }).locator('input');
      await humanHoverLocator(page, sprintAmountInput);
      await sprintAmountInput.fill('7');
      await beat(page, 800);

      await humanClickLocator(page, sprintDialog.getByRole('button', { name: 'Add All', exact: true }));
      await beat(page, 800);
    }

    // Layouts (top) — click to open, closes Sprints.
    await humanClickLocator(page, page.locator('#tab_Layouts'));
    await beat(page, 700);
    const layoutCard = page.locator('[class*="dashboardCard"]').first();
    if (await layoutCard.count()) {
      await humanClickLocator(page, layoutCard);
      await beat(page, 500);
      const confirmYes = page.locator('#contextMenuContent').getByRole('button', { name: 'Yes' });
      if (await confirmYes.count()) {
        await humanClickLocator(page, confirmYes);
        await page.waitForSelector('[id^="dashboardItem"]:not([id*="_"])', { state: 'attached', timeout: 10_000 });
        await beat(page, 1200);
      }
    }

    // Authors (right) — already open by default, no tab click.
    const firstAuthorCheckbox = page.locator('input.checkbox.checkbox-primary').first();
    if (await firstAuthorCheckbox.count()) {
      await humanClickLocator(page, firstAuthorCheckbox);
      await beat(page, 900);
      await humanClickLocator(page, firstAuthorCheckbox);
      await beat(page, 500);
    }

    // Tabs — click & drag: tab handles are draggable, so drag File Tree from the right bar onto the left bar, then close Layouts (a second click toggles it closed).
    // #tabBarLeft only renders once a drag starts, so dragTo()'s target-visibility precheck times out — dispatch the drag events directly instead.
    const fileTreeTab = page.locator('[id="tab_File Tree"]');
    await humanHoverLocator(page, fileTreeTab);
    await beat(page, 500);

    const viewport = page.viewportSize();
    if (viewport) {
      await humanMove(page, 15, viewport.height / 2);
      await beat(page, 400);
    }

    await page.evaluate(() => {
      const source = document.querySelector('[id="tab_File Tree"]')!;
      const target = document.querySelector('#tabBarLeft')!;
      const dt = new DataTransfer();
      // 0 === DragDropElementType.Tab, inlined since this runs in the page context and can't import the app's TS enum.
      dt.setData('text/plain', JSON.stringify({ dragDropElementType: 0, tabName: 'File Tree' }));
      source.dispatchEvent(new DragEvent('dragstart', { bubbles: true, cancelable: true, dataTransfer: dt }));
      target.dispatchEvent(new DragEvent('dragover', { bubbles: true, cancelable: true, dataTransfer: dt }));
      target.dispatchEvent(new DragEvent('drop', { bubbles: true, cancelable: true, dataTransfer: dt }));
      source.dispatchEvent(new DragEvent('dragend', { bubbles: true, cancelable: true, dataTransfer: dt }));
    });
    await beat(page, 900);

    await humanClickLocator(page, page.locator('#tab_Layouts'));
    await beat(page, 900);

    // File Tree (now on the left) — already open via the drag above.
    const fileSearchInput = page.locator('input[placeholder="Search"]:visible').first();
    if (await fileSearchInput.count()) {
      await humanFill(page, fileSearchInput, 'app');
      await beat(page, 1000);
    }

    // Help (right) — click to open, closes File Tree.
    await humanClickLocator(page, page.locator('#tab_Help'));
    await beat(page, 700);
    const changesHelpButton = page.getByRole('button', { name: 'Changes', exact: true });
    if (await changesHelpButton.count()) {
      await humanClickLocator(page, changesHelpButton);
      await beat(page, 1200);
      const backButton = page.getByRole('button', { name: 'back' });
      if (await backButton.count()) {
        await humanClickLocator(page, backButton);
        await beat(page, 500);
      }
    }

    await beat(page, 1500);
  });
});
