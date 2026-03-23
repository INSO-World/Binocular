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
