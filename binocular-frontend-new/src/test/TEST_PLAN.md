# Frontend Test Plan

**Framework**: Vitest + jsdom
**Test ID convention**: `U{file_index}.{test_index}` for unit tests, `C{file_index}.{test_index}` for component tests.
File indices are assigned in alphabetical order within each category.

---

# Unit Tests

---

## U1 — `authorBehaviour/collaboration/dataConverter`
**File**: `src/test/unit/plugins/visualizationPlugins/authorBehaviour/collaboration/dataConverter.test.ts`
**Source**: `src/plugins/visualizationPlugins/authorBehaviour/collaboration/src/utilities/dataConverter.ts`

### `convertIssuesToGraphData(accounts, settings)`

| # | Description | Input | Expected output |
|---|---|---|---|
| U1.1 | Returns empty nodes and links for empty input | `[]` | `{ nodes: [], links: [] }` |
| U1.2 | Creates one node per account | 2 accounts, no issues | 2 nodes with matching ids |
| U1.3 | Creates a link between two accounts that share an issue | 2 accounts with same issue | 1 link with `value: 1` |
| U1.4 | Increments link value for each shared issue | 2 accounts sharing 2 issues | link `value === 2` |
| U1.5 | Does not create a link when accounts share no issues | 2 accounts with disjoint issues | `links.length === 0` |
| U1.6 | Filters out links below `minEdgeValue` | `minEdgeValue: 2`, link value 1 | `links.length === 0` |
| U1.7 | Filters out links above `maxEdgeValue` | `maxEdgeValue: 2`, link value 3 | `links.length === 0` |
| U1.8 | Assigns connected accounts to the same group | 2 accounts sharing an issue | both nodes have equal `group` |
| U1.9 | Assigns isolated accounts to different groups | 2 accounts with disjoint issues | nodes have different `group` |

---

## U2 — `commits/fileChanges/dataConverter`
**File**: `src/test/unit/plugins/visualizationPlugins/commits/fileChanges/dataConverter.test.ts`
**Source**: `src/plugins/visualizationPlugins/commits/fileChanges/src/utilities/dataConverter.ts`

### `convertCommitDataToMetrics(commits, from, to, gapSize?, burstSize?)`

| # | Description | Input | Expected output |
|---|---|---|---|
| U2.1 | Returns all zeros for an empty commits array | `[]` | `{ mpc: 0, entropy: 0, maxBurst: 0, maxChangeset: 0, avgChangeset: 0 }` |
| U2.2 | Returns all zeros for null input | `null` | same all-zero object |
| U2.3 | Returns non-zero entropy for commits spread across the timeline | 3 commits spread across the year | `entropy > 0` |
| U2.4 | Returns zero entropy for a single commit | 1 commit | `entropy ≈ 0` |
| U2.5 | `mpc` is bounded between 0 and 100 | any valid input | `0 ≤ mpc ≤ 100` |
| U2.6 | Detects a burst of rapid commits | 3 commits within 1 hour | `maxBurst === 3` |
| U2.7 | Returns `maxBurst: 0` when commits are separated by more than gapSize | `gapSize: 1 ms`, 2 far-apart commits | `maxBurst === 0` |
| U2.8 | Computes `maxChangeset` from number of files per commit | commit with 3 files | `maxChangeset === 2` |
| U2.9 | Returns `maxChangeset: 0` when all commits touch 0 or 1 file | commits with 0/1 files | `maxChangeset === 0` |
| U2.10 | Computes `avgChangeset` as mean changeset size | commits with changeset sizes 2 and 4 | `avgChangeset ≈ 3` |
| U2.11 | Handles custom `burstSize` threshold | 2 commits, `burstSize: 3` | `maxBurst === 0` |

---

## U3 — `expertise/codeExpertise/dbUtils`
**File**: `src/test/unit/plugins/visualizationPlugins/expertise/codeExpertise/dbUtils.test.ts`
**Source**: `src/plugins/visualizationPlugins/expertise/codeExpertise/src/utilities/dbUtils.ts`

### `getHistoryForCommit(commit, allCommits)`

| # | Description | Input | Expected output |
|---|---|---|---|
| U3.1 | Returns only the commit itself for a genesis commit (no parents) | single root commit | `[commit.sha]` |
| U3.2 | Follows a linear chain and returns SHAs sorted newest-first | A→B→C chain, query C | `['ccc', 'bbb', 'aaa']` |
| U3.3 | Includes commits from both branches of a merge | diamond graph, query merge commit | all 4 SHAs present, length 4 |
| U3.4 | Does not duplicate commits already in history | diamond graph | each SHA appears once |
| U3.5 | Result is sorted with newest SHA first | 2-commit chain | first element is the head SHA |

---

## U4 — `issues/burndown/groupIssuesByGranularity`
**File**: `src/test/unit/plugins/visualizationPlugins/issues/burndown/groupIssuesByGranularity.test.ts`
**Source**: `src/plugins/visualizationPlugins/issues/burndown/src/chart/helper/groupIssuesByGranularity.ts`

### `groupIssuesByGranularity(start, end, issues, granularity)`

| # | Description | Input | Expected output |
|---|---|---|---|
| U4.1 | Yields one entry per day plus one final entry for a 3-day range | `start: Jan 1, end: Jan 3, 'day'` | 3 group entries |
| U4.2 | Always yields a final entry at the end date | same start and end date | last entry's `date` equals `end` |
| U4.3 | Assigns sequential ids starting at 0 | 2-day range | `groups[0].id === 0`, `groups[1].id === 1` |
| U4.4 | Includes an issue that is open during the queried date | issue open from May to July, query June | issue in `groups[0].issues` |
| U4.5 | Excludes an issue closed before the queried date | issue closed in March, query June | issue not in `groups[0].issues` |
| U4.6 | Yields one entry per month for a month-granularity range | Jan–Mar range, `'month'` | 3 group entries |

---

## U5 — `issues/burndown/pairUpDataPoints`
**File**: `src/test/unit/plugins/visualizationPlugins/issues/burndown/pairUpDataPoints.test.ts`
**Source**: `src/plugins/visualizationPlugins/issues/burndown/src/chart/helper/pairUpDataPoints.ts`

### `pairUpDataPoints(groups)`

| # | Description | Input | Expected output |
|---|---|---|---|
| U5.1 | Yields no pairs for an empty array | `[]` | iterator is empty |
| U5.2 | Yields no pairs for a single-element array | `[g0]` | iterator is empty |
| U5.3 | Yields one pair for a two-element array | `[g0, g1]` | 1 pair: `[g0, g1]` |
| U5.4 | Yields n-1 pairs for an n-element array | 4 elements | 3 pairs |
| U5.5 | Each pair consists of consecutive elements | `[g0, g1, g2]` | `pairs[0]=[g0,g1]`, `pairs[1]=[g1,g2]` |

---

## U6 — `issues/issuesTimeline/aggregateTimeTrackingData`
**File**: `src/test/unit/plugins/visualizationPlugins/issues/issuesTimeline/aggregateTimeTrackingData.test.ts`
**Source**: `src/plugins/visualizationPlugins/issues/issuesTimeline/src/chart/helper/aggregateTimeTrackingData.ts`

### `aggregateTimeTrackingData(entries)`

| # | Description | Input | Expected output |
|---|---|---|---|
| U6.1 | Returns empty map and `totalTime: 0` for empty input | `[]` | `{ map.size === 0, totalTime === 0 }` |
| U6.2 | Records a single entry correctly | Alice, 2h | `map.get('Alice') === 2`, `totalTime === 2` |
| U6.3 | Accumulates time for the same author | Alice 1h + Alice 3h | `map.get('Alice') === 4` |
| U6.4 | Keeps different authors separate | Alice 2h, Bob 5h | both entries correct, `totalTime === 7` |
| U6.5 | `totalTime` equals the sum of all `timeSpent` values | Alice 1.5h, Bob 2.5h, Alice 1h | `totalTime ≈ 5` |

---

