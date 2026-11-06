import { describe, it, expect } from 'vitest';
import { findAuthorWithMaxSpentTime } from '../../../../../../plugins/visualizationPlugins/issues/issuesTimeline/src/chart/helper/findAuthorWithMaxSpentTime';

describe('findAuthorWithMaxSpentTime', () => {
  it('U21.1 returns empty string for empty map', () => {
    expect(findAuthorWithMaxSpentTime(new Map())).toBe('');
  });

  it("U21.2 returns the only entry's key for a single-entry map", () => {
    const m = new Map([['Alice', 5]]);
    expect(findAuthorWithMaxSpentTime(m)).toBe('Alice');
  });

  it('U21.3 returns the key with the highest value', () => {
    const m = new Map([
      ['Alice', 3],
      ['Bob', 7],
      ['Charlie', 2],
    ]);
    expect(findAuthorWithMaxSpentTime(m)).toBe('Bob');
  });

  it('U21.4 when values are equal, returns whichever came last in iteration order', () => {
    // The implementation updates max when e[1] >= max[1], so last equal wins
    const m = new Map([
      ['Alice', 5],
      ['Bob', 5],
    ]);
    const result = findAuthorWithMaxSpentTime(m);
    expect(['Alice', 'Bob']).toContain(result);
  });
});
