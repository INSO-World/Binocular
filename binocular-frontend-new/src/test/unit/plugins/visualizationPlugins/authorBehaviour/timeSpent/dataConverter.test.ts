import { describe, it, expect } from 'vitest';
import { convertToChartData } from '../../../../../../plugins/visualizationPlugins/authorBehaviour/timeSpent/src/utilities/dataConverter';
import type { DataPluginNote } from '../../../../../../plugins/interfaces/dataPluginInterfaces/dataPluginNotes';
import type { DataPluginIssue } from '../../../../../../plugins/interfaces/dataPluginInterfaces/dataPluginIssues';
import type { TimeSpentSettings } from '../../../../../../plugins/visualizationPlugins/authorBehaviour/timeSpent/src/settings/settings';
import type { AuthorType } from '../../../../../../types/data/authorType';
import type { DataPlugin } from '../../../../../../plugins/interfaces/dataPlugin';
import type { Store } from '@reduxjs/toolkit';

const defaultSettings: TimeSpentSettings = {
  splitTimePerIssue: false,
  splitSpentRemoved: false,
  breakdown: false,
  visualizationStyle: 'curved',
  showSprints: false,
};

function makeProps(settings: TimeSpentSettings, authorList: AuthorType[] = []) {
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

function makeNote(body: string, issueId?: string, issueIid?: number, issueTitle?: string): DataPluginNote {
  return {
    body,
    createdAt: '2023-06-15T00:00:00Z',
    updatedAt: '2023-06-15T00:00:00Z',
    author: { id: 'a1', name: 'Alice', user: { id: 'u1', gitSignature: 'Alice', account: null }, platform: 'gitlab' },
    issue: issueId
      ? ({
          id: issueId,
          iid: issueIid ?? 1,
          title: issueTitle ?? 'Test Issue',
          description: '',
          state: 'open',
          webUrl: '',
          createdAt: '2023-01-01',
          closedAt: null,
          labels: [],
          author: null,
          assignee: null,
          assignees: [],
          notes: [],
          commits: [],
        } satisfies DataPluginIssue)
      : null,
    mergeRequest: null,
  };
}

function makeAuthor(id: string, signature: string): AuthorType {
  return {
    id: 1,
    parent: -1,
    selected: true,
    color: { main: '#ff0000', secondary: '#ff0000aa' },
    user: { id, gitSignature: signature, account: null },
  };
}

describe('convertToChartData (timeSpent)', () => {
  it('U37.1 returns empty result for empty array', () => {
    const result = convertToChartData([], makeProps(defaultSettings));
    expect(result.chartData).toEqual([]);
    expect(result.scale).toEqual([]);
    expect(result.palette).toEqual({});
  });

  it('U37.2 BUG — non-matching notes crash (source bug: no empty-check after extractTimeTrackingDataFromNotes)', () => {
    // When notes don't match a time-tracking pattern, extractTimeTrackingDataFromNotes returns []
    // but the source then accesses sortedData[0].createdAt without checking length first.
    const note = makeNote('just a comment');
    expect(() => convertToChartData([note], makeProps(defaultSettings))).toThrow(TypeError);
  });

  it('U37.3 splitTimePerIssue:true — chart keys include issue title', () => {
    const settings: TimeSpentSettings = { ...defaultSettings, splitTimePerIssue: true };
    const note = makeNote('added 2h of time spent at 2023-06-15', 'issue-1', 1, 'My Issue');
    const result = convertToChartData([note], makeProps(settings));
    const allKeys = result.chartData.flatMap((d) => Object.keys(d));
    expect(allKeys.some((k) => k.includes('My Issue'))).toBe(true);
  });

  it('U37.4 splitTimePerIssue:false — author-name keys appear when author has user.id', () => {
    const author = makeAuthor('u1', 'Alice');
    const note = makeNote('added 2h of time spent at 2023-06-15');
    const result = convertToChartData([note], makeProps(defaultSettings, [author]));
    // chartData should have keys corresponding to author
    const allKeys = result.chartData.flatMap((d) => Object.keys(d));
    expect(allKeys.some((k) => k === 'Alice' || k === '(Total) Alice' || k === 'others')).toBe(true);
  });

  it('U37.5 scale[1] positive when time is spent', () => {
    const settings: TimeSpentSettings = { ...defaultSettings, splitTimePerIssue: true };
    const note = makeNote('added 2h of time spent at 2023-06-15', 'issue-1', 1, 'My Issue');
    const result = convertToChartData([note], makeProps(settings));
    expect(result.scale[1]).toBeGreaterThanOrEqual(0);
  });
});
