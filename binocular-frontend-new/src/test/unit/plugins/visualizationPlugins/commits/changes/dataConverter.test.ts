import { describe, it, expect } from 'vitest';
import { convertToChartData } from '../../../../../../plugins/visualizationPlugins/commits/changes/src/utilities/dataConverter';
import type { DataPluginCommit } from '../../../../../../plugins/interfaces/dataPluginInterfaces/dataPluginCommits';
import type { ChangesSettings } from '../../../../../../plugins/visualizationPlugins/commits/changes/src/settings/settings';
import type { AuthorType } from '../../../../../../types/data/authorType';
import type { DataPlugin } from '../../../../../../plugins/interfaces/dataPlugin';
import type { Store } from '@reduxjs/toolkit';
import type { FileListElementType } from '../../../../../../types/data/fileListType';

const defaultSettings: ChangesSettings = {
  splitAdditionsDeletions: false,
  visualizationStyle: 'curved',
  showSprints: false,
};

function makeAuthor(id: string, signature: string): AuthorType {
  return {
    id: 1,
    parent: -1,
    selected: true,
    color: { main: '#ff0000', secondary: '#ff0000aa' },
    user: { id, gitSignature: signature, account: null },
  };
}

function makeProps(settings: ChangesSettings, authorList: AuthorType[] = [], fileList: FileListElementType[] = []) {
  return {
    settings,
    parameters: {
      parametersGeneral: { granularity: 'months', excludeMergeCommits: false },
      parametersDateRange: { from: '', to: '' },
    },
    authorList,
    fileList,
    sprintList: [],
    dataConnection: {} as unknown as DataPlugin,
    chartContainerRef: { current: null },
    store: {} as unknown as Store,
  };
}

function makeCommit(overrides: Partial<DataPluginCommit> = {}): DataPluginCommit {
  return {
    sha: 'abc123',
    shortSha: 'abc',
    messageHeader: 'feat: test',
    message: 'feat: test',
    user: { id: 'u1', gitSignature: 'Alice', account: null },
    branch: 'main',
    date: '2023-06-15T00:00:00Z',
    parents: [],
    webUrl: '',
    stats: { additions: 10, deletions: 5 },
    ...overrides,
  };
}

describe('convertToChartData (changes)', () => {
  it('U36.1 returns empty result for empty array', () => {
    const result = convertToChartData([], makeProps(defaultSettings));
    expect(result.chartData).toEqual([]);
    expect(result.palette).toEqual({});
    expect(result.scale).toEqual([]);
  });

  it('U36.2 commits are bucketed by date field', () => {
    const commit = makeCommit({ date: '2023-06-15T00:00:00Z' });
    const author = makeAuthor('u1', 'Alice');
    const result = convertToChartData([commit], makeProps(defaultSettings, [author]));
    // chartData should be non-empty (commit was processed)
    expect(result.chartData.length).toBeGreaterThan(0);
  });

  it('U36.3 splitAdditionsDeletions:false — author name appears as key', () => {
    const commit = makeCommit({ date: '2023-06-15T00:00:00Z', stats: { additions: 5, deletions: 2 } });
    const author = makeAuthor('u1', 'Alice');
    const result = convertToChartData([commit], makeProps(defaultSettings, [author]));
    const allKeys = result.chartData.flatMap((d) => Object.keys(d));
    expect(allKeys).toContain('Alice');
  });

  it('U36.4 splitAdditionsDeletions:true — (Additions) and (Deletions) keys appear', () => {
    const settings: ChangesSettings = { ...defaultSettings, splitAdditionsDeletions: true };
    const commit = makeCommit({ date: '2023-06-15T00:00:00Z', stats: { additions: 5, deletions: 2 } });
    const author = makeAuthor('u1', 'Alice');
    const result = convertToChartData([commit], makeProps(settings, [author]));
    const allKeys = result.chartData.flatMap((d) => Object.keys(d));
    expect(allKeys.some((k) => k.startsWith('(Additions)'))).toBe(true);
    expect(allKeys.some((k) => k.startsWith('(Deletions)'))).toBe(true);
  });

  it('U36.5 BUG — excludeMergeCommits:true with all-merge input crashes (source bug: no empty-check after filter)', () => {
    // The source filters out merge commits but then accesses sortedCommits[0].date without
    // re-checking length, so if all commits are merge commits, it throws a TypeError.
    const props = makeProps({ ...defaultSettings }, [], []);
    props.parameters.parametersGeneral.excludeMergeCommits = true;
    const merge = makeCommit({ message: 'Merge branch x into main', date: '2023-06-15T00:00:00Z' });
    expect(() => convertToChartData([merge], props)).toThrow(TypeError);
  });

  it('U36.6 scale computed with positive and negative values', () => {
    const settings: ChangesSettings = { ...defaultSettings, splitAdditionsDeletions: true };
    const commit = makeCommit({ date: '2023-06-15T00:00:00Z', stats: { additions: 10, deletions: 5 } });
    const author = makeAuthor('u1', 'Alice');
    const result = convertToChartData([commit], makeProps(settings, [author]));
    expect(result.scale[1]).toBeGreaterThanOrEqual(0);
    expect(result.scale[0]).toBeLessThanOrEqual(0);
  });
});
