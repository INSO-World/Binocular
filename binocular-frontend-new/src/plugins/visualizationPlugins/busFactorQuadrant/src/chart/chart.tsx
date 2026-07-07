import type { SettingsType } from '../settings/settings.tsx';
import type { DataPlugin } from '../../../../interfaces/dataPlugin.ts';
import type { ParametersType } from '../../../../../types/parameters/parametersType.ts';
import { handlePopoutResizing } from '../../../../utils/resizing.ts';
import { type RefObject, useEffect, useRef, useState } from 'react';
import type { Store } from '@reduxjs/toolkit';
import { useDispatch, useSelector } from 'react-redux';
import * as d3 from 'd3';
import { DataState, type ModulePoint, setDateRange, setRepoPath, setExcludedAuthors, setNeededModules } from '../reducer';

/**
 * Scatter/quadrant chart for the bus factor vs. CI error rate per module.
 * Each module is drawn as a dot: x = CI error rate, y = bus factor.
 * The background is split into four risk quadrants (OK / quality problem /
 * knowledge risk / critical) based on the thresholds from the settings.
 */
function Chart(props: {
  settings: SettingsType;
  dataConnection: DataPlugin;
  parameters: ParametersType;
  chartContainerRef: RefObject<HTMLDivElement | null>;
  store: Store;
}) {
  // Typed access to this widget's own (per-item) redux store
  type RootState = ReturnType<typeof props.store.getState>;
  const dispatch = useDispatch<typeof props.store.dispatch>();

  // Data + loading state come from the reducer (filled by the saga)
  const data = useSelector((s: RootState) => s.plugin.data as ModulePoint[]);
  const dataState = useSelector((s: RootState) => s.plugin.dataState);

  // Current pixel size of the chart, kept in state so d3 can redraw on resize
  const [chartWidth, setChartWidth] = useState(100);
  const [chartHeight, setChartHeight] = useState(100);
  // Which module is currently hovered (null = no tooltip shown)
  const [tooltip, setTooltip] = useState<{ x: number; y: number; point: ModulePoint } | null>(null);
  const svgRef = useRef<SVGSVGElement>(null);

  // Read the container size and store it, so the svg fills the whole widget
  function resize() {
    if (!props.chartContainerRef.current) return;
    if (props.chartContainerRef.current.offsetWidth !== chartWidth) setChartWidth(props.chartContainerRef.current.offsetWidth);
    if (props.chartContainerRef.current.offsetHeight !== chartHeight) setChartHeight(props.chartContainerRef.current.offsetHeight);
  }
  // Measure once on mount (and whenever the size state changes)
  useEffect(() => {
    resize();
  }, [props.chartContainerRef, chartHeight, chartWidth]);
  // Re-measure whenever the dashboard fires a RESIZE action (initial layout, drag-resize, popout)
  handlePopoutResizing(props.store, resize);

  // The following effects push settings/parameters into the reducer.
  // The saga listens for these actions and re-fetches the data.
  useEffect(() => {
    dispatch(setExcludedAuthors(props.settings.excludedAuthors ?? []));
  }, [props.settings.excludedAuthors]);
  useEffect(() => {
    dispatch(setNeededModules(props.settings.neededModules ?? []));
  }, [props.settings.neededModules]);
  useEffect(() => {
    dispatch(setDateRange(props.parameters.parametersDateRange));
  }, [props.parameters.parametersDateRange]);
  useEffect(() => {
    dispatch(setRepoPath(props.settings.repoPath));
  }, [props.settings.repoPath]);
  // Trigger a first load when the selected data source changes
  useEffect(() => {
    dispatch({ type: 'REFRESH' });
  }, [props.dataConnection]);

  // Main drawing effect: runs whenever the data, the size or the settings change
  useEffect(() => {
    // Start from a clean svg on every redraw
    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove();
    if (!data || data.length === 0) return;

    // Inner drawing area (chart minus the space reserved for the axes)
    const margin = { top: 20, right: 20, bottom: 45, left: 45 };
    const innerW = chartWidth - margin.left - margin.right;
    const innerH = chartHeight - margin.top - margin.bottom;
    if (innerW <= 0 || innerH <= 0) return;

    // Thresholds that split the four quadrants (from the settings)
    const bfT = props.settings.busFactorThreshold;
    const ciT = props.settings.ciErrorThreshold;
    // Upper end of the y axis: at least twice the threshold so the top zone never collapses
    const maxBF = Math.max(d3.max(data, (d) => d.busFactor) ?? 0, bfT * 2, 1);

    const g = svg.append('g').attr('transform', `translate(${margin.left},${margin.top})`);
    // x = CI error rate (0..1), y = bus factor (0 at bottom, high at the top)
    const x = d3.scaleLinear().domain([0, 1]).range([0, innerW]);
    const y = d3.scaleLinear().domain([0, maxBF]).range([innerH, 0]);

    // Definition of the four quadrants incl. color and label
    const quads = [
      { x0: 0, x1: ciT, y0: bfT, y1: maxBF, color: '#22c55e', label: 'OK' },
      { x0: ciT, x1: 1, y0: bfT, y1: maxBF, color: '#eab308', label: 'Quality problem' },
      { x0: 0, x1: ciT, y0: 0, y1: bfT, color: '#3b82f6', label: 'Knowledge Risk' },
      { x0: ciT, x1: 1, y0: 0, y1: bfT, color: '#ef4444', label: '⚠ CRITICAL' },
    ];
    // Draw a faint colored background rect + a centered label for each quadrant
    quads.forEach((q) => {
      g.append('rect')
        .attr('x', x(q.x0))
        .attr('y', y(q.y1))
        .attr('width', x(q.x1) - x(q.x0))
        .attr('height', y(q.y0) - y(q.y1))
        .attr('fill', q.color)
        .attr('opacity', 0.12);
      g.append('text')
        .attr('x', (x(q.x0) + x(q.x1)) / 2)
        .attr('y', (y(q.y0) + y(q.y1)) / 2)
        .attr('text-anchor', 'middle')
        .attr('fill', q.color)
        .attr('font-size', 11)
        .attr('font-weight', 'bold')
        .attr('opacity', 0.7)
        .text(q.label);
    });

    // Dashed divider lines that mark the two thresholds
    g.append('line') // vertical line at the CI-error threshold
      .attr('x1', x(ciT))
      .attr('x2', x(ciT))
      .attr('y1', 0)
      .attr('y2', innerH)
      .attr('stroke', '#999')
      .attr('stroke-dasharray', '4');
    g.append('line') // horizontal line at the bus-factor threshold
      .attr('x1', 0)
      .attr('x2', innerW)
      .attr('y1', y(bfT))
      .attr('y2', y(bfT))
      .attr('stroke', '#999')
      .attr('stroke-dasharray', '4');

    // X axis at the bottom, formatted as a percentage
    g.append('g')
      .attr('transform', `translate(0,${innerH})`)
      .call(
        d3
          .axisBottom(x)
          .ticks(5)
          .tickFormat((v) => `${Math.round(+v * 100)}%`),
      );
    // Y axis on the left (limit the ticks so we don't get fractional bus factors)
    g.append('g').call(d3.axisLeft(y).ticks(Math.min(maxBF, 6)));
    // Axis titles
    g.append('text')
      .attr('x', innerW / 2)
      .attr('y', innerH + 38)
      .attr('text-anchor', 'middle')
      .attr('font-size', 10)
      .text('CI-Error rate (low → high)');
    g.append('text')
      .attr('transform', 'rotate(-90)')
      .attr('x', -innerH / 2)
      .attr('y', -34)
      .attr('text-anchor', 'middle')
      .attr('font-size', 10)
      .text('Bus-Factor (low → high)');

    // One dot per module. clamp the x value to 1 in case the backend ever returns > 1
    g.selectAll('.mod-dot')
      .data(data)
      .join('circle')
      .attr('class', 'mod-dot')
      .attr('cx', (d) => x(Math.min(d.ciErrorRate, 1)))
      .attr('cy', (d) => y(d.busFactor))
      .attr('r', 6)
      .attr('fill', '#1f2937')
      .attr('stroke', '#fff')
      .attr('stroke-width', 1.5)
      .style('cursor', 'pointer')
      // Show the tooltip on hover; d3.pointer gives coordinates relative to the container
      .on('mouseenter', (event: MouseEvent, d: ModulePoint) => {
        const [mx, my] = d3.pointer(event, props.chartContainerRef.current);
        setTooltip({ x: mx, y: my, point: d });
      })
      .on('mouseleave', () => setTooltip(null));
  }, [data, chartWidth, chartHeight, props.settings]);

  return (
    // "relative" is needed so the absolutely positioned tooltip is placed inside this container
    <div className={'w-full h-full flex justify-center items-center relative'} ref={props.chartContainerRef}>
      {/* Different states: no data yet / loading spinner / finished */}
      {dataState === DataState.EMPTY && <div>NoData</div>}
      {dataState === DataState.FETCHING && <span className="loading loading-spinner loading-lg text-accent" />}
      {dataState === DataState.COMPLETE &&
        (data.length !== 0 ? (
          <svg ref={svgRef} width={chartWidth} height={chartHeight} />
        ) : (
          <div>No Data matching the selected Parameters!</div>
        ))}

      {tooltip &&
        (() => {
          // Flip horizontally when the point is on the right half, and vertically
          // when it's on the bottom half, so the tooltip never gets cut off at an edge.
          const openLeft = tooltip.x > chartWidth / 2;
          const openUp = tooltip.y > chartHeight / 2;
          return (
            <div
              className="absolute z-10 pointer-events-none bg-base-100 border border-base-300 rounded shadow p-2 text-xs"
              style={{
                left: tooltip.x + (openLeft ? -12 : 12),
                top: tooltip.y + (openUp ? -12 : 12),
                // translate(-100%) on an axis shifts the box by its own size on that axis,
                // so its right/bottom edge sits at the point instead of overflowing.
                transform: `translate(${openLeft ? '-100%' : '0'}, ${openUp ? '-100%' : '0'})`,
                maxWidth: 280,
              }}>
              <div className="font-bold mb-1 break-all">{tooltip.point.module}</div>
              <div>Bus Factor: {tooltip.point.busFactor}</div>
              <div className="mb-1">CI error rate: {(tooltip.point.ciErrorRate * 100).toFixed(1)}%</div>
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
  );
}
export default Chart;
