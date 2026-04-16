import { describe, it, expect } from 'vitest';
import {
  extractTouchedFiles,
  calculateExpertiseBrowserScores,
  buildPackageHierarchy,
} from '../../../../../../plugins/visualizationPlugins/expertise/knowledgeRadar/src/utilities/dataConverter';
import type { DataPluginCommit } from '../../../../../../plugins/interfaces/dataPluginInterfaces/dataPluginCommits';

function makeCommit(gitSignature: string, message: string, files: string[] = []): DataPluginCommit {
  return {
    sha: Math.random().toString(36).slice(2),
    shortSha: 'abc',
    messageHeader: message,
    message,
    user: { id: gitSignature, gitSignature, account: null },
    branch: 'main',
    date: '2023-06-15T00:00:00Z',
    parents: [],
    webUrl: '',
    stats: { additions: 1, deletions: 0 },
    files: {
      data: files.map((path) => ({ file: { path, webUrl: '', maxLength: 0 }, hunks: [], stats: { additions: 1, deletions: 0 } })),
    },
  };
}

describe('extractTouchedFiles', () => {
  it('U38.1 returns empty set for empty commits', () => {
    expect(extractTouchedFiles([], ['Alice'])).toEqual(new Set());
  });

  it('U38.2 returns files from matching developer', () => {
    const commit = makeCommit('Alice', 'feat: x', ['src/foo.ts']);
    const result = extractTouchedFiles([commit], ['Alice']);
    expect(result.has('src/foo.ts')).toBe(true);
  });

  it('U38.3 ignores commits from other developers', () => {
    const commit = makeCommit('Bob', 'feat: x', ['src/foo.ts']);
    const result = extractTouchedFiles([commit], ['Alice']);
    expect(result.size).toBe(0);
  });
});

describe('calculateExpertiseBrowserScores', () => {
  it('U38.4 returns empty array for empty commits', () => {
    expect(calculateExpertiseBrowserScores([], ['Alice'])).toEqual([]);
  });

  it('U38.5 filters out merge commits', () => {
    const merge = makeCommit('Alice', 'Merge branch x into main', ['src/foo.ts']);
    const result = calculateExpertiseBrowserScores([merge], ['Alice']);
    // All commits are merge commits, so nothing remains
    expect(result).toEqual([]);
  });

  it('U38.6 ownership score = dev commits / total commits for a file', () => {
    const aliceCommit = makeCommit('Alice', 'feat: feature', ['src/foo.ts']);
    const bobCommit = makeCommit('Bob', 'fix: bug', ['src/foo.ts']);
    // Alice has 1 of 2 commits touching src/foo.ts → ownership 0.5
    const result = calculateExpertiseBrowserScores([aliceCommit, bobCommit], ['Alice']);
    // Find src or src/foo.ts package
    const flat = JSON.stringify(result);
    expect(flat).toBeTruthy(); // result exists
    // Alice's share on src path should be 0.5
    function findScore(pkgs: { name: string; score: number; subpackages?: typeof pkgs }[], name: string): number | undefined {
      for (const p of pkgs) {
        if (p.name === name) return p.score;
        if (p.subpackages) {
          const found = findScore(p.subpackages, name);
          if (found !== undefined) return found;
        }
      }
      return undefined;
    }
    const fooScore = findScore(result, 'foo.ts');
    expect(fooScore).toBeCloseTo(0.5);
  });
});

describe('buildPackageHierarchy', () => {
  it('U38.7 empty map → empty array', () => {
    expect(buildPackageHierarchy(new Map())).toEqual([]);
  });

  it('U38.8 flat path produces single root package', () => {
    const scores = new Map([['src', 0.8]]);
    const result = buildPackageHierarchy(scores);
    expect(result).toHaveLength(1);
    expect(result[0].name).toBe('src');
    expect(result[0].score).toBeCloseTo(0.8);
  });

  it('U38.9 nested path a/b/c produces 3-level hierarchy', () => {
    const scores = new Map([
      ['a', 0],
      ['a/b', 0],
      ['a/b/c', 0.9],
    ]);
    const result = buildPackageHierarchy(scores);
    expect(result[0].name).toBe('a');
    expect(result[0].subpackages?.[0].name).toBe('b');
    expect(result[0].subpackages?.[0].subpackages?.[0].name).toBe('c');
  });

  it('U38.10 parent score is aggregated from children when parent score is 0', () => {
    const scores = new Map([
      ['a', 0],
      ['a/b', 1.0],
    ]);
    const result = buildPackageHierarchy(scores);
    // Parent 'a' should have aggregated score from child 'b'
    expect(result[0].score).toBeGreaterThan(0);
  });
});