## U7 — `issues/issuesTimeline/groupSimilarLabels`
**File**: `src/test/unit/plugins/visualizationPlugins/issues/issuesTimeline/groupSimilarLabels.test.ts`
**Source**: `src/plugins/visualizationPlugins/issues/issuesTimeline/src/chart/helper/groupSimilarLabels.ts`

### `groupSimilarLabels(labels, epsilon?, minPoints?)`

| # | Description | Input | Expected output |
|---|---|---|---|
| U7.1 | Returns an empty map for empty input | `[]` | `new Map()` |
| U7.2 | Treats an isolated string as noise (placed in `defaultLabelGroupId`) | `['foo'], ε=1, minPts=2` | `foo` in noise group |
| U7.3 | Groups closely related strings into the same cluster | `['bug', 'Bug'], ε=1, minPts=2` | one non-noise cluster containing both |
| U7.4 | Produces separate clusters for clearly different strings | `['bug','Bug','feature','Feature'], ε=1, minPts=2` | ≥ 2 clusters |
| U7.5 | All strings cluster together when epsilon is very large | `['a','bbb','ccccc'], ε=100, minPts=1` | 1 cluster containing all |
| U7.6 | Uses default `ε=3, minPts=2` when not specified | `['bug','bugs','feature']` | `bug` cluster also contains `bugs` |

---

## U8 — `issues/issuesTimeline/initializeLevenshteinDPTable`
**File**: `src/test/unit/plugins/visualizationPlugins/issues/issuesTimeline/initializeLevenshteinDPTable.test.ts`
**Source**: `src/plugins/visualizationPlugins/issues/issuesTimeline/src/chart/helper/initializeLevenshteinDPTable.ts`

### `initializeLevenshteinDPTable(a, b)`

| # | Description | Input | Expected output |
|---|---|---|---|
| U8.1 | Returns a table of dimensions `(a.length+1) × (b.length+1)` | `'cat', 'dog'` | 4 rows × 4 columns |
| U8.2 | First row is `[0, 1, 2, … b.length]` | `'cat', 'abcd'` | `table[0] === [0,1,2,3,4]` |
| U8.3 | First column is `[0, 1, 2, … a.length]` | `'abcd', 'cat'` | `table[i][0] === i` for each i |
| U8.4 | All interior cells `(i>0, j>0)` are -1 | `'ab', 'xy'` | `table[i][j] === -1` for i,j > 0 |
| U8.5 | Handles single-character strings | `'a', 'b'` | 2×2 table with `[0,1]` first row, `[1,-1]` second row |

---

## U9 — `issues/issuesTimeline/initializeLevenshteinMatrix`
**File**: `src/test/unit/plugins/visualizationPlugins/issues/issuesTimeline/initializeLevenshteinMatrix.test.ts`
**Source**: `src/plugins/visualizationPlugins/issues/issuesTimeline/src/chart/helper/initializeLevenshteinMatrix.ts`

### `initializeLevenshteinMatrix(strings)`

| # | Description | Input | Expected output |
|---|---|---|---|
| U9.1 | Returns an empty map for empty input | `[]` | `new Map()` |
| U9.2 | Contains an entry for every input string | `['bug','fix','feat']` | all 3 keys present |
| U9.3 | Self-distance is 0 | `['hello']` | `matrix.get('hello').get('hello') === 0` |
| U9.4 | Is symmetric — `distance(a,b) === distance(b,a)` | `['cat','dog']` | equal in both directions |
| U9.5 | Computes correct distance for known pairs | `['kitten','sitting']` | distance === 3 |
| U9.6 | Handles duplicate strings without error | `['bug','bug']` | self-distance 0, no throw |
| U9.7 | Single-element array has only self-distance | `['only']` | inner map has size 1 |

---

## U10 — `issues/issuesTimeline/levenshteinDistance`
**File**: `src/test/unit/plugins/visualizationPlugins/issues/issuesTimeline/levenshteinDistance.test.ts`
**Source**: `src/plugins/visualizationPlugins/issues/issuesTimeline/src/chart/helper/levenshteinDistance.ts`

### `levenshteinDistance(a, b)`

| # | Description | Input | Expected output |
|---|---|---|---|
| U10.1 | Returns 0 for identical strings | `'cat', 'cat'` | `0` |
| U10.2 | Returns `b.length` when `a` is empty | `'', 'abc'` | `3` |
| U10.3 | Returns `a.length` when `b` is empty | `'abc', ''` | `3` |
| U10.4 | Returns 0 for two empty strings | `'', ''` | `0` |
| U10.5 | Returns 1 for a single substitution | `'cat', 'bat'` | `1` |
| U10.6 | Returns 1 for a single insertion | `'cat', 'cats'` | `1` |
| U10.7 | Returns 1 for a single deletion | `'cats', 'cat'` | `1` |
| U10.8 | Calculates the classic kitten→sitting distance | `'kitten', 'sitting'` | `3` |
| U10.9 | Is commutative — `distance(a,b) === distance(b,a)` | `'sunday', 'saturday'` | equal in both directions |
| U10.10 | Handles strings differing only in case | `'Bug', 'bug'` | `1` |

---

## U11 — `ownership/codeOwnership/cryptoUtils`
**File**: `src/test/unit/plugins/visualizationPlugins/ownership/codeOwnership/cryptoUtils.test.ts`
**Source**: `src/plugins/visualizationPlugins/ownership/codeOwnership/src/utils/cryptoUtils.ts`

### `hash(value)`

> Requires `crypto.subtle` — available in jsdom 20+ / Node 19+. Vitest's jsdom environment covers this.

| # | Description | Input | Expected output |
|---|---|---|---|
| U11.1 | Returns a non-empty base64 string | `'hello'` | length > 0, valid base64 |
| U11.2 | Is deterministic — same input produces the same hash | `'hello'` called twice | both results identical |
| U11.3 | Different inputs produce different hashes | `'hello'` vs `'world'` | results differ |
| U11.4 | Empty string resolves without throwing | `''` | resolves to a non-empty string |

---

## U12 — `ownership/codeOwnership/dateUtils`
**File**: `src/test/unit/plugins/visualizationPlugins/ownership/codeOwnership/dateUtils.test.ts`
**Source**: `src/plugins/visualizationPlugins/ownership/codeOwnership/src/utils/dateUtils.ts`

### `formatDate(date, resolution)`

| # | Description | Input | Expected output |
|---|---|---|---|
| U12.1 | `years` resolution returns just the year | `new Date('2023-06-12'), 'years'` | `'2023'` |
| U12.2 | `months` resolution returns month name + year | `new Date('2023-06-12'), 'months'` | `'June 2023'` |
| U12.3 | `months` resolution — January (boundary: first month) | month index 0 | contains `'January'` |
| U12.4 | `months` resolution — December (boundary: last month) | month index 11 | contains `'December'` |
| U12.5 | `weeks` resolution starts with "Week starting at" | `new Date('2023-06-12'), 'weeks'` | starts with `'Week starting at'` |
| U12.6 | `days` resolution starts with the day name | `new Date('2023-06-12'), 'days'` | starts with `'Monday'` |
| U12.7 | Unknown resolution falls back to `toLocaleDateString()` | `date, 'hours'` | equals `date.toLocaleDateString()` |

### `getGranularityDuration(resolution)`

| # | Description | Input | Expected output |
|---|---|---|---|
| U12.8 | `years` returns unit `'year'` and a 1-year duration | `'years'` | `{ unit: 'year', interval: duration(1,'year') }` |
| U12.9 | `months` returns unit `'month'` and a 1-month duration | `'months'` | `unit === 'month'` |
| U12.10 | `weeks` returns unit `'week'` and a 1-week duration | `'weeks'` | `unit === 'week'` |
| U12.11 | `days` returns unit `'day'` and a 1-day duration | `'days'` | `unit === 'day'` |
| U12.12 | Unknown resolution returns `{ interval: 0, unit: '' }` | `'hours'` | `{ interval: 0, unit: '' }` |

---

## U13 — `ownership/codeOwnership/ownershipUtils`
**File**: `src/test/unit/plugins/visualizationPlugins/ownership/codeOwnership/ownershipUtils.test.ts`
**Source**: `src/plugins/visualizationPlugins/ownership/codeOwnership/src/utils/ownershipUtils.ts`

