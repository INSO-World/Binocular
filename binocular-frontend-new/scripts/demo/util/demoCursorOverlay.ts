// Synthetic on-screen cursor + click ripple for scripted demo-video recordings — CDP video capture never includes the OS cursor.

import type { Page } from '@playwright/test';

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
        // Skipped while frozen (see setCursorOverlayFrozen) so a caller can probe several real positions — genuine
        // mousemove events still reach the page normally — without the visible cursor jumping between them on camera.
        if ((window as unknown as { __demoCursorFrozen__?: boolean }).__demoCursorFrozen__) return;
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
// Also injects .__demoTitleCard__'s CSS (defined inside installCursorOverlay above), which demoTitleCard.ts's functions depend on.
export async function installCursorOverlayEverywhere(page: Page): Promise<void> {
  await page.addInitScript(installCursorOverlay);
  await page.evaluate(installCursorOverlay);
}

// Freezes (or unfreezes) the synthetic cursor's on-screen position. Real mouse/tooltip events keep firing while frozen, so
// a caller can probe candidates invisibly (e.g. hoverChartEntry's jitter retries), then unfreeze for one clean, visible glide.
export async function setCursorOverlayFrozen(page: Page, frozen: boolean): Promise<void> {
  await page.evaluate((f) => {
    (window as unknown as { __demoCursorFrozen__?: boolean }).__demoCursorFrozen__ = f;
  }, frozen);
}
