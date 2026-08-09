// Records a continuous Issues category video (Issues, Merge Requests, Issues Timeline, Burndown); offline via Mock Data. Run: npm run demo:record; render via render-demo-videos.mjs.

import { test } from '@playwright/test';
import {
  installCursorOverlayEverywhere,
  humanClickLocator,
  humanHoverLocator,
  humanSelectOption,
  beat,
  beginTitleCard,
  endTitleCardWhenReady,
  settingsControl,
  waitForVisReady,
  switchVisualization,
  loadFirstVis,
} from './demoHelpers.ts';
import { VISUALIZATIONS, MOCK_SPRINTS_STATE } from '../screenshots.setup.ts';

function reg(pluginName: string) {
  const entry = VISUALIZATIONS.find((v) => v.pluginName === pluginName);
  if (!entry) throw new Error(`No VISUALIZATIONS entry for ${pluginName}`);
  return entry;
}

test.describe('Demo video — categories', () => {
  test('Category: Issues', async ({ page }) => {
    test.setTimeout(4 * 60_000);

    await installCursorOverlayEverywhere(page);

    // Id-agnostic: switchVisualization() never reuses an item id, so these re-resolving locators stay correct across every segment.
    const item = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
    const settingsPanel = page.locator('[id$="_settings"]');

    // ─── Issues ─────────────────────────────────────────────────────────────────────────────
    {
      const entry = reg('Issues');
      await loadFirstVis(page, entry.pluginName, 'Issues', entry.settings, MOCK_SPRINTS_STATE);
      await endTitleCardWhenReady(page, waitForVisReady(item, entry));

      await humanHoverLocator(page, item.locator('svg').first());
      await beat(page, 1000);

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 500);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
      await beat(page, 800);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Breakdown (Total Open Issues):'));
      await beat(page, 1200);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Split Issues per Assignee:'));
      await beat(page, 1000);

      await humanSelectOption(page, settingsControl(settingsPanel, 'Visualization Style:'), 'stepped');
      await beat(page, 900);

      await humanClickLocator(page, settingsPanel);
      await beat(page, 400);
    }

    // ─── Merge Requests ─────────────────────────────────────────────────────────────────────
    {
      const entry = reg('Merge Requests');
      await beginTitleCard(page, 'Merge Requests');
      await switchVisualization(page, entry.pluginName);
      await endTitleCardWhenReady(page, waitForVisReady(item, entry));

      await humanHoverLocator(page, item.locator('svg').first());
      await beat(page, 1000);

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 500);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
      await beat(page, 800);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Breakdown (Total Open Merge Requests):'));
      await beat(page, 1200);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Split Merge Requests per Assignee:'));
      await beat(page, 1000);

      await humanSelectOption(page, settingsControl(settingsPanel, 'Visualization Style:'), 'stepped');
      await beat(page, 900);

      await humanClickLocator(page, settingsPanel);
      await beat(page, 400);
    }

    // ─── Issues Timeline ────────────────────────────────────────────────────────────────────
    {
      const entry = reg('Issues Timeline');
      await beginTitleCard(page, 'Issues Timeline');
      await switchVisualization(page, entry.pluginName);
      await endTitleCardWhenReady(page, waitForVisReady(item, entry));

      await humanHoverLocator(page, item.locator('svg').first());
      await beat(page, 1000);

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 500);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
      await beat(page, 800);

      await humanSelectOption(page, settingsControl(settingsPanel, 'Coloring Mode:'), 'author');
      await beat(page, 1200);

      await humanClickLocator(page, settingsPanel);
      await beat(page, 400);
    }

    // ─── Burndown ───────────────────────────────────────────────────────────────────────────
    // Only setting is Show Sprints.
    {
      const entry = reg('Burndown');
      await beginTitleCard(page, 'Burndown');
      await switchVisualization(page, entry.pluginName);
      await endTitleCardWhenReady(page, waitForVisReady(item, entry));

      await humanHoverLocator(page, item.locator('svg').first());
      await beat(page, 1000);

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 500);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
      await beat(page, 1200);

      await humanClickLocator(page, settingsPanel);
      await beat(page, 400);
    }
  });
});