### `extractOwnershipFromFileExcludingCommits(fileOwnershipData, commitsToExclude?)`

| # | Description | Input | Expected output |
|---|---|---|---|
| U13.1 | Counts all lines when no commits are excluded | 1 user, 1 hunk `{from:1, to:5}` | `ownedLines: 5` |
| U13.2 | Counts lines across multiple hunks | 1 user, 2 hunks: `{1,3}` and `{10,12}` | `ownedLines: 6` |
| U13.3 | Skips hunks whose `originalCommit` is in the exclude list | 2 hunks on commits `a` and `b`; exclude `[a]` | only hunk `b` counted |
| U13.4 | Returns `ownedLines: 0` when all commits are excluded | all hunks excluded | `ownedLines: 0` |
| U13.5 | Handles multiple users independently | 2 users, each with own hunks | separate `ownedLines` per user |
| U13.6 | Returns empty array for empty input | `[]` | `[]` |
| U13.7 | Default param: no `commitsToExclude` counts everything | omit second arg | all hunks counted |

### `extractFileOwnership(ownershipData, commitsToExclude?)`

| # | Description | Input | Expected output |
|---|---|---|---|
| U13.8 | Uses most-recent commit's ownership for each file | 2 commits touching same file | ownership from newer commit wins |
| U13.9 | Includes files from all commits when disjoint | 2 commits touching different files | both files present in result |
| U13.10 | Keys result by file path | any input | `result['src/foo.ts']` is array of `OwnershipResult` |
| U13.11 | Passes `commitsToExclude` through to inner function | exclude a commit sha | lines from excluded commit not counted |
| U13.12 | Empty `ownershipData` returns empty object | `[]` | `{}` |

---

## U14 — `utils/extractTimeTrackingDataFromNotes`
**File**: `src/test/unit/plugins/visualizationPlugins/utils/extractTimeTrackingDataFromNotes.test.ts`
**Source**: `src/plugins/visualizationPlugins/utils/extractTimeTrackingDataFromNotes.ts`

### `extractTimeTrackingDataFromNotes(notes)`

| # | Description | Input | Expected output |
|---|---|---|---|
| U14.1 | Returns empty array for `null` input | `null` | `[]` |
| U14.2 | Returns empty array for `undefined` input | `undefined` | `[]` |
| U14.3 | Returns empty array when no note matches a time-tracking pattern | non-tracking notes | `[]` |
| U14.4 | `"added Xh"` note produces a positive `timeSpent` entry in hours | `'added 2h of time spent …'` | `timeSpent === 2` |
| U14.5 | `"added Xh Ym"` parses both hours and minutes | `'added 1h 30m …'` | `timeSpent ≈ 1.5` |
| U14.6 | `"added Xm"` parses minutes only | `'added 30m …'` | `timeSpent ≈ 0.5` |
| U14.7 | `"subtracted Xh"` produces a negative `timeSpent` entry | `'subtracted 1h …'` | `timeSpent === -1` |
| U14.8 | `"deleted Xh of spent time"` produces a negative entry | `'deleted 2h …'` | `timeSpent === -2` |
| U14.9 | `"deleted -Xh of spent time"` (double-negative) produces a positive entry | `'deleted -2h …'` | `timeSpent === 2` |
| U14.10 | `"removed time spent"` documents reversed-processing behavior | add then remove, same issue | addition still lands in result |
| U14.11 | Accumulates multiple add notes | 2 add notes | result has 2 entries |

---

## U15 — `redux/reducer/general/dashboardReducer`
**File**: `src/test/unit/redux/reducer/general/dashboardReducer.test.ts`
**Source**: `src/redux/reducer/general/dashboardReducer.ts`

> All tests use a 10×10 test grid and clear `localStorage` in `beforeEach`.

### `addDashboardItem`

| # | Description | Expected outcome |
|---|---|---|
| U15.1 | Places the item and adds it to `dashboardItems` | `dashboardItems.length === 1` |
| U15.2 | Increments `dashboardItemCount` | count goes from 0 to 1 |
| U15.3 | Marks grid cells with the item id | cells at item's x/y filled with item id |
| U15.4 | Clears `placeableItem` after placement | `placeableItem === undefined` |
| U15.5 | Sets `initialized: true` | `initialized === true` |

### `moveDashboardItem`

| # | Description | Expected outcome |
|---|---|---|
| U15.6 | Updates item position when target space is free | item's `x`, `y` updated |
| U15.7 | Clears old cells and fills new cells after move | old cells = 0, new cells = item id |
| U15.8 | Does not move when target space is occupied by another item | item position unchanged |

### `deleteDashboardItem`

| # | Description | Expected outcome |
|---|---|---|
| U15.9 | Removes the item from `dashboardItems` | list length reduced by 1 |
| U15.10 | Clears item's cells in `dashboardState` | previously filled cells = 0 |
| U15.11 | Leaves sibling items' cells untouched | sibling item cells still correct |

### `clearDashboard`

| # | Description | Expected outcome |
|---|---|---|
| U15.12 | Resets `dashboardItems` to `[]` | `dashboardItems.length === 0` |
| U15.13 | Resets `dashboardState` to all-zero grid | every cell is 0 |
| U15.14 | Resets `dashboardItemCount` to 0 | `dashboardItemCount === 0` |

### `setDashboardState`

| # | Description | Expected outcome |
|---|---|---|
| U15.15 | Assigns sequential ids starting at 1 | first item id = 1, second = 2 |
| U15.16 | Fills `dashboardState` grid correctly for each item | cells within item bounds hold item id |
| U15.17 | Sets `initialized: true` | `initialized === true` |

### `placeDashboardItem` / `updateDashboardItem`

| # | Description | Expected outcome |
|---|---|---|
| U15.18 | `placeDashboardItem` sets `placeableItem` and `initialized` | `placeableItem` equals payload, `initialized === true` |
| U15.19 | `updateDashboardItem` replaces matching item in the list | item's fields reflect payload |

---

## U16 — `redux/reducer/general/notificationsReducer`
**File**: `src/test/unit/redux/reducer/general/notificationsReducer.test.ts`
**Source**: `src/redux/reducer/general/notificationsReducer.ts`

### `addNotification`

| # | Description | Input | Expected output |
|---|---|---|---|
| U16.1 | Appends notification to list | empty state + action | `notificationList.length === 1` |
| U16.2 | Assigns `currID` as the notification id | `currID: 5` | notification gets `id: 5` |
| U16.3 | Increments `currID` after each add | add twice | `currID === 2` |
| U16.4 | Multiple notifications accumulate | add 3 | list length 3 |

### `removeNotification`

| # | Description | Input | Expected output |
|---|---|---|---|
| U16.5 | Removes notification with matching id | add then remove by id | `notificationList` empty |
| U16.6 | Does not remove other notifications | 2 items, remove one | remaining item still present |
| U16.7 | No-op when id does not exist | remove id 99 from empty state | list unchanged |

---

## U17 — `redux/reducer/general/tabsReducer`
**File**: `src/test/unit/redux/reducer/general/tabsReducer.test.ts`
**Source**: `src/redux/reducer/general/tabsReducer.ts`

> Uses `vi.spyOn(Storage.prototype, ...)` to verify localStorage interaction.

### `setTabList`

| # | Description | Expected outcome |
|---|---|---|
| U17.1 | Replaces `tabList` with provided array | `tabList` equals payload |
| U17.2 | Replaces a non-empty list with a new one | old entries gone, new entries present |
| U17.3 | Persists the new state to `localStorage` | `localStorage.setItem` called |

### `clearTabsStorage`

| # | Description | Expected outcome |
|---|---|---|
| U17.4 | Calls `localStorage.removeItem` | `removeItem` spy was called |

---

## U18 — `redux/reducer/parameters/parametersReducer`
**File**: `src/test/unit/redux/reducer/parameters/parametersReducer.test.ts`
**Source**: `src/redux/reducer/parameters/parametersReducer.ts`

> Uses `vi.spyOn(Storage.prototype, ...)` to verify localStorage interaction.

### `setParametersGeneral`

