import { describe, it, expect } from 'vitest';
import * as d3 from 'd3';
import {
  splitPositiveNegativeData,
  getNonEmptyKeys,
  getClosestIndex,
  computeVisibleYDomain,
  PositiveNegativeSide,
} from '../../../components/stackedAreaChart/utils';

// ChartData: { date: number, [key: string]: number }
type ChartData = { date: number; [key: string]: number };
type Palette = { [key: string]: { main: string; secondary: string } };

const T1 = new Date('2023-01-01').getTime();
const T2 = new Date('2023-06-01').getTime();
const T3 = new Date('2023-12-31').getTime();

// ─── splitPositiveNegativeData ────────────────────────────────────────────────

describe('splitPositiveNegativeData', () => {
  const data: ChartData[] = [{ date: T1, a: 10, b: -5, c: 0 }];

  it('C6.1 POSITIVE side keeps positive values and zeroes out negatives', () => {
    const result = splitPositiveNegativeData(data, PositiveNegativeSide.POSITIVE);
    expect(result[0].a).toBe(10);
    expect(result[0].b).toBe(0);
  });

  it('C6.2 POSITIVE side keeps zero values (0 >= 0)', () => {
    const result = splitPositiveNegativeData(data, PositiveNegativeSide.POSITIVE);
    expect(result[0].c).toBe(0);
  });

  it('C6.3 NEGATIVE side keeps negative values and zeroes out positives', () => {
    const result = splitPositiveNegativeData(data, PositiveNegativeSide.NEGATIVE);
    expect(result[0].b).toBe(-5);
    expect(result[0].a).toBe(0);
  });

  it('C6.4 preserves the date field on every row', () => {
    const result = splitPositiveNegativeData(data, PositiveNegativeSide.POSITIVE);
    expect(result[0].date).toBe(T1);
  });

  it('C6.5 returns empty array for empty input', () => {
    expect(splitPositiveNegativeData([], PositiveNegativeSide.POSITIVE)).toEqual([]);
  });
});

// ─── getNonEmptyKeys ──────────────────────────────────────────────────────────

describe('getNonEmptyKeys', () => {
  const data: ChartData[] = [{ date: T1, zeros: 0, nonzero: 1, tiny: 0.002 }];

  it('C6.6 excludes keys where every value is 0', () => {
    expect(getNonEmptyKeys(['zeros'], data)).toEqual([]);
  });

  it('C6.7 includes keys with at least one value > 0.002', () => {
    expect(getNonEmptyKeys(['nonzero'], data)).toContain('nonzero');
  });

  it('C6.8 excludes key whose max absolute value is exactly 0.002 (threshold is exclusive)', () => {
    expect(getNonEmptyKeys(['tiny'], data)).toEqual([]);
  });

  it('C6.9 includes keys with negative values (uses Math.abs)', () => {
    const negData: ChartData[] = [{ date: T1, neg: -5 }];
    expect(getNonEmptyKeys(['neg'], negData)).toContain('neg');
  });

  it('C6.10 returns empty when no keys provided', () => {
    expect(getNonEmptyKeys([], data)).toEqual([]);
  });
});

// ─── getClosestIndex ──────────────────────────────────────────────────────────

describe('getClosestIndex', () => {
  const data: ChartData[] = [{ date: T1 }, { date: T2 }, { date: T3 }];
  const xScale = d3
    .scaleTime()
    .domain([new Date(T1), new Date(T3)])
    .range([0, 1000]);

  it('C6.11 returns 0 when x maps to a time before or equal to the first data point', () => {
    expect(getClosestIndex(0, data, xScale)).toBe(0);
  });

  it('C6.12 returns last index when x maps to the rightmost data point', () => {
    expect(getClosestIndex(1000, data, xScale)).toBe(2);
  });

  it('C6.13 returns index of nearest data point for a midpoint x', () => {
    // x=500 → roughly mid-year, closest to T2 (June 1)
    const idx = getClosestIndex(500, data, xScale);
    expect(idx).toBeGreaterThanOrEqual(0);
    expect(idx).toBeLessThan(data.length);
  });

  it('C6.14 returns 0 for a single-entry dataset', () => {
    const single: ChartData[] = [{ date: T1 }];
    expect(getClosestIndex(0, single, xScale)).toBe(0);
  });
});

// ─── computeVisibleYDomain ────────────────────────────────────────────────────

describe('computeVisibleYDomain', () => {
  const palette: Palette = {
    a: { main: 'red', secondary: 'pink' },
    b: { main: 'blue', secondary: 'lightblue' },
  };

  it('C6.15 returns [0, 1] when no data falls within the brush domain', () => {
    const data: ChartData[] = [{ date: T1, a: 10 }];
    const brushOutside: [number, number] = [T3 + 1000, T3 + 2000];
    expect(computeVisibleYDomain(data, brushOutside, palette)).toEqual([0, 1]);
  });

  it('C6.16 max of returned domain exceeds the largest stacked positive value', () => {
    const data: ChartData[] = [
      { date: T1, a: 10, b: 5 },
      { date: T2, a: 20, b: 15 },
    ];
    const brush: [number, number] = [T1 - 1, T3 + 1];
    const [, max] = computeVisibleYDomain(data, brush, palette);
    // stacked max ≥ 35 (20 + 15), domain adds 5% padding
    expect(max).toBeGreaterThan(35);
  });

  it('C6.17 min is less than 0 when data has negative values', () => {
    const data: ChartData[] = [{ date: T1, a: -10 }];
    const brush: [number, number] = [T1 - 1, T3 + 1];
    const [min] = computeVisibleYDomain(data, brush, palette);
    expect(min).toBeLessThan(0);
  });

  it('C6.18 uses padding of 1 when all values are below the visibility threshold (range = 0)', () => {
    // Values below 0.002 are filtered out by getNonEmptyKeys, leaving no stackable keys.
    // maxVal and minVal both stay 0 → range = 0 → padding = 0*0.05 || 1 = 1.
    const data: ChartData[] = [
      { date: T1, a: 0.001 },
      { date: T2, a: 0.001 },
    ];
    const singleKeyPalette: Palette = { a: { main: 'red', secondary: 'pink' } };
    const brush: [number, number] = [T1 - 1, T3 + 1];
    const [min, max] = computeVisibleYDomain(data, brush, singleKeyPalette);
    // padding=1 on both sides → domain = [-1, 1], range = 2
    expect(max - min).toBeCloseTo(2, 5);
  });
});
