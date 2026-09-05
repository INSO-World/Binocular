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

    // "Builds" isn't in this plugin's defaultSettings (only commits/contributors/issues/mergeRequests
    // default true), so it's off until toggled — turn it on first so all five tiles the narration
    // describes are actually visible from the intro onward, not just four.
    await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
    await settingsPanel.waitFor({ state: 'visible' });
    await beat(page, 500);
    await humanClickLocator(page, settingsControl(settingsPanel, 'Show builds:'));
    await beat(page, 500);
    await closeSubWindow(page, settingsPanel);
    await beat(page, 500);

    // Cue 1: intro.
    // Beats below are scaled to k=0.5 of pure cue-duration: measured inherent action overhead
    // (O≈15.4s for this test's ~11 actions) plus narration text already this short means matching
    // beat=duration exactly would overshoot the video well past the narration total — see the
    // O/U/N/TPure calculation method in recalc-beats-v2.mjs. 0.5 is a floor, not the raw solve
    // (which wanted k≈0.33), so holds don't get uncomfortably fast.
    await humanHoverLocator(page, item, HOVER_STEPS);
    await beat(page, 4700); // cue 1 (k=0.5)

    await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
    await settingsPanel.waitFor({ state: 'visible' });
    await beat(page, 500);

    // Toggle one tile off, then back on — a single representative example instead of touching every tile.
    const mergeRequestsToggle = settingsControl(settingsPanel, 'Show merge requests:');
    await humanClickLocator(page, mergeRequestsToggle);
    await beat(page, 900); // un-narrated b-roll (mis-scaled by an earlier pass — restored)
    await humanClickLocator(page, mergeRequestsToggle);
    await beat(page, 2320); // cue 2 (k=0.5)

    await closeSubWindow(page, settingsPanel);
    await beat(page, 500);

    // Cue 3: point at the Contributors tile.
    await humanHoverLocator(page, item, HOVER_STEPS);
    await beat(page, 1710); // cue 3 (k=0.5)

    // Cue 4: the date-range-scoping caveat, held on the panel.
    await beat(page, 4330); // cue 4 (k=0.5)

    dumpBeatLog('demo-category-statistics');
  });
});