| # | Description | Expected outcome |
|---|---|---|
| U18.1 | Updates `parametersGeneral` | `state.parametersGeneral` equals payload |
| U18.2 | Persists to `localStorage` | `setItem` spy called |

### `setParametersDateRange`

| # | Description | Expected outcome |
|---|---|---|
| U18.3 | Updates `parametersDateRange` | `state.parametersDateRange` equals payload |
| U18.4 | Persists to `localStorage` | `setItem` spy called |

### `clearParametersStorage`

| # | Description | Expected outcome |
|---|---|---|
| U18.5 | Calls `localStorage.removeItem` | `removeItem` spy called |

---

## U19 — `exportReducer`
**File**: `src/test/unit/redux/reducer/export/exportReducer.test.ts`
**Source**: `src/redux/reducer/export/exportReducer.ts`

| # | Description | Expected outcome |
|---|---|---|
| U19.1 | `setExportType` updates `exportType` | `state.exportType === ExportType.image` |
| U19.2 | `setExportType` overwrites a previous value | second dispatch wins |
| U19.3 | `setExportSVGData` stores an SVG string | `state.exportSVGData === svg` |
| U19.4 | `setExportName` sets the filename | `state.exportName === 'my-chart'` |
| U19.5 | `setExportName` overwrites a previous name | second dispatch wins |

---

## U20 — Exception classes
**File**: `src/test/unit/plugins/visualizationPlugins/ownership/codeOwnership/exceptions.test.ts`
**Source**: `src/plugins/visualizationPlugins/ownership/codeOwnership/src/utils/exception/`

| # | Description | Expected outcome |
|---|---|---|
| U20.1 | `RuntimeException` is an `instanceof Error` | `e instanceof Error` |
| U20.2 | `message` is set correctly | `e.message === 'test message'` |
| U20.3 | `name` is set to the provided value | `e.name === 'MyName'` |
| U20.4 | `code` is set when provided | `e.code === 42` |
| U20.5 | `code` is `undefined` when omitted | `e.code === undefined` |
| U20.6 | `InvalidArgumentException` is `instanceof RuntimeException` | passes |
| U20.7 | `InvalidArgumentException.name === 'InvalidArgumentException'` | passes |
| U20.8 | `InvalidArgumentException` propagates `code` | `e.code === 99` |
| U20.9 | `NoImplementationException` is `instanceof RuntimeException` | passes |
| U20.10 | `NoImplementationException.name === 'NoImplementationException'` | passes |

---

## U21 — `findAuthorWithMaxSpentTime`
**File**: `src/test/unit/plugins/visualizationPlugins/issues/issuesTimeline/findAuthorWithMaxSpentTime.test.ts`
**Source**: `src/plugins/visualizationPlugins/issues/issuesTimeline/src/chart/helper/findAuthorWithMaxSpentTime.ts`

| # | Description | Expected outcome |
|---|---|---|
| U21.1 | Returns `''` for empty map | `''` |
| U21.2 | Returns the only entry's key for a single-entry map | `'Alice'` |
| U21.3 | Returns the key with the highest value | `'Bob'` |
| U21.4 | When values are equal, result is one of the tied keys | in `['Alice', 'Bob']` |

---

## U22 — `groupIntoTracks`
**File**: `src/test/unit/plugins/visualizationPlugins/issues/issuesTimeline/groupIntoTracks.test.ts`
**Source**: `src/plugins/visualizationPlugins/issues/issuesTimeline/src/chart/helper/groupIntoTracks.ts`

| # | Description | Expected outcome |
|---|---|---|
| U22.1 | Returns empty array for empty issues | `[]` |
| U22.2 | Single issue goes into a single track | `tracks.length === 1` |
| U22.3 | Two non-overlapping sequential issues stay in one track | `tracks.length === 1, tracks[0].length === 2` |
| U22.4 | Two overlapping issues split into two tracks | `tracks.length === 2` |
| U22.5 | Issue without `closedAt` uses `maxDate` for overlap check | `tracks.length === 2` |

---

## U23 — `groupMergeRequests`
**File**: `src/test/unit/plugins/visualizationPlugins/issues/issuesTimeline/groupMergeRequests.test.ts`
**Source**: `src/plugins/visualizationPlugins/issues/issuesTimeline/src/chart/helper/groupMergeRequests.ts`

| # | Description | Expected outcome |
|---|---|---|
| U23.1 | Returns empty array for empty input | `[]` |
| U23.2 | MRs in the same month land in one group | `groups.length === 1` |
| U23.3 | MRs in different months land in separate groups | `groups.length === 2` |
| U23.4 | Multiple MRs in same month are all in that group | `groups[0].length === 4` |
| U23.5 | Returns one sub-array per distinct month | `groups.length === 3` |

---

## U24 — `accountsReducer`
**File**: `src/test/unit/redux/reducer/data/accountsReducer.test.ts`
**Source**: `src/redux/reducer/data/accountsReducer.ts`

> Uses `localStorage.clear()` in `beforeEach`.

| # | Description | Expected outcome |
|---|---|---|
| U24.1 | `setAccountList` adds new accounts for the given `dataPluginId` | list length 2 |
| U24.2 | `setAccountList` auto-assigns incrementing `localId` | `localId` values 1, 2 |
| U24.3 | `setAccountList` removes accounts no longer in payload | removed account absent |
| U24.4 | `setAccountList` persists to localStorage | `setItem` called |
| U24.5 | `setAccountsDataPluginId` updates `dataPluginId` | `state.dataPluginId === 42` |
| U24.6 | `setAccountsDataPluginId` persists to localStorage | `setItem` called |
| U24.7 | `clearAccountsStorage` calls `localStorage.removeItem` | `removeItem` called |

---

## U25 — `sprintsReducer`
**File**: `src/test/unit/redux/reducer/data/sprintsReducer.test.ts`
**Source**: `src/redux/reducer/data/sprintsReducer.ts`

> Mocks `document.getElementById('addSprintDialog')` for `sprintToEdit` action.

| # | Description | Expected outcome |
|---|---|---|
| U25.1 | `setSprints` replaces sprint list | new list in state |
| U25.2 | `setSprints` persists to localStorage | `setItem` called |
| U25.3 | `addSprint` assigns `currID` as sprint's `id` | `sprint.id === 5` |
| U25.4 | `addSprint` increments `currID` | `currID === 1` |
| U25.5 | `addSprint` persists to localStorage | `setItem` called |
| U25.6 | `deleteSprint` removes matching sprint | removed sprint absent |
| U25.7 | `deleteSprint` leaves other sprints intact | other sprint present |
| U25.8 | `sprintToEdit` sets `state.sprintToEdit` and calls `showModal()` | both asserted |
| U25.9 | `saveSprint` updates the sprint in the list | name updated |
| U25.10 | `saveSprint` clears `state.sprintToEdit` | `null` |
| U25.11 | `clearSprintStorage` calls `localStorage.removeItem` | `removeItem` called |

---

## U26 — `authorsReducer`
**File**: `src/test/unit/redux/reducer/data/authorsReducer.test.ts`
**Source**: `src/redux/reducer/data/authorsReducer.ts`

> Uses `localStorage.clear()` in `beforeEach`.

| # | Description | Expected outcome |
|---|---|---|
| U26.1 | `setDragging` updates `dragging` flag | `dragging === true` |
| U26.2 | `moveAuthorToOther` sets `parent = 0` for the target author | `a.parent === 0` |
| U26.3 | `moveAuthorToOther` also sets `parent = 0` for children | child `parent === 0` |
| U26.4 | `resetAuthor` sets `parent = -1` for the target author | `a.parent === -1` |
| U26.5 | `setParentAuthor` sets the parent relationship | `a.parent === 1` |
| U26.6 | `setParentAuthor` ignores self-assignment | parent unchanged |
| U26.7 | `checkAllAuthors` sets all `selected = true` | all true |
| U26.8 | `uncheckAllAuthors` sets all `selected = false` | all false |
| U26.9 | `switchAuthorSelection` toggles `selected` on target and children | both toggled |
| U26.10 | `clearAuthorsStorage` calls `localStorage.removeItem` | `removeItem` called |

