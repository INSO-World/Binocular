# E2E Tests

**Framework**: Playwright + Chromium
**Test ID convention**: `E{file_index}.{test_index}`

Tests cover full-browser behavior that jsdom cannot verify: the app boot sequence from localStorage, cross-component wiring in `App.tsx`, `tabController` DOM ID generation, real SVG rendering by D3 plugins, drag interactions, downloads, and popups.

**Deliberately out of scope** (already covered at component/integration level):
- Internal dialog tab-switching and step navigation (C3, C4)
- Redux reducer logic and localStorage round-trips (I-series)
- D3 data correctness or specific SVG coordinates

---

## Fixtures (`src/test/e2e/fixtures/appFixtures.ts`)

| Fixture | localStorage seed | Use for |
|---|---|---|
| `freshApp` | none | setup wizard auto-open paths |
| `initializedApp` | initialized settings, empty dashboard | UI wiring without data |
| `appWithVisualization` | one "Repository Stats" item, no data plugin | mount-time smoke without data |
| `mockDataApp` | Mock Data plugin (id 1, default) + one "Changes" item at (0,0) 12×8 with a matching occupancy grid | everything data-driven |

`mockDataApp` seeds **set-if-absent** (`if (!localStorage.getItem(key))`): init scripts re-run on every navigation, so state the app persists itself (deletions, parameter changes, sprints, merges) survives `page.reload()` within a test. The Mock Data plugin (`src/plugins/dataPlugins/mockData/`) resolves all collections from in-memory fixtures — 4 users (`tester@github.com` … `tester4@github.com`), 11 commits (June–August 2024) with per-file stats, files `index.js`, `src/app.js`, `src/app.css` — so charts render real series without any backend.

`mockBackendRoutes` (exported) stubs `/api/**`, `/graphQl`, and aborts `/wsapi/**`; every fixture applies it.

---

## E1 — Tab navigation
**File**: `src/test/e2e/tab-navigation.test.ts`
**Source**: `src/components/tabMenu/tabController/tabController.tsx`

Fixture: `initializedApp`. Verifies that `tabController.tsx` generates correct `id="tab_{displayName}"` DOM IDs at runtime and that clicking them drives the full Redux tab-selection flow. The individual `Tab` wrapper (C22) does not cover the controller or ID generation.

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
**Source**: `src/App.tsx` — `TabControllerButton` and `TabControllerButtonThemeSwitch`

The Settings and Export buttons call `document.getElementById('...').showModal()` directly. This cross-component call (App shell → dialog element) is not testable at the component level — it requires a real document. C3 covers the dialog internals; this suite only covers the wiring.

| # | Description | Setup | Expected |
|---|---|---|---|
| E3.1 | Settings button visible in top bar | initializedApp | `img[alt="Settings"]` visible |
| E3.2 | Clicking Settings opens `#settingsDialog` | click `button:has(img[alt="Settings"])` | `dialog#settingsDialog` has `open` attribute |
| E3.3 | Export button visible in top bar | initializedApp | `img[alt="Export"]` visible |
| E3.4 | Clicking Export opens `#exportDialog` | click `button:has(img[alt="Export"])` | `dialog#exportDialog` has `open` attribute |
| E3.5 | Theme switch renders in top bar | initializedApp | `label:has(input.theme-controller)` attached |
| E3.6 | Clicking theme switch changes `data-theme` on root | click `label:has(input.theme-controller)` | `[data-theme].first()` updates to opposite theme |

---

## E4 — Visualization smoke
**File**: `src/test/e2e/visualization-smoke.test.ts`
**Source**: `src/plugins/visualizationPlugins/stats/repositoryStats/`

Fixture: `appWithVisualization` — one "Repository Stats" item with `dataPluginId: undefined`. The goal is to catch mount-time crashes in a real Chromium browser, not assert data correctness. (Data-driven SVG assertions live in E7.)

| # | Description | Setup | Expected |
|---|---|---|---|
| E4.1 | Dashboard item container renders | appWithVisualization | `#dashboardItem1` attached to DOM |
| E4.2 | Plugin name visible in interaction bar | appWithVisualization | "Repository Stats" text inside `#dashboardItem1` |
| E4.3 | Plugin renders loading state without crashing | `dataPluginId: undefined` → "No Data Plugin Selected" | text visible; proves Chromium mount succeeded |
| E4.4 | No uncaught page errors during plugin mount | `page.on('pageerror')` listener | zero error events |
| E4.5 | Settings panel opens on settings button click | click first button inside `#dashboardItem1` | panel content visible |

