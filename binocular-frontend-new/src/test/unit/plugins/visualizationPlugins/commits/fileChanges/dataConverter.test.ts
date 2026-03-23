import { describe, it, expect } from 'vitest';
import { convertCommitDataToMetrics } from '../../../../../../plugins/visualizationPlugins/commits/fileChanges/src/utilities/dataConverter';
import type { DataPluginCommit } from '../../../../../../plugins/interfaces/dataPluginInterfaces/dataPluginCommits';

const FIRST = '2023-01-01T00:00:00Z';
const LAST = '2023-12-31T23:59:59Z';

function makeCommit(date: string, filePaths: string[] = []): DataPluginCommit {
  return {
    sha: date,
    shortSha: date.slice(0, 7),
    messageHeader: 'commit',
    message: 'commit',
    user: { id: 'u1', gitSignature: 'User One', account: null },
    branch: 'main',
    date,
    parents: [],
    webUrl: '',
    stats: { additions: 0, deletions: 0 },
    files: {
      data: filePaths.map((p) => ({
        file: { path: p, webUrl: '', maxLength: 0 },
        hunks: [],
      })),
    },
  };
}

describe('convertCommitDataToMetrics', () => {
  it('U2.1 returns all zeros for an empty commits array', () => {
    const result = convertCommitDataToMetrics([], FIRST, LAST);
    expect(result).toEqual({ mpc: 0, entropy: 0, maxBurst: 0, maxChangeset: 0, avgChangeset: 0 });
  });

  it('U2.2 returns all zeros when passed null-ish input', () => {
    // @ts-expect-error testing JS runtime behaviour
    const result = convertCommitDataToMetrics(null, FIRST, LAST);
    expect(result).toEqual({ mpc: 0, entropy: 0, maxBurst: 0, maxChangeset: 0, avgChangeset: 0 });
  });

  it('U2.3 returns non-zero entropy for commits spread across the timeline', () => {
    const commits = [makeCommit('2023-01-15T00:00:00Z'), makeCommit('2023-06-15T00:00:00Z'), makeCommit('2023-11-15T00:00:00Z')];
    const { entropy } = convertCommitDataToMetrics(commits, FIRST, LAST);
    expect(entropy).toBeGreaterThan(0);
  });

  it('U2.4 returns zero entropy for a single commit (distribution is deterministic)', () => {
    const commits = [makeCommit('2023-06-15T00:00:00Z')];
    const { entropy } = convertCommitDataToMetrics(commits, FIRST, LAST);
    // single-bucket distribution → p=1 → -1*log2(1) = 0
    expect(entropy).toBeCloseTo(0, 5);
  });

  it('U2.5 mpc is bounded between 0 and 100', () => {
    const commits = [makeCommit('2023-01-02T00:00:00Z'), makeCommit('2023-12-30T00:00:00Z')];
    const { mpc } = convertCommitDataToMetrics(commits, FIRST, LAST);
    expect(mpc).toBeGreaterThanOrEqual(0);
    expect(mpc).toBeLessThanOrEqual(100);
  });

  it('U2.6 detects a burst of rapid commits', () => {
    // Three commits within the same hour — well under the default gapSize (1 day)
    const commits = [makeCommit('2023-06-01T10:00:00Z'), makeCommit('2023-06-01T10:30:00Z'), makeCommit('2023-06-01T11:00:00Z')];
    const { maxBurst } = convertCommitDataToMetrics(commits, FIRST, LAST);
    expect(maxBurst).toBe(3);
  });

  it('U2.7 returns maxBurst = 0 when commits are always separated by more than gapSize', () => {
    const commits = [makeCommit('2023-01-01T00:00:00Z'), makeCommit('2023-06-01T00:00:00Z')];
    // gapSize = 1 ms — every pair is a "gap"
    const { maxBurst } = convertCommitDataToMetrics(commits, FIRST, LAST, 1);
    expect(maxBurst).toBe(0);
  });

  it('U2.8 computes maxChangeset from the number of files per commit (>1 file)', () => {
    // commit with 3 files → changesetSize = 3-1 = 2
    const commits = [makeCommit('2023-06-01T00:00:00Z', ['a.ts', 'b.ts', 'c.ts'])];
    const { maxChangeset } = convertCommitDataToMetrics(commits, FIRST, LAST);
    expect(maxChangeset).toBe(2);
  });

  it('U2.9 returns maxChangeset = 0 when all commits touch 0 or 1 file', () => {
    const commits = [makeCommit('2023-01-01T00:00:00Z', []), makeCommit('2023-06-01T00:00:00Z', ['single.ts'])];
    const { maxChangeset } = convertCommitDataToMetrics(commits, FIRST, LAST);
    expect(maxChangeset).toBe(0);
  });

  it('U2.10 computes avgChangeset as mean changeset size across qualifying commits', () => {
    // commit1: 3 files → changesetSize 2; commit2: 5 files → changesetSize 4; avg = 3
    const commits = [
      makeCommit('2023-03-01T00:00:00Z', ['a.ts', 'b.ts', 'c.ts']),
      makeCommit('2023-09-01T00:00:00Z', ['a.ts', 'b.ts', 'c.ts', 'd.ts', 'e.ts']),
    ];
    const { avgChangeset } = convertCommitDataToMetrics(commits, FIRST, LAST);
    expect(avgChangeset).toBeCloseTo(3, 5);
  });

  it('U2.11 handles custom burstSize threshold', () => {
    // Two commits in a row — default burstSize=2 would count them; with burstSize=3 they should not
    const commits = [makeCommit('2023-06-01T10:00:00Z'), makeCommit('2023-06-01T10:30:00Z')];
    const { maxBurst } = convertCommitDataToMetrics(commits, FIRST, LAST, 1000 * 60 * 60 * 24, 3);
    expect(maxBurst).toBe(0);
  });
});
