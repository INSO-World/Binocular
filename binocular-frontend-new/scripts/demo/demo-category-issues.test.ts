// Records a continuous Issues category video (Issues, Merge Requests, Issues Timeline, Burndown); offline via Mock Data.
// Run: npm run demo:record; render via render-demo-videos.mjs.

import { test } from '@playwright/test';
import { installCursorOverlayEverywhere } from './util/demoCursorOverlay.ts';
import {
  humanClickLocator,
  humanSelectOption,
  beat,
  settingsControl,
  waitForVisReady,
  closeSubWindow,
  dumpBeatLog,
  resetBeatClock,
} from './util/demoInteractions.ts';
import { hoverChartEntry, hoverRankedElement } from './util/demoChartHover.ts';
import { switchVisualization, loadDemoVis } from './util/demoDashboardSetup.ts';
import { beginTitleCard, endTitleCardWhenReady } from './util/demoTitleCard.ts';
import { VISUALIZATIONS } from '../visualizations.ts';
import { DEMO_SPRINTS_STATE } from './demoSetup.ts';

// Faster cursor travel (default humanMove is 25 steps) for snappier hovers, paired with longer beats so the viewer can still read the tooltip.
const HOVER_STEPS = 10;

function reg(pluginName: string) {
  const entry = VISUALIZATIONS.find((v) => v.pluginName === pluginName);
  if (!entry) throw new Error(`No VISUALIZATIONS entry for ${pluginName}`);
  return entry;
}

