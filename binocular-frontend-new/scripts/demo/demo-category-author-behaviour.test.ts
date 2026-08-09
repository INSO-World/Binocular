// Records a continuous AuthorBehaviour category video (Time Spent, Collaboration, Repository Activity); offline via Mock Data. Run: npm run demo:record; render via render-demo-videos.mjs.

import { test } from '@playwright/test';
import {
  installCursorOverlayEverywhere,
  humanClickLocator,
  humanHoverLocator,
  humanSelectOption,
  humanFill,
  humanDrag,
  beat,
  beginTitleCard,
  endTitleCardWhenReady,
  settingsControl,
  switchVisualization,
  loadFirstVis,
  waitForLocatorStable,
} from './demoHelpers.ts';
import { MOCK_SPRINTS_STATE } from '../screenshots.setup.ts';

test.describe('Demo video — categories', () => {
  test('Category: Author Behaviour', async ({ page }) => {
    test.setTimeout(4 * 60_000);

    await installCursorOverlayEverywhere(page);

    // Id-agnostic: switchVisualization() never reuses an item id, so these re-resolving locators stay correct across every segment.
    const item = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
    const settingsPanel = page.locator('[id$="_settings"]');

    // ─── Time Spent ─────────────────────────────────────────────────────────────────────────
    {
      await loadFirstVis(
        page,
        'Time Spent',
        'Time Spent',
        {
          breakdown: false,
          visualizationStyle: 'curved',
          splitTimePerIssue: false,
          splitSpentRemoved: false,
          showSprints: false,
        },
        MOCK_SPRINTS_STATE,
      );
      await endTitleCardWhenReady(page, page.waitForSelector('svg g path'));

      await humanHoverLocator(page, item.locator('svg').first());
      await beat(page, 1000);

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 500);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
      await beat(page, 800);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Breakdown (Total Time):'));
      await beat(page, 1000);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Split Time per Issue:'));
      await beat(page, 1000);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Split Spent and Removed:'));
      await beat(page, 1000);

      await humanSelectOption(page, settingsControl(settingsPanel, 'Visualization Style:'), 'stepped');
      await beat(page, 900);

      await humanClickLocator(page, settingsPanel);
      await beat(page, 400);
    }

    // ─── Collaboration ──────────────────────────────────────────────────────────────────────
    // No visualizationStyle in this plugin's settings schema, so nothing is lost by not presetting it through switchVisualization().
    {
      await beginTitleCard(page, 'Collaboration');
      await switchVisualization(page, 'Collaboration');
      await endTitleCardWhenReady(
        page,
        page.waitForSelector('text=Simulating graph layout...', { state: 'hidden', timeout: 15_000 }).catch(() => {}),
      );

      const node = item.locator('svg image, svg circle').first();
      if (await node.count()) {
        // The force simulation reheats on resize, so wait for the node to stop drifting or the drag below misses it.
        await waitForLocatorStable(node);
        const box = await node.boundingBox();
        if (box) {
          await humanDrag(page, { x: box.x + box.width / 2, y: box.y + box.height / 2 }, { x: box.x + 120, y: box.y + 60 });
          await beat(page, 500);
        }
      }

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 700);

      const minInput = settingsPanel.locator('input[type="number"]').first();
      if (await minInput.count()) {
        await humanFill(page, minInput, '2');
        await beat(page, 1200);
      }

      const maxInput = settingsPanel.locator('input[type="number"]').nth(1);
      if (await maxInput.count()) {
        await humanFill(page, maxInput, '50');
        await beat(page, 1200);
      }

      await humanClickLocator(page, settingsControl(settingsPanel, 'Include commit message references'));
      await beat(page, 1000);

      await humanClickLocator(page, settingsPanel);
      await beat(page, 400);
    }

    // ─── Repository Activity ────────────────────────────────────────────────────────────────
    // Only setting is Show Activity Timeline.
    {
      await beginTitleCard(page, 'Repository Activity');
      await switchVisualization(page, 'Repository Activity');
      await endTitleCardWhenReady(page, page.waitForSelector('svg rect'));

      await humanHoverLocator(page, item.locator('svg').first());
      await beat(page, 1000);

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 500);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Activity Timeline:'));
      await beat(page, 1400);

      await humanClickLocator(page, settingsPanel);
      await beat(page, 400);

      // The week view defaults to the current real-world week, outside the mock dataset's window — land on a May week that has data instead.
      await humanClickLocator(page, item.getByRole('button', { name: 'Select week' }));
      await beat(page, 500);

      const mayWeek = item.getByRole('button', { name: /^May \d+ - May \d+$/ });
      for (let i = 0; i < 8 && !(await mayWeek.count()); i++) {
        await humanClickLocator(page, item.getByRole('button', { name: 'Previous 3 months' }));
        await beat(page, 300);
      }
      await humanClickLocator(page, mayWeek.first());
      await beat(page, 1400);
    }
  });
});
