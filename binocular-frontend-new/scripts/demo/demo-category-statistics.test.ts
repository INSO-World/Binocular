// Records the one-segment Statistics category video (Repository Stats); offline via Mock Data. Run: npm run demo:record; render via render-demo-videos.mjs.

import { test } from '@playwright/test';
import {
  installCursorOverlayEverywhere,
  humanClickLocator,
  humanHoverLocator,
  beat,
  showTitleCard,
  settingsControl,
  openTabIfClosed,
  resizeToViewableArea,
} from './demoHelpers.ts';
import { loadVis } from '../screenshots.setup.ts';

test.describe('Demo video — categories', () => {
  test('Category: Statistics', async ({ page }) => {
    test.setTimeout(3 * 60_000);

    await installCursorOverlayEverywhere(page);
    await showTitleCard(page, 'Repository Stats');

    await loadVis(page, 'Repository Stats');
    await page.locator('#dashboardItem1').getByText('Contributors').first().waitFor({ state: 'visible', timeout: 20_000 });

    const item = page.locator('#dashboardItem1');
    const settingsPanel = page.locator('#dashboardItem1_settings');

    // loadVis() closes the Authors sidebar again after populating it — reopen it plus Visualizations, then resize to the resulting viewable area.
    await openTabIfClosed(page, 'Authors');
    await openTabIfClosed(page, 'Visualizations');
    await resizeToViewableArea(page, item);

    await humanHoverLocator(page, item);
    await beat(page, 1200);

    await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
    await settingsPanel.waitFor({ state: 'visible' });
    await beat(page, 600);

    await humanClickLocator(page, settingsControl(settingsPanel, 'Show contributors:'));
    await beat(page, 1000);

    await humanClickLocator(page, settingsControl(settingsPanel, 'Show commits:'));
    await beat(page, 1000);

    await humanClickLocator(page, settingsControl(settingsPanel, 'Show issues:'));
    await beat(page, 1000);

    await humanClickLocator(page, settingsControl(settingsPanel, 'Show builds:'));
    await beat(page, 1000);

    await humanClickLocator(page, settingsControl(settingsPanel, 'Show merge requests:'));
    await beat(page, 1000);

    await humanClickLocator(page, settingsPanel);
    await beat(page, 400);

    await humanHoverLocator(page, item);
    await beat(page, 1500);
  });
});