test.describe('Demo video — categories', () => {
  test('Category: Issues', async ({ page }) => {
    test.setTimeout(7 * 60_000);

    resetBeatClock();
    await installCursorOverlayEverywhere(page);

    // Id-agnostic: switchVisualization() never reuses an item id, so these re-resolving locators stay correct across every segment.
    const item = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
    const settingsPanel = page.locator('[id$="_settings"]');

    // ─── Issues ─────────────────────────────────────────────────────────────────────────────
    {
      const entry = reg('Issues');
      // Plugin defaults, not entry.settings: the visualization is added through the overview search on camera, which carries no settings.
      await loadDemoVis(page, entry.pluginName, { title: 'Issues', sprintsState: DEMO_SPRINTS_STATE });
      await waitForVisReady(item, entry);

      // Issues uses the shared StackedAreaChart (same as Time Spent), so hover a real data point instead of the bare svg.
      // Covers cue 1 (intro, no dedicated action) + cue 2 ("point out the chart") — both play over this one hover.
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 20750);

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 900);

      // Show Sprints isn't narrated in this segment — brief b-roll only.
      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
      await beat(page, 700);
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 700);

      // Closed issues render as the negative band — hover one now, before Breakdown reshapes this into per-category series.
      // Cue 6's audio ("Closed line climbing") is reassigned to the next matching hover below since this one can't be pushed later.
      await hoverChartEntry(page, item, 'negative', HOVER_STEPS);
      await beat(page, 800);

      // Cue 3: Breakdown toggle.
      await humanClickLocator(page, settingsControl(settingsPanel, 'Breakdown (Total Open Issues):'));
      await beat(page, 16060);
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 700);

      // Cue 4: Split Issues per Assignee.
      await humanClickLocator(page, settingsControl(settingsPanel, 'Split Issues per Assignee:'));
      await beat(page, 6950);
      // Skip the "unassigned" bucket — land on a real author's series instead.
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS, undefined, 'unassigned');
      await beat(page, 700);

      // Cue 5: Visualization Style.
      await humanSelectOption(page, settingsControl(settingsPanel, 'Visualization Style:'), 'stepped');
      await beat(page, 10680);
      // Cue 6 (Closed line climbing) reassigned here — best available hover once the audio track reaches it.
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS, undefined, 'unassigned');
      await beat(page, 6900);

      // Cue 7: reopened-issues caveat — the long closing commentary, held after the panel closes.
      await closeSubWindow(page, settingsPanel);
      await beat(page, 28600);
    }

    // ─── Merge Requests ─────────────────────────────────────────────────────────────────────
    {
      const entry = reg('Merge Requests');
      await beginTitleCard(page, 'Merge Requests');
      await switchVisualization(page, entry.pluginName);
      await endTitleCardWhenReady(page, waitForVisReady(item, entry));

      // Cue 8: intro.
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 10420);

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 900);

      // Show Sprints isn't narrated in this segment — brief b-roll only.
      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
      await beat(page, 700);
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 700);

      // Merged and Closed merge requests both render as negative bands — target Closed specifically before Breakdown reshapes them.
      // Both hovers are technical b-roll, not individually narrated.
      await hoverChartEntry(page, item, 'negative', HOVER_STEPS, 'Closed');
      await beat(page, 700);

      // Same for Merged — its own negative band, distinct from Closed above.
      await hoverChartEntry(page, item, 'negative', HOVER_STEPS, 'Merged');
      await beat(page, 700);

      // Cue 9: Breakdown toggle.
      await humanClickLocator(page, settingsControl(settingsPanel, 'Breakdown (Total Open Merge Requests):'));
      await beat(page, 10060);
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 700);

      // Cue 10: Split per Assignee toggle.
      await humanClickLocator(page, settingsControl(settingsPanel, 'Split Merge Requests per Assignee:'));
      await beat(page, 4740);
      // Cue 11: fallback-bucket caveat, held on this hover.
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 24310);

      // Visualization Style isn't narrated in this segment — brief b-roll only.
      await humanSelectOption(page, settingsControl(settingsPanel, 'Visualization Style:'), 'stepped');
      await beat(page, 700);
      // Cue 12: account-linking-gap caveat, held on this final hover.
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 21990);

      await closeSubWindow(page, settingsPanel);
      await beat(page, 600);
    }

    // ─── Issues Timeline ────────────────────────────────────────────────────────────────────
    {
      const entry = reg('Issues Timeline');
      await beginTitleCard(page, 'Issues Timeline');
      await switchVisualization(page, entry.pluginName);
      await endTitleCardWhenReady(page, waitForVisReady(item, entry));

      // Issue bars open their detail dialog on click, not hover — position on a real bar and click it open.
      // Its Close button can hide behind the settings panel, so both helpers below verify via :hover that the target is really on top.
      async function clickAnIssueBar() {
        const issueBars = item.locator('g[class*="issue"]');
        if (!(await issueBars.count())) return;
        await hoverRankedElement(page, item, 'g[class*="issue"]', HOVER_STEPS);
        const onIssueBar = await page.evaluate(() => {
          const hovered = document.querySelectorAll(':hover');
          return !!hovered[hovered.length - 1]?.closest('g[class*="issue"]');
        });
        if (!onIssueBar) return;
        await page.mouse.down();
        await page.waitForTimeout(80);
        await page.mouse.up();
      }

      async function closeIssueDialog() {
        const closeButton = item.getByRole('button', { name: 'Close', exact: true });
        if (!(await closeButton.count())) return;
        const box = await closeButton.boundingBox();
        if (!box) return;
        await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2, { steps: HOVER_STEPS });
        const onCloseButton = await page.evaluate(() => {
          const top = document.querySelectorAll(':hover');
          const el = top[top.length - 1];
          return el?.tagName === 'BUTTON' && el.textContent?.trim() === 'Close';
        });
        if (!onCloseButton) return;
        await page.mouse.down();
        await page.waitForTimeout(80);
        await page.mouse.up();
        await beat(page, 2000);
      }

      // Show issue info once, then close it — dashboardItem's outside-click handler closes the settings panel on any outside mousedown.
      // Cue 13: this opening click (settings changes below stay back-to-back with the chart untouched) fills the intro's dead air.
      await clickAnIssueBar();
      await beat(page, 8800);
      await closeIssueDialog();

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 900);

      // Show Sprints isn't narrated in this segment — brief b-roll only.
      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
      await beat(page, 700);

      // Cue 14: Coloring Mode — plugin default is already 'author', so re-picking it would be a no-op on camera.
      // 'time-spent' ("Most Spent Time") is a real visible change.
      await humanSelectOption(page, settingsControl(settingsPanel, 'Coloring Mode:'), 'time-spent');
      await beat(page, 10610);

      await closeSubWindow(page, settingsPanel);
      await beat(page, 700);

      // Cue 15: "point out two bars on separate tracks" — plain hold on the now-unobstructed chart (no click needed).
      await beat(page, 10130);

      // Now safe to click the chart again — the panel is already closed, so the outside-click handler is a no-op.
      // Cue 16: the long track-assignment caveat plays across this demo click plus the trailing hold below.
      await clickAnIssueBar();
      await beat(page, 3000);
      await closeIssueDialog();
      await beat(page, 30470);
    }

    // ─── Burndown ───────────────────────────────────────────────────────────────────────────
    // Only setting is Show Sprints.
    {
      const entry = reg('Burndown');
      await beginTitleCard(page, 'Burndown');
      await switchVisualization(page, entry.pluginName);
      await endTitleCardWhenReady(page, waitForVisReady(item, entry));

      // Cue 17: intro — detail dialog is hover-based (onMouseEnter/onMouseLeave per data-point circle), not click.
      // Target a real point instead of the bare svg center, which has no handler of its own.
      await hoverRankedElement(page, item, 'circle[class*="data-point"]', HOVER_STEPS);
      await beat(page, 10650);

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 900);

      // Cue 18: Show Sprints.
      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
      await beat(page, 9650);
      // Unnarrated b-roll.
      await hoverRankedElement(page, item, 'circle[class*="data-point"]', HOVER_STEPS);
      await beat(page, 700);

      // Cue 19: the long granularity-quirk caveat — the final beat of the video, held here.
      await closeSubWindow(page, settingsPanel);
      await beat(page, 38950);
    }

    dumpBeatLog('demo-category-issues');
  });
});
