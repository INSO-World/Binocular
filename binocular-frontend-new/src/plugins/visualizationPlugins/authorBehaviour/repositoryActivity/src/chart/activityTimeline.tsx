import { useMemo } from 'react';
import * as d3 from 'd3';
import type { ActivityTimelineProps, HeatmapCell } from '../utilities/types';
import Heatmap from './heatmap';

function ActivityTimeline({
  data,
  startDate,
  endDate,
  minCellSize = 15,
  color = '#3182bd',
  cellPadding = 2,
  onCellClick = null,
  showLegend = true,
  legendTitle = 'Activities',
  scaleHorizontal = false,
  scaleVertical = false,
  containerWidth = 0,
  containerHeight = 0,
}: ActivityTimelineProps) {
  const { gridData, rowLabels, colLabels } = useMemo(() => {
    const dayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    const monthLabels: string[] = [];
    const gridData: HeatmapCell[] = [];
    const dataMap = new Map<string, { value: number; tooltip?: string }>();
    data.forEach((item) => {
      if (!item.date) return; // Skip items without a date (e.g., during view transition)
      const dateStr = `${item.date.getFullYear()}-${String(item.date.getMonth() + 1).padStart(2, '0')}-${String(item.date.getDate()).padStart(2, '0')}`;
      dataMap.set(dateStr, { value: item.value, tooltip: item.tooltip });
    });

    const currentDate = new Date(startDate);
    // Align to Monday: (day + 6) % 7 gives days since Monday (Mon=0, Tue=1, ..., Sun=6)
    currentDate.setDate(currentDate.getDate() - ((currentDate.getDay() + 6) % 7));

    // Calculate weeks based on aligned start date to end date
    const totalDays = Math.ceil((endDate.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) + 1;
    const weeks = Math.ceil(totalDays / 7);

    let currentMonth = -1;
    let colIndex = 0;

    for (let week = 0; week < weeks; week++) {
      const weekStartDate = new Date(currentDate);
      const month = weekStartDate.getMonth();

      if (month !== currentMonth) {
        monthLabels.push(d3.timeFormat('%b')(weekStartDate));
        currentMonth = month;
      } else {
        monthLabels.push('');
      }

      for (let day = 0; day < 7; day++) {
        const cellDate = new Date(currentDate);
        const dateStr = `${cellDate.getFullYear()}-${String(cellDate.getMonth() + 1).padStart(2, '0')}-${String(cellDate.getDate()).padStart(2, '0')}`;
        const cellData = dataMap.get(dateStr);

        const isInRange = cellDate >= startDate && cellDate <= endDate;

        // Only add cells that are within the date range
        if (isInRange) {
          const value = cellData?.value || 0;
          gridData.push({
            row: day,
            col: colIndex,
            value: value,
            tooltip:
              cellData?.tooltip ||
              `<strong>${d3.timeFormat('%b %d, %Y')(cellDate)}</strong><br/>${value} ${value !== 1 ? 'activities' : 'activity'}`,
            metadata: { date: new Date(cellDate), isInRange },
          });
        }

        currentDate.setDate(currentDate.getDate() + 1);
      }

      colIndex++;
    }

    return { gridData, rowLabels: dayLabels, colLabels: monthLabels };
  }, [data, startDate, endDate]);

  return (
    <Heatmap
      data={gridData}
      rowLabels={rowLabels}
      colLabels={colLabels}
      minCellSize={minCellSize}
      color={color}
      cellPadding={cellPadding}
      showLegend={showLegend}
      legendTitle={legendTitle}
      onCellClick={onCellClick}
      scaleHorizontal={scaleHorizontal}
      scaleVertical={scaleVertical}
      containerWidth={containerWidth}
      containerHeight={containerHeight}
    />
  );
}

export default ActivityTimeline;
