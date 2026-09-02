import { test } from '@playwright/test';
import { installCursorOverlayEverywhere } from './util/demoCursorOverlay.ts';
import {
  humanClickLocator,
  humanSelectOption,
  humanFill,
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

// Records the Commits category video (Changes, Sum Commits, File Changes, Commit By File, Change Frequency); offline via Mock Data.
// Run: npm run demo:record; render via render-demo-videos.mjs.

// Faster cursor travel (default humanMove is 25 steps) for snappier hovers, paired with longer beats so the viewer can still read the tooltip.
const HOVER_STEPS = 10;

function reg(pluginName: string) {
  const entry = VISUALIZATIONS.find((v) => v.pluginName === pluginName);
  if (!entry) throw new Error(`No VISUALIZATIONS entry for ${pluginName}`);
  return entry;
}

test.describe('Demo video — categories', () => {
  test('Category: Commits', async ({ page }) => {
    test.setTimeout(11 * 60_000);

    resetBeatClock();
    await installCursorOverlayEverywhere(page);

    // Id-agnostic: switchVisualization() never reuses an item id, so these re-resolving locators stay correct across every segment.
    const item = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
    const settingsPanel = page.locator('[id$="_settings"]');

    // ─── Changes ────────────────────────────────────────────────────────────────────────────
    {
      const entry = reg('Changes');
      // Plugin defaults, not entry.settings: the visualization is added through the overview search on camera, which carries no settings.
      await loadDemoVis(page, entry.pluginName, { title: 'Changes', sprintsState: DEMO_SPRINTS_STATE });
      await waitForVisReady(item, entry);

      // Changes uses the shared StackedAreaChart (same as Time Spent), so hover a real data point instead of the bare svg.
      // Carries cues 1+2 (intro + "point out the stacked area chart") — the intro has no dedicated action of its own.
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 17020);

      // Split Additions/Deletions defaults to on, so the negative Deletions bands exist here — hover one before splitting is turned off below.
      // Unnarrated b-roll (no cue covers this hover) — keep brief.
      await hoverChartEntry(page, item, 'negative', HOVER_STEPS);
      await beat(page, 700);

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 500);

      // Cue 3: "toggle Split Additions and Deletions on".
      await humanClickLocator(page, settingsControl(settingsPanel, 'Split Additions and Deletions:'));
      await beat(page, 13850);
      // Splitting off merges Additions/Deletions back into one combined series per author — hover it to show the new total.
      // Unnarrated b-roll.
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 700);

      // Cue 4: "Visualization Style switches the interpolation...".
      await humanSelectOption(page, settingsControl(settingsPanel, 'Visualization Style:'), 'stepped');
      await beat(page, 10290);
      // Cue 5: "hover over one author's tall band".
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 5530);

      // Show Sprints isn't called out by its own cue in this segment — unnarrated b-roll, kept brief so cue 6's long
      // caveat (which starts right after cue 5) has room to play across the rest of this segment.
      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
      await beat(page, 700);
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 900);

      // Cue 6 ("hold on the chart, no further action") — settles here for the remainder of its 26.85s.
      await closeSubWindow(page, settingsPanel);
      await beat(page, 25550);
    }

    // ─── Sum Commits ────────────────────────────────────────────────────────────────────────
    {
      const entry = reg('Sum Commits');
      await beginTitleCard(page, 'Sum Commits');
      await switchVisualization(page, entry.pluginName);
      await endTitleCardWhenReady(page, waitForVisReady(item, entry));

      // Bar chart, not StackedAreaChart: the svg center lands in a gap, so target real bars; rank varies below so the cursor visits a different bar each time.
      // Cue 7 intro plays here — no dedicated "point out" cue precedes cue 8 in this segment.
      await hoverRankedElement(page, item, '.bar.main', HOVER_STEPS, 0);
      await beat(page, 10550);

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 500);

      // Cue 8: "Show Mean draws a dashed line...".
      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Mean:'));
      await beat(page, 9140);
      // The dashed mean line is a static reference (no tooltip of its own) — hover a bar to show the chart is still interactive.
      // Unnarrated b-roll.
      await hoverRankedElement(page, item, '.bar.main', HOVER_STEPS, 1);
      await beat(page, 700);

      // Cue 9: "Show other authors folds anyone not on your author list...".
      await humanClickLocator(page, settingsControl(settingsPanel, 'Show other authors:'));
      await beat(page, 7880);
      // More bars are on screen now — pick one further down the ranking for variety.
      // Unnarrated b-roll.
      await hoverRankedElement(page, item, '.bar.main', HOVER_STEPS, 3);
      await beat(page, 700);

      // Minimum Commits isn't called out by its own cue — unnarrated b-roll.
      await humanFill(page, settingsControl(settingsPanel, 'Minimum Commits'), '70');
      await beat(page, 600);
      // Infinity clamps to the last (smallest) bar, showing the lowest author who still cleared the threshold.
      // Unnarrated b-roll.
      await hoverRankedElement(page, item, '.bar.main', HOVER_STEPS, Infinity);
      await beat(page, 700);

      // Cue 10: "Top N Authors trims the bar chart down to just the leaders...".
      await humanFill(page, settingsControl(settingsPanel, 'Top N Authors'), '3');
      await beat(page, 9120);
      // Only 3 bars remain — hover the middle one, distinct from the lowest bar just shown above.
      // Cue 11: "hover the tallest bar" (closest available match — rank 1 is the middle of the remaining 3).
      await hoverRankedElement(page, item, '.bar.main', HOVER_STEPS, 1);
      await beat(page, 5270);

      // Cue 12 ("hold on the chart") — the long leaderboard-caveat paragraph settles here.
      await closeSubWindow(page, settingsPanel);
      await beat(page, 40380);
    }

    // ─── File Changes ───────────────────────────────────────────────────────────────────────
    // NOTE: code's toggle order differs from the .md's narration order; cues below are matched by actual action, not .md order.
    // Cues 16+17 fold into the final settle beat since no action follows "Show extra Metrics" for them to attach to.
    {
      const entry = reg('File Changes');
      await beginTitleCard(page, 'File Changes');
      await switchVisualization(page, entry.pluginName);
      // No file selected yet, so svg g path never renders — skip that readiness wait (card would sit for maxMs) and pick the file live below.
      await endTitleCardWhenReady(page, Promise.resolve());

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      const fileSearchInput = settingsControl(settingsPanel, 'File:').first();
      await fileSearchInput.waitFor({ state: 'visible', timeout: 8_000 }).catch(() => {});
      if (await fileSearchInput.count()) {
        await fileSearchInput.fill('app-routing');
        await settingsControl(settingsPanel, 'File:').last().selectOption('frontend/src/app/app-routing.module.ts');
      }
      await waitForVisReady(item, entry);
      // Cue 13 intro plays here — first stable frame once the file loads.
      await beat(page, 10460);
      // File Changes uses the shared StackedAreaChart (same as Time Spent), so hover a real data point instead of the bare svg.
      // Unnarrated b-roll.
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 700);

      // Split Additions/Deletions defaults to on, so the negative Deletions bands exist here — hover one before splitting is turned off below.
      // Unnarrated b-roll.
      await hoverChartEntry(page, item, 'negative', HOVER_STEPS);
      await beat(page, 700);

      // Show Sprints isn't called out by its own cue in this segment — unnarrated b-roll.
      await humanClickLocator(page, settingsControl(settingsPanel, 'Show Sprints:'));
      await beat(page, 600);
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 700);

      // Cue 15: "Visualization Style is the same curved/stepped/linear cosmetic choice...".
      await humanSelectOption(page, settingsControl(settingsPanel, 'Visualization Style:'), 'linear');
      await beat(page, 6950);
      // Show the new linear (straight-segment) curve landing on a real data point.
      // Unnarrated b-roll.
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 700);

      // Split is still on, so negative Deletions bands still exist — hover one to show linear style applies before the Split toggle merges them away.
      // Unnarrated b-roll.
      await hoverChartEntry(page, item, 'negative', HOVER_STEPS);
      await beat(page, 700);

      // Split Additions and Deletions (off) isn't called out by its own cue in this segment — unnarrated b-roll.
      await humanClickLocator(page, settingsControl(settingsPanel, 'Split Additions and Deletions:'));
      await beat(page, 600);
      // Splitting off merges Additions/Deletions back into one combined series — hover it to show the new total.
      // Unnarrated b-roll.
      await hoverChartEntry(page, item, 'positive', HOVER_STEPS);
      await beat(page, 700);

      // Show extra Metrics swaps in a static, non-interactive readout — no tooltip left to hover, so just hold long enough to read.
      // Cue 14: "Show extra Metrics reveals a row of summary numbers...".
      await humanClickLocator(page, settingsControl(settingsPanel, 'Show extra Metrics'));
      await beat(page, 10360);

      // Cues 16+17 (entropy/MaxBurst read, then the rename caveat) fold in here — see segment note above.
      await closeSubWindow(page, settingsPanel);
      await beat(page, 53040);
    }

    // ─── Commit By File ─────────────────────────────────────────────────────────────────────
    // No settings flyout — navigate by clicking a folder box (title starts "Folder "), then back out via the breadcrumb button.
    // NOTE: folder-drilling, breadcrumb-popping, and the second commit's rank-hover loop are unnarrated padding —
    // cues 19 and 20 each get one full held beat instead of being spread across those loops.
    {
      const entry = reg('Commit By File');
      await beginTitleCard(page, 'Commit By File');
      await switchVisualization(page, entry.pluginName);
      await endTitleCardWhenReady(page, waitForVisReady(item, entry));

      // Switch to a commit with both additions and deletions so the tooltip's red deletions color shows, not just green.
      // Unnarrated setup.
      const commitSelect = item.locator('input').first();
      await humanFill(page, commitSelect, 'registration');
      await beat(page, 500);
      await page.keyboard.press('Enter');
      // Cue 18 intro plays here — first stable frame once the "registration" commit loads.
      await beat(page, 11960);

      // Plain HTML boxes, not an svg chart — its only <svg> is a tiny folder icon, so svg.first() used to hover that corner instead of a real box.
      // Cue 19: "hover the largest segment, then a small one" + the changeRatio explanation.
      await hoverRankedElement(page, item, '[title]', HOVER_STEPS);
      await beat(page, 19850);

      // Folders (not files) carry a "Folder " title prefix and are clickable; drill down a couple of levels while they exist.
      // Unnarrated — see segment note above.
      for (let depth = 0; depth < 2; depth++) {
        const folderBox = item.locator('[title^="Folder "]').first();
        if (!(await folderBox.count())) break;
        await humanClickLocator(page, folderBox);
        await beat(page, 500);
        await hoverRankedElement(page, item, '[title]', HOVER_STEPS);
        await beat(page, 600);
      }

      // The breadcrumb button only renders once a folder's been entered — keep popping until it's gone so currentPath is back at root.
      // currentPath is local state, not tied to sha, so it must reset before the next commit switch or getFolderByPath() would fail.
      const backButton = item.getByRole('button').filter({ hasText: '<' });
      while (await backButton.count()) {
        await humanClickLocator(page, backButton);
        await beat(page, 500);
      }

      // "test: removed e2e tests" is pure-deletion (every box fully red) — hover several to give the red palette its own moment.
      // Unnarrated setup + b-roll — see segment note above.
      await humanFill(page, commitSelect, 'removed e2e');
      await beat(page, 500);
      await page.keyboard.press('Enter');
      await beat(page, 500);

      for (let rank = 0; rank < 4; rank++) {
        await hoverRankedElement(page, item, '[title]', HOVER_STEPS, rank);
        await beat(page, 600);
      }

      // Cue 20 ("hold on the chart, no further action") settles here for its full 21.37s.
      await beat(page, 21670);
    }

    // ─── Change Frequency ───────────────────────────────────────────────────────────────────
    // No settings flyout — navigation happens via the in-chart "Directory" drawer instead.
    {
      const entry = reg('Change Frequency');
      await beginTitleCard(page, 'Change Frequency');
      await switchVisualization(page, entry.pluginName);
      await endTitleCardWhenReady(page, waitForVisReady(item, entry));

      // Scatter plot: the svg center rarely lands on a data point, so target a circle's own center instead.
      // Cue 21 intro plays here — no dedicated "point out" cue precedes cue 22 in this segment.
      await hoverRankedElement(page, item, 'circle', HOVER_STEPS);
      await beat(page, 12720);

      const dirBtn = item.getByRole('button', { name: 'Directory' });
      if (await dirBtn.count()) {
        await humanClickLocator(page, dirBtn);
        await item.locator('[class*="directoryDrawerOpen"]').waitFor({ state: 'visible' });
        // Cue 22 ("click Directory, then frontend, then src") spans the loop below, which drills 4 levels though the .md calls out 2.
        // Each iteration's beat is sized to cover cue 22's full length across all 4 clicks.
        await beat(page, 700);

        for (const folder of ['frontend', 'src', 'app', 'components']) {
          const entryBtn = item.locator('[class*="directoryEntry"]').filter({ hasText: folder }).first();
          if (await entryBtn.count()) {
            await humanClickLocator(page, entryBtn);
            await item.locator('[class*="breadcrumb"]').getByText(folder).first().waitFor({ state: 'visible', timeout: 5_000 });
            await beat(page, 2700);
          }
        }

        // Show the scatter plot now reflects the drilled-down folder before the drawer closes.
        // Cue 23: "point at a large, brightly colored top-level folder".
        await hoverRankedElement(page, item, 'circle', HOVER_STEPS);
        await beat(page, 8030);

        await humanClickLocator(page, dirBtn);
        await item.locator('[class*="directoryDrawerOpen"]').waitFor({ state: 'hidden' });
      }

      // Cue 24 ("hold on the chart") — the closing directory-stats caveat settles here for its full 38.18s.
      await beat(page, 38700);
    }

    dumpBeatLog('demo-category-commits');
  });
});
