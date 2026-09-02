// Records the one-segment Ownership category video (Code Ownership); offline via Mock Data. Run: npm run demo:record; render via render-demo-videos.mjs.

import { test } from '@playwright/test';
import { installCursorOverlayEverywhere } from './util/demoCursorOverlay.ts';
import {
  humanClickLocator,
  humanSelectOption,
  beat,
  settingsControl,
  closeSubWindow,
  dumpBeatLog,
  resetBeatClock,
} from './util/demoInteractions.ts';
import { hoverChartEntry } from './util/demoChartHover.ts';
import { loadDemoVis } from './util/demoDashboardSetup.ts';
import { DEMO_SPRINTS_STATE } from './demoSetup.ts';

// Faster cursor travel (default humanMove is 25 steps) for snappier hovers, paired with longer beats so the viewer can still read the tooltip.
const HOVER_STEPS = 10;

test.describe('Demo video — categories', () => {
  test('Category: Ownership', async ({ page }) => {
    test.setTimeout(4 * 60_000);

    resetBeatClock();
    await installCursorOverlayEverywhere(page);

    // Plugin defaults, not a preset: the visualization is added through the overview search on camera, which carries no settings.
    await loadDemoVis(page, 'Code Ownership', { title: 'Code Ownership', sprintsState: DEMO_SPRINTS_STATE });
    await page.waitForSelector('svg g path');
    // Cue 1 (intro, no dedicated action) plays over this static just-loaded view.
    await beat(page, 12000);

    // Id-agnostic: consistent with the multi-segment demo files even though this one never calls switchVisualization().
    const item = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
    const settingsPanel = page.locator('[id$="_settings"]');

    // Code Ownership uses the shared StackedAreaChart (same as Time Spent), so hover a real data point instead of the bare svg.
    await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
    await beat(page, 11300); // cue 2

    await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
    await settingsPanel.waitFor({ state: 'visible' });
    await beat(page, 1200); // give the Branch dropdown's async allBranches fetch a moment to land (un-narrated b-roll)

    await humanSelectOption(page, settingsControl(settingsPanel, 'Display Mode:'), 'relative');
    await beat(page, 10450); // cue 3

    const branchSelect = settingsControl(settingsPanel, 'Branch:');
    const branchOptionCount = await branchSelect.locator('option').count();
    if (branchOptionCount > 1) {
      await humanSelectOption(page, branchSelect, { index: 1 });
      await beat(page, 5270); // cue 4
    }

    // One cue narrates both Visualization Style and Show Sprints together — duration split proportionally between them.
    await humanSelectOption(page, settingsControl(settingsPanel, 'Visualization Style:'), 'stepped');
    await beat(page, 6290); // cue 5 (part 1 of 2)

    await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
    await beat(page, 5590); // cue 5 (part 2 — kept at beat index only, first part above anchors the cue's real timing)

    await closeSubWindow(page, settingsPanel);
    await beat(page, 500); // un-narrated b-roll

    // Final hover triggers cue 6 ("the natural read...") immediately followed by cue 7 (the long caveat) — no
    // further action follows, so this beat must hold long enough to cover both cues back to back.
    await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
    await beat(page, 46440);

    dumpBeatLog('demo-category-ownership');
  });
});