---

## U27 — `filesReducer`
**File**: `src/test/unit/redux/reducer/data/filesReducer.test.ts`
**Source**: `src/redux/reducer/data/filesReducer.ts`

> Mocks `writeFileListToStorage` via `vi.mock(...)`.

| # | Description | Expected outcome |
|---|---|---|
| U27.1 | `loadState` loads all fields from payload | all fields set |
| U27.2 | `setFileList` stores tree, list, count under `dataPluginId` | each keyed correctly |
| U27.3 | `setFileList` calls `writeFileListToStorage` | mock called once |
| U27.4 | `setFilesDataPluginId` updates `dataPluginId` | `state.dataPluginId === 7` |
| U27.5 | `setFilesDataPluginId` calls `writeFileListToStorage` | mock called once |
| U27.6 | `updateFileListElement` (update=true) toggles `checked` on matching files | `checked === false` |
| U27.7 | `updateFileListElement` (update=false) does not touch `fileList` | `checked` unchanged |
| U27.8 | `checkAllFiles` sets `checked = true` for every file | all true |
| U27.9 | `uncheckAllFiles` sets `checked = false` for every file | all false |
| U27.10 | `removeFileList` deletes entries for the given id | all three maps cleaned |

---

## U28 — `layoutReducer`
**File**: `src/test/unit/redux/reducer/general/layoutReducer.test.ts`
**Source**: `src/redux/reducer/general/layoutReducer.ts`

| # | Description | Expected outcome |
|---|---|---|
| U28.1 | `addCustomLayout` appends layout to `customLayouts` | length 1 |
| U28.2 | `addCustomLayout` auto-assigns `id` from `customLayoutCount` | `id === 3` |
| U28.3 | `addCustomLayout` increments `customLayoutCount` | `count === 1` |
| U28.4 | `addCustomLayout` persists to localStorage | `setItem` called |
| U28.5 | `saveChanges` updates layout with matching id | name updated |
| U28.6 | `saveChanges` does not affect other layouts | other name unchanged |
| U28.7 | `deleteCustomLayout` removes layout with matching id | removed layout absent |
| U28.8 | `deleteCustomLayout` persists to localStorage | `setItem` called |

---

## U29 — `settingsReducer`
**File**: `src/test/unit/redux/reducer/settings/settingsReducer.test.ts`
**Source**: `src/redux/reducer/settings/settingsReducer.ts`

> Mocks `distinct-colors` via `vi.mock(...)`.

| # | Description | Expected outcome |
|---|---|---|
| U29.1 | `setGeneralSettings` replaces `general` field | `gridSize` updated |
| U29.2 | `setGeneralSettings` persists to localStorage | `setItem` called |
| U29.3 | `addDataPlugin` (no id) appends with auto-assigned id | `id` defined |
| U29.4 | `addDataPlugin` (no id) sets `isDefault = true` for the first plugin | `isDefault === true` |
| U29.5 | `addDataPlugin` (no id) assigns color from mocked `distinctColors` | color starts with `#` |
| U29.6 | `addDataPlugin` (existing id) updates plugin in-place, list length unchanged | length 1, name updated |
| U29.7 | `removeDataPlugin` removes plugin with matching id | absent from list |
| U29.8 | `setDataPluginAsDefault` sets `isDefault = true` only on the matching plugin | one true, others false |
| U29.9 | `initializeSettingsState` sets `initialized = true` | `initialized === true` |
| U29.10 | `setLocalDatabaseLoadingState` updates the loading state enum | enum value set |
| U29.11 | `setLocalDatabaseLoadingMessage` updates the loading message string | message set |
| U29.12 | `clearSettingsStorage` calls `localStorage.removeItem` | `removeItem` called |

---

## U30 — `highlightDropArea`
**File**: `src/test/unit/components/dashboard/highlightDropArea.test.ts`
**Source**: `src/components/dashboard/dashboardHelper.ts`

> Tests the boolean return value only. DOM `?.classList` calls are no-ops when elements are absent.

| # | Description | Expected outcome |
|---|---|---|
| U30.1 | Empty grid returns `true` | `true` |
| U30.2 | Cell occupied by the moving item itself returns `true` | `true` |
| U30.3 | Cell occupied by a different item returns `false` | `false` |
| U30.4 | Occupied cell outside the drop area returns `true` | `true` |
| U30.5 | Multiple conflicting cells return `false` | `false` |

---

## U31 — `showContextMenu`
**File**: `src/test/unit/components/contextMenu/showContextMenu.test.ts`
**Source**: `src/components/contextMenu/contextMenuHelper.ts`

> Sets up the 3 required DOM elements in `beforeEach`; mocks `window.innerWidth/Height` and `showModal`.

| # | Description | Expected outcome |
|---|---|---|
| U31.1 | `y < innerHeight/2` → `top` set, `bottom` `'auto'` | `top === '90px'` |
| U31.2 | `y >= innerHeight/2` → `bottom` set, `top` `'auto'` | `bottom` computed correctly |
| U31.3 | `x < innerWidth/2` → `left` set, `right` `'auto'` | `left === '190px'` |
| U31.4 | `x >= innerWidth/2` → `right` set, `left` `'auto'` | `right` computed correctly |
| U31.5 | Creates one `<li>` per option | 3 options → 3 `<li>` elements |
| U31.6 | Sets option label text correctly | `span.textContent === 'Delete'` |
| U31.7 | Sets icon `src` when icon is provided | `img.src` contains `'trash.svg'` |
| U31.8 | Does not set `src` when icon is `null` | `img.getAttribute('src')` is null |
| U31.9 | Clicking option span invokes the option function | mock `fn` called once |
| U31.10 | Calls `showModal()` on the dialog | spy called |

---

## U32 — `showInfoTooltip`
**File**: `src/test/unit/components/infoTooltip/showInfoTooltip.test.ts`
**Source**: `src/components/infoTooltip/infoTooltipHelper.ts`

> Same DOM setup pattern as U31.

| # | Description | Expected outcome |
|---|---|---|
| U32.1 | `y < innerHeight/2` → `top` set, `bottom` `'auto'` | `top === '90px'` |
| U32.2 | `y >= innerHeight/2` → `bottom` set, `top` `'auto'` | `bottom` computed correctly |
| U32.3 | `x < innerWidth/2` → `left` set, `right` `'auto'` | `left === '190px'` |
| U32.4 | `x >= innerWidth/2` → `right` set, `left` `'auto'` | `right` computed correctly |
| U32.5 | Renders `<h1>` with correct headline | `h1.innerText === 'Overview'` |
| U32.6 | Renders `<p>` with correct body text | `p.innerText === 'Shows commits...'` |
| U32.7 | No compatibility section when arg omitted | `#compatibility` absent |
| U32.8 | Compatibility section rendered when arg provided | `#compatibility` present |
| U32.9 | `github: true` shows "yes" | content contains `'GitHub: yes'` |
| U32.10 | `pouchDB: false` shows "no" | content contains `'PouchDB: no'` |
| U32.11 | Calls `showModal()` on the dialog | spy called |

---

## U33 — `issues/issues/dataConverter`
**File**: `src/test/unit/plugins/visualizationPlugins/issues/issues/dataConverter.test.ts`
**Source**: `src/plugins/visualizationPlugins/issues/issues/src/utilities/dataConverter.ts`

| # | Description |
|---|---|
| U33.1 | Returns empty result for empty array |
| U33.2 | `breakdown:false` — opened issue produces positive `Opened` count |
| U33.3 | `breakdown:false` — closed issue produces negative `Closed` count |
| U33.4 | `breakdown:true` — `Open` count increments on open, decrements on close |
| U33.5 | Scale `[0]` ≤ 0, `[1]` > 0 for opened issues |
| U33.6 | `splitIssuesPerAuthor:true` — keys prefixed with `"Opened Issues"` or `"Open Issues"` |
| U33.7 | Unassigned issue (no assignee) creates key containing `"unassigned"` |

---

## U34 — `issues/mergeRequests/dataConverter`
**File**: `src/test/unit/plugins/visualizationPlugins/issues/mergeRequests/dataConverter.test.ts`
**Source**: `src/plugins/visualizationPlugins/issues/mergeRequests/src/utilities/dataConverter.ts`

