// Records a continuous Commits category video (Changes, Sum Commits, File Changes, Commit By File, Change Frequency); offline via Mock Data. Run: npm run demo:record; render via render-demo-videos.mjs.

import { test } from '@playwright/test';
import {
  installCursorOverlayEverywhere,
  humanClickLocator,
  humanHoverLocator,
  humanSelectOption,
  humanFill,
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
  test('Category: Commits', async ({ page }) => {
    test.setTimeout(5 * 60_000);

    await installCursorOverlayEverywhere(page);

    // Id-agnostic: switchVisualization() never reuses an item id, so these re-resolving locators stay correct across every segment.
    const item = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
    const settingsPanel = page.locator('[id$="_settings"]');

    // ─── Changes ────────────────────────────────────────────────────────────────────────────
    {
      const entry = reg('Changes');
      await loadFirstVis(page, entry.pluginName, 'Changes', entry.settings, MOCK_SPRINTS_STATE);
      await endTitleCardWhenReady(page, waitForVisReady(item, entry));

      await humanHoverLocator(page, item.locator('svg').first());
      await beat(page, 1000);

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 500);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Split Additions and Deletions:'));
      await beat(page, 1000);

      await humanSelectOption(page, settingsControl(settingsPanel, 'Visualization Style:'), 'stepped');
      await beat(page, 900);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
      await beat(page, 800);

      await humanClickLocator(page, settingsPanel);
      await beat(page, 400);
    }

    // ─── Sum Commits ────────────────────────────────────────────────────────────────────────
    {
      const entry = reg('Sum Commits');
      await beginTitleCard(page, 'Sum Commits');
      await switchVisualization(page, entry.pluginName);
      await endTitleCardWhenReady(page, waitForVisReady(item, entry));

      await humanHoverLocator(page, item.locator('svg').first());
      await beat(page, 1000);

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 500);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Mean:'));
      await beat(page, 900);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Show other authors:'));
      await beat(page, 900);

      await humanFill(page, settingsControl(settingsPanel, 'Minimum Commits'), '2');
      await beat(page, 900);

      await humanFill(page, settingsControl(settingsPanel, 'Top N Authors'), '3');
      await beat(page, 1000);

      await humanClickLocator(page, settingsPanel);
      await beat(page, 400);
    }

    // ─── File Changes ───────────────────────────────────────────────────────────────────────
    {
      const entry = reg('File Changes');
      await beginTitleCard(page, 'File Changes');
      await switchVisualization(page, entry.pluginName);

      // A fresh add has no preset file, so pick one now (still behind the title card) or endTitleCardWhenReady below times out on an empty chart.
      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      const fileSearchInput = settingsControl(settingsPanel, 'File:').first();
      await fileSearchInput.waitFor({ state: 'visible', timeout: 8_000 }).catch(() => {});
      if (await fileSearchInput.count()) {
        await fileSearchInput.fill('app-routing');
        await settingsControl(settingsPanel, 'File:').last().selectOption('frontend/src/app/app-routing.module.ts');
      }
      await humanClickLocator(page, settingsPanel);

      await endTitleCardWhenReady(page, waitForVisReady(item, entry));

      await humanHoverLocator(page, item.locator('svg').first());
      await beat(page, 1000);

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 500);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
      await beat(page, 800);

      await humanSelectOption(page, settingsControl(settingsPanel, 'Visualization Style:'), 'linear');
      await beat(page, 900);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Split Additions and Deletions:'));
      await beat(page, 900);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Show extra Metrics'));
      await beat(page, 1000);

      await humanClickLocator(page, settingsPanel);
      await beat(page, 400);
    }

    // ─── Commit By File ─────────────────────────────────────────────────────────────────────
    // No settings component for this plugin — just hold the rendered chart.
    {
      const entry = reg('Commit By File');
      await beginTitleCard(page, 'Commit By File');
      await switchVisualization(page, entry.pluginName);
      await endTitleCardWhenReady(page, waitForVisReady(item, entry));

      await humanHoverLocator(page, item.locator('svg').first());
      await beat(page, 1800);
    }

    // ─── Change Frequency ───────────────────────────────────────────────────────────────────
    // No settings flyout — navigation happens via the in-chart "Directory" drawer instead.
    {
      const entry = reg('Change Frequency');
      await beginTitleCard(page, 'Change Frequency');
      await switchVisualization(page, entry.pluginName);
      await endTitleCardWhenReady(page, waitForVisReady(item, entry));

      await humanHoverLocator(page, item.locator('svg').first());
      await beat(page, 800);

      const dirBtn = item.getByRole('button', { name: 'Directory' });
      if (await dirBtn.count()) {
        await humanClickLocator(page, dirBtn);
        await item.locator('[class*="directoryDrawerOpen"]').waitFor({ state: 'visible' });
        await beat(page, 500);

        for (const folder of ['frontend', 'src']) {
          const entryBtn = item.locator('[class*="directoryEntry"]').filter({ hasText: folder }).first();
          if (await entryBtn.count()) {
            await humanClickLocator(page, entryBtn);
            await item.locator('[class*="breadcrumb"]').getByText(folder).first().waitFor({ state: 'visible', timeout: 5_000 });
            await beat(page, 700);
          }
        }

        await humanClickLocator(page, dirBtn);
        await item.locator('[class*="directoryDrawerOpen"]').waitFor({ state: 'hidden' });
      }

      await beat(page, 1500);
    }
  });
});
