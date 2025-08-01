import {type MutableRefObject, useEffect, useMemo, useRef } from 'react';
import * as d3 from 'd3';
import {type ScaleLinear, type ScaleTime, symbol, symbolTriangle } from 'd3';
import type {ChartData, Palette} from './chart.tsx';
import type {SprintType} from '../../../../../types/data/sprintType.ts';
import type {DefaultSettings} from '../settings/settings.tsx';
import { PositiveNegativeSide, splitPositiveNegativeData } from '../utilities/utilities.ts';
import { round } from 'lodash';

const MARGIN = { top: 30, right: 30, bottom: 50, left: 50 };

type AreaChartProps = {
  width: number;
  height: number;
  data: ChartData[];
  scale: number[];
  palette: Palette;
  sprintList: SprintType[];
  settings: DefaultSettings;
};

export const StackedAreaChart = ({ width, height, data, scale, palette, sprintList, settings }: AreaChartProps) => {
  // bounds = area inside the graph axis = calculated by substracting the margins
  const svgRef = useRef(null);
  const tooltipRef = useRef(null);
  const boundsWidth = width - MARGIN.right - MARGIN.left;
  const boundsHeight = height - MARGIN.top - MARGIN.bottom;
  // Y axis
  const yScale = useMemo(() => {
    return d3.scaleLinear().domain([scale[0], scale[1]]).range([boundsHeight, 0]);
  }, [boundsHeight, scale]);

  // X axis
  const [xMin, xMax] = d3.extent(data, (d) => new Date(d.date).getTime());
  const xScale = useMemo(() => {
    return d3
      .scaleTime()
      .domain([xMin || 0, xMax || 0])
      .range([0, boundsWidth]);
  }, [boundsWidth, xMax, xMin]);

  let idleTimeout: number | null = null;
  function idled() {
    idleTimeout = null;
  }

  const brush = d3
    .brushX()
    .extent([
      [0, 0],
      [width, height],
    ])
    .on('end', (e) => {
      const svgElement = d3.select(svgRef.current);
      const extent = e.selection;
      if (!extent) {
        //This Timeout is necessary because it not the reset of the brush would trigger the reset of the domain
        // and the brushing wouldn't work.
        if (!idleTimeout) return (idleTimeout = window.setTimeout(idled, 350));
        xScale.domain([xMin || 0, xMax || 0]);
      } else {
        xScale.domain([xScale.invert(extent[0]), xScale.invert(extent[1])]);

        svgElement.select<SVGGElement>('.brush').call(brush.move.bind(this), null);
      }
      // d3/typescript sometimes does weird things and throws an error where no error is.
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      svgElement.select('.xAxis').transition().duration(1000).call(d3.axisBottom(xScale));
      updateDataLines(palette, data, settings.visualizationStyle, xScale, yScale, svgRef);
      updateSprintAreas(sprintList, xScale, yScale, scale[0], scale[1], svgRef);
    });

  // Render the X and Y axis using d3.js, not react
  useEffect(() => {
    const svgElement = d3.select(svgRef.current);
    svgElement.selectAll('*').remove();
    svgElement
      .append('g')
      .attr('class', 'xAxis')
      .attr('transform', 'translate(0,' + boundsHeight + ')')
      .call(d3.axisBottom(xScale));
    svgElement.append('g').call(d3.axisLeft(yScale));
    svgElement.append('g').attr('class', 'brush').call(brush);

    generateDataLines(palette, data, settings.visualizationStyle, xScale, yScale, svgRef, tooltipRef);
    if (settings.showSprints) {
      generateSprintAreas(sprintList, xScale, yScale, scale[0], scale[1], svgRef);
    }
  }, [xScale, yScale, boundsHeight, settings.showSprints]);

  return (
    <>
      <div
        ref={tooltipRef}
        className={'card bg-base-100 shadow-xl rounded border-2 p-2 break-all'}
        style={{
          position: 'fixed',
          visibility: 'hidden',
          minWidth: '10rem',
          maxWidth: '20rem',
        }}>
        <div className={'tooltip-label font-bold text-xs'}>Label</div>
        <div className={'tooltip-value badge badge-outline'}>Value</div>
      </div>
      <svg width={width} height={height} xmlns="http://www.w3.org/2000/svg">
        <g width={boundsWidth} height={boundsHeight} ref={svgRef} transform={`translate(${[MARGIN.left, MARGIN.top].join(',')})`}></g>
      </svg>
    </>
  );
};