| # | Description |
|---|---|
| U34.1 | Returns empty result for empty array |
| U34.2 | `breakdown:false, state:'MERGED'` → negative `Merged` count |
| U34.3 | `breakdown:false, state:'CLOSED'` → negative `Closed` count |
| U34.4 | `breakdown:true` → `Open` count positive when MR is open |
| U34.5 | Scale `[1]` > 0 for opened MRs |

---

## U35 — `builds/builds/dataConverter`
**File**: `src/test/unit/plugins/visualizationPlugins/builds/builds/dataConverter.test.ts`
**Source**: `src/plugins/visualizationPlugins/builds/builds/src/utilities/dataConverter.ts`

| # | Description |
|---|---|
| U35.1 | Returns empty result for empty array |
| U35.2 | Success build lands in correct time bucket |
| U35.3 | `splitBuildsPerAuthor:false` — failed builds are negated |
| U35.4 | `splitBuildsPerAuthor:true` — keys include `"builds"` substring |
| U35.5 | Scale `[1]` > 0 for success builds |

---

## U36 — `commits/changes/dataConverter`
**File**: `src/test/unit/plugins/visualizationPlugins/commits/changes/dataConverter.test.ts`
**Source**: `src/plugins/visualizationPlugins/commits/changes/src/utilities/dataConverter.ts`

| # | Description |
|---|---|
| U36.1 | Returns empty result for empty array |
| U36.2 | Commits are bucketed by `date` field |
| U36.3 | `splitAdditionsDeletions:false` — author name appears as key |
| U36.4 | `splitAdditionsDeletions:true` — `(Additions)` and `(Deletions)` keys appear |
| U36.5 | BUG: `excludeMergeCommits:true` with all-merge input crashes (no empty-check after filter) |
| U36.6 | Scale `[1]` ≥ 0 and `[0]` ≤ 0 for `splitAdditionsDeletions:true` |

---

## U37 — `authorBehaviour/timeSpent/dataConverter`
**File**: `src/test/unit/plugins/visualizationPlugins/authorBehaviour/timeSpent/dataConverter.test.ts`
**Source**: `src/plugins/visualizationPlugins/authorBehaviour/timeSpent/src/utilities/dataConverter.ts`

| # | Description |
|---|---|
| U37.1 | Returns empty result for empty array |
| U37.2 | BUG: notes with no time-tracking match crash (no empty-check after `extractTimeTrackingDataFromNotes`) |
| U37.3 | `splitTimePerIssue:true` — chart keys include issue title |
| U37.4 | `splitTimePerIssue:false` — author-name or `"others"` key appears |
| U37.5 | Scale `[1]` ≥ 0 when time is spent |

---

## U38 — `expertise/knowledgeRadar/dataConverter`
**File**: `src/test/unit/plugins/visualizationPlugins/expertise/knowledgeRadar/dataConverter.test.ts`
**Source**: `src/plugins/visualizationPlugins/expertise/knowledgeRadar/src/utilities/dataConverter.ts`

| # | Description |
|---|---|
| U38.1 | `extractTouchedFiles` — returns empty set for empty commits |
| U38.2 | `extractTouchedFiles` — returns files from matching developer |
| U38.3 | `extractTouchedFiles` — ignores commits from other developers |
| U38.4 | `calculateExpertiseBrowserScores` — returns empty array for empty commits |
| U38.5 | `calculateExpertiseBrowserScores` — filters out merge commits |
| U38.6 | `calculateExpertiseBrowserScores` — ownership score = dev commits / total commits |
| U38.7 | `buildPackageHierarchy` — empty map → empty array |
| U38.8 | `buildPackageHierarchy` — flat path produces single root package with correct score |
| U38.9 | `buildPackageHierarchy` — nested path produces 3-level hierarchy |
| U38.10 | `buildPackageHierarchy` — parent score is aggregated from children when parent score is 0 |

---

## U39 — `authorBehaviour/repositoryActivity/types`
**File**: `src/test/unit/plugins/visualizationPlugins/authorBehaviour/repositoryActivity/types.test.ts`
**Source**: `src/plugins/visualizationPlugins/authorBehaviour/repositoryActivity/src/utilities/types.ts`

| # | Description |
|---|---|
| U39.1 | `isDataPluginCommit` returns true for commit-shaped object |
| U39.2 | `isDataPluginCommit` returns false for build object |
| U39.3 | `isDataPluginBuild` returns true for build-shaped object |
| U39.4 | `isDataPluginBuild` returns false for commit without webUrl |
| U39.5 | `isDataPluginIssue` returns true for issue without mergedAt |
| U39.6 | `isDataPluginIssue` returns false for object with mergedAt |
| U39.7 | `isDataPluginMergeRequest` returns true for object with iid+title+mergedAt |
| U39.8 | BUG: `isDataPluginMergeRequest` returns false for actual DataPluginMergeRequest (no mergedAt field) |
| U39.9 | BUG: `isDataPluginNote` returns false for actual DataPluginNote (no noteableType field) |
| U39.10 | `isDataPluginNote` returns true for object with body+noteableType |
| U39.11 | `isDataPluginBranch` returns true for branch object |
| U39.12 | `isDataPluginBranch` returns false for commit object |
| U39.13 | `getActivityType` — commit → `"commit"` |
| U39.14 | `getActivityType` — build → `"build"` |
| U39.15 | `getActivityType` — issue → `"issue"` |
| U39.16 | `getActivityType` — branch → `"branch"` |
| U39.17 | `getActivityDate` — commit uses `date` field |
| U39.18 | `getActivityDate` — build uses `createdAt` field |
| U39.19 | `formatActivityCounts` — all-zero → `"0 activities"` |
| U39.20 | `formatActivityCounts` — count=1 uses singular |
| U39.21 | `formatActivityCounts` — count=3 uses plural |
| U39.22 | `formatActivityCounts` — multiple types joined by comma |

---

## U40 — `infoTooltipHelper` (`showInfoTooltip`)
**File**: `src/test/unit/components/infoTooltip/showInfoTooltip.test.ts`
**Source**: `src/components/infoTooltip/infoTooltipHelper.ts`

| # | Description |
|---|---|
| U32.1 | `y < innerHeight/2` → `top` set, `bottom: auto` |
| U32.2 | `y >= innerHeight/2` → `bottom` set, `top: auto` |
| U32.3 | `x < innerWidth/2` → `left` set, `right: auto` |
| U32.4 | `x >= innerWidth/2` → `right` set, `left: auto` |
| U32.5 | Renders `<h1>` with correct headline |
| U32.6 | Renders `<p>` with correct body text |
| U32.7 | No compatibility section when `compatibilityInfo` is omitted |
| U32.8 | Compatibility section rendered when arg provided |
| U32.9 | `github: true` shows `"yes"` for GitHub |
| U32.10 | `pouchDB: false` shows `"no"` for PouchDB |
| U32.11 | Calls `showModal()` on the dialog |

---

## U41 — `fileTreeUtilities` (`generateFileTree`, `filterFileTree`)
**File**: `src/test/unit/components/fileTreeUtilities.test.ts`
**Source**: `src/components/tabs/fileTree/fileList/fileListUtilities/fileTreeUtilities.tsx`
**Note**: Module has top-level `await navigator.storage.getDirectory()`. Uses `vi.stubGlobal` + dynamic `import()` in `beforeAll` to handle OPFS.

| # | Description |
|---|---|
| U41.1 | `generateFileTree` — empty files array → empty tree |
| U41.2 | `generateFileTree` — single flat file → one File node |
| U41.3 | `generateFileTree` — nested path `src/index.ts` → Folder `src` containing File `index.ts` |
| U41.4 | `generateFileTree` — two files in same folder share one folder node |
| U41.5 | `generateFileTree` — files at different roots produce separate root nodes |
| U41.6 | `filterFileTree` — matching search returns only matching file |
| U41.7 | `filterFileTree` — no match removes all files and empty folders |
| U41.8 | `filterFileTree` — search matching all files returns all |
| U41.9 | `filterFileTree` — leaf node (no children) returned unchanged |


