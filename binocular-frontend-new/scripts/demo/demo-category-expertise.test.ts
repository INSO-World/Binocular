// Records a continuous Expertise category video (Code Expertise, Knowledge Radar; Code Hotspots excluded as popoutOnly); offline via Mock Data. Run: npm run demo:record; render via render-demo-videos.mjs.

import { test } from '@playwright/test';
import {
  installCursorOverlayEverywhere,
  humanHoverLocator,
  humanClickLocator,
  humanSelectOption,
  beat,
  showTitleCard,
  settingsControl,
  beginTitleCard,
  switchVisualization,
  endTitleCardWhenReady,
  loadFirstVis,
} from './demoHelpers.ts';

test.describe('Demo video — categories', () => {
  test('Category: Expertise', async ({ page }) => {
    test.setTimeout(3 * 60_000);

    await installCursorOverlayEverywhere(page);
    await showTitleCard(page, 'Expertise');

    // Id-agnostic: switchVisualization() never reuses an item id, so these re-resolving locators stay correct across both segments.
    const item = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
    const settingsPanel = page.locator('[id$="_settings"]');

    // ─── Code Expertise ─────────────────────────────────────────────────────────────────────
    {
      // No explicit settings — plugin defaults, matching screenshots.setup.ts's VISUALIZATIONS entry for this plugin.
      await loadFirstVis(page, 'Code Expertise', 'Code Expertise');
      await endTitleCardWhenReady(page, page.waitForSelector('svg text'));

      // Ring chart: segment size, owned/replaced band, CI pass/fail arc, commit-count band.
      await humanHoverLocator(page, item.locator('svg'));
      await beat(page, 1600);

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 800); // give the Branch dropdown's async allBranches fetch a moment to land

      const branchSelect = settingsControl(settingsPanel, 'Branch:');
      const branchOptionCount = await branchSelect.locator('option').count();
      if (branchOptionCount > 1) {
        await humanSelectOption(page, branchSelect, { index: 1 });
        await beat(page, 1200);
      }

      await humanClickLocator(page, settingsPanel);
      await beat(page, 400);
    }

    // ─── Knowledge Radar ────────────────────────────────────────────────────────────────────
    // No settings component for this plugin.
    {
      await beginTitleCard(page, 'Knowledge Radar');
      await switchVisualization(page, 'Knowledge Radar');
      await endTitleCardWhenReady(page, page.waitForSelector('svg text'));

      await humanHoverLocator(page, item.locator('svg'));
      await beat(page, 1800);
    }
  });
});
