import * as d3 from 'd3';
import { type ScaleLinear, type ScaleTime } from 'd3';
import type { ChartData, Palette } from './StackedAreaChart.tsx';

export type PathDatum = { key: string; side: 'positive' | 'negative'; index: number };
export type TooltipSelection = d3.Selection<HTMLDivElement | null, unknown, null, undefined>;

export enum PositiveNegativeSide {
  POSITIVE,
  NEGATIVE,
}

export function splitPositiveNegativeData(data: ChartData[], side: PositiveNegativeSide): ChartData[] {
  return data.map((d) => {
    const newD: ChartData = { date: d.date };
    Object.keys(d).forEach((k) => {
      if (k !== 'date') {
        if (d[k] >= 0 && side === PositiveNegativeSide.POSITIVE) {
          newD[k] = d[k];
        } else if (d[k] < 0 && side === PositiveNegativeSide.NEGATIVE) {
          newD[k] = d[k];
        } else {
          newD[k] = 0;
        }
      }
    });
    return newD;
  });
}

export function getNonEmptyKeys(keys: string[], data: ChartData[]): string[] {
  return keys.filter((key) => data.some((d) => key in d && Math.abs(d[key]) > 0.002));
}

export function computeVisibleYDomain(
  data: ChartData[],
  brushDomain: [number, number],
  palette: Palette,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  d3offset?: (series: any, order: number[]) => void,
): [number, number] {
  const visibleData = data.filter((d) => {
    const t = new Date(d.date).getTime();
    return t >= brushDomain[0] && t <= brushDomain[1];
  });
  if (visibleData.length === 0) return [0, 1];

  const allKeys = Object.keys(palette);
  const positiveData = splitPositiveNegativeData(visibleData, PositiveNegativeSide.POSITIVE);
  const negativeData = splitPositiveNegativeData(visibleData, PositiveNegativeSide.NEGATIVE);
  const positiveKeys = getNonEmptyKeys(allKeys, positiveData);
  const negativeKeys = getNonEmptyKeys(allKeys, negativeData);

  let maxVal = 0;
  let minVal = 0;

  if (positiveKeys.length > 0) {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const positiveStack = d3.stack().keys(positiveKeys) as any;
    if (d3offset) positiveStack.offset(d3offset);
    const stackedPositive = positiveStack(positiveData);
    stackedPositive.forEach((layer: [number, number][]) => {
      layer.forEach((point) => {
        if (point[1] > maxVal) maxVal = point[1];
      });
    });
  }

  if (negativeKeys.length > 0) {
    const stackedNegative = d3.stack().keys(negativeKeys)(negativeData);
    stackedNegative.forEach((layer) => {
      layer.forEach((point) => {
        if (point[0] < minVal) minVal = point[0];
      });
    });
  }

  const range = maxVal - minVal;
  const padding = range * 0.05 || 1;
  return [minVal - padding, maxVal + padding];
}

export function getClosestIndex(x: number, data: ChartData[], xScale: ScaleTime<number, number>): number {
  const targetTimestamp = Math.round(xScale.invert(x).getTime());
  const timestamps = data.map((d) => new Date(d.date).getTime());

  let closestIndex = 0;
  let minDiff = Math.abs(timestamps[0] - targetTimestamp);

  for (let i = 1; i < timestamps.length; i++) {
    const diff = Math.abs(timestamps[i] - targetTimestamp);
    if (diff < minDiff) {
      closestIndex = i;
      minDiff = diff;
    }
  }
  return closestIndex;
}

export function positionTooltip(tooltip: TooltipSelection, svgEl: SVGGElement, e: MouseEvent): void {
  const visRect = svgEl.getBoundingClientRect();
  const middleX = visRect.x + visRect.width / 2;
  const middleY = visRect.y + visRect.height / 2;
  const tooltipRect = (tooltip.node() as HTMLDivElement).getBoundingClientRect();
  tooltip.style('left', (middleX < e.pageX ? e.pageX - tooltipRect.width : e.pageX) + 'px');
  tooltip.style('top', (middleY < e.pageY ? e.pageY - tooltipRect.height : e.pageY + 20) + 'px');
}

export function setTooltipContent(tooltip: TooltipSelection, label: string, value: string): void {
  tooltip.select('.tooltip-label').text(label);
  tooltip.select('.tooltip-value').text(value);
}

export function updateAreaPaths(
  areasGroup: d3.Selection<SVGGElement, unknown, null, undefined>,
  transitionDuration: number,
  palette: Palette,
  data: ChartData[],
  visualizationStyle: string,
  xScale: ScaleTime<number, number>,
  yScale: ScaleLinear<number, number>,
  positiveKeys: string[],
  negativeKeys: string[],
  stackedPositiveData: d3.Series<ChartData, string>[],
  stackedNegativeData: d3.Series<ChartData, string>[],
): void {
  const curveType = visualizationStyle === 'curved' ? d3.curveMonotoneX : visualizationStyle === 'stepped' ? d3.curveStep : d3.curveLinear;

  const pathData: PathDatum[] = [
    ...positiveKeys.map((key, i): PathDatum => ({ key, side: 'positive', index: i })),
    ...negativeKeys.map((key, i): PathDatum => ({ key, side: 'negative', index: i })),
  ];

  const paths = areasGroup.selectAll<SVGPathElement, PathDatum>('path').data(pathData, (d) => `${d.side}-${d.key}`);

  paths.exit().transition().duration(transitionDuration).ease(d3.easeCubicOut).attr('fill-opacity', 0).remove();

  const entered = paths.enter().append<SVGPathElement>('path').attr('fill-opacity', 0).attr('stroke-width', 1);

  entered
    .merge(paths)
    .attr('fill', (d) => palette[d.key].main)
    .attr('stroke', (d) => palette[d.key].main)
    .transition()
    .duration(transitionDuration)
    .ease(d3.easeCubicOut)
    .attr('fill-opacity', 0.3)
    .attr('d', (d) => {
      const stackedData = d.side === 'positive' ? stackedPositiveData : stackedNegativeData;
      return d3
        .area<ChartData>()
        .curve(curveType)
        .x((point) => xScale(new Date(point.date).getTime()))
        .y1((_point, j) => yScale(stackedData[d.index][j][0]))
        .y0((_point, j) => yScale(stackedData[d.index][j][1]))(data);
    });
}