---

# React Component Tests

**Framework**: Vitest + React Testing Library (`@testing-library/react`)
**Test environment**: jsdom
**Test ID convention**: `C{file_index}.{test_index}` (alphabetical file order)

## Testing strategy per component type

| Type | Approach |
|---|---|
| Dumb/presentational | Render with props, assert DOM output and callback calls |
| Redux-connected | Wrap in `<Provider store={configureStore(...)}>`, dispatch actions, assert DOM |
| D3 components | Assert SVG container renders; skip internal D3 canvas details |
| Dialog/modal | Assert open/closed states, button interactions |

---

## C1 — `dataPluginQuickSelect`
**File**: `src/test/component/dataPluginQuickSelect/dataPluginQuickSelect.test.tsx`
**Source**: `src/components/dataPluginQuickSelect/dataPluginQuickSelect.tsx`

Redux-connected `<select>`. Reads `settings.database.dataPlugins`.

| # | Description | Setup | Expected |
|---|---|---|---|
| C1.1 | Renders a `<select>` element | store with plugins | `<select>` present |
| C1.2 | Renders one `<option>` per data plugin | store with 3 plugins | 3 option elements |
| C1.3 | Pre-selects the currently active plugin | `selected` prop matches plugin id | that option is selected |
| C1.4 | Calls `onChange` with the selected plugin when user picks | change select | `onChange` called with plugin object |
| C1.5 | Select is disabled when `dataPlugins` list is empty | store with `dataPlugins: []` | `<select disabled>` |
| C1.6 | Each option has background color from plugin palette | store with coloured plugins | `style` attribute on option contains color |

---

## C2 — `notificationController`
**File**: `src/test/component/notificationController/notificationController.test.tsx`
**Source**: `src/components/notificationController/notificationController.tsx`

Connected to `notificationsReducer`. Renders a toast list; each toast has an icon, text, and click-to-dismiss handler.

| # | Description | Setup | Expected |
|---|---|---|---|
| C2.1 | Renders nothing when notification list is empty | store with `notificationList: []` | no `[role="alert"]` elements |
| C2.2 | Renders one toast per notification | store with 2 notifications | 2 alert elements |
| C2.3 | Displays notification text | notification with `text: 'Build failed'` | `'Build failed'` visible |
| C2.4 | Renders correct indicator for `error` type | `type: AlertType.error` | alert has `alert-error` class |
| C2.5 | Renders correct indicator for `success` type | `type: AlertType.success` | alert has `alert-success` class |
| C2.6 | Renders correct indicator for `warning` type | `type: AlertType.warning` | alert has `alert-warning` class |
| C2.7 | Renders correct indicator for `info` type | `type: AlertType.information` | alert has `alert-info` class |
| C2.8 | Clicking a toast dispatches `removeNotification` after 1 s | click, advance timers 1.1 s | notification removed from store |
| C2.9 | Auto-dismisses after 10 seconds | dispatch notification, advance 11.1 s | notification removed from store |
| C2.10 | Multiple notifications dismissed independently | 2 toasts, click one | only that notification removed |

---

## C3 — `settingsDialog`
**File**: `src/test/component/settingsDialog/settingsDialog.test.tsx`
**Source**: `src/components/settingsDialog/settingsDialog.tsx`

Tab switcher with "General" and "Database" tabs.

| # | Description | Setup | Expected |
|---|---|---|---|
| C3.1 | "General" tab content is visible by default | render | General content present |
| C3.2 | "Database" tab content is hidden initially | render | Database content absent |
| C3.3 | Clicking "Database" tab shows Database content | click Database tab | Database content present |
| C3.4 | Clicking back to "General" tab hides Database content | switch to Database, click General | General visible, Database hidden |
| C3.5 | Active tab has visual indicator (`tab-active` class on General initially) | render | General tab element has `tab-active` class |

---

## C4 — `setupDialog`
**File**: `src/test/component/setupDialog/setupDialog.test.tsx`
**Source**: `src/components/setupDialog/setupDialog.tsx`

Multi-step wizard (pages 1–4). Uses local `page` state.

| # | Description | Setup | Expected |
|---|---|---|---|
| C4.1 | Renders page 1 (Start) by default | render | `page-start` testid present, `page-database` absent |
| C4.2 | Clicking "Next" advances to page 2 | render, click Next | `page-database` testid present |
| C4.3 | Clicking "Back" on page 2 returns to page 1 | advance to page 2, click Back | `page-start` testid present |
| C4.4 | "Back" button is absent on page 1 | render | no button with text `/back/i` |
| C4.5 | Clicking through all 4 pages reaches the Summary page | click Next 3 times | `page-summary` testid present |
| C4.6 | Clicking "Save" on last page dispatches `initializeDashboardState` | navigate to page 4, click Save | `store.getState().dashboard.initialized === true` |
| C4.7 | "Cancel" button is present | render | button with text `/cancel/i` in DOM |
| C4.8 | Progress indicator shows step elements for all 4 pages | render | `setupStep1`–`setupStep4` all in DOM |

---

## C5 — `StackedAreaChart`
**File**: `src/test/component/stackedAreaChart/StackedAreaChart.test.tsx`
**Source**: `src/components/stackedAreaChart/StackedAreaChart.tsx`

D3-driven visualization. Tests focus on mount/unmount and edge-case data, not D3 internals.

| # | Description | Setup | Expected |
|---|---|---|---|
| C5.1 | Renders an `<svg>` element | render with width/height/data | at least one `<svg>` in DOM |
| C5.2 | SVG has the supplied width and height | `width: 800, height: 400` | `svg[width]='800'`, `svg[height]='400'` |
| C5.3 | Does not throw for empty `data` array | `data: []` | no error thrown, `<svg>` present |
| C5.4 | Does not throw for empty `palette` | `palette: {}` | no error thrown |
| C5.5 | Renders some SVG structure (at least a `<g>` child) | render with valid data | `svg g` elements present |
| C5.6 | Unmounts cleanly (no uncaught exceptions) | render then `unmount()` | no throw |

---

## C6 — `stackedAreaChart/utils`
**File**: `src/test/component/stackedAreaChart/utils.test.ts`
**Source**: `src/components/stackedAreaChart/utils.ts`

Pure utility functions for the StackedAreaChart component.

### `splitPositiveNegativeData(data, side)`

| # | Description | Input | Expected output |
|---|---|---|---|
| C6.1 | POSITIVE side keeps positive values and zeroes out negatives | `{ a:10, b:-5 }`, POSITIVE | `a===10, b===0` |
| C6.2 | POSITIVE side keeps zero values (`0 >= 0`) | `{ c:0 }`, POSITIVE | `c===0` |
| C6.3 | NEGATIVE side keeps negative values and zeroes out positives | `{ a:10, b:-5 }`, NEGATIVE | `b===-5, a===0` |
| C6.4 | Preserves the `date` field on every row | any input | `result[0].date === input date` |
| C6.5 | Returns empty array for empty input | `[]` | `[]` |

### `getNonEmptyKeys(keys, data)`

| # | Description | Input | Expected output |
|---|---|---|---|
| C6.6 | Excludes keys where every value is 0 | key always 0 | key not in result |
| C6.7 | Includes keys with at least one value > 0.002 | key with value 1 | key in result |
| C6.8 | Threshold is exclusive: value 0.002 is still filtered | value exactly `0.002` | key excluded |
| C6.9 | Works for negative values (uses `Math.abs`) | key with value `-5` | key included |
| C6.10 | Returns empty when no keys provided | `[], data` | `[]` |

### `getClosestIndex(x, data, xScale)`

| # | Description | Input | Expected output |
|---|---|---|---|
| C6.11 | Returns 0 when x maps to a time before or equal to the first data point | `x=0` | `0` |
| C6.12 | Returns last index when x maps to the rightmost data point | `x=1000` | `data.length - 1` |
| C6.13 | Returns index of nearest data point for a midpoint x | `x=500`, 3-point data | valid index in `[0, length)` |
| C6.14 | Returns 0 for a single-entry dataset | 1-element data, `x=0` | `0` |

