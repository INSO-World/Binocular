// Records the "core" scene (setup wizard + tabs tour) from demo-core-v3.md; offline via Mock Data. Run: npm run demo:record; render via render-demo-videos.mjs.
// Parameters/Authors tabs are open by default, so this file interacts with their contents directly instead of clicking their tab handles.

import { test } from '@playwright/test';
import { installCursorOverlay } from './util/demoCursorOverlay.ts';
import {
  humanClickLocator,
  humanFill,
  beat,
  settingsControl,
  humanHoverLocator,
  humanMove,
  humanDrag,
  dumpBeatLog,
  resetBeatClock,
} from './util/demoInteractions.ts';
import { showTitleCard } from './util/demoTitleCard.ts';
import { mockBackendRoutes } from './util/demoDashboardSetup.ts';

test.describe('Demo video — core', () => {
  test('Binocular core', async ({ page }) => {
    test.setTimeout(7 * 60_000);

    resetBeatClock();
    await page.addInitScript(installCursorOverlay);
    await mockBackendRoutes(page);
    await page.goto('/');

    // Holds for cue 1's full narration (the "hook" — plays over the logo, before the wizard gets any attention).
    await showTitleCard(page, 'Binocular', 27100);

    // ─── Setup wizard — connect Mock Data, pick a recommended dashboard ────────────────────
    const dialog = page.locator('#setupDialog');
    await dialog.waitFor({ state: 'attached' });
    // Covers cue 2 (wizard intro) + cue 3 ("Step one is just a welcome screen") — both narrate the same Start screen.
    await beat(page, 14460);

    await humanClickLocator(page, dialog.getByRole('button', { name: 'Next' })); // Start -> Database
    await beat(page, 19020); // cue 4: "Step two connects that data source..."

    await humanClickLocator(page, dialog.locator('.card', { hasText: 'Mock Data' }).getByRole('button', { name: 'Add' }));
    await beat(page, 3520); // cue 5: "One click, and Binocular has a dataset to work with."

    await humanClickLocator(page, dialog.getByRole('button', { name: 'Next' })); // Database -> Authors
    await beat(page, 17620); // cue 6: "Step three reviews the authors..."
    await humanClickLocator(page, dialog.getByRole('button', { name: 'Next' })); // Authors -> Dashboard
    await beat(page, 5200); // cue 7: "Step four offers a couple of recommended starting dashboards..."

    await humanClickLocator(page, dialog.getByRole('button', { name: 'Select' }).first());
    await beat(page, 4020); // cue 8: "Picking one pre-populates your workspace..."

    await humanClickLocator(page, dialog.getByRole('button', { name: 'Next' })); // Dashboard -> Summary
    await beat(page, 5860); // cue 9: "And step five is just a summary..."

    const reloaded = page.waitForEvent('load');
    await humanClickLocator(page, dialog.getByRole('button', { name: 'Save' }));
    await reloaded;
    // Shown right after 'load' (not after dashboard-ready) so its hold time covers the blank mounting gap.
    // Holds for cue 10 ("Hit Save, Binocular reloads once...").
    await showTitleCard(page, 'Around the Dashboard', 5940);
    await page.waitForSelector('#tabBarTop', { state: 'visible' });
    await page.waitForSelector('[id^="dashboardItem"]:not([id*="_"])', { state: 'attached', timeout: 10_000 });
    await beat(page, 18560); // cue 11: "Around the dashboard, a ring of tabs..." (Parameters already open)

    // ─── Tabs tour ──────────────────────────────────────────────────────────────────────────

    // Parameters (top) — already open by default, no tab click.
    const excludeMergeCommits = settingsControl(page.locator('body'), 'Exclude Merge Commits:');
    await excludeMergeCommits.waitFor({ state: 'visible' });
    await humanClickLocator(page, excludeMergeCommits);
    await beat(page, 4320); // cue 12: "A quick one-click nudge, and every chart..."

    // Date range left untouched — Mock Data ignores from/to on almost every collection's getAll(), so it wouldn't show anyway.

    // Visualizations (top) — click to open, closes Parameters.
    await humanClickLocator(page, page.locator('#tab_Visualizations'));
    await beat(page, 6890); // cue 13: "Visualizations is where you add new charts..."

    // Delete the existing item, add Time Spent via the full plugin selector, then grow it into the freed space.
    // Cue 14 ("No dragging required, though dragging works too.") narrates the payoff (the resize drag) at the end; other beats are brief b-roll.
    const existingItem = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
    if (await existingItem.count()) {
      // Captured before deletion so the new item's resize drag later can't overlap a neighbor.
      const freedBox = await existingItem.boundingBox();
      const existingItemId = await existingItem.getAttribute('id');

      await humanClickLocator(page, existingItem.locator('[class*="settingsButton"]'));
      const existingSettingsPanel = page.locator(`#${existingItemId}_settings`);
      await existingSettingsPanel.waitFor({ state: 'visible' });
      await beat(page, 2790);

      await humanClickLocator(page, existingSettingsPanel.getByRole('button', { name: 'Delete', exact: true }));
      await beat(page, 500);

      await humanClickLocator(page, page.locator('button.btn-square.btn-primary.btn-sm'));
      const overviewDialog = page.locator('#visualizationOverview');
      await overviewDialog.waitFor({ state: 'visible' });
      await beat(page, 500);

      // Search narrows to a single result, avoiding a scroll in the mousemove-sensitive overview dialog.
      await humanFill(page, overviewDialog.getByPlaceholder('Search'), 'Time Spent');
      await beat(page, 500);

      await humanClickLocator(page, overviewDialog.locator('button:has(img[alt="Time Spent"])'));
      await beat(page, 500);

      // The click adds the item but leaves the modal open — Escape is a more deterministic close than a mousemove.
      await page.keyboard.press('Escape');
      await beat(page, 400);

      const newItem = page.locator('[id^="dashboardItem"]:not([id*="_"])').filter({ hasText: 'Time Spent' });
      await newItem.waitFor({ state: 'attached', timeout: 10_000 });
      await beat(page, 500);

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
          await beat(page, 3200); // cue 14: "No dragging required, though dragging works too."
        }
      }
    }

    // Sprints (top) — click to open, closes Visualizations.
    await humanClickLocator(page, page.locator('#tab_Sprints'));
    await beat(page, 4430); // cue 15: "Sprints lets you define time boxes..."
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
      await beat(page, 500);

      // Pre-populated fields reject humanFill's clear-then-type (onChange guards on Number(value) > 0), so fill() replaces the value in one shot.
      const sprintLengthInput = sprintDialog.locator('label', { hasText: 'Sprint Length (Days):' }).locator('input');
      await humanHoverLocator(page, sprintLengthInput);
      await sprintLengthInput.fill('14');
      await beat(page, 500);

      const sprintAmountInput = sprintDialog.locator('label', { hasText: 'Amount:' }).locator('input');
      await humanHoverLocator(page, sprintAmountInput);
      await sprintAmountInput.fill('7');
      await beat(page, 500);

      await humanClickLocator(page, sprintDialog.getByRole('button', { name: 'Add All', exact: true }));
      await beat(page, 3220); // cue 16: "Name it, and it's immediately available as an overlay."
    }

    // Layouts (top) — click to open, closes Sprints.
    await humanClickLocator(page, page.locator('#tab_Layouts'));
    await beat(page, 6130); // cue 17: "Layouts holds recommended dashboard presets..."
    const layoutCard = page.locator('[class*="dashboardCard"]').first();
    if (await layoutCard.count()) {
      await humanClickLocator(page, layoutCard);
      await beat(page, 500);
      const confirmYes = page.locator('#contextMenuContent').getByRole('button', { name: 'Yes' });
      if (await confirmYes.count()) {
        await humanClickLocator(page, confirmYes);
        await page.waitForSelector('[id^="dashboardItem"]:not([id*="_"])', { state: 'attached', timeout: 10_000 });
        await beat(page, 3520); // cue 18: "Swapping the whole dashboard is one click and one confirmation."
      }
    }

    // Authors (right) — already open by default, no tab click, so there's no natural pause point for cue 19's
    // intro ("On the other side, Authors lists every contributor...") — hold here before touching anything.
    await beat(page, 12220);
    const firstAuthorCheckbox = page.locator('input.checkbox.checkbox-primary').first();
    if (await firstAuthorCheckbox.count()) {
      await humanClickLocator(page, firstAuthorCheckbox);
      await beat(page, 500);
      await humanClickLocator(page, firstAuthorCheckbox);
      await beat(page, 2740); // cue 20: "It's a live filter, not just a list."
    }

    // Tabs — click & drag: drag File Tree from the right bar to the left bar, then close Layouts (second click toggles it closed).
    // #tabBarLeft only renders once a drag starts, so dragTo()'s target-visibility precheck times out — dispatch the drag events directly instead.
    const fileTreeTab = page.locator('[id="tab_File Tree"]');
    await humanHoverLocator(page, fileTreeTab);
    await beat(page, 500);

    const viewport = page.viewportSize();
    if (viewport) {
      await humanMove(page, 15, viewport.height / 2);
      await beat(page, 500);
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
    await beat(page, 8410); // cue 21: "File Tree mirrors the repository's folder structure..."

    await humanClickLocator(page, page.locator('#tab_Layouts'));
    await beat(page, 500);

    // File Tree (now on the left) — already open via the drag above.
    const fileSearchInput = page.locator('input[placeholder="Search"]:visible').first();
    if (await fileSearchInput.count()) {
      await humanFill(page, fileSearchInput, 'app');
      await beat(page, 2170); // cue 22: "Search narrows it instantly."
    }

    // Help (right) — click to open, closes File Tree.
    await humanClickLocator(page, page.locator('#tab_Help'));
    await beat(page, 9000); // cue 23: "And Help is built into the tool itself..."
    const changesHelpButton = page.getByRole('button', { name: 'Changes', exact: true });
    if (await changesHelpButton.count()) {
      await humanClickLocator(page, changesHelpButton);
      await beat(page, 900);
      const backButton = page.getByRole('button', { name: 'back' });
      if (await backButton.count()) {
        await humanClickLocator(page, backButton);
        await beat(page, 2910); // cue 24: "No separate documentation site required."
      }
    }

    // Covers cue 25 (bridge line) + cue 26 (wrap-up) — both generic, no specific on-screen action.
    await beat(page, 26220);

    dumpBeatLog('demo-core-v3');
  });
});
