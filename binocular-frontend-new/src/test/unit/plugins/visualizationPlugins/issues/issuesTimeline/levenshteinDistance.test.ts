import { describe, it, expect } from 'vitest';
import { levenshteinDistance } from '../../../../../../plugins/visualizationPlugins/issues/issuesTimeline/src/chart/helper/levenshteinDistance';

describe('levenshteinDistance', () => {
  it('U10.1 returns 0 for identical strings', () => {
    expect(levenshteinDistance('cat', 'cat')).toBe(0);
  });

  it('U10.2 returns the length of b when a is empty', () => {
    expect(levenshteinDistance('', 'abc')).toBe(3);
  });

  it('U10.3 returns the length of a when b is empty', () => {
    expect(levenshteinDistance('abc', '')).toBe(3);
  });

  it('U10.4 returns 0 for two empty strings', () => {
    expect(levenshteinDistance('', '')).toBe(0);
  });

  it('U10.5 returns 1 for a single substitution', () => {
    expect(levenshteinDistance('cat', 'bat')).toBe(1);
  });

  it('U10.6 returns 1 for a single insertion', () => {
    expect(levenshteinDistance('cat', 'cats')).toBe(1);
  });

  it('U10.7 returns 1 for a single deletion', () => {
    expect(levenshteinDistance('cats', 'cat')).toBe(1);
  });

  it('U10.8 calculates the classic kitten→sitting distance (3)', () => {
    expect(levenshteinDistance('kitten', 'sitting')).toBe(3);
  });

  it('U10.9 is commutative – distance(a, b) equals distance(b, a)', () => {
    expect(levenshteinDistance('sunday', 'saturday')).toBe(levenshteinDistance('saturday', 'sunday'));
  });

  it('U10.10 handles strings differing only in case', () => {
    expect(levenshteinDistance('Bug', 'bug')).toBe(1);
  });
});
