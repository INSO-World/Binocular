# E2E Tests

**Framework**: Playwright + Chromium
**Test ID convention**: `E{file_index}.{test_index}`

Tests cover full-browser behavior that jsdom cannot verify: the app boot sequence from localStorage, cross-component wiring in `App.tsx`, `tabController` DOM ID generation, and real SVG rendering by D3 plugins.

**Deliberately out of scope** (already covered at component/integration level):
- Internal dialog tab-switching and step navigation (C3, C4)
- Redux reducer logic and localStorage round-trips (I-series)
- D3 data correctness or specific SVG coordinates

---

## E1 — Tab navigation
**File**: `src/test/e2e/tab-navigation.test.ts`
**Source**: `src/components/tabMenu/tabController/tabController.tsx`

Fixture: `initializedApp`. Verifies that `tabController.tsx` generates correct `id="tab_{displayName}"` DOM IDs at runtime (line 388) and that clicking them drives the full Redux tab-selection flow. The individual `Tab` wrapper (C22) does not cover the controller or ID generation.

| # | Description | Setup | Expected |
|---|---|---|---|
| E1.1 | Top tab bar renders | initializedApp | `#tabBarTop` visible |
| E1.2 | Parameters tab handle present | initializedApp | `#tab_Parameters` in DOM |
| E1.3 | Visualizations tab handle present | initializedApp | `#tab_Visualizations` in DOM |
| E1.4 | Clicking Visualizations shows tab content | click `#tab_Visualizations` | "Visualization Selector" text visible |
| E1.5 | Clicking active tab again hides its content | click active tab a second time | "Visualization Selector" no longer visible |
| E1.6 | Right-side tab handles present | initializedApp | `#tab_Authors`, `[id="tab_File Tree"]`, `#tab_Help` in DOM |
| E1.7 | Clicking Authors opens right panel | click `#tab_Authors` | `#tabBarRight` visible |

---

## E2 — App initialization
**File**: `src/test/e2e/app-initialization.test.ts`
**Source**: `src/App.tsx` — `useEffect([settingsInitialized, dashboardInitialized])`

Tests the boot-time `useEffect` that opens `#setupDialog` when `state.settings.initialized === false` or `state.dashboard.initialized === false`. Redux hydrates these flags from `localStorage` before React mounts, so the only way to test this path is in a real browser with controlled `localStorage`.

| # | Description | Fixture | Expected |
|---|---|---|---|
| E2.1 | Fresh load auto-opens setup dialog | freshApp (no localStorage) | `#setupDialog[open]` present |
| E2.2 | Seeded localStorage suppresses setup dialog | initializedApp | `#setupDialog` does not have `open` attribute |
| E2.3 | Seeded localStorage: tab bar visible, no loading overlay | initializedApp | `#tabBarTop` visible; no open `dialog` |
| E2.4 | Theme from localStorage applied to root element | custom seed: `theme=binocularDark` | root div has `data-theme="binocularDark"` |

---

## E3 — Header button wiring
**File**: `src/test/e2e/header-buttons.test.ts`
**Source**: `src/App.tsx` lines 103–124 — `TabControllerButton` and `TabControllerButtonThemeSwitch`

The Settings and Export buttons call `document.getElementById('...').showModal()` directly. This cross-component call (App shell → dialog element) is not testable at the component level — it requires a real document. C3 covers the dialog internals; this suite only covers the wiring.

| # | Description | Setup | Expected |
|---|---|---|---|
| E3.1 | Settings button visible in top bar | initializedApp | `img[alt="Settings"]` visible (`TabControllerButton` renders `<button><img alt={name}>`, no text node) |
| E3.2 | Clicking Settings opens `#settingsDialog` | click `button:has(img[alt="Settings"])` | `dialog#settingsDialog` has `open` attribute |
| E3.3 | Export button visible in top bar | initializedApp | `img[alt="Export"]` visible |
| E3.4 | Clicking Export opens `#exportDialog` | click `button:has(img[alt="Export"])` | `dialog#exportDialog` has `open` attribute |
| E3.5 | Theme switch renders in top bar | initializedApp | `label:has(input.theme-controller)` attached (DaisyUI swap hides the raw checkbox; the label is the interactable element) |
| E3.6 | Clicking theme switch changes `data-theme` on root | click `label:has(input.theme-controller)` | `[data-theme].first()` updates to opposite theme; `toHaveAttribute` auto-waits for React re-render |

---

## E4 — Visualization smoke
**File**: `src/test/e2e/visualization-smoke.test.ts`
**Source**: `src/plugins/visualizationPlugins/stats/repositoryStats/`

Fixture: `appWithVisualization` — `dashboardStateV1` seeded with one "Repository Stats" item (`id: 1`) at grid (0,0), 12×8. GraphQL is mocked to return `{"data":{}}` so the plugin renders in an empty/loading state. The goal is to catch mount-time crashes in a real Chromium browser, not assert data correctness. jsdom (used by C-series tests) cannot verify actual SVG layout.

| # | Description | Setup | Expected |
|---|---|---|---|
| E4.1 | Dashboard item container renders | appWithVisualization | `#dashboardItem1` attached to DOM |
| E4.2 | Plugin name visible in interaction bar | appWithVisualization | "Repository Stats" text inside `#dashboardItem1` |
| E4.3 | Plugin renders loading state without crashing | `dataPluginId: undefined` → "No Data Plugin Selected" | text visible inside `#dashboardItem1`; proves Chromium mount succeeded |
| E4.4 | No uncaught page errors during plugin mount | `page.on('pageerror')` listener | zero error events |
| E4.5 | Settings panel opens on settings button click | click first button inside `#dashboardItem1` | panel content visible |

---

## E2E test file locations

```
src/test/e2e/
├── fixtures/
│   └── appFixtures.ts                  (initializedApp, freshApp, appWithVisualization)
├── tab-navigation.test.ts              (E1)
├── app-initialization.test.ts          (E2)
├── header-buttons.test.ts              (E3)
└── visualization-smoke.test.ts         (E4)
```

---

## Notes

- **No backend required**: all `/api/**`, `/graphQl`, and `/wsapi/**` routes are intercepted via `page.route()`.
- **localStorage seeding**: via `page.addInitScript()` — runs before any page script, so Redux hydrates from the seeded values before the first render. All keys use the `bino_` prefix (e.g. `bino_settingsStateV1`). If tests suddenly show the setup dialog, grep the reducers for their current `localStorage.getItem(...)` call to find the active key name.
- **WebSocket**: aborting `/wsapi/**` causes Socket.io to log a console error but does not produce an uncaught `pageerror`.
- **tabsStateV1**: seeding `{ tabList: [] }` is safe — `tabController.tsx` regenerates the full tab list from its `children` prop on mount when lengths differ.
- **D3 / SVG**: Actual SVG rendering requires a functional data plugin. E4.3 instead verifies the "No Data Plugin Selected" loading state, which proves the plugin mounts in Chromium without throwing. Full SVG smoke tests would require injecting a mock data plugin into the Redux store.
