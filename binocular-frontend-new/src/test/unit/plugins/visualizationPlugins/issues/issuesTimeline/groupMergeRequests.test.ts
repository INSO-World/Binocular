import { describe, it, expect } from 'vitest';
import moment from 'moment';
import { groupMergeRequests } from '../../../../../../plugins/visualizationPlugins/issues/issuesTimeline/src/chart/helper/groupMergeRequests';
import type { MappedDataPluginMergeRequest } from '../../../../../../plugins/visualizationPlugins/issues/issuesTimeline/src/chart/types';

function makeMR(id: string, createdAt: string, closedAt: string): MappedDataPluginMergeRequest {
  return {
    id,
    iid: 1,
    title: id,
    state: 'merged',
    webUrl: '',
    createdAt: moment(createdAt),
    closedAt: moment(closedAt),
    updatedAt: null,
    sourceBranch: 'feature',
    targetBranch: 'main',
    author: null,
    assignee: null,
    assignees: [],
    notes: [],
  };
}

describe('groupMergeRequests', () => {
  it('U23.1 returns empty array for empty input', () => {
    expect(groupMergeRequests([])).toEqual([]);
  });

  it('U23.2 MRs in the same month land in one group', () => {
    const mr1 = makeMR('1', '2024-01-05', '2024-01-10');
    const mr2 = makeMR('2', '2024-01-15', '2024-01-20');
    const groups = groupMergeRequests([mr1, mr2]);
    expect(groups).toHaveLength(1);
    expect(groups[0]).toHaveLength(2);
  });

  it('U23.3 MRs in different months land in separate groups', () => {
    const mr1 = makeMR('1', '2024-01-05', '2024-01-10');
    const mr2 = makeMR('2', '2024-02-05', '2024-02-10');
    const groups = groupMergeRequests([mr1, mr2]);
    expect(groups).toHaveLength(2);
  });

  it('U23.4 multiple MRs in same month are all in that group', () => {
    const mrs = ['01', '08', '15', '22'].map((d) => makeMR(d, `2024-03-${d}`, `2024-03-${d}`));
    const groups = groupMergeRequests(mrs);
    expect(groups).toHaveLength(1);
    expect(groups[0]).toHaveLength(4);
  });

  it('U23.5 returns one sub-array per distinct month', () => {
    const mr1 = makeMR('1', '2024-01-01', '2024-01-31');
    const mr2 = makeMR('2', '2024-02-01', '2024-02-28');
    const mr3 = makeMR('3', '2024-02-15', '2024-02-20');
    const mr4 = makeMR('4', '2024-03-01', '2024-03-31');
    const groups = groupMergeRequests([mr1, mr2, mr3, mr4]);
    expect(groups).toHaveLength(3);
  });
});
