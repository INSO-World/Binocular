import type { TimeTrackingData } from '../../../../../types/timeTrackingDataType';

export const aggregateTimeTrackingData = (timeTrackingData: TimeTrackingData[]) => {
  const aggregatedTimeTrackingData = new Map<string, number>();
  let totalTime = 0;

  for (const { author, timeSpent } of timeTrackingData) {
    const key = author.user?.gitSignature ?? author.name;
    aggregatedTimeTrackingData.set(key, (aggregatedTimeTrackingData.get(key) ?? 0) + timeSpent);
    totalTime += timeSpent;
  }

  return { aggregatedTimeTrackingData, totalTime };
};