---

## E5 — Add visualization via click
**File**: `src/test/e2e/add-visualization.test.ts`
**Source**: `src/components/tabs/visualizations/visualizationSelector/` + `src/components/dashboard/dashboard.tsx` (automatic placement)

Fixture: `initializedApp`. Clicking a plugin button in the Visualizations tab auto-places an item on the empty dashboard. Item locators must exclude the `_settings`/`_help` sub-panels (`[id^="dashboardItem"]:not([id*="_"])`); plugin buttons exist twice (selector panel + overview dialog), so use `.first()`.

| # | Description | Setup | Expected |
|---|---|---|---|
| E5.1 | Visualizations tab shows recommended plugin buttons | click `#tab_Visualizations` | `button:has(img[alt="Builds"])` visible |
| E5.2 | Clicking a viz button places item on dashboard | click Builds button | a `dashboardItem` element attached |
| E5.3 | Dashboard item shows the clicked plugin name | click Changes button | "Changes" text inside the new item |
| E5.4 | Two different viz buttons add two items | click Builds, then Issues | item count 2 |

---

## E6 — Tab drag to new alignment
**File**: `src/test/e2e/tab-drag.test.ts`
**Source**: `src/components/tabMenu/tabController/tabController.tsx` — `moveTab`, `TabDropHint`

Fixture: `initializedApp`. Tab handles are HTML5-draggable; Playwright's `dragTo()` drives the full dragstart → drop sequence between tab bars.

| # | Description | Setup | Expected |
|---|---|---|---|
| E6.1 | Parameters tab starts in the top bar | initializedApp | `#tab_Parameters` inside `#tabBarTop` |
| E6.2 | Dragging Parameters to the right bar moves it | `dragTo(#tabBarRight)` | handle re-parents to `#tabBarRight`, gone from top |
| E6.3 | Dragging Authors from right to top moves it | `dragTo(#tabBarTop)` | handle re-parents to `#tabBarTop` |
| E6.4 | Drop zones disappear after the drop | drag + drop | "Drop Here" hints not visible |

---

## E7 — Mock Data visualization
**File**: `src/test/e2e/mock-data-visualization.test.ts`
**Source**: `src/plugins/visualizationPlugins/commits/changes/`, `src/plugins/dataPlugins/mockData/`, `src/components/tabs/parameters/`

Fixture: `mockDataApp`. End-to-end data path: parameters → saga → data plugin → D3. The y-axis heuristic: real commit stats push ticks into 3 digits (`/[1-9]\d{2}/`); with no data the converter only emits ±0.001 placeholders, so the axis never reaches 100.

**Adaptation**: "date range filters chart content" is untestable with Mock Data — its `getAll(from, to)` ignores the range — so E7.3 asserts the refetch instead (the plugin logs `Getting Commits from <from> to <to>` on every fetch, proving the new range travelled the whole pipeline).

| # | Description | Setup | Expected |
|---|---|---|---|
| E7.1 | Status bar shows the configured plugin | mockDataApp | "Mock Data #1" in `[class*="dataPluginElement"]` |
| E7.2 | Changes chart renders stacked-area series | mockDataApp | `svg g.areas path` visible; y-axis matches `/[1-9]\d{2}/` |
| E7.3 | Changing the From date refetches with the new range | fill first `datetime-local` in top tab | console message `Getting Commits from 2023-01-01T00:00` |
| E7.4 | Switching granularity re-buckets the x-axis | `Granularity:` → `years` | x-axis text content changes |
| E7.5 | Granularity + merge-commit toggle persist across reload | select `months`, `check()` Exclude Merge Commits, reload | values restored (plain `check()` — regression guard for the TabController stale-render fix) |

---

## E8 — Dashboard item lifecycle
**File**: `src/test/e2e/dashboard-item-lifecycle.test.ts`
**Source**: `src/components/dashboard/dashboard.tsx` (occupancy grid, drag-place), `dashboardItem.tsx` (Shift-delete)

