// Hovers a StackedAreaChart at a single, clearly-visible data point so its tooltip reads well on camera.

import type { Locator, Page } from '@playwright/test';
import { humanHoverLocator, humanMove } from './demoInteractions.ts';
import { setCursorOverlayFrozen } from './demoCursorOverlay.ts';

// Points inside a stacked band: only isPointInFill hits yield a tooltip; __data__.side splits positive/negative, __data__.key names the series.
// keyIncludes targets one series (e.g. "Closed"); keyExcludes skips one case-insensitively (e.g. skip "unassigned" for a real author).
async function chartEntryPoints(
  item: Locator,
  side: 'positive' | 'negative',
  keyIncludes?: string,
  keyExcludes?: string,
): Promise<{ x: number; y: number }[]> {
  return item
    .locator('svg')
    .first()
    .evaluate(
      (svg, { wantedSide, wantedKey, unwantedKey }) => {
        const candidates: { x: number; y: number; thicknessPx: number }[] = [];
        // Searches outward from center, not left-to-right — the leftmost valid column is often a near-zero sliver that reads as nothing on camera.
        // Dense 2% steps (~41 columns), not coarse fractions: a coarse grid can step right over a spiky series' single-point spike.
        const fxOrder = Array.from({ length: 41 }, (_, i) => 0.5 + (i % 2 === 0 ? 1 : -1) * Math.ceil(i / 2) * 0.02).filter(
          (f) => f > 0 && f < 1,
        );
        const VERTICAL_SCAN_STEPS = 40;

        for (const path of Array.from(svg.querySelectorAll<SVGPathElement>('g.areas path'))) {
          const datum = (path as SVGPathElement & { __data__?: { side?: string; key?: string } }).__data__;
          if (datum?.side !== wantedSide) continue;
          if (wantedKey && !datum?.key?.includes(wantedKey)) continue;
          if (unwantedKey && datum?.key?.toLowerCase().includes(unwantedKey.toLowerCase())) continue;
          const ctm = path.getScreenCTM();
          const box = path.getBBox();
          if (!ctm || box.width === 0 || box.height === 0) continue;
          // The handler sits on <g> wrapping g.areas; a point only works if that group is what's under the cursor, not an overlay on top.
          const handlerGroup = path.closest('g.areas')?.parentElement;
          // Local-y-units-to-screen-pixels scale factor, to rank candidates by their actual on-screen thickness.
          const pxPerUnitY = Math.hypot(ctm.c, ctm.d);

          for (const fx of fxOrder) {
            const x = box.x + box.width * fx;
            // A key's area is one contiguous vertical run at a given x — scan top to bottom for that run so thickness can be measured and ranked.
            let runStart: number | null = null;
            let best: { start: number; end: number } | null = null;
            for (let i = 0; i <= VERTICAL_SCAN_STEPS; i++) {
              const y = box.y + (box.height * i) / VERTICAL_SCAN_STEPS;
              const inside = path.isPointInFill(new DOMPoint(x, y));
              if (inside && runStart === null) runStart = y;
              if (!inside && runStart !== null) {
                if (!best || y - runStart > best.end - best.start) best = { start: runStart, end: y };
                runStart = null;
              }
            }
            if (runStart !== null) {
              const y = box.y + box.height;
              if (!best || y - runStart > best.end - best.start) best = { start: runStart, end: y };
            }
            if (!best) continue;

            const point = new DOMPoint(x, (best.start + best.end) / 2);
            const screen = point.matrixTransform(ctm);
            const hit = document.elementFromPoint(screen.x, screen.y);
            if (!hit || !handlerGroup?.contains(hit)) continue;
            candidates.push({ x: screen.x, y: screen.y, thicknessPx: (best.end - best.start) * pxPerUnitY });
          }
        }
        // Thickest bands first — a near-invisible sliver (e.g. a permanent baseline) is a last resort, not the first hover target; nothing is discarded.
        candidates.sort((a, b) => b.thicknessPx - a.thicknessPx);
        return candidates.map(({ x, y }) => ({ x, y }));
      },
      { wantedSide: side, wantedKey: keyIncludes, unwantedKey: keyExcludes },
    );
}

