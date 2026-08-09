// Shared helpers for scripted demo-video recordings — human-paced Playwright interactions, not correctness testing; screenshots.setup.ts remains the source of truth for seeding.

import type { Locator, Page } from '@playwright/test';
import { buildDashboard, gotoSeededDashboard, revealAuthorList, waitForDashboardMounted } from '../screenshots.setup.ts';

// ─── Synthetic cursor overlay ──────────────────────────────────────────────────

// CDP video capture never includes the OS cursor, so this draws a synthetic one; must be self-contained since Playwright evaluates it inside the page.
export function installCursorOverlay() {
  const CURSOR_ID = '__demoCursor__';

  // document.body is null at document_start, so defer body-dependent setup to DOMContentLoaded (resolves immediately if body already exists).
  function attach() {
    if (document.getElementById(CURSOR_ID)) return;

    // Tilted arrow glyph; the -1px/-2px margin nudges its top-left so the tip (hotspot) lands exactly on the real cursor position.
    const cursorSvg =
      '<svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 22 22">' +
      '<path d="M2 1 L2 17 L6.5 13.3 L9.2 19.2 L12 17.9 L9.3 12 L15 12 Z" ' +
      'fill="white" stroke="black" stroke-width="1.3" stroke-linejoin="round"/></svg>';
    const cursorUrl = `url("data:image/svg+xml,${encodeURIComponent(cursorSvg)}")`;

    const style = document.createElement('style');
    style.textContent = `
      #${CURSOR_ID} {
        position: fixed; top: 0; left: 0; width: 22px; height: 22px; margin: -1px 0 0 -2px;
        background-image: ${cursorUrl}; background-repeat: no-repeat; background-size: contain;
        filter: drop-shadow(0 1px 2px rgba(0,0,0,0.45));
        pointer-events: none; z-index: 2147483647; will-change: transform;
      }
      .__demoRipple__ {
        position: fixed; width: 40px; height: 40px; margin: -20px 0 0 -20px; border-radius: 50%;
        border: 3px solid rgba(255, 60, 60, 0.9); pointer-events: none; z-index: 2147483646;
        animation: __demoRippleAnim__ 0.5s ease-out forwards;
      }
      @keyframes __demoRippleAnim__ {
        from { transform: scale(0.3); opacity: 1; }
        to   { transform: scale(1.4); opacity: 0; }
      }
      .__demoTitleCard__ {
        position: fixed; inset: 0; display: flex; align-items: center; justify-content: center;
        background: rgba(15, 15, 20, 0.88); color: white; font: 700 3rem/1.2 sans-serif;
        z-index: 2147483645; transition: opacity 0.4s ease; pointer-events: none;
      }
    `;
    document.head.appendChild(style);

    // showModal() dialogs render in the browser's top layer above any z-index, so re-parent the cursor/ripple into the open dialog to stay visible.
    function currentOverlayParent(): HTMLElement {
      return document.querySelector('dialog:modal') ?? document.body;
    }

    const cursor = document.createElement('div');
    cursor.id = CURSOR_ID;
    currentOverlayParent().appendChild(cursor);

    window.addEventListener(
      'mousemove',
      (e) => {
        const parent = currentOverlayParent();
        if (cursor.parentElement !== parent) parent.appendChild(cursor);
        cursor.style.transform = `translate3d(${e.clientX}px, ${e.clientY}px, 0)`;
      },
      { passive: true },
    );

    window.addEventListener(
      'mousedown',
      (e) => {
        const ripple = document.createElement('div');
        ripple.className = '__demoRipple__';
        ripple.style.left = `${e.clientX}px`;
        ripple.style.top = `${e.clientY}px`;
        currentOverlayParent().appendChild(ripple);
        ripple.addEventListener('animationend', () => ripple.remove());
        setTimeout(() => ripple.remove(), 700);
      },
      { passive: true },
    );
  }

  if (document.body) {
    attach();
  } else {
    document.addEventListener('DOMContentLoaded', attach, { once: true });
  }
}

// Registers the overlay for future navigations AND evaluates it once now, so the very first showTitleCard() (before any goto()) is styled too.
export async function installCursorOverlayEverywhere(page: Page): Promise<void> {
  await page.addInitScript(installCursorOverlay);
  await page.evaluate(installCursorOverlay);
}

