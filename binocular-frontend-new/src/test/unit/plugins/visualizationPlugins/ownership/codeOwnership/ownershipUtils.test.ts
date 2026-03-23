import { describe, it, expect } from 'vitest';
import {
  extractOwnershipFromFileExcludingCommits,
  extractFileOwnership,
} from '../../../../../../plugins/visualizationPlugins/ownership/codeOwnership/src/utils/ownershipUtils';
import type { DataPluginFileOwnership } from '../../../../../../plugins/interfaces/dataPluginInterfaces/dataPluginCommits';
import type { OwnershipData } from '../../../../../../types/data/ownershipType';

// ─── helpers ──────────────────────────────────────────────────────────────────

function makeOwnership(user: string, hunks: { commit: string; from: number; to: number }[]): DataPluginFileOwnership {
  return {
    user,
    hunks: hunks.map((h) => ({
      originalCommit: h.commit,
      lines: [{ from: h.from, to: h.to }],
    })),
  };
}

function makeCommit(sha: string, files: { path: string; ownership: DataPluginFileOwnership[] }[]): OwnershipData {
  return {
    sha,
    date: new Date(),
    ownership: {},
    files: files.map((f) => ({ path: f.path, action: 'modify', filename: f.path.split('/').pop()!, ownership: f.ownership })),
  };
}

// ─── extractOwnershipFromFileExcludingCommits ─────────────────────────────────

describe('extractOwnershipFromFileExcludingCommits', () => {
  it('U13.1 counts all lines when no commits are excluded', () => {
    const data = [makeOwnership('alice', [{ commit: 'abc', from: 1, to: 5 }])];
    const result = extractOwnershipFromFileExcludingCommits(data);
    expect(result[0].ownedLines).toBe(5);
  });

  it('U13.2 counts lines across multiple hunks for the same user', () => {
    const data = [
      makeOwnership('alice', [
        { commit: 'abc', from: 1, to: 3 },
        { commit: 'def', from: 10, to: 12 },
      ]),
    ];
    const result = extractOwnershipFromFileExcludingCommits(data);
    expect(result[0].ownedLines).toBe(6); // (3-1+1) + (12-10+1) = 3+3
  });

  it('U13.3 skips hunks whose originalCommit is in the exclude list', () => {
    const data = [
      makeOwnership('alice', [
        { commit: 'abc', from: 1, to: 5 },
        { commit: 'def', from: 10, to: 14 },
      ]),
    ];
    const result = extractOwnershipFromFileExcludingCommits(data, ['abc']);
    expect(result[0].ownedLines).toBe(5); // only 'def' hunk counted
  });

  it('U13.4 returns ownedLines 0 when all commits are excluded', () => {
    const data = [makeOwnership('alice', [{ commit: 'abc', from: 1, to: 10 }])];
    const result = extractOwnershipFromFileExcludingCommits(data, ['abc']);
    expect(result[0].ownedLines).toBe(0);
  });

  it('U13.5 handles multiple users independently', () => {
    const data = [makeOwnership('alice', [{ commit: 'abc', from: 1, to: 4 }]), makeOwnership('bob', [{ commit: 'def', from: 5, to: 7 }])];
    const result = extractOwnershipFromFileExcludingCommits(data);
    expect(result.find((r) => r.user === 'alice')!.ownedLines).toBe(4);
    expect(result.find((r) => r.user === 'bob')!.ownedLines).toBe(3);
  });

  it('U13.6 returns empty array for empty input', () => {
    expect(extractOwnershipFromFileExcludingCommits([])).toEqual([]);
  });

  it('U13.7 counts everything when commitsToExclude is omitted', () => {
    const data = [makeOwnership('alice', [{ commit: 'sha1', from: 1, to: 10 }])];
    const result = extractOwnershipFromFileExcludingCommits(data);
    expect(result[0].ownedLines).toBe(10);
  });
});

// ─── extractFileOwnership ─────────────────────────────────────────────────────

describe('extractFileOwnership', () => {
  it('U13.8 uses the most-recent commit ownership for each file', () => {
    const oldOwnership = [makeOwnership('alice', [{ commit: 'old', from: 1, to: 5 }])];
    const newOwnership = [makeOwnership('bob', [{ commit: 'new', from: 1, to: 20 }])];
    const commits = [
      makeCommit('sha-old', [{ path: 'src/file.ts', ownership: oldOwnership }]),
      makeCommit('sha-new', [{ path: 'src/file.ts', ownership: newOwnership }]),
    ];
    // sha-new is the last in array → most recent → wins
    const result = extractFileOwnership(commits);
    expect(result['src/file.ts'][0].user).toBe('bob');
    expect(result['src/file.ts'][0].ownedLines).toBe(20);
  });

  it('U13.9 includes files from all commits when files are disjoint', () => {
    const commits = [
      makeCommit('sha1', [{ path: 'src/a.ts', ownership: [makeOwnership('alice', [{ commit: 'c1', from: 1, to: 3 }])] }]),
      makeCommit('sha2', [{ path: 'src/b.ts', ownership: [makeOwnership('bob', [{ commit: 'c2', from: 1, to: 5 }])] }]),
    ];
    const result = extractFileOwnership(commits);
    expect('src/a.ts' in result).toBe(true);
    expect('src/b.ts' in result).toBe(true);
  });

  it('U13.10 keys result by file path', () => {
    const commits = [makeCommit('sha1', [{ path: 'foo/bar.ts', ownership: [makeOwnership('alice', [{ commit: 'c1', from: 1, to: 1 }])] }])];
    const result = extractFileOwnership(commits);
    expect(Object.keys(result)).toContain('foo/bar.ts');
  });

  it('U13.11 passes commitsToExclude through to line counting', () => {
    const ownership = [makeOwnership('alice', [{ commit: 'excluded', from: 1, to: 10 }])];
    const commits = [makeCommit('sha1', [{ path: 'file.ts', ownership }])];
    const result = extractFileOwnership(commits, ['excluded']);
    expect(result['file.ts'][0].ownedLines).toBe(0);
  });

  it('U13.12 returns empty object for empty ownershipData', () => {
    expect(extractFileOwnership([])).toEqual({});
  });
});