// A candidate's (x,y) comes from the rendered curve, but the app's tooltip re-derives x to the nearest data index and tests
// exact unsmoothed band bounds — a visually-filled point can miss on either axis, so both get a wide jitter sweep below.
const CANDIDATE_JITTER: { dx: number; dy: number }[] = [
  { dx: 0, dy: 0 },
  { dx: -3, dy: 0 },
  { dx: 3, dy: 0 },
  { dx: 0, dy: -3 },
  { dx: 0, dy: 3 },
  { dx: -6, dy: 0 },
  { dx: 6, dy: 0 },
  { dx: 0, dy: -6 },
  { dx: 0, dy: 6 },
  { dx: -9, dy: 0 },
  { dx: 9, dy: 0 },
  { dx: 0, dy: -9 },
  { dx: 0, dy: 9 },
  { dx: -12, dy: 0 },
  { dx: 12, dy: 0 },
  { dx: 0, dy: -12 },
  { dx: 0, dy: 12 },
  { dx: 0, dy: -16 },
  { dx: 0, dy: 16 },
  { dx: 0, dy: -20 },
  { dx: 0, dy: 20 },
  { dx: 0, dy: -25 },
  { dx: 0, dy: 25 },
];

// Geometric thickness (the sort key) is only a proxy for actual data value — different columns sit at different slopes/zoom.
// Comparing several resolved candidates' real values, not stopping at the first that works, lands on the most prominent point.
const MAX_CANDIDATES_TO_COMPARE = 8;

// Moves the cursor onto the most prominent (highest-magnitude) data entry so its tooltip shows on camera.
// `keyIncludes` narrows to one series (e.g. "Closed"); `keyExcludes` skips one (e.g. "unassigned") to land on a real author.
export async function hoverChartEntry(
  page: Page,
  item: Locator,
  side: 'positive' | 'negative' = 'positive',
  steps?: number,
  keyIncludes?: string,
  keyExcludes?: string,
): Promise<void> {
  const tooltipValue = item.locator('.tooltip-value');
  const tooltipLabel = item.locator('.tooltip-label');
  const cursor = page.locator('#__demoCursor__');

  // Where the visible cursor currently rests, so a resolved point can still glide there cleanly for the camera below,
  // instead of the jitter search itself (real mouse jumps, invisible while frozen) being what the recording shows.
  const startBox = await cursor.boundingBox().catch(() => null);
  const start = startBox ? { x: startBox.x + 11, y: startBox.y + 11 } : null; // 11 ≈ half the 22px cursor glyph

  let best: { x: number; y: number; magnitude: number } | null = null;
  await setCursorOverlayFrozen(page, true);
  // The tooltip is a separate fixed element outside the cursor overlay, so freezing the cursor doesn't stop it teleporting per candidate.
  // Force opacity (not visibility, which isVisible() below still reads) to hide it while leaving our success signal untouched.
  await tooltipValue.evaluate((el) => {
    const container = (el.closest('[style*="visibility"]') as HTMLElement | null) ?? (el as HTMLElement);
    container.dataset.demoHiddenOpacity = container.style.opacity;
    container.style.setProperty('opacity', '0', 'important');
  });
  try {
    const points = (await chartEntryPoints(item, side, keyIncludes, keyExcludes)).slice(0, MAX_CANDIDATES_TO_COMPARE);
    for (const point of points) {
      for (const { dx, dy } of CANDIDATE_JITTER) {
        await page.mouse.move(point.x + dx, point.y + dy);
        await page.waitForTimeout(80);
        if (!(await tooltipValue.isVisible())) continue;
        const text = (await tooltipValue.innerText()).trim();
        if (side === 'negative' && !text.startsWith('-')) continue;
        const label = await tooltipLabel.innerText();
        if (keyIncludes && !label.includes(keyIncludes)) continue;
        if (keyExcludes && label.toLowerCase().includes(keyExcludes.toLowerCase())) continue;
        const magnitude = Math.abs(parseFloat(text));
        if (!Number.isNaN(magnitude) && (!best || magnitude > best.magnitude)) {
          best = { x: point.x + dx, y: point.y + dy, magnitude };
        }
        break; // this candidate resolved (or its jitter sweep is exhausted) — move on to the next candidate to compare
      }
    }
    // Silent reset back to the pre-search resting spot — still frozen, so this doesn't flash on screen — giving the
    // glide below a real start-to-end motion instead of a zero-distance "move" that would just snap into place.
    if (best && start) await page.mouse.move(start.x, start.y);
  } finally {
    await tooltipValue.evaluate((el) => {
      const container = (el.closest('[style*="visibility"]') as HTMLElement | null) ?? (el as HTMLElement);
      const prevOpacity = container.dataset.demoHiddenOpacity;
      delete container.dataset.demoHiddenOpacity;
      if (prevOpacity) container.style.setProperty('opacity', prevOpacity);
      else container.style.removeProperty('opacity');
    });
    await setCursorOverlayFrozen(page, false);
  }

  if (best) {
    await humanMove(page, best.x, best.y, steps);
    await page.waitForTimeout(500);
    return;
  }
  // Nothing accepted the cursor — park on the chart rather than fail the recording, but warn, since a silent no-op is how a tooltip-less scene happens.
  console.warn(
    `hoverChartEntry: no ${side}${keyIncludes ? ` "${keyIncludes}"` : ''} entry took the cursor — parking on the chart without a tooltip`,
  );
  await humanHoverLocator(page, item.locator('svg').first(), steps);
}

