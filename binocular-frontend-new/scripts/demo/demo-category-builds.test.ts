// Records the one-segment Builds category video; offline via Mock Data. Run: npm run demo:record; render via render-demo-videos.mjs.

import { test } from '@playwright/test';
import {
  installCursorOverlayEverywhere,
  humanClickLocator,
  humanHoverLocator,
  humanSelectOption,
  beat,
  showTitleCard,
  settingsControl,
  openTabIfClosed,
  resizeToViewableArea,
} from './demoHelpers.ts';
import { loadVis, MOCK_SPRINTS_STATE } from '../screenshots.setup.ts';

test.describe('Demo video — categories', () => {
  test('Category: Builds', async ({ page }) => {
    test.setTimeout(3 * 60_000);

    await installCursorOverlayEverywhere(page);
    await showTitleCard(page, 'Builds');

    await loadVis(page, 'Builds', { visualizationStyle: 'curved', splitBuildsPerAuthor: false, showSprints: false }, MOCK_SPRINTS_STATE);
    await page.waitForSelector('svg g path');

    const item = page.locator('#dashboardItem1');
    const settingsPanel = page.locator('#dashboardItem1_settings');

    // loadVis() closes the Authors sidebar again after populating it — reopen it plus Visualizations, then resize to the resulting viewable area.
    await openTabIfClosed(page, 'Authors');
    await openTabIfClosed(page, 'Visualizations');
    await resizeToViewableArea(page, item);

    await humanHoverLocator(page, item.locator('svg'));
    await beat(page, 1200);

    await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
    await settingsPanel.waitFor({ state: 'visible' });
    await beat(page, 600);

    await humanClickLocator(page, settingsControl(settingsPanel, 'Split Builds per Author:'));
    await beat(page, 1400);

    await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
    await beat(page, 800);

    await humanSelectOption(page, settingsControl(settingsPanel, 'Visualization Style:'), 'stepped');
    await beat(page, 900);

    await humanClickLocator(page, settingsPanel);
    await beat(page, 400);

    await humanHoverLocator(page, item.locator('svg g path').first());
    await beat(page, 1800);
  });
});
