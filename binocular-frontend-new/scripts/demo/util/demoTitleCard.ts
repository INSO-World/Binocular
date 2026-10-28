// Baked-in title cards for scripted demo-video recordings, since each per-visualization video is uploaded as its own short YouTube clip.
// Relies on .__demoTitleCard__'s CSS, which installCursorOverlay() (demoCursorOverlay.ts) injects — callers must install the cursor overlay first.

import type { Page } from '@playwright/test';

export async function showTitleCard(page: Page, text: string, ms = 1200): Promise<void> {
  await page.evaluate((label) => {
    const card = document.createElement('div');
    card.className = '__demoTitleCard__';
    card.textContent = label;
    document.body.appendChild(card);
  }, text);
  await page.waitForTimeout(ms);
  await endTitleCard(page);
}

// Split in two so a "ready" check isn't raced against loadDemoVis()'s own reload; beginTitleCard shows the card, endTitleCardWhenReady waits then fades it.
export async function beginTitleCard(page: Page, text: string): Promise<void> {
  await page.evaluate((label) => {
    const card = document.createElement('div');
    card.className = '__demoTitleCard__';
    card.textContent = label;
    document.body.appendChild(card);
  }, text);
}

// Fades out whatever title card is currently up and waits for the transition to finish.
export async function endTitleCard(page: Page): Promise<void> {
  await page.evaluate(() => {
    const card = document.querySelector('.__demoTitleCard__') as HTMLElement | null;
    if (!card) return;
    card.style.opacity = '0';
    setTimeout(() => card.remove(), 400);
  });
  await page.waitForTimeout(400);
}

export async function endTitleCardWhenReady(
  page: Page,
  ready: Promise<unknown>,
  opts: { minMs?: number; maxMs?: number } = {},
): Promise<void> {
  const { minMs = 1000, maxMs = 10000 } = opts;
  await Promise.all([page.waitForTimeout(minMs), Promise.race([ready.catch(() => {}), page.waitForTimeout(maxMs)])]);
  await endTitleCard(page);
}