function positionTooltip(tooltip: d3.Selection<null, unknown, null, undefined>, svgRef: React.MutableRefObject<null>, e: MouseEvent) {
  if (svgRef.current) {
    const visRect = (svgRef.current as SVGSVGElement).getBoundingClientRect();
    const middleX = visRect.x + visRect.width / 2;
    const middleY = visRect.y + visRect.height / 2;
    const tooltipRect = (tooltip.node() as unknown as HTMLDivElement).getBoundingClientRect();
    if (middleX < e.pageX) {
      tooltip.style('left', e.pageX - tooltipRect.width + 'px');
    } else {
      tooltip.style('left', e.pageX + 'px');
    }
    if (middleY < e.pageY) {
      tooltip.style('top', e.pageY - tooltipRect.height + 'px');
    } else {
      tooltip.style('top', e.pageY + 20 + 'px');
    }
  }
}

function setTooltipContent(tooltip: d3.Selection<null, unknown, null, undefined>, tooltipLable: string, tooltipValue: string) {
  tooltip.select('.tooltip-label').text(tooltipLable);
  tooltip.select('.tooltip-value').text(tooltipValue);
}

function generateDataLines(
  palette: Palette,
  data: ChartData[],
  visualizationStyle: string,
  xScale: ScaleTime<number, number, never>,
  yScale: ScaleLinear<number, number, never>,
  svgRef: MutableRefObject<null>,
  tooltipRef: MutableRefObject<null>,
) {
  const svgElement = d3.select(svgRef.current);
  const stackedPositiveData = d3.stack().keys(Object.keys(palette))(splitPositiveNegativeData(data, PositiveNegativeSide.POSITIVE));
  const stackedNegativeData = d3.stack().keys(Object.keys(palette))(splitPositiveNegativeData(data, PositiveNegativeSide.NEGATIVE));
  Object.keys(palette).forEach((dataItemName, i) => {
    const areaBuilderPositive = d3
      .area<ChartData>()
      .curve(visualizationStyle === 'curved' ? d3.curveMonotoneX : visualizationStyle === 'stepped' ? d3.curveStep : d3.curveLinear)
      .x((d) => xScale(new Date(d.date).getTime()))
      .y1((_d, j) => yScale(stackedPositiveData[i][j][0]))
      .y0((_d, j) => yScale(stackedPositiveData[i][j][1]));
    svgElement
      .append('path')
      .datum(data)
      .attr('class', `positiveChartArea${i}`)
      .attr('fill', palette[dataItemName].main)
      .attr('fill-opacity', 0.3)
      .attr('stroke', palette[dataItemName].main)
      .attr('stroke-width', 1)
      .attr('d', areaBuilderPositive)
      .on('mouseover', () => {
        return d3.select(tooltipRef.current).style('visibility', 'visible');
      })
      .on('mousemove', (e: MouseEvent, d: ChartData[]) => {
        const [x] = d3.pointer(e);
        const closestIndex = getClosestIndex(x, d, xScale);
        const tooltip = d3.select(tooltipRef.current);
        tooltip.style('border-color', palette[dataItemName].secondary);
        setTooltipContent(tooltip, dataItemName, `${round(d[closestIndex][dataItemName])}`);
        positionTooltip(tooltip, svgRef, e);
        return tooltip;
      })
      .on('mouseout', () => {
        return d3.select(tooltipRef.current).style('visibility', 'hidden');
      });

    const areaBuilderNegative = d3
      .area<ChartData>()
      .curve(visualizationStyle === 'curved' ? d3.curveMonotoneX : visualizationStyle === 'stepped' ? d3.curveStep : d3.curveLinear)
      .x((d) => xScale(new Date(d.date).getTime()))
      .y1((_d, j) => yScale(stackedNegativeData[i][j][0]))
      .y0((_d, j) => yScale(stackedNegativeData[i][j][1]));
    svgElement
      .append('path')
      .datum(data)
      .attr('class', `negativeChartArea${i}`)
      .attr('fill', palette[dataItemName].main)
      .attr('fill-opacity', 0.3)
      .attr('stroke', palette[dataItemName].main)
      .attr('stroke-width', 1)
      .attr('d', areaBuilderNegative)
      .on('mouseover', () => {
        return d3.select(tooltipRef.current).style('visibility', 'visible');
      })
      .on('mousemove', (e: MouseEvent, d: ChartData[]) => {
        const [x] = d3.pointer(e);
        const closestIndex = getClosestIndex(x, d, xScale);
        const tooltip = d3.select(tooltipRef.current);
        tooltip.style('border-color', palette[dataItemName].secondary);
        setTooltipContent(tooltip, dataItemName, `${round(d[closestIndex][dataItemName])}`);
        positionTooltip(tooltip, svgRef, e);
        return tooltip;
      })
      .on('mouseout', () => {
        return d3.select(tooltipRef.current).style('visibility', 'hidden');
      });
  });
}