// ─── Human-paced interaction helpers ───────────────────────────────────────────

// Intermediate steps so the cursor animates smoothly and drag/resize handlers reading movementX/Y get realistic deltas instead of one jump.
export async function humanMove(page: Page, x: number, y: number, steps = 25): Promise<void> {
  await page.mouse.move(x, y, { steps });
}

export async function humanClick(page: Page, x: number, y: number): Promise<void> {
  await humanMove(page, x, y);
  await page.mouse.down();
  await page.waitForTimeout(80);
  await page.mouse.up();
}

// A preceding settings change can trigger a re-render that detaches the element before boundingBox() runs — retry briefly to ride it out.
async function boundingBoxWithRetry(locator: Locator, attempts = 5, delayMs = 200) {
  for (let i = 0; i < attempts; i++) {
    const box = await locator.boundingBox();
    if (box) return box;
    if (i < attempts - 1) await locator.page().waitForTimeout(delayMs);
  }
  return null;
}

// Clicks the locator's visual center via real mouse movement so the synthetic cursor animates to it on camera.
export async function humanClickLocator(page: Page, locator: Locator): Promise<void> {
  const box = await boundingBoxWithRetry(locator);
  if (!box) throw new Error('humanClickLocator: locator has no bounding box (not visible/attached)');
  await humanClick(page, box.x + box.width / 2, box.y + box.height / 2);
}

export async function humanHoverLocator(page: Page, locator: Locator): Promise<void> {
  const box = await boundingBoxWithRetry(locator);
  if (!box) throw new Error('humanHoverLocator: locator has no bounding box (not visible/attached)');
  await humanMove(page, box.x + box.width / 2, box.y + box.height / 2);
}

// Every settings.tsx wraps one control per <label> with a text sibling, so a label-text lookup covers all of them; `:visible` skips the closed panel's dormant duplicate controls.
export function settingsControl(container: Locator, labelText: string): Locator {
  return container.locator('label', { hasText: labelText }).locator('select:visible, input:visible');
}

// Keeps Visualizations/Authors open throughout every category video; idempotent (checks tabHandleSelected*) so it's safe to call again on every plugin swap.
export async function openTabIfClosed(page: Page, tabName: string): Promise<void> {
  const tab = page.locator(`#tab_${tabName}`);
  const cls = (await tab.getAttribute('class')) ?? '';
  if (!cls.includes('tabHandleSelected')) {
    await humanClickLocator(page, tab);
  }
}

// Native <select> option lists render via the OS and aren't captured on camera, so just hover for the visual before driving it with selectOption().
export async function humanSelectOption(page: Page, select: Locator, value: string | { index: number } | { label: string }): Promise<void> {
  await humanHoverLocator(page, select);
  await page.waitForTimeout(150);
  await select.selectOption(value);
}

// Clicks the input, clears it, and types the new value with per-character delay so it's visible in the recording.
export async function humanFill(page: Page, input: Locator, value: string): Promise<void> {
  await humanClickLocator(page, input);
  await input.fill('');
  await input.pressSequentially(value, { delay: 60 });
}

// Mirrors screenshots.test.ts's wait logic: each VISUALIZATIONS entry declares exactly one of these three readiness signals.
export async function waitForVisReady(
  item: Locator,
  opts: { waitFor?: string; waitForText?: string; waitForHidden?: string },
): Promise<void> {
  if (opts.waitFor) {
    await item.locator(opts.waitFor).first().waitFor({ state: 'visible', timeout: 20_000 });
  } else if (opts.waitForText) {
    await item.getByText(opts.waitForText).first().waitFor({ state: 'visible', timeout: 20_000 });
  } else if (opts.waitForHidden) {
    await item.getByText(opts.waitForHidden).first().waitFor({ state: 'hidden', timeout: 20_000 });
  }
}

export async function mockBackendRoutes(page: Page): Promise<void> {
  await page.route('/api/**', (route) => route.fulfill({ status: 200, contentType: 'application/json', body: '{}' }));
  await page.route('/graphQl', (route) =>
    route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ data: {} }) }),
  );
  await page.route('/wsapi/**', (route) => route.abort());
}

