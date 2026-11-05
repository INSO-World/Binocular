import { describe, it, expect } from 'vitest';
import { initializeLevenshteinMatrix } from '../../../../../../plugins/visualizationPlugins/issues/issuesTimeline/src/chart/helper/initializeLevenshteinMatrix';

describe('initializeLevenshteinMatrix', () => {
  it('U9.1 returns an empty map for an empty input array', () => {
    expect(initializeLevenshteinMatrix([])).toEqual(new Map());
  });

  it('U9.2 contains an entry for every input string', () => {
    const matrix = initializeLevenshteinMatrix(['bug', 'fix', 'feat']);
    expect(matrix.has('bug')).toBe(true);
    expect(matrix.has('fix')).toBe(true);
    expect(matrix.has('feat')).toBe(true);
  });

  it('U9.3 self-distance is 0', () => {
    const matrix = initializeLevenshteinMatrix(['hello']);
    expect(matrix.get('hello')!.get('hello')).toBe(0);
  });

  it('U9.4 is symmetric – distance(a, b) equals distance(b, a)', () => {
    const matrix = initializeLevenshteinMatrix(['cat', 'dog']);
    expect(matrix.get('cat')!.get('dog')).toBe(matrix.get('dog')!.get('cat'));
  });

  it('U9.5 computes the correct distance for known pairs', () => {
    const matrix = initializeLevenshteinMatrix(['kitten', 'sitting']);
    expect(matrix.get('kitten')!.get('sitting')).toBe(3);
  });

  it('U9.6 handles duplicate strings without error', () => {
    const matrix = initializeLevenshteinMatrix(['bug', 'bug']);
    expect(matrix.get('bug')!.get('bug')).toBe(0);
  });

  it('U9.7 single-element array has only self-distance', () => {
    const matrix = initializeLevenshteinMatrix(['only']);
    expect(matrix.get('only')!.get('only')).toBe(0);
    expect(matrix.get('only')!.size).toBe(1);
  });
});
