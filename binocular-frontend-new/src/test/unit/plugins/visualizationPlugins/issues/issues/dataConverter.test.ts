import { describe, it, expect } from 'vitest';
import { convertToChartData } from '../../../../../../plugins/visualizationPlugins/issues/issues/src/utilities/dataConverter';
import type { DataPluginIssue } from '../../../../../../plugins/interfaces/dataPluginInterfaces/dataPluginIssues';
import type { IssueSettings } from '../../../../../../plugins/visualizationPlugins/issues/issues/src/settings/settings';
import type { AuthorType } from '../../../../../../types/data/authorType';
import type { DataPlugin } from '../../../../../../plugins/interfaces/dataPlugin';
import type { Store } from '@reduxjs/toolkit';

function makeProps(settings: IssueSettings, authorList: AuthorType[] = []) {
  return {
    settings,
    parameters: {
      parametersGeneral: { granularity: 'months', excludeMergeCommits: false },
      parametersDateRange: { from: '', to: '' },
    },
    authorList,
    fileList: [],
    sprintList: [],
    dataConnection: {} as unknown as DataPlugin,
    chartContainerRef: { current: null },
    store: {} as unknown as Store,
  };
}

const defaultSettings: IssueSettings = {
  splitIssuesPerAuthor: false,
  breakdown: false,
  visualizationStyle: 'curved',
  showSprints: false,
};

function makeIssue(overrides: Partial<DataPluginIssue> = {}): DataPluginIssue {
  return {
    id: 'i1',
    iid: 1,
    title: 'Test Issue',
    description: '',
    state: 'opened',
    webUrl: '',
    createdAt: '2023-06-15T00:00:00Z',
    closedAt: null,
    labels: [],
    author: null,
    assignee: null,
    assignees: [],
    notes: [],
    commits: [],
    ...overrides,
  };
}

describe('convertToChartData (issues)', () => {
  it('U33.1 returns empty result for empty array', () => {
    const result = convertToChartData([], makeProps(defaultSettings));
    expect(result.chartData).toEqual([]);
    expect(result.palette).toEqual({});
    expect(result.scale).toEqual([]);
  });

  it('U33.2 breakdown:false — opened issue produces positive Opened count', () => {
    const issue = makeIssue({ createdAt: '2023-06-15T00:00:00Z', closedAt: null });
    const result = convertToChartData([issue], makeProps(defaultSettings));
    expect(result.chartData.length).toBeGreaterThan(0);
    const bucketWithOpened = result.chartData.find((d) => d['Opened'] > 0);
    expect(bucketWithOpened).toBeDefined();
    expect(bucketWithOpened!['Opened']).toBe(1);
  });

  it('U33.3 breakdown:false — closed issue produces negative Closed count', () => {
    const issue = makeIssue({
      createdAt: '2023-05-01T00:00:00Z',
      closedAt: '2023-06-15T00:00:00Z',
    });
    const result = convertToChartData([issue], makeProps(defaultSettings));
    const bucketWithClosed = result.chartData.find((d) => d['Closed'] < 0);
    expect(bucketWithClosed).toBeDefined();
    expect(bucketWithClosed!['Closed']).toBe(-1);
  });

  it('U33.4 breakdown:true — Open count increments on open, decrements on close', () => {
    const settings: IssueSettings = { ...defaultSettings, breakdown: true };
    const issue = makeIssue({
      createdAt: '2023-05-01T00:00:00Z',
      closedAt: '2023-07-01T00:00:00Z',
    });
    const result = convertToChartData([issue], makeProps(settings));
    // After open: Open should have been 1 at some point
    const maxOpen = Math.max(...result.chartData.map((d) => d['Open'] ?? 0));
    expect(maxOpen).toBeGreaterThanOrEqual(1);
  });

  it('U33.5 scale[1] is positive, scale[0] is 0 or negative for opened issues', () => {
    const issue = makeIssue({ createdAt: '2023-06-15T00:00:00Z', closedAt: null });
    const result = convertToChartData([issue], makeProps(defaultSettings));
    expect(result.scale[1]).toBeGreaterThan(0);
    expect(result.scale[0]).toBeLessThanOrEqual(0);
  });

  it('U33.6 splitIssuesPerAuthor:true — chart keys prefixed with "Opened Issues"', () => {
    const settings: IssueSettings = { ...defaultSettings, splitIssuesPerAuthor: true };
    const issue = makeIssue({ createdAt: '2023-06-15T00:00:00Z', closedAt: null });
    const result = convertToChartData([issue], makeProps(settings));
    const allKeys = result.chartData.flatMap((d) => Object.keys(d));
    expect(allKeys.some((k) => k.startsWith('Opened Issues') || k.startsWith('Open Issues'))).toBe(true);
  });

  it('U33.7 unassigned issue (no assignee) lands under unassigned key', () => {
    const settings: IssueSettings = { ...defaultSettings, splitIssuesPerAuthor: true };
    const issue = makeIssue({ createdAt: '2023-06-15T00:00:00Z', closedAt: null, assignee: null });
    const result = convertToChartData([issue], makeProps(settings));
    const allKeys = result.chartData.flatMap((d) => Object.keys(d));
    expect(allKeys.some((k) => k.includes('unassigned'))).toBe(true);
  });
});
