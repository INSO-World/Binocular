import { describe, it, expect } from 'vitest';
import { convertToChartData } from '../../../../../../plugins/visualizationPlugins/issues/mergeRequests/src/utilities/dataConverter';
import type { DataPluginMergeRequest } from '../../../../../../plugins/interfaces/dataPluginInterfaces/dataPluginMergeRequests';
import type { MergeRequestsSettings } from '../../../../../../plugins/visualizationPlugins/issues/mergeRequests/src/settings/settings';
import type { AuthorType } from '../../../../../../types/data/authorType';
import type { DataPlugin } from '../../../../../../plugins/interfaces/dataPlugin';
import type { Store } from '@reduxjs/toolkit';

function makeProps(settings: MergeRequestsSettings, authorList: AuthorType[] = []) {
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

const defaultSettings: MergeRequestsSettings = {
  splitMergeRequestsPerAuthor: false,
  breakdown: false,
  visualizationStyle: 'curved',
  showSprints: false,
};

function makeMR(overrides: Partial<DataPluginMergeRequest> = {}): DataPluginMergeRequest {
  return {
    id: 'mr1',
    iid: 1,
    title: 'Test MR',
    state: 'OPENED',
    webUrl: '',
    createdAt: '2023-06-15T00:00:00Z',
    closedAt: null,
    updatedAt: null,
    sourceBranch: 'feature',
    targetBranch: 'main',
    author: null,
    assignee: null,
    assignees: [],
    notes: [],
    ...overrides,
  };
}

describe('convertToChartData (mergeRequests)', () => {
  it('U34.1 returns empty result for empty array', () => {
    const result = convertToChartData([], makeProps(defaultSettings));
    expect(result.chartData).toEqual([]);
    expect(result.palette).toEqual({});
    expect(result.scale).toEqual([]);
  });

  it('U34.2 breakdown:false, state:MERGED → negative Merged count', () => {
    const mr = makeMR({
      createdAt: '2023-05-01T00:00:00Z',
      closedAt: '2023-06-15T00:00:00Z',
      state: 'MERGED',
    });
    const result = convertToChartData([mr], makeProps(defaultSettings));
    const bucket = result.chartData.find((d) => d['Merged'] < 0);
    expect(bucket).toBeDefined();
    expect(bucket!['Merged']).toBe(-1);
  });

  it('U34.3 breakdown:false, state:CLOSED → negative Closed count', () => {
    const mr = makeMR({
      createdAt: '2023-05-01T00:00:00Z',
      closedAt: '2023-06-15T00:00:00Z',
      state: 'CLOSED',
    });
    const result = convertToChartData([mr], makeProps(defaultSettings));
    const bucket = result.chartData.find((d) => d['Closed'] < 0);
    expect(bucket).toBeDefined();
    expect(bucket!['Closed']).toBe(-1);
  });

  it('U34.4 breakdown:true → running Open count is positive when MR is open', () => {
    const settings: MergeRequestsSettings = { ...defaultSettings, breakdown: true };
    const mr = makeMR({ createdAt: '2023-05-01T00:00:00Z', closedAt: null });
    const result = convertToChartData([mr], makeProps(settings));
    const maxOpen = Math.max(...result.chartData.map((d) => d['Open'] ?? 0));
    expect(maxOpen).toBeGreaterThanOrEqual(1);
  });

  it('U34.5 scale computed — positive scale reflects opened MRs', () => {
    const mr = makeMR({ createdAt: '2023-06-15T00:00:00Z', closedAt: null });
    const result = convertToChartData([mr], makeProps(defaultSettings));
    expect(result.scale[1]).toBeGreaterThan(0);
  });
});
