import type { SettingsType } from '../settings/settings.tsx';
import type { DataPlugin } from '../../../../interfaces/dataPlugin.ts';
import type { ParametersType } from '../../../../../types/parameters/parametersType.ts';
import { handlePopoutResizing } from '../../../../utils/resizing.ts';
import { type RefObject, useEffect, useRef, useState } from 'react';
import type { Store } from '@reduxjs/toolkit';
import { useDispatch, useSelector } from 'react-redux';
import * as d3 from 'd3';
import { DataState, type Point, setDateRange, setRepoPath, setGranularity } from '../reducer';

function Chart(props: {
  settings: SettingsType;
  dataConnection: DataPlugin;
  parameters: ParametersType;
  chartContainerRef: RefObject<HTMLDivElement | null>;
  store: Store;
}) {
  type RootState = ReturnType<typeof props.store.getState>;
  type AppDispatch = typeof props.store.dispatch;
  const useAppDispatch = () => useDispatch<AppDispatch>();
  const dispatch: AppDispatch = useAppDispatch();

  const data = useSelector((state: RootState) => state.plugin.data as Point[]);
  const dataState = useSelector((state: RootState) => state.plugin.dataState);

  const [chartWidth, setChartWidth] = useState(100);
  const [chartHeight, setChartHeight] = useState(100);
  const svgRef = useRef<SVGSVGElement>(null);
  const [tooltip, setTooltip] = useState<{ x: number; y: number; point: Point } | null>(null);

  /**
   * RESIZE Logic START
   */
  function resize() {
    if (!props.chartContainerRef.current) return;
    if (props.chartContainerRef.current?.offsetWidth !== chartWidth) {
      setChartWidth(props.chartContainerRef.current.offsetWidth);
    }
    if (props.chartContainerRef.current?.offsetHeight !== chartHeight) {
      setChartHeight(props.chartContainerRef.current.offsetHeight);
    }
  }

  useEffect(() => {
    resize();
  }, [props.chartContainerRef, chartHeight, chartWidth]);

  handlePopoutResizing(props.store, resize);

  useEffect(() => {
    dispatch(setDateRange(props.parameters.parametersDateRange));
  }, [props.parameters.parametersDateRange]);

  useEffect(() => {
    dispatch(setGranularity(props.parameters.parametersGeneral.granularity));
  }, [props.parameters.parametersGeneral]);

  useEffect(() => {
    dispatch(setRepoPath(props.settings.repoPath));
  }, [props.settings.repoPath]);

  useEffect(() => {
    dispatch({ type: 'REFRESH' });
  }, [props.dataConnection]);

  useEffect(() => {
    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove();
    if (!data || data.length === 0) return;

    const margin = { top: 20, right: 55, bottom: 50, left: 45 };
    const innerW = chartWidth - margin.left - margin.right;
    const innerH = chartHeight - margin.top - margin.bottom;
    if (innerW <= 0 || innerH <= 0) return;

    const g = svg.append('g').attr('transform', `translate(${margin.left},${margin.top})`);

    const x = d3
      .scalePoint<string>()
      .domain(data.map((d) => d.id))
      .range([0, innerW])
      .padding(0.5);
    const yLeft = d3
      .scaleLinear()
      .domain([0, (d3.max(data, (d) => d.busFactor) ?? 0) * 1.2])
      .range([innerH, 0]);
    const yRight = d3.scaleLinear().domain([0, 1]).range([innerH, 0]);

    g.append('g')
      .attr('transform', `translate(0,${innerH})`)
      .call(d3.axisBottom(x))
      .selectAll('text')
      .attr('transform', 'rotate(-40)')
      .style('text-anchor', 'end');

    g.append('g').call(d3.axisLeft(yLeft).ticks(5)).attr('color', '#3b82f6');

    g.append('g')
      .attr('transform', `translate(${innerW},0)`)
      .call(
        d3
          .axisRight(yRight)
          .ticks(5)
          .tickFormat((v) => `${Math.round(+v * 100)}%`),
      )
      .attr('color', '#ef4444');

    const busLine = d3
      .line<Point>()
      .x((d) => x(d.id)!)
      .y((d) => yLeft(d.busFactor));
    const ciLine = d3
      .line<Point>()
      .x((d) => x(d.id)!)
      .y((d) => yRight(d.ciErrorRate));

    g.append('path').datum(data).attr('fill', 'none').attr('stroke', '#3b82f6').attr('stroke-width', 2).attr('d', busLine);
    g.append('path').datum(data).attr('fill', 'none').attr('stroke', '#ef4444').attr('stroke-width', 2).attr('d', ciLine);
    g.selectAll('.bf-dot')
      .data(data)
      .join('circle')
      .attr('class', 'bf-dot')
      .attr('cx', (d) => x(d.id)!)
      .attr('cy', (d) => yLeft(d.busFactor))
      .attr('r', 4)
      .attr('fill', '#3b82f6')
      .style('cursor', 'pointer')
      .on('mouseenter', (event: MouseEvent, d: Point) => {
        const [mx, my] = d3.pointer(event, props.chartContainerRef.current);
        setTooltip({ x: mx, y: my, point: d });
      })
      .on('mouseleave', () => setTooltip(null));
  }, [data, chartWidth, chartHeight]);

  return (
    <>
      <div className={'w-full h-full flex justify-center items-center relative'} ref={props.chartContainerRef}>
        {dataState === DataState.EMPTY && <div>NoData</div>}
        {dataState === DataState.FETCHING && (
          <div>
            <span className="loading loading-spinner loading-lg text-accent"></span>
          </div>
        )}
        {dataState === DataState.COMPLETE &&
          (data.length !== 0 ? (
            <svg ref={svgRef} width={chartWidth} height={chartHeight} />
          ) : (
            <div>No Data matching the selected Parameters!</div>
          ))}

        {tooltip &&
          (() => {
            const openLeft = tooltip.x > chartWidth / 2;
            return (
              <div
                className="absolute z-10 pointer-events-none bg-base-100 border border-base-300 rounded shadow p-2 text-xs"
                style={{
                  left: tooltip.x + (openLeft ? -12 : 12),
                  top: tooltip.y + 12,
                  transform: openLeft ? 'translateX(-100%)' : undefined,
                  maxWidth: 280,
                }}>
                <div className="font-bold mb-1">
                  {tooltip.point.id} — Bus Factor {tooltip.point.busFactor}
                </div>
                <ul>
                  {(tooltip.point.topAuthors ?? []).map((a) => (
                    <li key={a.gitSignature} className="flex justify-between gap-2">
                      <span className="truncate">{a.gitSignature.replace(/\s*<.*>$/, '')}</span>
                      <span className="tabular-nums">{(a.percentage * 100).toFixed(1)}%</span>
                    </li>
                  ))}
                </ul>
              </div>
            );
          })()}
      </div>
    </>
  );
}

export default Chart;
