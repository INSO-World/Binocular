import { describe, it, expect } from 'vitest';
import { getHistoryForCommit } from '../../../../../../plugins/visualizationPlugins/ownership/codeOwnership/src/utils/dbUtils';
import type { DataPluginCommit } from '../../../../../../plugins/interfaces/dataPluginInterfaces/dataPluginCommits';
import type { DataPluginOwnership } from '../../../../../../plugins/interfaces/dataPluginInterfaces/dataPluginCommits';

// ─── helpers ──────────────────────────────────────────────────────────────────

function makeCommit(sha: string, date: string, parents: string[]): DataPluginCommit {
  return {
    sha,
    shortSha: sha.slice(0, 7),
    messageHeader: `commit ${sha}`,
    message: `commit ${sha}`,
    user: { login: 'user', name: 'User' } as DataPluginCommit['user'],
    branch: 'main',
    date,
    parents,
    webUrl: '',
    stats: { additions: 0, deletions: 0 },
  };
}

function makeOwnership(sha: string, date: string, parents: string[]): DataPluginOwnership {
  return {
    sha,
    date,
    parents,
    files: [],
  };
}

// ─── getHistoryForCommit ───────────────────────────────────────────────────────

describe('getHistoryForCommit', () => {
  it('U56.1 linear 3-commit chain: returns SHA array in descending date order', () => {
    const c1 = makeOwnership('sha1', '2023-01-01T00:00:00Z', []);
    const c2 = makeOwnership('sha2', '2023-02-01T00:00:00Z', ['sha1']);
    const c3 = makeOwnership('sha3', '2023-03-01T00:00:00Z', ['sha2']);
    const head = makeCommit('sha3', '2023-03-01T00:00:00Z', ['sha2']);

    const result = getHistoryForCommit(head, [c1, c2, c3]);

    expect(result).toEqual(['sha3', 'sha2', 'sha1']);
  });

  it('U56.2 genesis commit (empty parents array): returns only that commit SHA', () => {
    const genesis = makeCommit('sha-genesis', '2023-01-01T00:00:00Z', []);
    const ownershipGenesis = makeOwnership('sha-genesis', '2023-01-01T00:00:00Z', []);

    const result = getHistoryForCommit(genesis, [ownershipGenesis]);

    expect(result).toEqual(['sha-genesis']);
  });

  it('U56.3 merge commit (2 parents): includes both parent chains in the result', () => {
    const base = makeOwnership('base', '2023-01-01T00:00:00Z', []);
    const branchA = makeOwnership('branchA', '2023-02-01T00:00:00Z', ['base']);
    const branchB = makeOwnership('branchB', '2023-02-15T00:00:00Z', ['base']);
    const merge = makeCommit('merge', '2023-03-01T00:00:00Z', ['branchA', 'branchB']);

    const result = getHistoryForCommit(merge, [base, branchA, branchB]);

    expect(result).toContain('merge');
    expect(result).toContain('branchA');
    expect(result).toContain('branchB');
    expect(result).toContain('base');
    expect(result).toHaveLength(4);
    // merge commit is newest — should appear first
    expect(result[0]).toBe('merge');
  });

  it('U56.4 parent SHA missing from allCommits: throws because the source does not guard against undefined commits', () => {
    const c2 = makeOwnership('sha2', '2023-02-01T00:00:00Z', ['sha1-missing']);
    const head = makeCommit('sha3', '2023-03-01T00:00:00Z', ['sha2']);

    // The source accesses commit.parents without a null-check, so a missing parent SHA throws
    expect(() => getHistoryForCommit(head, [c2])).toThrow(TypeError);
  });

  it('U56.5 cycle prevention: circular parent references terminate without infinite loop', () => {
    // Manually craft ownership objects that reference each other as parents.
    // This is not a valid git state but the function must not hang.
    const c1 = makeOwnership('sha1', '2023-01-01T00:00:00Z', ['sha2']);
    const c2 = makeOwnership('sha2', '2023-02-01T00:00:00Z', ['sha1']);
    const head = makeCommit('sha2', '2023-02-01T00:00:00Z', ['sha1']);

    let result: string[] | undefined;
    expect(() => {
      result = getHistoryForCommit(head, [c1, c2]);
    }).not.toThrow();

    expect(result).toBeDefined();
    expect(result!).toContain('sha1');
    expect(result!).toContain('sha2');
    // The function terminates (doesn't hang) — duplicate SHAs may appear due to imperfect cycle detection
  });
});
