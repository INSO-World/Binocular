import { describe, it, expect } from 'vitest';
import {
  isDataPluginCommit,
  isDataPluginBuild,
  isDataPluginIssue,
  isDataPluginMergeRequest,
  isDataPluginNote,
  isDataPluginBranch,
  getActivityType,
  getActivityDate,
  formatActivityCounts,
} from '../../../../../../plugins/visualizationPlugins/authorBehaviour/repositoryActivity/src/utilities/types';
import type { AnyActivityDataPlugin } from '../../../../../../plugins/visualizationPlugins/authorBehaviour/repositoryActivity/src/utilities/types';

const commit = {
  sha: 'abc',
  messageHeader: 'feat',
  stats: { additions: 1, deletions: 0 },
  date: '2023-06-15T00:00:00Z',
  message: 'feat',
} as unknown as AnyActivityDataPlugin;
const build = { status: 'success', webUrl: 'http://x', createdAt: '2023-06-15T00:00:00Z' } as unknown as AnyActivityDataPlugin;
const issue = { iid: 1, title: 'Bug', state: 'open', createdAt: '2023-06-15T00:00:00Z' } as unknown as AnyActivityDataPlugin;
// Note: actual DataPluginMergeRequest has no mergedAt, so this is a plain object with mergedAt to satisfy the guard
const mrWithMergedAt = {
  iid: 1,
  title: 'MR',
  mergedAt: '2023-06-15T00:00:00Z',
  createdAt: '2023-06-15T00:00:00Z',
} as unknown as AnyActivityDataPlugin;
// Actual DataPluginMergeRequest without mergedAt (bug: isDataPluginMergeRequest won't match)
const actualMR = {
  iid: 1,
  title: 'MR',
  state: 'MERGED',
  createdAt: '2023-06-15T00:00:00Z',
  closedAt: null,
} as unknown as AnyActivityDataPlugin;
// Note: actual DataPluginNote has no noteableType, so this is a plain object with noteableType
const noteWithNoteableType = {
  body: 'hello',
  noteableType: 'Issue',
  createdAt: '2023-06-15T00:00:00Z',
} as unknown as AnyActivityDataPlugin;
// Actual DataPluginNote without noteableType
const actualNote = { body: 'hello', createdAt: '2023-06-15T00:00:00Z' } as unknown as AnyActivityDataPlugin;
const branch = {
  branch: 'main',
  active: true,
  tracksFileRenames: false,
  latestCommit: '2023-06-15T00:00:00Z',
} as unknown as AnyActivityDataPlugin;

describe('isDataPluginCommit', () => {
  it('U39.1 returns true for object with sha, messageHeader, stats', () => {
    expect(isDataPluginCommit(commit)).toBe(true);
  });

  it('U39.2 returns false for build object', () => {
    expect(isDataPluginCommit(build)).toBe(false);
  });
});

describe('isDataPluginBuild', () => {
  it('U39.3 returns true for object with status and webUrl', () => {
    expect(isDataPluginBuild(build)).toBe(true);
  });

  it('U39.4 returns false for commit object (no webUrl)', () => {
    const commitNoUrl = { sha: 'abc', messageHeader: 'feat', stats: {} } as unknown as AnyActivityDataPlugin;
    expect(isDataPluginBuild(commitNoUrl)).toBe(false);
  });
});

describe('isDataPluginIssue', () => {
  it('U39.5 returns true for issue object without mergedAt', () => {
    expect(isDataPluginIssue(issue)).toBe(true);
  });

  it('U39.6 returns false for object with mergedAt', () => {
    expect(isDataPluginIssue(mrWithMergedAt)).toBe(false);
  });
});

describe('isDataPluginMergeRequest', () => {
  it('U39.7 returns true for object with iid, title, and mergedAt field', () => {
    expect(isDataPluginMergeRequest(mrWithMergedAt)).toBe(true);
  });

  it('U39.8 BUG — actual DataPluginMergeRequest (no mergedAt field) returns false', () => {
    // DataPluginMergeRequest interface has no mergedAt; isDataPluginMergeRequest checks for it
    // This documents a bug in the type guard
    expect(isDataPluginMergeRequest(actualMR)).toBe(false);
  });
});

describe('isDataPluginNote', () => {
  it('U39.9 BUG — actual DataPluginNote (no noteableType) returns false', () => {
    // DataPluginNote interface has no noteableType; isDataPluginNote checks for it
    // This documents a bug in the type guard
    expect(isDataPluginNote(actualNote)).toBe(false);
  });

  it('U39.10 returns true for object with body and noteableType field', () => {
    expect(isDataPluginNote(noteWithNoteableType)).toBe(true);
  });
});

describe('isDataPluginBranch', () => {
  it('U39.11 returns true for branch object', () => {
    expect(isDataPluginBranch(branch)).toBe(true);
  });

  it('U39.12 returns false for commit object', () => {
    expect(isDataPluginBranch(commit)).toBe(false);
  });
});

describe('getActivityType', () => {
  it('U39.13 commit → "commit"', () => {
    expect(getActivityType(commit)).toBe('commit');
  });

  it('U39.14 build → "build"', () => {
    expect(getActivityType(build)).toBe('build');
  });

  it('U39.15 issue → "issue"', () => {
    expect(getActivityType(issue)).toBe('issue');
  });

  it('U39.16 branch → "branch"', () => {
    expect(getActivityType(branch)).toBe('branch');
  });
});

describe('getActivityDate', () => {
  it('U39.17 commit uses date field', () => {
    const d = getActivityDate(commit);
    expect(d).toBeInstanceOf(Date);
    expect(d!.getFullYear()).toBe(2023);
  });

  it('U39.18 build uses createdAt field', () => {
    const d = getActivityDate(build);
    expect(d).toBeInstanceOf(Date);
  });
});

describe('formatActivityCounts', () => {
  it('U39.19 all-zero counts → "0 activities"', () => {
    const counts = { commit: 0, build: 0, issue: 0, mergeRequest: 0, note: 0, branch: 0, unknown: 0 };
    expect(formatActivityCounts(counts)).toBe('0 activities');
  });

  it('U39.20 count=1 uses singular', () => {
    const counts = { commit: 1, build: 0, issue: 0, mergeRequest: 0, note: 0, branch: 0, unknown: 0 };
    expect(formatActivityCounts(counts)).toContain('1 commit');
  });

  it('U39.21 count=3 uses plural', () => {
    const counts = { commit: 3, build: 0, issue: 0, mergeRequest: 0, note: 0, branch: 0, unknown: 0 };
    expect(formatActivityCounts(counts)).toContain('3 commits');
  });

  it('U39.22 multiple types joined by comma', () => {
    const counts = { commit: 2, build: 1, issue: 0, mergeRequest: 0, note: 0, branch: 0, unknown: 0 };
    const result = formatActivityCounts(counts);
    expect(result).toContain('2 commits');
    expect(result).toContain('1 build');
    expect(result).toContain(',');
  });
});