Fixture: `mockDataApp` (the seeded occupancy grid marks the Changes item's cells — overlap detection reads this grid). Drag-place uses manual `DataTransfer` dispatch: after `dragstart` the capture zone overlays everything, so Playwright's `dragTo()` pointer-event check never passes. `dragstart` stores the `DataTransfer` on `window`, then `dragenter`/`dragover`/`drop` on the zone reuse the same instance.

**Adaptation**: "drag-place out of bounds" was dropped — drop targets are clamped to the grid, so the failure state is unreachable.

| # | Description | Setup | Expected |
|---|---|---|---|
| E8.1 | Drag-placing onto an occupied area warns and adds nothing | drop a viz at 0.75× the existing item's box | `.alert-warning` matches /overlap/; item count stays 1 |
| E8.2 | Shift reveals the delete button; clicking removes the item | hold Shift, click `[class*="deleteButton"]` | `#dashboardItem1` detached |
| E8.3 | Deleted item stays deleted after reload | delete, `page.reload()` | item still absent (set-if-absent seeding) |

---

## E9 — Setup wizard completion
**File**: `src/test/e2e/setup-wizard.test.ts`
**Source**: `src/components/setupDialog/`

Fixture: `freshApp`. Full first-run flow with the Mock Data plugin; saving triggers a real page reload (`page.waitForEvent('load')`).

| # | Description | Setup | Expected |
|---|---|---|---|
| E9.1 | Completing the wizard initializes the app | Start → Database (add Mock Data, "Default" badge) → Authors → Dashboard (Select) → Summary → Save | wizard closed after reload; plugin in status bar; dashboard items attached |
| E9.2 | Database page warns when the default backend is unreachable; Retry recovers | route override 503 → then 200 + Retry | warning text shown, then "Connect!" visible |
| E9.3 | Cancelling leaves the app uninitialized | Cancel, reload | wizard reopens |

---

## E10 — Authors
**File**: `src/test/e2e/authors.test.ts`
**Source**: `src/components/tabs/authors/authorList/authorList.tsx`, `src/redux/reducer/data/authorsReducer.ts`

Fixture: `mockDataApp`; the Authors tab is the default-selected right tab. Merging uses the same manual `DataTransfer` pattern as E8.1 (the author id travels through the transfer, so dragstart and drop must share one instance). Title locators need `{ exact: true }` where one title is a substring of another ("Check all authors" ⊂ "Uncheck all authors", case-insensitive).

| # | Description | Setup | Expected |
|---|---|---|---|
| E10.1 | Author list loads all mock authors, checked | mockDataApp | 4 `input.checkbox`, all checked; signatures visible |
| E10.2 | Uncheck-all drains the chart; check-all restores | title buttons | y-axis loses, then regains `/[1-9]\d{2}/` |
| E10.3 | Drag-merge groups two authors; survives reload | drag tester2 onto tester | 3 checkboxes; still 3 after reload |
| E10.4 | "move to other" via context menu | right-click author → `#contextMenuContent` | "No Authors in Other" gone; author listed under Other |

---

## E11 — Dashboard item tools
**File**: `src/test/e2e/dashboard-item-tools.test.ts`
**Source**: `src/components/dashboard/dashboard.tsx` (move/resize/cellSize), `dashboardItem.tsx` (toolbar), `dataPluginQuickSelect.tsx`, export dialog

Fixture: `mockDataApp` (E11.3 seeds two Mock Data plugins inline). Move/resize use synthetic `MouseEvent`s because the handlers consume `mousemove.movementX/Y`, which CDP input does not populate reliably: `mousedown` arms the mode synchronously (ref-based), `mouseover` on the capture zone seeds the indicator, `mousemove` carries the delta. The delta is derived from the td inline width via the `appCellSize` helper, which first waits until that state agrees with the live dashboard width (the ResizeObserver callback lands a beat after the opening tab panels narrow the layout). Persisted positions are asserted from `bino_dashboardStateV1` — the reducers write through on every move/resize.

| # | Description | Setup | Expected |
|---|---|---|---|
| E11.1 | Header drag moves the item | movementX = 2 cells | persisted `x` 0 → 4 (medium grid multiplier 2) |
| E11.2 | Right resize bar widens the item | movementX = 2 cells on `dashboardItemResizeBarRight` | persisted `width` 12 → 16, `x` stays 0 |
| E11.3 | Item settings switch the data plugin | two-plugin seed; settings panel → quick-select dropdown (role-button + button list, **not** a native `<select>`) | header shows "(Mock Data #2)" |
| E11.4 | Export button downloads the chart as SVG | `[class*="exportButton"]` → `#exportDialog` → Export SVG | download `suggestedFilename` contains `ChangesExport` |
| E11.5 | Popout shows placeholder; closing restores chart | `[class*="popoutButton"]`, `waitForEvent('popup')` | "Popped Out!" → Close Popout → chart paths visible again |
| E11.6 | cellSize follows a dashboard resize without the visualization debounce | `setViewportSize`, wait a fixed 50ms (< the 100ms debounce) | td inline width agrees with dashboard width / 20 (regression guard for the cellSize re-measure fix) |

---

## E12 — Date range quick buttons
**File**: `src/test/e2e/parameters-quick-buttons.test.ts`
**Source**: `src/components/tabs/parameters/dataRange/dateRange.tsx`

Fixture: `mockDataApp`. The From row is located by `tr` containing "From:" inside the top tab content (the per-item settings panel renders identical controls in a hidden sub-window).

| # | Description | Setup | Expected |
|---|---|---|---|
| E12.1 | +M / -M shift the From date by one month | click `+M`, then `-M` | month +1 (mod 12), then back |
| E12.2 | Shift flips the buttons to ±Y and shifts a year | hold Shift, click `+Y` | year +1; releasing Shift restores `+M` |
| E12.3 | T sets the date to today | `getByTitle('set date to today')` | value's date part equals today (UTC) |

---

## E13 — Sprints
**File**: `src/test/e2e/sprints.test.ts`
**Source**: `src/components/tabs/sprints/`, `src/redux/reducer/data/sprintsReducer.ts`

Fixture: `mockDataApp`. Add/edit share `#addSprintDialog`; only the name is required (From/To default to a valid 14-day range). Edit/delete go through the right-click context menu (`#contextMenuContent`).

| # | Description | Setup | Expected |
|---|---|---|---|
| E13.1 | Adding a sprint shows it and survives reload | Add Sprint dialog; dismiss `.alert-success` toast by clicking it | sprint visible; still visible after reload **without** re-clicking the tab (see Notes: tab toggle) |
| E13.2 | Edit via context menu renames | right-click → edit → dialog prefilled → Save | new name shown, old gone |
| E13.3 | Delete via context menu removes | right-click → delete | sprint gone |

---

## E14 — Settings & storage
**File**: `src/test/e2e/settings-storage.test.ts`
**Source**: `src/components/settingsDialog/` (generalSettings, clearStorageDialog, databaseSettings/connectedDataPlugins)

Fixture: `mockDataApp`. The global settings button is `getByRole('button', { name: 'Settings', exact: true })` — `exact` keeps the lowercase-titled authors settings button out of the match.

| # | Description | Setup | Expected |
|---|---|---|---|
| E14.1 | Grid size changes grid density and persists | select Small (0) / Large (2), close with Escape | 40 / 10 background rows; `general.gridSize` persisted |
| E14.2 | Export → clear → import round-trips the configuration | Export Storage (download) → Clear Storage with "Reload Page" toggle **off** → `setInputFiles` the export → Import | status bar "No DataPlugins Configured" + Reload Page button after clear; "Storage loaded successfully!" + keys restored after import |
| E14.3 | Deleting the data plugin empties the status bar | Database tab → Delete | "No DataPlugins Configured" |

---

## E15 — Dashboard layouts
**File**: `src/test/e2e/layouts.test.ts`
**Source**: `src/components/tabs/layouts/layoutSelector/layoutSelector.tsx`, `src/components/dashboard/recommendedDashboards/`

Fixture: `mockDataApp`. Applying a recommended layout goes through the shared confirmation dialog (`#contextMenu` with Yes/No buttons); the "Default" layout holds two items (Changes + Builds) wired to the default data plugin. Known app issue: the "Save Dashboard" button is rendered twice (`layoutSelector.tsx`) — tests use `.first()`.

| # | Description | Setup | Expected |
|---|---|---|---|
| E15.1 | Saving a custom layout adds a card that survives reload | Save Dashboard → "Layout name" → Save | "My Layout" button visible, also after reload |
| E15.2 | Applying Default replaces the dashboard after confirmation | click Default card → Yes | 2 persisted items (Builds + Changes); "Builds (Mock Data #1)" header visible (regression guard for the header-spacing fix) |
| E15.3 | Declining leaves the dashboard untouched | click Default card → No | original single Changes item intact |

---

## E16 — File tree
**File**: `src/test/e2e/file-tree.test.ts`
**Source**: `src/components/tabs/fileTree/`, `src/components/fileTree/`, Changes dataConverter (file filtering)

Fixture: `mockDataApp`. Mock Data indexes 3 files; folders start collapsed and expand on click. Search filters by full path. The Changes converter skips per-file stats for unchecked paths, so uncheck-all drains the chart.

| # | Description | Setup | Expected |
|---|---|---|---|
| E16.1 | Tree lists indexed files; folders expand on click | open File Tree tab | "3 Files indexed"; `index.js` visible; `app.js` only after clicking `src` |
| E16.2 | Search filters by path | expand `src`, search "app" | `index.js` hidden, `app.js`/`app.css` visible; clearing restores |
| E16.3 | Uncheck-all drains the Changes chart; check-all restores | title buttons (`exact: true` for "Check all files") | y-axis loses, then regains `/[1-9]\d{2}/` |

---

## E17 — Help tab
**File**: `src/test/e2e/help-tab.test.ts`
**Source**: `src/components/tabs/help/`

Fixture: `mockDataApp`.

| # | Description | Setup | Expected |
|---|---|---|---|
| E17.1 | General help sections + per-component help reachable | open Help tab; click "Changes" plugin button; click "back" | collapse titles (Dashboard, Zoom) visible; per-plugin help opens with a back button and returns |

---

## E2E test file locations

```
src/test/e2e/
├── fixtures/
│   └── appFixtures.ts                  (initializedApp, freshApp, appWithVisualization, mockDataApp, mockBackendRoutes)
├── tab-navigation.test.ts              (E1)
├── app-initialization.test.ts          (E2)
├── header-buttons.test.ts              (E3)
├── visualization-smoke.test.ts         (E4)
├── add-visualization.test.ts           (E5)
├── tab-drag.test.ts                    (E6)
├── mock-data-visualization.test.ts     (E7)
├── dashboard-item-lifecycle.test.ts    (E8)
├── setup-wizard.test.ts                (E9)
├── authors.test.ts                     (E10)
├── dashboard-item-tools.test.ts        (E11)
├── parameters-quick-buttons.test.ts    (E12)
├── sprints.test.ts                     (E13)
├── settings-storage.test.ts            (E14)
├── layouts.test.ts                     (E15)
├── file-tree.test.ts                   (E16)
└── help-tab.test.ts                    (E17)
```

---

## Notes

- **No backend required**: all `/api/**`, `/graphQl`, and `/wsapi/**` routes are intercepted via `page.route()`. Data-driven tests use the in-browser Mock Data plugin instead of a live database.
- **Screenshots project**: `playwright.config.ts` includes the release-screenshot script (`scripts/screenshots.test.ts`) only when run with `--project=screenshots` or `SCREENSHOTS=1` — it needs a live backend on port 48763 and is excluded from the default run.
- **localStorage seeding**: via `page.addInitScript()` — runs before any page script, so Redux hydrates from the seeded values before the first render. All keys use the `bino_` prefix (e.g. `bino_settingsStateV1`). If tests suddenly show the setup dialog, grep the reducers for their current `localStorage.getItem(...)` call to find the active key name. For reload-persistence tests, seed set-if-absent (see `mockDataApp`) — init scripts re-run on every navigation and would otherwise overwrite app-persisted state.
- **Tab toggle after reload**: the selected tab persists in `bino_tabsStateV1`. Clicking a tab handle toggles visibility, so clicking the same tab again after `page.reload()` *closes* the restored panel — assert content directly instead.
- **WebSocket**: aborting `/wsapi/**` causes Socket.io to log a console error but does not produce an uncaught `pageerror`.
- **tabsStateV1**: seeding `{ tabList: [] }` is safe — `tabController.tsx` regenerates the full tab list from its `children` prop when lengths differ.
- **D3 / SVG**: real SVG assertions are possible via `mockDataApp` (E7.2). The 3-digit y-axis tick regex `/[1-9]\d{2}/` distinguishes plotted data from the ±0.001 placeholder scale.
- **Drag patterns**: HTML5 DnD between tab bars works with Playwright `dragTo()` (E6); drops onto the dashboard capture zone need manual `DataTransfer` dispatch (E8.1, E10.3); item move/resize needs synthetic `MouseEvent`s with `movementX` (E11.1/E11.2).
- **Context menu / confirmation dialog**: both render into the shared `dialog#contextMenu` + `#contextMenuContent`; any click inside closes it, so menu items and Yes/No buttons need no extra dismissal.
- **App-fix regression guards**: E7.5 (plain `check()` ⇢ TabController derives tab contents during render, no one-frame stale revert), E15.2 (`getByText('Builds (Mock Data #1)')` ⇢ header spans separated by a real space), E11.6 (50ms convergence ⇢ cellSize updates on every ResizeObserver callback instead of riding the 100ms visualization debounce). Each was verified red on the pre-fix code.