// Hovers the nth-largest on-screen match for selector (rank 0 = largest), so the cursor lands on a real entry; rank clamps once a filter shrinks the list.
export async function hoverRankedElement(page: Page, item: Locator, selector: string, steps?: number, rank = 0): Promise<void> {
  const point = await item.evaluate(
    (root, { sel, rank }) => {
      const yFractions = [0.5, 0.15, 0.85, 0.3, 0.7, 0.05, 0.95];
      const svgRoot = root.querySelector('svg');
      const candidates: { x: number; y: number; area: number }[] = [];
      for (const el of Array.from(root.querySelectorAll<HTMLElement | SVGElement>(sel))) {
        const rect = el.getBoundingClientRect();
        if (rect.width <= 0 || rect.height <= 0) continue;
        const x = rect.x + rect.width / 2;
        // Also accepts a same-column sibling stacked on top (e.g. a bar's co-author segment covering the whole bar) via x/width match.
        // A real overlay (settings panel) still gets rejected.
        const hitPoint = yFractions
          .map((fy) => ({ x, y: rect.y + rect.height * fy }))
          .find(({ x, y }) => {
            const hit = document.elementFromPoint(x, y);
            if (!hit) return false;
            if (el.contains(hit)) return true;
            if (!svgRoot?.contains(hit)) return false;
            const hitRect = hit.getBoundingClientRect();
            return Math.abs(hitRect.x - rect.x) < 2 && Math.abs(hitRect.width - rect.width) < 2;
          });
        if (!hitPoint) continue;
        candidates.push({ x: hitPoint.x, y: hitPoint.y, area: rect.width * rect.height });
      }
      candidates.sort((a, b) => b.area - a.area);
      if (candidates.length === 0) return null;
      return candidates[Math.min(rank, candidates.length - 1)];
    },
    { sel: selector, rank },
  );

  if (!point) {
    console.warn(`hoverRankedElement: no visible "${selector}" element found — parking on the chart instead`);
    await humanHoverLocator(page, item, steps);
    return;
  }
  await humanMove(page, point.x, point.y, steps);
}
