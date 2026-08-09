import { defineConfig, devices } from '@playwright/test';

// The screenshots project drives docs tooling and needs a live Binocular backend, so a
// plain `playwright test` run must not pick it up. It is only registered when explicitly
// requested via `--project=screenshots` (npm run screenshots) or SCREENSHOTS=1.
const includeScreenshots =
  !!process.env.SCREENSHOTS ||
  process.argv.some((arg, i) => arg === '--project=screenshots' || (arg === '--project' && process.argv[i + 1] === 'screenshots'));

// The demo project records scripted demo videos (see scripts/demo/) and must never
// run as part of the regular test suite or CI. Same gating pattern as `screenshots` above:
// it's only registered when explicitly requested via `--project=demo` or DEMO=1, and it lives
// in its own testDir outside `./src/test/e2e`, so an unqualified `playwright test` can never
// reach it either way.
const includeDemo =
  !!process.env.DEMO || process.argv.some((arg, i) => arg === '--project=demo' || (arg === '--project' && process.argv[i + 1] === 'demo'));

export default defineConfig({
  testDir: './src/test/e2e',
  // Only set for the demo project — see globalSetup.ts for why (pays Vite's one-time cold-start
  // compile cost before any demo test's video recording starts, instead of inside it).
  globalSetup: includeDemo ? './scripts/demo/globalSetup.ts' : undefined,
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  // Cap at 3 workers locally — the Vite dev server struggles under 10+ concurrent browser connections
  workers: process.env.CI ? 1 : 3,
  reporter: includeDemo
    ? [['list'], ['json', { outputFile: 'test-results/demo-results.json' }]]
    : [['html', { outputFolder: 'playwright-report', open: 'never' }]],
  use: {
    baseURL: 'http://localhost:8080',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      testIgnore: '**/screenshots.test.ts',
      use: { ...devices['Desktop Chrome'] },
    },
    ...(includeScreenshots
      ? [
          {
            name: 'screenshots',
            testDir: './scripts',
            testMatch: 'screenshots.test.ts',
            timeout: 660_000, // 11 min ceiling — individual tests override via test.setTimeout()
            use: { ...devices['Desktop Chrome'], viewport: { width: 1920, height: 1080 } },
          },
        ]
      : []),
    ...(includeDemo
      ? [
          {
            // demo:record passes --workers=1: 3 parallel browsers cold-hitting the shared Vite
            // dev server (see webServer below) contend for on-demand compilation, which was
            // blanking out the first 40-50s of every recorded video. One worker keeps the dev
            // server's module cache warm across the whole run instead.
            name: 'demo',
            testDir: './scripts/demo',
            timeout: 300_000, // 5 min ceiling per scripted scene
            use: {
              ...devices['Desktop Chrome'],
              viewport: { width: 1920, height: 1080 },
              video: { mode: 'on' as const, size: { width: 1920, height: 1080 } },
            },
          },
        ]
      : []),
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:8080',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },
});
