// Records the Expertise category video (Code Expertise, Knowledge Radar; Code Hotspots excluded as popoutOnly); offline via Mock Data.
// Run: npm run demo:record; render via render-demo-videos.mjs.

import { test } from '@playwright/test';
import { installCursorOverlayEverywhere } from './util/demoCursorOverlay.ts';
import {
  humanHoverLocator,
  humanClickLocator,
  humanSelectOption,
  beat,
  settingsControl,
  closeSubWindow,
  dumpBeatLog,
  resetBeatClock,
} from './util/demoInteractions.ts';
import { hoverRankedElement } from './util/demoChartHover.ts';
import { beginTitleCard, endTitleCardWhenReady } from './util/demoTitleCard.ts';
import { switchVisualization, loadDemoVis } from './util/demoDashboardSetup.ts';

// Faster cursor travel (default humanMove is 25 steps) for snappier hovers, paired with longer beats so the viewer can still read the tooltip.
const HOVER_STEPS = 10;

test.describe('Demo video — categories', () => {
  test('Category: Expertise', async ({ page }) => {
    test.setTimeout(5 * 60_000);

    resetBeatClock();
    await installCursorOverlayEverywhere(page);

    // Id-agnostic: switchVisualization() never reuses an item id, so these re-resolving locators stay correct across both segments.
    const item = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
    const settingsPanel = page.locator('[id$="_settings"]');

    // ─── Code Expertise ─────────────────────────────────────────────────────────────────────
    {
      await loadDemoVis(page, 'Code Expertise', { title: 'Code Expertise' });
      await page.waitForSelector('svg text');

      // Ring chart: segment size, owned/replaced band, CI pass/fail arc, commit-count band. The svg center is empty space
      // between segments, so target a real developer segment via its dotted commits-path (id ends "_commitsPath") instead.
      await hoverRankedElement(page, item, 'path[id$="_commitsPath"]', HOVER_STEPS);
      await beat(page, 40280); // cues 1+2+3+4 — holds through 4 narration cues describing this one segment (size, bands, arc, dotted count)

      await humanClickLocator(page, item.locator('[class*="settingsButton"]'));
      await settingsPanel.waitFor({ state: 'visible' });
      await beat(page, 700); // un-narrated b-roll — give the Branch dropdown's async allBranches fetch a moment to land

      const branchSelect = settingsControl(settingsPanel, 'Branch:');
      const branchOptionCount = await branchSelect.locator('option').count();
      if (branchOptionCount > 1) {
        await humanSelectOption(page, branchSelect, { index: 1 });
        await beat(page, 7000); // cue 5 — narrates the Branch selector itself
        // Show the ring chart re-rendered for the newly selected branch, hovering a real segment rather than the center.
        await hoverRankedElement(page, item, 'path[id$="_commitsPath"]', HOVER_STEPS);
        await beat(page, 4260); // cue 6 — "the tempting read: resident expert"
      }

      await closeSubWindow(page, settingsPanel);
      await beat(page, 19990); // cue 7 — long "be careful with expert" correction, held on the closed panel
    }

    // ─── Knowledge Radar ────────────────────────────────────────────────────────────────────
    // No settings component for this plugin.
    {
      await beginTitleCard(page, 'Knowledge Radar');
      await switchVisualization(page, 'Knowledge Radar');
      await endTitleCardWhenReady(page, page.waitForSelector('svg text'));

      // Hover a real top-level folder (rank 0 = the largest axis label on screen) instead of the empty chart center.
      await hoverRankedElement(page, item, '.axis-label-group', HOVER_STEPS);
      await beat(page, 8760); // cue 8 — intro: "Knowledge Radar plots expertise differently..."

      // Each axis label is its own package/folder segment; drilling into frontend/src/app/components takes one click per level.
      // Cue 9 (~8.4s, "clicking drills deeper...") is spread evenly across all click/hover beats below (4 clicks + 3 hovers).
      const packagePath = ['frontend', 'src', 'app', 'components'];
      for (let i = 0; i < packagePath.length; i++) {
        const label = item.locator('.axis-label-group').filter({ hasText: new RegExp(`^${packagePath[i]}$`) });
        if (!(await label.count())) break;
        await humanClickLocator(page, label);
        await beat(page, 1700); // cue 9 (first iteration only) — "clicking drills deeper..." spread across this whole loop
        // The last level gets its own targeted hover below instead of this generic one — skip it here.
        if (i < packagePath.length - 1) {
          // Show the freshly drawn ring by hovering one of its own folders, not the blank center.
          await hoverRankedElement(page, item, '.axis-label-group', HOVER_STEPS);
          await beat(page, 1200);
        }
      }

      // "event-search" has several contributing authors (74/13/9/4%) — hover it so the tooltip shows a real multi-author breakdown.
      // Holds through both the "natural read" cue and the long ownership-ratio correction cue that follows.
      const multiAuthorComponent = item.locator('.axis-label-group').filter({ hasText: /^event-search$/ });
      if (await multiAuthorComponent.count()) {
        await humanHoverLocator(page, multiAuthorComponent, HOVER_STEPS);
        await beat(page, 42730); // cues 10+11
      }
    }

    dumpBeatLog('demo-category-expertise');
  });
});
