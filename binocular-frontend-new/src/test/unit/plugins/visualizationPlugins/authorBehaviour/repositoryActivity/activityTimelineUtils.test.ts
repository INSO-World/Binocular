import { describe, it, expect } from 'vitest';
import { convertToActivityTimelineFormat } from '../../../../../../plugins/visualizationPlugins/authorBehaviour/repositoryActivity/src/utilities/activityTimelineUtils';
import type { AnyActivityDataPlugin } from '../../../../../../plugins/visualizationPlugins/authorBehaviour/repositoryActivity/src/utilities/types';

// Minimal DataPluginCommit shape (commit is identified by 'sha' + 'messageHeader' + 'stats')
function makeCommit(date: string): AnyActivityDataPlugin {
  return {
    sha: 'abc',
    messageHeader: 'test',
    date,
    stats: { additions: 0, deletions: 0 },
    files: { data: [] },
    parents: [],
    user: { gitSignature: 'test', id: 'u1' },
  } as unknown as AnyActivityDataPlugin;
}

describe('convertToActivityTimelineFormat', () => {
  it('U51.1 empty array returns empty chartData', () => {
    const { chartData } = convertToActivityTimelineFormat([]);
    expect(chartData).toHaveLength(0);
  });

  it('U51.2 single activity returns one entry with value 1', () => {
    const { chartData } = convertToActivityTimelineFormat([makeCommit('2024-01-15')]);
    expect(chartData).toHaveLength(1);
    expect(chartData[0].value).toBe(1);
  });

  it('U51.3 two activities on same day collapse to one entry with value 2', () => {
    const { chartData } = convertToActivityTimelineFormat([makeCommit('2024-01-15'), makeCommit('2024-01-15')]);
    expect(chartData).toHaveLength(1);
    expect(chartData[0].value).toBe(2);
  });

  it('U51.4 activities on different days produce separate entries', () => {
    const { chartData } = convertToActivityTimelineFormat([makeCommit('2024-01-15'), makeCommit('2024-01-16')]);
    expect(chartData).toHaveLength(2);
  });

  it('U51.5 branch activity without latestCommit (null date) is skipped', () => {
    const branch = { branch: 'main', active: true, latestCommit: null } as unknown as AnyActivityDataPlugin;
    const { chartData } = convertToActivityTimelineFormat([branch]);
    expect(chartData).toHaveLength(0);
  });

  it('U51.6 output is sorted ascending by date', () => {
    const { chartData } = convertToActivityTimelineFormat([makeCommit('2024-03-01'), makeCommit('2024-01-01'), makeCommit('2024-02-01')]);
    expect(chartData[0].date.getTime()).toBeLessThan(chartData[1].date.getTime());
    expect(chartData[1].date.getTime()).toBeLessThan(chartData[2].date.getTime());
  });
});