function updateDataLines(
  palette: Palette,
  data: ChartData[],
  visualizationStyle: string,
  xScale: ScaleTime<number, number, never>,
  yScale: ScaleLinear<number, number, never>,
  svgRef: MutableRefObject<null>,
) {
  const svgElement = d3.select(svgRef.current);
  const stackedPositiveData = d3.stack().keys(Object.keys(palette))(splitPositiveNegativeData(data, PositiveNegativeSide.POSITIVE));
  const stackedNegativeData = d3.stack().keys(Object.keys(palette))(splitPositiveNegativeData(data, PositiveNegativeSide.NEGATIVE));
  Object.keys(palette).forEach((_author, i) => {
    const areaBuilderPositive = d3
      .area<ChartData>()
      .curve(visualizationStyle === 'curved' ? d3.curveMonotoneX : visualizationStyle === 'stepped' ? d3.curveStep : d3.curveLinear)
      .x((d) => xScale(new Date(d.date).getTime()))
      .y1((_d, j) => yScale(stackedPositiveData[i][j][0]))
      .y0((_d, j) => yScale(stackedPositiveData[i][j][1]));
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    svgElement.select(`.positiveChartArea${i}`).transition().duration(1000).attr('d', areaBuilderPositive);
    const areaBuilderNegative = d3
      .area<ChartData>()
      .curve(visualizationStyle === 'curved' ? d3.curveMonotoneX : visualizationStyle === 'stepped' ? d3.curveStep : d3.curveLinear)
      .x((d) => xScale(new Date(d.date).getTime()))
      .y1((_d, j) => yScale(stackedNegativeData[i][j][0]))
      .y0((_d, j) => yScale(stackedNegativeData[i][j][1]));
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    svgElement.select(`.negativeChartArea${i}`).transition().duration(1000).attr('d', areaBuilderNegative);
  });
}

