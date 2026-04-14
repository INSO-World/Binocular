import { describe, it, expect } from 'vitest';
import { initializeLevenshteinDPTable } from '../../../../../../plugins/visualizationPlugins/issues/issuesTimeline/src/chart/helper/initializeLevenshteinDPTable';

describe('initializeLevenshteinDPTable', () => {
  it('U8.1 returns a table of dimensions (a.length+1) × (b.length+1)', () => {
    const table = initializeLevenshteinDPTable('cat', 'dog');
    expect(table).toHaveLength(4); // 3+1 rows
    expect(table[0]).toHaveLength(4); // 3+1 columns
  });

  it('U8.2 first row is [0, 1, 2, … b.length]', () => {
    const table = initializeLevenshteinDPTable('cat', 'abcd');
    expect(table[0]).toEqual([0, 1, 2, 3, 4]);
  });

  it('U8.3 first column is [0, 1, 2, … a.length]', () => {
    const table = initializeLevenshteinDPTable('abcd', 'cat');
    const firstCol = table.map((row) => row[0]);
    expect(firstCol).toEqual([0, 1, 2, 3, 4]);
  });

  it('U8.4 all interior cells (i>0, j>0) are -1', () => {
    const table = initializeLevenshteinDPTable('ab', 'xy');
    for (let i = 1; i < table.length; i++) {
      for (let j = 1; j < table[i].length; j++) {
        expect(table[i][j]).toBe(-1);
      }
    }
  });

  it('U8.5 handles single-character strings', () => {
    const table = initializeLevenshteinDPTable('a', 'b');
    expect(table).toHaveLength(2);
    expect(table[0]).toHaveLength(2);
    expect(table[0]).toEqual([0, 1]);
    expect(table[1][0]).toBe(1);
    expect(table[1][1]).toBe(-1);
  });
});