### `computeVisibleYDomain(data, brushDomain, palette)`

| # | Description | Input | Expected output |
|---|---|---|---|
| C6.15 | Returns `[0, 1]` when no data falls within the brush domain | brush outside all dates | `[0, 1]` |
| C6.16 | Max of returned domain exceeds the largest stacked positive value | `a:20, b:15` | `max > 35` |
| C6.17 | Min is less than 0 when data has negative values | `a: -10` | `min < 0` |
| C6.18 | Uses padding of 1 when all values are below the visibility threshold (range = 0) | values `0.001` (filtered out) | `max - min ≈ 2` |

---

## C7 — `tabContent/fileListFile`
**File**: `src/test/component/tabContent/fileListFile.test.tsx`
**Source**: `src/components/tabs/fileTree/fileList/fileListElements/fileListFile.tsx`

File entry component in the file tree.

| # | Description | Setup | Expected |
|---|---|---|---|
| C7.1 | Renders the file name | `file.name: 'foo.ts'` | `'foo.ts'` visible |
| C7.2 | Checkbox is checked when `file.checked` is true | `checked: true` | checkbox is checked |
| C7.3 | Checkbox is unchecked when `file.checked` is false | `checked: false` | checkbox is unchecked |
| C7.4 | Clicking checkbox dispatches `updateFileListElement` | click checkbox | file's `checked` state toggled in store |

---

## C8 — `tabContent/fileListFolder`
**File**: `src/test/component/tabContent/fileListFolder.test.tsx`
**Source**: `src/components/tabs/fileTree/fileList/fileListElements/fileListFolder.tsx`

Recursive folder node in the file tree.

| # | Description | Setup | Expected |
|---|---|---|---|
| C8.1 | Renders the folder name | `folder.name: 'components'` | `'components'` visible |
| C8.2 | Children are hidden when folder is collapsed (`foldedOut: false`) | collapsed folder with child | child name not visible |
| C8.3 | Children are visible when folder is expanded (`foldedOut: true`) | expanded folder with child | child name visible |
| C8.4 | Folder with `foldedOut: false` shows collapsed state | collapsed | name visible, children hidden |
| C8.5 | Renders nested folder when expanded | parent expanded, child sub-folder | sub-folder name visible |

---

## C9 — `tabContent/fileSearch`
**File**: `src/test/component/tabContent/fileSearch.test.tsx`
**Source**: `src/components/tabs/fileTree/fileSearch/fileSearch.tsx`

Text input for filtering the file tree.

| # | Description | Setup | Expected |
|---|---|---|---|
| C9.1 | Renders a text input | render | `<input>` present |
| C9.2 | Input is empty by default | render with no initial value | `value` is empty string |
| C9.3 | Typing updates the input value | type `'src'` | input shows `'src'` |
| C9.4 | `onChange` callback is called after typing | type `'src'` | `setFileSearch` called with `'src'` |
| C9.5 | Clearing the input calls `onChange` with empty string | type then clear | `setFileSearch` called with `''` |

---

## C10 — `tabContent/parametersGeneral`
**File**: `src/test/component/tabContent/parametersGeneral.test.tsx`
**Source**: `src/components/tabs/parameters/parametersGeneral/parametersGeneral.tsx`

Presentational component. Receives `parametersGeneral` and `setParametersGeneral` as props.

| # | Description | Setup | Expected |
|---|---|---|---|
| C10.1 | Renders a granularity `<select>` | render with default props | select element present |
| C10.2 | Granularity dropdown shows the current value | `granularity: 'months'` | `'months'` is selected option |
| C10.3 | Changing granularity calls `setParametersGeneral` with new value | change select to `'days'` | callback called with `{ granularity: 'days', … }` |
| C10.4 | Renders an "exclude merge commits" checkbox | any props | checkbox present |
| C10.5 | Checkbox reflects current `excludeMergeCommits` state | `excludeMergeCommits: true` | checkbox is checked |
| C10.6 | Toggling checkbox calls `setParametersGeneral` with inverted value | click checkbox when `excludeMergeCommits: true` | callback called with boolean value |
| C10.7 | All controls are disabled when `disabled` prop is `true` | `disabled: true` | select + checkbox have `disabled` attribute |

---

## C11 — `tabController/tabControllerButton`
**File**: `src/test/component/tabController/tabControllerButton.test.tsx`
**Source**: `src/components/tabMenu/tabControllerButton/tabControllerButton.tsx`

Simple toolbar button.

| # | Description | Setup | Expected |
|---|---|---|---|
| C11.1 | Renders a `<button>` | render with icon prop | `<button>` in DOM |
| C11.2 | Clicking calls the `onClick` prop | click | `onClick` called once |
| C11.3 | Button is not disabled by default (no `disabled` prop support) | render | `<button>` not disabled |
| C11.4 | Renders the passed icon/label (name as alt text) | `name: 'Export'` | `img[alt='Export']` visible |
| C11.5 | `onClick` fires normally (component has no disabled prop) | render, click | `onClick` called |

---

## C12 — `DashboardItem`
**File**: `src/test/component/dashboard/dashboardItem.test.tsx`
**Source**: `src/components/dashboard/dashboardItem/dashboardItem.tsx`

> Mocks: `pluginRegistry.ts` (fake plugin), `dataPluginStorage.ts` (never-resolving promise keeps component in loading state), `fileTreeUtilities.tsx`.
> Event queries: mouseDown on plugin name span (bubbles to interaction bar); DOM child index for resize bars; `getAllByRole('button')` for toolbar buttons.

| # | Description | Setup | Expected |
|---|---|---|---|
| C12.1 | Renders nothing when item has no `x` or `y` | `x: undefined, y: undefined` | `#dashboardItem1` absent |
| C12.2 | Renders the plugin name in the interaction bar | default props | `'TestPlugin'` in DOM |
| C12.3 | Shows "No Data Plugin Selected" before data plugin loads | DataPlugin mock never resolves | text visible |
| C12.4 | mouseDown on interaction bar calls `setDragResizeItem(id, drag)` | mouseDown on name span (bubbles) | called with `(1, DragResizeMode.drag)` |
| C12.5 | mouseDown on top resize bar calls `setDragResizeItem(id, resizeTop)` | `#dashboardItem1.children[3]` | called with `(1, DragResizeMode.resizeTop)` |
| C12.6 | Help panel hidden by default, shown after clicking help button | click `getAllByRole('button')[1]` | panel `display` changes to `'block'` |

---

## Component test file locations

```
src/test/component/
├── dashboard/
│   └── dashboardItem.test.tsx                                     (C12)
├── dataPluginQuickSelect/
│   └── dataPluginQuickSelect.test.tsx                             (C1)
├── notificationController/
│   └── notificationController.test.tsx                            (C2)
├── settingsDialog/
│   └── settingsDialog.test.tsx                                    (C3)
├── setupDialog/
│   └── setupDialog.test.tsx                                       (C4)
├── stackedAreaChart/
│   ├── StackedAreaChart.test.tsx                                  (C5)
│   └── utils.test.ts                                              (C6)
├── tabContent/
│   ├── fileListFile.test.tsx                                      (C7)
│   ├── fileListFolder.test.tsx                                    (C8)
│   ├── fileSearch.test.tsx                                        (C9)
│   └── parametersGeneral.test.tsx                                 (C10)
└── tabController/
    └── tabControllerButton.test.tsx                               (C11)
```

---

## Notes

- **Redux-connected components** (C1, C2, C3, C4): Use a pre-configured test store via `configureStore` from Redux Toolkit. Pass it via `<Provider>`.
- **Timers** (C2.8, C2.9): Use `vi.useFakeTimers()` / `vi.advanceTimersByTime()`. Call `vi.useRealTimers()` in `afterEach`.
- **D3 components** (C5): jsdom does not support `SVGElement.getBBox()` or layout. Mock or stub methods that require a real browser layout engine.
- **SCSS modules**: Vitest handles CSS modules via identity proxy — use `data-testid` or element type selectors for assertions, not class names.
- **ResizeObserver**: jsdom does not implement it. A global mock is defined in `setup.ts`.
