// Records the one-segment Statistics category video (Repository Stats); offline via Mock Data. Run: npm run demo:record; render via render-demo-videos.mjs.

import { test } from '@playwright/test';
import { installCursorOverlayEverywhere } from './util/demoCursorOverlay.ts';
import {
  humanClickLocator,
  humanHoverLocator,
  beat,
  settingsControl,
  closeSubWindow,
  dumpBeatLog,
  resetBeatClock,
} from './util/demoInteractions.ts';
import { loadDemoVis } from './util/demoDashboardSetup.ts';

// Faster cursor travel (default humanMove is 25 steps) for snappier hovers, paired with longer beats so the viewer can still read the tooltip.
const HOVER_STEPS = 10;

test.describe('Demo video — categories', () => {
  test('Category: Statistics', async ({ page }) => {
    test.setTimeout(3 * 60_000);

    resetBeatClock();
    await installCursorOverlayEverywhere(page);

    await loadDemoVis(page, 'Repository Stats', { title: 'Repository Stats' });

    // Id-agnostic: consistent with the multi-segment demo files even though this one never calls switchVisualization().
    const item = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
    const settingsPanel = page.locator('[id$="_settings"]');
    await item.getByText('Contributors').first().waitFor({ state: 'visible', timeout: 20_000 });

    // Cue 1 reads over this initial hover: "Repository Stats is the plainest visualization ...".
    await humanHoverLocator(page, item, HOVER_STEPS);
    await beat(page, 15830);

    await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
    await settingsPanel.waitFor({ state: 'visible' });
    await beat(page, 500);

    // Un-narrated b-roll toggles — the .md only calls out Merge requests and Builds individually.
    await humanClickLocator(page, settingsControl(settingsPanel, 'Show contributors:'));
    await beat(page, 500);

    await humanClickLocator(page, settingsControl(settingsPanel, 'Show commits:'));
    await beat(page, 500);

    await humanClickLocator(page, settingsControl(settingsPanel, 'Show issues:'));
    await beat(page, 500);

    // Reordered ahead of Builds (was after) to match the .md's narrated order — cue 2 is "Show merge requests off".
    // Cue 3 is "Show builds off"; independent toggles, so swapping them has no effect on the end state.
    await humanClickLocator(page, settingsControl(settingsPanel, 'Show merge requests:'));
    await beat(page, 8230);

    await humanClickLocator(page, settingsControl(settingsPanel, 'Show builds:'));
    await beat(page, 7590); // cue 3

    await closeSubWindow(page, settingsPanel);
    await beat(page, 500);

    // Fulfills cue 4 ("point at the Contributors tile") and cue 5 ("hold on the panel"), which has no action of its own.
    // Both cues' narration plays out over this single sustained hover.
    await humanHoverLocator(page, item, HOVER_STEPS);
    await beat(page, 43660);

    dumpBeatLog('demo-category-statistics');
  });
});