export async function humanDrag(
  page: Page,
  from: { x: number; y: number },
  to: { x: number; y: number },
  intermediateSteps = 6,
): Promise<void> {
  await humanMove(page, from.x, from.y);
  await page.mouse.down();
  for (let i = 1; i <= intermediateSteps; i++) {
    const t = i / intermediateSteps;
    await humanMove(page, from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t, 8);
    await page.waitForTimeout(40);
  }
  await page.waitForTimeout(120);
  await page.mouse.up();
}

// Named wait — holds the current frame long enough for a viewer to read it.
export function beat(page: Page, ms = 1200): Promise<void> {
  return page.waitForTimeout(ms);
}

// Waits until a locator's bounding box stops moving — e.g. Collaboration's networkChart nodes keep drifting after a resize-triggered reheat.
export async function waitForLocatorStable(locator: Locator, tries = 30, intervalMs = 200): Promise<void> {
  let previous: { x: number; y: number } | null = null;
  for (let i = 0; i < tries; i++) {
    const box = await locator.boundingBox();
    if (box && previous && Math.abs(box.x - previous.x) < 1 && Math.abs(box.y - previous.y) < 1) return;
    previous = box ? { x: box.x, y: box.y } : null;
    await locator.page().waitForTimeout(intervalMs);
  }
}

// ─── Demo dashboard bootstrapping ──────────────────────────────────────────────

// A reasonable starting size (in fine grid units) — the actual fit gets corrected by resizeToViewableArea() once Authors/Visualizations are both open.
const FIRST_ITEM_HEIGHT_UNITS = 13 * 2;

// Reload-based bootstrap for the first plugin in a demo scene; only begins the title card — callers call endTitleCardWhenReady(page, readyPromise) themselves.
export async function loadFirstVis(
  page: Page,
  pluginName: string,
  title: string,
  itemSettings?: object,
  sprintsState?: string,
): Promise<void> {
  await gotoSeededDashboard(page, buildDashboard(pluginName, 40, FIRST_ITEM_HEIGHT_UNITS, itemSettings), sprintsState);
  await beginTitleCard(page, title);
  await waitForDashboardMounted(page);
  await revealAuthorList(page, { keepOpen: true });
  await openTabIfClosed(page, 'Visualizations');
  await resizeToViewableArea(page, page.locator('[id^="dashboardItem"]:not([id*="_"])').first());
}

// Walks up from a dashboard item to find the `.dashboard` scroll container by computed style, since a demo video shouldn't show scrolled-off content.
async function getViewableDashboardBox(page: Page): Promise<{ x: number; y: number; width: number; height: number } | null> {
  return page.evaluate(() => {
    let el: Element | null = document.querySelector('[id^="dashboardItem"]:not([id*="_"])');
    while (el && el !== document.body) {
      if (getComputedStyle(el).overflowY === 'scroll') break;
      el = el.parentElement;
    }
    if (!el || el === document.body) return null;
    const rect = el.getBoundingClientRect();
    return { x: rect.x, y: rect.y, width: rect.width, height: rect.height };
  });
}

// dashboard.tsx's `cellSize` updates asynchronously via ResizeObserver after a tab open/close, so poll a grid cell's rendered width until it settles.
async function waitForCellSizeStable(page: Page): Promise<void> {
  let previous: number | null = null;
  for (let i = 0; i < 15; i++) {
    const current = await page.evaluate(() => document.getElementById('cellY0X0')?.getBoundingClientRect().width ?? null);
    if (current !== null && previous !== null && Math.abs(current - previous) < 0.5) return;
    previous = current;
    await page.waitForTimeout(100);
  }
}

