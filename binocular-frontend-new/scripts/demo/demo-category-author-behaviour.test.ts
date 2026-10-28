import { test } from '@playwright/test';
import { installCursorOverlayEverywhere } from './util/demoCursorOverlay.ts';
import {
  humanClickLocator,
  humanHoverLocator,
  humanSelectOption,
  humanFill,
  humanDrag,
  beat,
  settingsControl,
  waitForLocatorStable,
  closeSubWindow,
  openItemSettings,
  dumpBeatLog,
  resetBeatClock,
} from './util/demoInteractions.ts';
import { hoverChartEntry } from './util/demoChartHover.ts';
import { switchVisualization, loadDemoVis } from './util/demoDashboardSetup.ts';
import { beginTitleCard, endTitleCardWhenReady } from './util/demoTitleCard.ts';
import { DEMO_SPRINTS_STATE } from './demoSetup.ts';

const HOVER_STEPS = 10;

test.describe('Demo video — categories', () => {
  test('Category: Author Behaviour', async ({ page }) => {
    test.setTimeout(6 * 60_000);

    resetBeatClock();
    await installCursorOverlayEverywhere(page);

    // Id-agnostic: switchVisualization() never reuses an item id, so these re-resolving locators stay correct across every segment.
    const item = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
    const settingsPanel = page.locator('[id$="_settings"]');

    // ─── Time Spent ─────────────────────────────────────────────────────────────────────────
    {
      // Plugin defaults, not a preset: the visualization is added through the overview search on camera, which carries no settings.
      await loadDemoVis(page, 'Time Spent', { title: 'Time Spent', sprintsState: DEMO_SPRINTS_STATE });
      await page.waitForSelector('svg g path');

      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 5100); // cue 1

      await openItemSettings(page, item, settingsPanel);

      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
      await beat(page, 500); // un-narrated b-roll
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 500); // un-narrated b-roll

      await humanClickLocator(page, settingsControl(settingsPanel, 'Breakdown (Total Time):'));
      await beat(page, 400); // un-narrated b-roll
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 6390); // cue 2

      await humanClickLocator(page, settingsControl(settingsPanel, 'Split Time per Issue:'));
      await beat(page, 400); // un-narrated b-roll
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 5940); // cue 3

      await humanClickLocator(page, settingsControl(settingsPanel, 'Split Spent and Removed:'));
      await beat(page, 400); // un-narrated b-roll
      await hoverChartEntry(page, item, 'negative', HOVER_STEPS);
      await beat(page, 5000); // cue 4

      await humanSelectOption(page, settingsControl(settingsPanel, 'Visualization Style:'), 'stepped');
      await beat(page, 32810); // cues 5+6
    }
    // ─── Collaboration ──────────────────────────────────────────────────────────────────────
    {
      const waitForGraphSettled = () =>
        page.waitForSelector('text=Simulating graph layout...', { state: 'hidden', timeout: 15_000 }).catch(() => {});

      await beginTitleCard(page, 'Collaboration');
      await switchVisualization(page, 'Collaboration', { fast: true });
      await endTitleCardWhenReady(
        page,
        page.waitForSelector('text=Simulating graph layout...', { state: 'visible', timeout: 5_000 }).catch(() => {}),
      );

      // With the overlay gone, let the viewer watch the layout actually settle instead of hiding that wait behind the title card.
      await waitForGraphSettled();
      await beat(page, 5220); // cue 7

      // Drag a node
      const firstNode = item.locator('.node-group').first();
      await waitForLocatorStable(firstNode);
      const nodeBox = await firstNode.boundingBox();
      if (nodeBox) {
        const from = { x: nodeBox.x + nodeBox.width / 2, y: nodeBox.y + nodeBox.height / 2 };
        await humanDrag(page, from, { x: from.x + 600, y: from.y - 600 });
      }
      await beat(page, 4880); // cue 10

      const firstEdge = item.locator('.links line').first();
      await humanHoverLocator(page, firstEdge, HOVER_STEPS);
      await beat(page, 7560); // cue 9

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 8380); // cue 8

      const minInput = settingsPanel.locator('input[type="number"]').first();
      if (await minInput.count()) {
        await humanFill(page, minInput, '1');
        await waitForGraphSettled();
        await beat(page, 500); // un-narrated b-roll
      }

      const maxInput = settingsPanel.locator('input[type="number"]').nth(1);
      if (await maxInput.count()) {
        await humanFill(page, maxInput, '50');
        await waitForGraphSettled();
        await beat(page, 8540); // cue 11
      }

      await humanClickLocator(page, settingsControl(settingsPanel, 'Include commit message references'));
      await waitForGraphSettled();
      await beat(page, 10610); // cue 12

      await closeSubWindow(page, settingsPanel);
      await beat(page, 21060); // cues 13+14
    }

    // ─── Repository Activity ────────────────────────────────────────────────────────────────
    {
      await beginTitleCard(page, 'Repository Activity');
      await switchVisualization(page, 'Repository Activity', { fast: true });
      await endTitleCardWhenReady(page, page.waitForSelector('svg rect'));

      await humanHoverLocator(page, item.locator('svg').first(), HOVER_STEPS);
      await beat(page, 16590); // cues 15+16

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 500); // un-narrated b-roll

      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Activity Timeline:'));
      await beat(page, 8200); // cue 17

      await closeSubWindow(page, settingsPanel);
      await beat(page, 500); // un-narrated b-roll

      await humanClickLocator(page, item.getByRole('button', { name: 'Select week' }));
      await beat(page, 500); // un-narrated b-roll

      const mayWeek = item.getByRole('button', { name: /^May \d+ - May \d+$/ });
      for (let i = 0; i < 8 && !(await mayWeek.count()); i++) {
        await humanClickLocator(page, item.getByRole('button', { name: 'Previous 3 months' }));
        await beat(page, 500); // un-narrated b-roll
      }
      await humanClickLocator(page, mayWeek.first());
      await beat(page, 500); // un-narrated b-roll

      await humanClickLocator(page, item.getByRole('button', { name: 'Next week' }));
      await beat(page, 500); // un-narrated b-roll

      const may15Tile = item
        .locator('svg[width]')
        .first()
        .locator('rect')
        .nth(15 * 7 + 4);
      await humanHoverLocator(page, may15Tile, HOVER_STEPS);
      await beat(page, 20300); // cue 18
    }

    dumpBeatLog('demo-category-author-behaviour');
  });
});
