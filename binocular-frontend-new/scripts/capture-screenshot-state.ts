/**
 * capture-screenshot-state.ts
 *
 * One-time setup: opens a visible browser so you can configure the data plugin
 * connection, then automatically saves the browser state once setup is complete.
 * The saved state is replayed by the Playwright screenshot tests so they work
 * against your real ArangoDB data without any interactive prompts.
 *
 * Usage:
 *   npm run screenshots:setup
 *
 * Requirements:
 *   - Backend + frontend running: npm run dev:concurrently
 *   - Data indexed in ArangoDB
 *
 * Workflow:
 *   1. Browser opens and the Binocular app loads.
 *   2. If the setup dialog appears, configure your data plugin on the Database
 *      page, continue through the steps, and click Save on the Summary page.
 *   3. After the page reloads with your settings, state is saved automatically.
 *
 * To re-capture (e.g. after changing the data plugin):
 *   Delete scripts/screenshot-state.json and run this again.
 */

import { chromium } from 'playwright';
import path from 'path';
import fs from 'fs';

const BASE_URL = process.env.BINOCULAR_URL ?? 'http://localhost:8080';
const STATE_FILE = path.resolve('scripts/screenshot-state.json');

(async () => {
  console.log(`\nBinocular screenshot state capture`);
  console.log(`  App URL: ${BASE_URL}`);
  console.log(`  Output:  ${STATE_FILE}\n`);

  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
  const page = await context.newPage();

  await page.goto(BASE_URL, { waitUntil: 'load' });

  // Wait for React to mount before checking dialog state.
  // The setup dialog is always in the DOM once React renders; showModal() is
  // called by a useEffect when settings are not yet initialized.
  await page.waitForSelector('#setupDialog', { state: 'attached', timeout: 15_000 });

  // Check if settings are already initialized (app was previously configured).
  const alreadyReady = await page.evaluate(
    ([sk, dk]) => {
      try {
        const s = JSON.parse(localStorage.getItem(sk) ?? '{}');
        const d = JSON.parse(localStorage.getItem(dk) ?? '{}');
        return s?.initialized === true && d?.initialized === true;
      } catch {
        return false;
      }
    },
    ['bino_settingsStateV1', 'bino_dashboardStateV1'],
  );

  if (!alreadyReady) {
    console.log('Setup dialog detected — complete the setup wizard:');
    console.log('  Database page → configure your data plugin → Next → … → Save\n');

    // Poll until settings are initialized. We use a manual loop so that we
    // survive the location.reload() that the Save button triggers — navigations
    // cause waitForFunction to throw.
    let ready = false;

    while (!ready) {
      try {
        // Wait for the current page to finish loading (handles reload after Save).
        await page.waitForLoadState('domcontentloaded', { timeout: 10_000 });

        ready = await page.evaluate(
          ([sk, dk]) => {
            try {
              const s = JSON.parse(localStorage.getItem(sk) ?? '{}');
              const d = JSON.parse(localStorage.getItem(dk) ?? '{}');
              return s?.initialized === true && d?.initialized === true;
            } catch {
              return false;
            }
          },
          ['bino_settingsStateV1', 'bino_dashboardStateV1'],
        );

        if (!ready) await page.waitForTimeout(500);
      } catch {
        // Frame detached or navigation in progress (e.g. location.reload()).
        // Wait for the next load to settle, then retry.
        try {
          await page.waitForLoadState('load', { timeout: 15_000 });
        } catch {
          /* still navigating — next loop iteration will retry */
        }
      }
    }

    console.log('Setup complete.');
  }

  await page.waitForSelector('#tabBarTop', { state: 'visible', timeout: 15_000 });
  await page.waitForTimeout(1000);

  fs.mkdirSync(path.dirname(STATE_FILE), { recursive: true });
  await context.storageState({ path: STATE_FILE });

  console.log(`\n✓ State saved → ${STATE_FILE}`);
  console.log('Run "npm run screenshots" to generate release screenshots.\n');

  await browser.close();
})();