// Drags an item's resize handle to the dashboard's visible edge so it fills the screen; overshooting is safe since dashboardReducer.ts clamps it, and it retries once in case cellSize hadn't settled yet.
export async function resizeToViewableArea(page: Page, item: Locator): Promise<void> {
  for (let attempt = 0; attempt < 2; attempt++) {
    await waitForCellSizeStable(page);

    const viewableBox = await getViewableDashboardBox(page);
    if (!viewableBox) return;

    const resizeHandle = item.locator('[class*="dashboardItemResizeBarBottomRight"]');
    const handleBox = await resizeHandle.boundingBox();
    if (!handleBox) return;

    await humanDrag(
      page,
      { x: handleBox.x + handleBox.width / 2, y: handleBox.y + handleBox.height / 2 },
      { x: viewableBox.x + viewableBox.width, y: viewableBox.y + viewableBox.height },
      4,
    );
    await page.waitForTimeout(200);

    // Items are inset up to ~20px from the true grid edge, so compare the gap to viewableBox's edge (with slack) instead of raw width/height.
    const resultBox = await item.boundingBox();
    if (
      resultBox &&
      viewableBox.x + viewableBox.width - (resultBox.x + resultBox.width) < 30 &&
      viewableBox.y + viewableBox.height - (resultBox.y + resultBox.height) < 30
    ) {
      return;
    }
  }
}

// Swaps the dashboard's item to a different plugin without a reload; the new item gets default settings, so callers needing a preset must re-apply it.
export async function switchVisualization(page: Page, pluginName: string): Promise<Locator> {
  await openTabIfClosed(page, 'Visualizations');
  await page.waitForTimeout(400);

  const currentItem = page.locator('[id^="dashboardItem"]:not([id*="_"])').first();
  const currentItemId = await currentItem.getAttribute('id');

  await humanClickLocator(page, currentItem.locator('[class*="settingsButton"]'));
  const currentSettingsPanel = page.locator(`#${currentItemId}_settings`);
  await currentSettingsPanel.waitFor({ state: 'visible' });
  await page.waitForTimeout(300);

  await humanClickLocator(page, currentSettingsPanel.getByRole('button', { name: 'Delete', exact: true }));
  await page.waitForTimeout(100);

  await humanClickLocator(page, page.locator('button.btn-square.btn-primary.btn-sm'));
  const overviewDialog = page.locator('#visualizationOverview');
  await overviewDialog.waitFor({ state: 'visible' });
  await page.waitForTimeout(100);

  await humanFill(page, overviewDialog.getByPlaceholder('Search'), pluginName);
  await page.waitForTimeout(300);

  await humanClickLocator(page, overviewDialog.locator(`button:has(img[alt="${pluginName}"])`));
  await page.keyboard.press('Escape');
  await page.waitForTimeout(300);

  const newItem = page.locator('[id^="dashboardItem"]:not([id*="_"])').filter({ hasText: pluginName });
  await newItem.waitFor({ state: 'attached', timeout: 10_000 });
  await page.waitForTimeout(300);

  await resizeToViewableArea(page, newItem);
  await page.waitForTimeout(300);

  return newItem;
}

// Baked-in title card for a scene/clip, since each per-visualization video is uploaded as its own short YouTube clip.
export async function showTitleCard(page: Page, text: string, ms = 1200): Promise<void> {
  await page.evaluate((label) => {
    const card = document.createElement('div');
    card.className = '__demoTitleCard__';
    card.textContent = label;
    document.body.appendChild(card);
  }, text);
  await page.waitForTimeout(ms);
  await page.evaluate(() => {
    const card = document.querySelector('.__demoTitleCard__') as HTMLElement | null;
    if (!card) return;
    card.style.opacity = '0';
    setTimeout(() => card.remove(), 400);
  });
  await page.waitForTimeout(400);
}

// Split in two so a "ready" check isn't raced against loadFirstVis()'s own reload; beginTitleCard shows the card, endTitleCardWhenReady waits then fades it.
export async function beginTitleCard(page: Page, text: string): Promise<void> {
  await page.evaluate((label) => {
    const card = document.createElement('div');
    card.className = '__demoTitleCard__';
    card.textContent = label;
    document.body.appendChild(card);
  }, text);
}

export async function endTitleCardWhenReady(
  page: Page,
  ready: Promise<unknown>,
  opts: { minMs?: number; maxMs?: number } = {},
): Promise<void> {
  const { minMs = 1000, maxMs = 10000 } = opts;
  await Promise.all([page.waitForTimeout(minMs), Promise.race([ready.catch(() => {}), page.waitForTimeout(maxMs)])]);
  await page.evaluate(() => {
    const card = document.querySelector('.__demoTitleCard__') as HTMLElement | null;
    if (!card) return;
    card.style.opacity = '0';
    setTimeout(() => card.remove(), 400);
  });
  await page.waitForTimeout(400);
}
