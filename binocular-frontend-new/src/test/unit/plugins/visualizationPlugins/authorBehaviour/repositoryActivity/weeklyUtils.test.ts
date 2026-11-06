import { describe, it, expect } from 'vitest';
import { convertToWeeklyFormat } from '../../../../../../plugins/visualizationPlugins/authorBehaviour/repositoryActivity/src/utilities/weeklyUtils';
import type { AnyActivityDataPlugin } from '../../../../../../plugins/visualizationPlugins/authorBehaviour/repositoryActivity/src/utilities/types';

const WEEK_START = new Date(2024, 0, 1, 0, 0, 0, 0); // Jan 1 2024 00:00 local

function makeCommit(date: Date): AnyActivityDataPlugin {
  return {
    sha: 'abc123',
    shortSha: 'abc',
    messageHeader: 'test commit',
    message: 'test commit',
    date: date.toISOString(),
    stats: { additions: 1, deletions: 0 },
    parents: [],
    branch: 'main',
    webUrl: '',
    user: { login: 'user', name: 'User' },
  } as unknown as AnyActivityDataPlugin;
}

describe('convertToWeeklyFormat – empty data', () => {
  it('U41.1 returns 168 cells (24×7) for empty data', () => {
    const { chartData } = convertToWeeklyFormat([], WEEK_START);
    expect(chartData).toHaveLength(168);
  });

  it('U41.2 all cells have value 0 for empty data', () => {
    const { chartData } = convertToWeeklyFormat([], WEEK_START);
    expect(chartData.every((cell) => cell.value === 0)).toBe(true);
  });
});

describe('convertToWeeklyFormat – labels', () => {
  it('U41.3 rowLabels is always 7 entries', () => {
    const { rowLabels } = convertToWeeklyFormat([], WEEK_START);
    expect(rowLabels).toHaveLength(7);
  });

  it('U41.4 colLabels is always 24 entries', () => {
    const { colLabels } = convertToWeeklyFormat([], WEEK_START);
    expect(colLabels).toHaveLength(24);
  });
});

describe('convertToWeeklyFormat – activity placement', () => {
  it('U41.5 commit within week is counted in correct cell (day 0, hour 9)', () => {
    const commitDate = new Date(2024, 0, 1, 9, 0, 0, 0);
    const { chartData } = convertToWeeklyFormat([makeCommit(commitDate)], WEEK_START);
    const cell = chartData.find((c) => c.row === 0 && c.col === 9);
    expect(cell?.value).toBe(1);
  });

  it('U41.6 activity outside the week is excluded (1 day before weekStart)', () => {
    const beforeWeek = new Date(2023, 11, 31, 12, 0, 0, 0);
    const { chartData } = convertToWeeklyFormat([makeCommit(beforeWeek)], WEEK_START);
    expect(chartData.every((c) => c.value === 0)).toBe(true);
  });

  it('U41.7 multiple activities in same hour/day sum correctly (3 commits → value 3)', () => {
    const commitDate = new Date(2024, 0, 1, 9, 0, 0, 0);
    const data = [makeCommit(commitDate), makeCommit(commitDate), makeCommit(commitDate)];
    const { chartData } = convertToWeeklyFormat(data, WEEK_START);
    const cell = chartData.find((c) => c.row === 0 && c.col === 9);
    expect(cell?.value).toBe(3);
  });

  it('U41.8 cell row equals days-from-weekStart, col equals hour (day 2, hour 14)', () => {
    const commitDate = new Date(2024, 0, 3, 14, 0, 0, 0);
    const { chartData } = convertToWeeklyFormat([makeCommit(commitDate)], WEEK_START);
    const cell = chartData.find((c) => c.row === 2 && c.col === 14);
    expect(cell?.value).toBeGreaterThanOrEqual(1);
  });
});