function generateSprintAreas(
  sprints: SprintType[],
  xScale: ScaleTime<number, number, never>,
  yScale: ScaleLinear<number, number, never>,
  yMax: number,
  yMin: number,
  svgRef: MutableRefObject<null>,
) {
  const svgElement = d3.select(svgRef.current);
  svgElement
    .append('defs')
    .append('pattern')
    .attr('id', 'diagonalHatch')
    .attr('patternUnits', 'userSpaceOnUse')
    .attr('width', 8)
    .attr('height', 8)
    .append('path')
    .attr('d', 'M-1,1 l2,-2 M0,8 l8,-8 M3,5 l2,-2')
    .attr('stroke', '#ff3b30')
    .attr('stroke-width', 1);

  sprints.forEach((sprint) => {
    svgElement
      .append('rect')
      .attr('id', `sprintStartLine${sprint.id}`)
      .attr('y', yScale(yMin))
      .attr('x', xScale(new Date(sprint.startDate)))
      .attr('height', yScale(yMax) - yScale(yMin))
      .attr('width', 1)
      .attr('fill', '#4cd964');
    svgElement
      .append('path')
      .attr('id', `sprintStartLineTriangle${sprint.id}`)
      .attr('d', symbol().type(symbolTriangle))
      .attr('width', '10')
      .attr('height', '10')
      .attr('transform', 'translate(' + (xScale(new Date(sprint.startDate)) + 4) + ',' + (yScale(yMin) + 5) + ') rotate(90)')
      .style('fill', '#4cd964');
    svgElement
      .append('rect')
      .attr('id', `sprintEndLine${sprint.id}`)
      .attr('y', yScale(yMin))
      .attr('x', xScale(new Date(sprint.endDate)))
      .attr('height', yScale(yMax) - yScale(yMin))
      .attr('width', 1)
      .attr('fill', '#ff3b30');
    svgElement
      .append('path')
      .attr('id', `sprintEndLineTriangle${sprint.id}`)
      .attr('d', symbol().type(symbolTriangle))
      .attr('width', '10')
      .attr('height', '10')
      .attr('transform', 'translate(' + (xScale(new Date(sprint.endDate)) - 3) + ',' + (yScale(yMin) + 5) + ') rotate(-90)')
      .style('fill', '#ff3b30');

    svgElement
      .append('rect')
      .attr('id', `sprintBackground${sprint.id}`)
      .attr('y', yScale(0) - 15)
      .attr('x', xScale(new Date(sprint.startDate)))
      .attr('height', 15)
      .attr('width', xScale(new Date(sprint.endDate)) - xScale(new Date(sprint.startDate)))
      .attr('fill', 'white');
    svgElement
      .append('rect')
      .attr('id', `sprintBackgroundStripes${sprint.id}`)
      .attr('y', yScale(0) - 15)
      .attr('x', xScale(new Date(sprint.startDate)))
      .attr('height', 15)
      .attr('width', xScale(new Date(sprint.endDate)) - xScale(new Date(sprint.startDate)))
      .attr('fill', 'url(#diagonalHatch)')
      .attr('stroke', '#ff3b30');
    svgElement
      .append('text')
      .attr('id', `sprintText${sprint.id}`)
      .attr('y', yScale(0) - 4)
      .attr('x', xScale(new Date(sprint.startDate)) + 2)
      .attr('height', 10)
      .attr('font-size', '.75rem')
      .attr('paint-order', 'stroke')
      .attr('stroke', 'white')
      .attr('stroke-width', '4px')
      .text(sprint.name);
  });
}

function updateSprintAreas(
  sprints: SprintType[],
  xScale: ScaleTime<number, number, never>,
  yScale: ScaleLinear<number, number, never>,
  yMax: number,
  yMin: number,
  svgRef: MutableRefObject<null>,
) {
  const svgElement = d3.select(svgRef.current);
  sprints.forEach((sprint) => {
    svgElement
      .select(`#sprintStartLine${sprint.id}`)
      .attr('y', yScale(yMin))
      .attr('x', xScale(new Date(sprint.startDate)))
      .attr('height', yScale(yMax) - yScale(yMin))
      .attr('width', 1);

    svgElement
      .select(`#sprintStartLineTriangle${sprint.id}`)
      .attr('width', '10')
      .attr('height', '10')
      .attr('transform', 'translate(' + (xScale(new Date(sprint.startDate)) + 4) + ',' + (yScale(yMin) + 5) + ') rotate(90)');

    svgElement
      .select(`#sprintEndLine${sprint.id}`)
      .attr('y', yScale(yMin))
      .attr('x', xScale(new Date(sprint.endDate)))
      .attr('height', yScale(yMax) - yScale(yMin))
      .attr('width', 1);

    svgElement
      .select(`#sprintEndLineTriangle${sprint.id}`)
      .attr('width', '10')
      .attr('height', '10')
      .attr('transform', 'translate(' + (xScale(new Date(sprint.endDate)) - 3) + ',' + (yScale(yMin) + 5) + ') rotate(-90)');

    svgElement
      .select(`#sprintBackground${sprint.id}`)
      .attr('y', yScale(0) - 15)
      .attr('x', xScale(new Date(sprint.startDate)))
      .attr('height', 15)
      .attr('width', xScale(new Date(sprint.endDate)) - xScale(new Date(sprint.startDate)));

    svgElement
      .select(`#sprintBackgroundStripes${sprint.id}`)
      .attr('y', yScale(0) - 15)
      .attr('x', xScale(new Date(sprint.startDate)))
      .attr('height', 15)
      .attr('width', xScale(new Date(sprint.endDate)) - xScale(new Date(sprint.startDate)));

    svgElement
      .select(`#sprintText${sprint.id}`)
      .attr('y', yScale(0) - 4)
      .attr('x', xScale(new Date(sprint.startDate)) + 2)
      .attr('height', 10);
  });
}

function getClosestIndex(x: number, data: ChartData[], xScale: ScaleTime<number, number, never>) {
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
