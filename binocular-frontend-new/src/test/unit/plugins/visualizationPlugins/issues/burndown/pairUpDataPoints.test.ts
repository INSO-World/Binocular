import { describe, it, expect } from 'vitest';
import { pairUpDataPoints } from '../../../../../../plugins/visualizationPlugins/issues/burndown/src/chart/helper/pairUpDataPoints';
import type { IssuesGroupedByGranularity } from '../../../../../../plugins/visualizationPlugins/issues/burndown/src/chart/types';
import moment from 'moment';

function makeGroup(id: number): IssuesGroupedByGranularity {
  return { id, date: moment('2023-01-01').add(id, 'days'), issues: [] };
}

describe('pairUpDataPoints', () => {
  it('U5.1 yields no pairs for an empty array', () => {
    expect([...pairUpDataPoints([])]).toHaveLength(0);
  });

  it('U5.2 yields no pairs for a single-element array', () => {
    expect([...pairUpDataPoints([makeGroup(0)])]).toHaveLength(0);
  });

  it('U5.3 yields one pair for a two-element array', () => {
    const pairs = [...pairUpDataPoints([makeGroup(0), makeGroup(1)])];
    expect(pairs).toHaveLength(1);
    expect(pairs[0][0].id).toBe(0);
    expect(pairs[0][1].id).toBe(1);
  });

  it('U5.4 yields n-1 pairs for an n-element array', () => {
    const data = [makeGroup(0), makeGroup(1), makeGroup(2), makeGroup(3)];
    expect([...pairUpDataPoints(data)]).toHaveLength(3);
  });

  it('U5.5 each pair consists of consecutive elements', () => {
    const data = [makeGroup(0), makeGroup(1), makeGroup(2)];
    const pairs = [...pairUpDataPoints(data)];
    expect(pairs[0]).toEqual([data[0], data[1]]);
    expect(pairs[1]).toEqual([data[1], data[2]]);
  });
});
