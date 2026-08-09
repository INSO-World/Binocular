// Vite lazily pre-bundles each new dependency it discovers, so load every plugin here first — outside any recorded clip — to pre-bundle chart-specific code once.
import { chromium } from '@playwright/test';
import { VISUALIZATIONS, loadVis } from '../screenshots.setup.ts';
import { waitForVisReady } from './demoHelpers.ts';

export default async function globalSetup() {
  const browser = await chromium.launch();
  try {
    const context = await browser.newContext({ baseURL: 'http://localhost:8080' });
    const page = await context.newPage();
    const item = page.locator('#dashboardItem1');
    for (const entry of VISUALIZATIONS) {
      try {
        await loadVis(page, entry.pluginName, entry.settings);
        await waitForVisReady(item, entry);
      } catch {
        // Best-effort warmup — a plugin that fails here just pays its own cold-start cost during the real recording instead.
      }
    }
  } finally {
    await browser.close();
  }
}
