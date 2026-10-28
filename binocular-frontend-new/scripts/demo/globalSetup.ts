import { chromium } from '@playwright/test';
import { VISUALIZATIONS } from '../visualizations.ts';
import { buildDashboard, revealAuthorList, waitForDashboardMounted } from '../seedState.ts';
import { DEMO_ITEM_HEIGHT_UNITS, gotoDemoDashboard } from './demoSetup.ts';
import { waitForVisReady } from './util/demoInteractions.ts';

export default async function globalSetup() {
  const browser = await chromium.launch();
  try {
    const context = await browser.newContext({ baseURL: 'http://localhost:8080' });
    const page = await context.newPage();
    const item = page.locator('#dashboardItem1');
    for (const entry of VISUALIZATIONS) {
      try {
        // Deliberately skips the resize/tab choreography of loadDemoVis() — this only needs each plugin's code to be compiled once.
        await gotoDemoDashboard(page, buildDashboard(entry.pluginName, 40, DEMO_ITEM_HEIGHT_UNITS, entry.settings));
        await waitForDashboardMounted(page);
        await revealAuthorList(page, { keepOpen: true });
        await waitForVisReady(item, entry);
      } catch {
        // Best-effort warmup — a plugin that fails here just pays its own cold-start cost during the real recording instead.
      }
    }
  } finally {
    await browser.close();
  }
}
