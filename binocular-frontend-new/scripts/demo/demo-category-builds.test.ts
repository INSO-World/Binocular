// Records the one-segment Builds category video; offline via Mock Data. Run: npm run demo:record; render via render-demo-videos.mjs.

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

const HOVER_STEPS = 10;

test.describe('Demo video — categories', () => {
  test('Category: Builds', async ({ page }) => {
    test.setTimeout(3 * 60_000);

    resetBeatClock();
    await installCursorOverlayEverywhere(page);

    await loadDemoVis(page, 'Builds', { title: 'Builds', sprintsState: DEMO_SPRINTS_STATE });
    await page.waitForSelector('svg g path');

    const item = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
    const settingsPanel = page.locator('[id$="_settings"]');

    await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
    await beat(page, 17480); // cues 1+2

    await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
    await settingsPanel.waitFor({ state: 'visible' });
    await beat(page, 600); // un-narrated b-roll

    await humanClickLocator(page, settingsControl(settingsPanel, 'Split Builds per Author:'));
    await beat(page, 6830); // cue 3

    await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
    await beat(page, 5500); // cue 4

    await humanSelectOption(page, settingsControl(settingsPanel, 'Visualization Style:'), 'stepped');
    await beat(page, 4970); // cue 5

    await closeSubWindow(page, settingsPanel);
    await beat(page, 600); // un-narrated b-roll

    await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
    await beat(page, 43540); // cues 6+7

    dumpBeatLog('demo-category-builds');
  });
});
