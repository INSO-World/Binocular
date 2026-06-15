import { describe, it, expect } from 'vitest';
import { aggregateTimeTrackingData } from '../../../../../../plugins/visualizationPlugins/issues/issuesTimeline/src/chart/helper/aggregateTimeTrackingData';
import type { TimeTrackingData } from '../../../../../../plugins/visualizationPlugins/types/timeTrackingDataType';
import type { DataPluginAccount } from '../../../../../../plugins/interfaces/dataPluginInterfaces/dataPluginAccounts';

function makeAccount(name: string): DataPluginAccount {
  return { id: name, name, user: null, platform: 'gitlab' };
}

function makeEntry(name: string, timeSpent: number): TimeTrackingData {
  return { author: makeAccount(name), timeSpent, createdAt: '2023-01-01', issue: null, mergeRequest: null };
}

describe('aggregateTimeTrackingData', () => {
  it('U6.1 returns empty map and totalTime 0 for empty input', () => {
    const { aggregatedTimeTrackingData, totalTime } = aggregateTimeTrackingData([]);
    expect(aggregatedTimeTrackingData.size).toBe(0);
    expect(totalTime).toBe(0);
  });

  it('U6.2 records a single entry correctly', () => {
    const { aggregatedTimeTrackingData, totalTime } = aggregateTimeTrackingData([makeEntry('Alice', 2)]);
    expect(aggregatedTimeTrackingData.get('Alice')).toBe(2);
    expect(totalTime).toBe(2);
  });

  it('U6.3 accumulates time for the same author', () => {
    const data = [makeEntry('Alice', 1), makeEntry('Alice', 3)];
    const { aggregatedTimeTrackingData, totalTime } = aggregateTimeTrackingData(data);
    expect(aggregatedTimeTrackingData.get('Alice')).toBe(4);
    expect(totalTime).toBe(4);
  });

  it('U6.4 keeps different authors separate', () => {
    const data = [makeEntry('Alice', 2), makeEntry('Bob', 5)];
    const { aggregatedTimeTrackingData, totalTime } = aggregateTimeTrackingData(data);
    expect(aggregatedTimeTrackingData.get('Alice')).toBe(2);
    expect(aggregatedTimeTrackingData.get('Bob')).toBe(5);
    expect(totalTime).toBe(7);
  });

  it('U6.5 totalTime equals the sum of all timeSpent values', () => {
    const data = [makeEntry('Alice', 1.5), makeEntry('Bob', 2.5), makeEntry('Alice', 1)];
    const { totalTime } = aggregateTimeTrackingData(data);
    expect(totalTime).toBeCloseTo(5);
  });
});
