import { describe, it, expect } from 'vitest';
import {
  formatDate,
  getGranularityDuration,
} from '../../../../../../plugins/visualizationPlugins/ownership/codeOwnership/src/utils/dateUtils';
import moment from 'moment';

// Use a fixed locale-independent date so tests aren't locale-sensitive where possible.
// 2023-06-12 is a Monday. 2023-01-01 is a Sunday.

describe('formatDate', () => {
  it('U12.1 years resolution returns just the year string', () => {
    expect(formatDate(new Date('2023-06-12'), 'years')).toBe('2023');
  });

  it('U12.2 months resolution returns month name + year', () => {
    expect(formatDate(new Date('2023-06-12'), 'months')).toBe('June 2023');
  });

  it('U12.3 months resolution – January (boundary: first month)', () => {
    expect(formatDate(new Date('2023-01-15'), 'months')).toContain('January');
  });

  it('U12.4 months resolution – December (boundary: last month)', () => {
    expect(formatDate(new Date('2023-12-15'), 'months')).toContain('December');
  });

  it('U12.5 weeks resolution starts with "Week starting at"', () => {
    const result = formatDate(new Date('2023-06-12'), 'weeks'); // Monday
    expect(result).toMatch(/^Week starting at/);
  });

  it('U12.6 days resolution starts with the day name', () => {
    const result = formatDate(new Date('2023-06-12'), 'days'); // Monday
    expect(result).toMatch(/^Monday/);
  });

  it('U12.7 unknown resolution falls back to toLocaleDateString', () => {
    const date = new Date('2023-06-12');
    expect(formatDate(date, 'hours')).toBe(date.toLocaleDateString());
  });
});

describe('getGranularityDuration', () => {
  it('U12.8 years returns unit "year" and a 1-year duration', () => {
    const { unit, interval } = getGranularityDuration('years');
    expect(unit).toBe('year');
    expect(moment.isDuration(interval)).toBe(true);
    expect((interval as moment.Duration).as('years')).toBe(1);
  });

  it('U12.9 months returns unit "month" and a 1-month duration', () => {
    const { unit, interval } = getGranularityDuration('months');
    expect(unit).toBe('month');
    expect((interval as moment.Duration).as('months')).toBe(1);
  });

  it('U12.10 weeks returns unit "week" and a 1-week duration', () => {
    const { unit, interval } = getGranularityDuration('weeks');
    expect(unit).toBe('week');
    expect((interval as moment.Duration).as('weeks')).toBe(1);
  });

  it('U12.11 days returns unit "day" and a 1-day duration', () => {
    const { unit, interval } = getGranularityDuration('days');
    expect(unit).toBe('day');
    expect((interval as moment.Duration).as('days')).toBe(1);
  });

  it('U12.12 unknown resolution returns interval 0 and empty unit string', () => {
    const { unit, interval } = getGranularityDuration('hours');
    expect(unit).toBe('');
    expect(interval).toBe(0);
  });
});

describe('formatDate (U12)', () => {
  it('U12.13 years resolution returns just the 4-digit year', () => {
    expect(formatDate(new Date('2021-08-25'), 'years')).toBe('2021');
  });

  it('U12.14 months resolution returns month+year string', () => {
    expect(formatDate(new Date('2021-08-25'), 'months')).toBe('August 2021');
  });

  it('U12.15 weeks resolution returns a non-empty string', () => {
    const result = formatDate(new Date('2021-08-23'), 'weeks'); // Monday
    expect(typeof result).toBe('string');
    expect(result.length).toBeGreaterThan(0);
  });

  it('U12.16 days resolution returns a day-level formatted string', () => {
    const result = formatDate(new Date('2021-08-23'), 'days'); // Monday
    expect(result).toMatch(/^Monday/);
    expect(result).toContain(',');
  });

  it('U12.17 unknown resolution "hours" falls through to default without throwing', () => {
    const date = new Date('2021-08-25');
    expect(() => formatDate(date, 'hours')).not.toThrow();
    expect(formatDate(date, 'hours')).toBe(date.toLocaleDateString());
  });
});

describe('getGranularityDuration (U12)', () => {
  it('U12.18 years returns a duration equivalent to 1 year', () => {
    const { interval } = getGranularityDuration('years');
    expect(moment.isDuration(interval)).toBe(true);
    expect((interval as moment.Duration).as('years')).toBe(1);
  });

  it('U12.19 months returns a duration equivalent to 1 month', () => {
    const { interval } = getGranularityDuration('months');
    expect(moment.isDuration(interval)).toBe(true);
    expect((interval as moment.Duration).as('months')).toBe(1);
  });

  it('U12.20 weeks returns a duration equivalent to 1 week', () => {
    const { interval } = getGranularityDuration('weeks');
    expect(moment.isDuration(interval)).toBe(true);
    expect((interval as moment.Duration).as('weeks')).toBe(1);
  });

  it('U12.21 days returns a duration equivalent to 1 day', () => {
    const { interval } = getGranularityDuration('days');
    expect(moment.isDuration(interval)).toBe(true);
    expect((interval as moment.Duration).as('days')).toBe(1);
  });

  it('U12.22 unknown resolution "hours" falls through to default — result is defined', () => {
    const result = getGranularityDuration('hours');
    expect(result).toBeDefined();
    expect(result.interval).toBe(0);
    expect(result.unit).toBe('');
  });
});
