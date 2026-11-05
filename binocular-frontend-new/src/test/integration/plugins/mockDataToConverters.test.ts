// I17 — MockData → 6 convertToChartData functions
//
// Verifies that real MockData output can be piped through each visualization
// plugin's convertToChartData (or equivalent) without errors and produces
// valid, non-NaN chart-ready data.
// No Redux — pure data-layer integration.
//
// Converters tested:
//   1. builds/builds        — convertToChartData
//   2. commits/changes      — convertToChartData
//   3. commits/fileChanges  — convertCommitDataToChangesChartData
//   4. issues/issues        — convertToChartData
//   5. issues/mergeRequests — convertToChartData
//   6. authorBehaviour/timeSpent — convertToChartData

import { describe, it, expect, beforeEach } from 'vitest';

import MockData from '../../../plugins/dataPlugins/mockData/src/index.ts';
import { convertToChartData as buildsConverter } from '../../../plugins/visualizationPlugins/builds/builds/src/utilities/dataConverter.ts';
import { convertToChartData as changesConverter } from '../../../plugins/visualizationPlugins/commits/changes/src/utilities/dataConverter.ts';
import { convertCommitDataToChangesChartData } from '../../../plugins/visualizationPlugins/commits/fileChanges/src/utilities/dataConverter.ts';
import { convertToChartData as issuesConverter } from '../../../plugins/visualizationPlugins/issues/issues/src/utilities/dataConverter.ts';
import { convertToChartData as mrConverter } from '../../../plugins/visualizationPlugins/issues/mergeRequests/src/utilities/dataConverter.ts';
import { convertToChartData as timeSpentConverter } from '../../../plugins/visualizationPlugins/authorBehaviour/timeSpent/src/utilities/dataConverter.ts';

const FROM = '2024-01-01T00:00:00.000Z';
const TO = '2024-12-31T23:59:59.000Z';

/** Minimal props stub — converters only read settings and parameters.granularity */
function makeProps(settings: object) {
  return {
    settings,
    authorList: [],
    fileList: [],
    sprintList: [],
    parameters: {
      parametersGeneral: { granularity: 'weeks', excludeMergeCommits: false },
      parametersDateRange: { from: FROM, to: TO },
    },
  } as any; // eslint-disable-line @typescript-eslint/no-explicit-any
}

function assertNoNaN(chartData: Array<Record<string, unknown>>) {
  for (const row of chartData) {
    for (const [key, val] of Object.entries(row)) {
      if (key !== 'date') {
        expect(Number.isNaN(val), `${key} should not be NaN`).toBe(false);
      }
    }
  }
}

describe('I17 — MockData → 6 data converters', () => {
  let plugin: MockData;

  beforeEach(async () => {
    plugin = new MockData();
    await plugin.init();
  });

  // ── I17.1 — builds converter ──────────────────────────────────────────────

  it('I17.1 — builds convertToChartData returns { chartData, scale, palette } with no NaN', async () => {
    const builds = await plugin.builds.getAll(FROM, TO);
    expect(builds.length).toBeGreaterThan(0);

    const result = buildsConverter(builds, makeProps({ splitBuildsPerAuthor: false }));

    expect(result).toHaveProperty('chartData');
    expect(result).toHaveProperty('scale');
    expect(result).toHaveProperty('palette');
    assertNoNaN(result.chartData as Array<Record<string, unknown>>);
  });

  // ── I17.2 — changes converter ─────────────────────────────────────────────

  it('I17.2 — changes convertToChartData returns { chartData, scale, palette } with no NaN', async () => {
    const commits = await plugin.commits.getAll(FROM, TO);
    expect(commits.length).toBeGreaterThan(0);

    const result = changesConverter(commits, makeProps({ splitAdditionsDeletions: false }));

    expect(result).toHaveProperty('chartData');
    expect(result).toHaveProperty('scale');
    expect(result).toHaveProperty('palette');
    assertNoNaN(result.chartData as Array<Record<string, unknown>>);
  });

  // ── I17.3 — fileChanges converter ────────────────────────────────────────

  it('I17.3 — fileChanges convertCommitDataToChangesChartData returns { commitChartData, commitScale, commitPalette } with no NaN', async () => {
    const commits = await plugin.commits.getAll(FROM, TO);
    expect(commits.length).toBeGreaterThan(0);

    const params = {
      parametersGeneral: { granularity: 'weeks', excludeMergeCommits: false },
      parametersDateRange: { from: FROM, to: TO },
    };
    const result = convertCommitDataToChangesChartData(commits, [], false, params);

    expect(result).toHaveProperty('commitChartData');
    expect(result).toHaveProperty('commitScale');
    expect(result).toHaveProperty('commitPalette');
    assertNoNaN(result.commitChartData as Array<Record<string, unknown>>);
  });

  // ── I17.4 — issues converter ─────────────────────────────────────────────

  it('I17.4 — issues convertToChartData returns { chartData, scale, palette } with no NaN', async () => {
    const issues = await plugin.issues.getAll(FROM, TO);
    expect(issues.length).toBeGreaterThan(0);

    const result = issuesConverter(issues, makeProps({ splitIssuesPerAuthor: false, breakdown: false }));

    expect(result).toHaveProperty('chartData');
    expect(result).toHaveProperty('scale');
    expect(result).toHaveProperty('palette');
    assertNoNaN(result.chartData as Array<Record<string, unknown>>);
  });

  // ── I17.5 — mergeRequests converter ──────────────────────────────────────

  it('I17.5 — mergeRequests convertToChartData returns { chartData, scale, palette } with no NaN', async () => {
    const mrs = await plugin.mergeRequests.getAll(FROM, TO);
    expect(mrs.length).toBeGreaterThan(0);

    const result = mrConverter(mrs, makeProps({ splitMergeRequestsPerAuthor: false, breakdown: false }));

    expect(result).toHaveProperty('chartData');
    expect(result).toHaveProperty('scale');
    expect(result).toHaveProperty('palette');
    assertNoNaN(result.chartData as Array<Record<string, unknown>>);
  });

  // ── I17.6 — timeSpent converter ───────────────────────────────────────────

  it('I17.6 — timeSpent convertToChartData returns { chartData, scale, palette } with no NaN', async () => {
    const notes = await plugin.notes.getAll(FROM, TO);
    expect(notes.length).toBeGreaterThan(0);

    const result = timeSpentConverter(notes, makeProps({ splitTimePerIssue: false, splitSpentRemoved: false, breakdown: false }));

    expect(result).toHaveProperty('chartData');
    expect(result).toHaveProperty('scale');
    expect(result).toHaveProperty('palette');
    assertNoNaN(result.chartData as Array<Record<string, unknown>>);
  });
});
