import { describe, it, expect } from 'vitest';
import { getHistoryForCommit } from '../../../../../../plugins/visualizationPlugins/expertise/codeExpertise/src/utilities/dbUtils';
import type { DataPluginCommit, DataPluginOwnership } from '../../../../../../plugins/interfaces/dataPluginInterfaces/dataPluginCommits';

function makeOwnershipCommit(sha: string, parents: string[], date: string): DataPluginOwnership {
  return { sha, date, parents, files: [] };
}

function makeCommit(sha: string, parents: string[], date: string): DataPluginCommit {
  return {
    sha,
    shortSha: sha.slice(0, 7),
    messageHeader: '',
    message: '',
    user: { id: '', gitSignature: '', account: null },
    branch: 'main',
    date,
    parents,
    webUrl: '',
    stats: { additions: 0, deletions: 0 },
  };
}

describe('getHistoryForCommit', () => {
  it('U3.1 returns only the commit itself for a genesis commit (no parents)', () => {
    const genesis = makeCommit('aaa', [], '2023-01-01');
    const all: DataPluginOwnership[] = [makeOwnershipCommit('aaa', [], '2023-01-01')];
    const result = getHistoryForCommit(genesis, all);
    expect(result).toEqual(['aaa']);
  });

  it('U3.2 follows a linear chain and returns shas sorted newest-first', () => {
    // chain: A (oldest) → B → C (newest)
    const all: DataPluginOwnership[] = [
      makeOwnershipCommit('aaa', [], '2023-01-01'),
      makeOwnershipCommit('bbb', ['aaa'], '2023-06-01'),
      makeOwnershipCommit('ccc', ['bbb'], '2023-12-01'),
    ];
    const head = makeCommit('ccc', ['bbb'], '2023-12-01');
    const result = getHistoryForCommit(head, all);
    expect(result).toEqual(['ccc', 'bbb', 'aaa']);
  });

  it('U3.3 includes commits from both branches of a merge', () => {
    // A → B and A → C, then merge D (parents: B, C)
    const all: DataPluginOwnership[] = [
      makeOwnershipCommit('aaa', [], '2023-01-01'),
      makeOwnershipCommit('bbb', ['aaa'], '2023-03-01'),
      makeOwnershipCommit('ccc', ['aaa'], '2023-03-15'),
      makeOwnershipCommit('ddd', ['bbb', 'ccc'], '2023-06-01'),
    ];
    const head = makeCommit('ddd', ['bbb', 'ccc'], '2023-06-01');
    const result = getHistoryForCommit(head, all);
    expect(result).toContain('aaa');
    expect(result).toContain('bbb');
    expect(result).toContain('ccc');
    expect(result).toContain('ddd');
    expect(result).toHaveLength(4);
  });

  it('U3.4 does not duplicate commits already in history', () => {
    // diamond: A → B, A → C, B+C → D
    const all: DataPluginOwnership[] = [
      makeOwnershipCommit('aaa', [], '2023-01-01'),
      makeOwnershipCommit('bbb', ['aaa'], '2023-02-01'),
      makeOwnershipCommit('ccc', ['aaa'], '2023-03-01'),
      makeOwnershipCommit('ddd', ['bbb', 'ccc'], '2023-06-01'),
    ];
    const head = makeCommit('ddd', ['bbb', 'ccc'], '2023-06-01');
    const result = getHistoryForCommit(head, all);
    const unique = new Set(result);
    expect(unique.size).toBe(result.length);
  });

  it('U3.5 result is sorted with newest sha first', () => {
    const all: DataPluginOwnership[] = [makeOwnershipCommit('aaa', [], '2023-01-01'), makeOwnershipCommit('bbb', ['aaa'], '2023-12-01')];
    const head = makeCommit('bbb', ['aaa'], '2023-12-01');
    const [first, second] = getHistoryForCommit(head, all);
    expect(first).toBe('bbb');
    expect(second).toBe('aaa');
  });
});
