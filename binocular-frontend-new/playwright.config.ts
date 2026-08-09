import { defineConfig, devices } from '@playwright/test';

// The screenshots project drives docs tooling and needs a live Binocular backend, so a
// plain `playwright test` run must not pick it up. It is only registered when explicitly
// requested via `--project=screenshots` (npm run screenshots) or SCREENSHOTS=1.
const includeScreenshots =
  !!process.env.SCREENSHOTS ||
  process.argv.some((arg, i) => arg === '--project=screenshots' || (arg === '--project' && process.argv[i + 1] === 'screenshots'));

export default defineConfig({
  testDir: './src/test/e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  // Cap at 3 workers locally — the Vite dev server struggles under 10+ concurrent browser connections
  workers: process.env.CI ? 1 : 3,
  reporter: [['html', { outputFolder: 'playwright-report', open: 'never' }]],
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
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:8080',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },
});
