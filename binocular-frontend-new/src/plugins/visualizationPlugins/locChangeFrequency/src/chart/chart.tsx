import type { SettingsType } from '../settings/settings.tsx';
import type { DataPlugin } from '../../../../interfaces/dataPlugin.ts';
import type { ParametersType } from '../../../../../types/parameters/parametersType.ts';
import { handlePopoutResizing } from '../../../../utils/resizing.ts';
import { type RefObject, useEffect, useRef, useState } from 'react';
import type { Store } from '@reduxjs/toolkit';
import { useDispatch, useSelector } from 'react-redux';
import * as d3 from 'd3';
import { DataState, type ModuleHotspot, setDateRange, setRepoPath, setNeededModules } from '../reducer';

// Sequential single-hue ramp (light -> dark). Light end = "near zero" and is
// allowed to recede toward the surface. Never use a rainbow for magnitude.
const BLUE_RAMP = ['#cde2fb', '#9ec5f4', '#6da7ec', '#3987e5', '#256abf', '#184f95', '#0d366b'];
const INK_PRIMARY = '#0b0b0b';
const INK_MUTED = '#898781';
const SURFACE = '#fcfcfb';

/**
 * Hotspot treemap: area = module size (LOC, structural dimension),
 * color = change frequency (temporal dimension). Big + dark cells are the hotspots.
 */
function Chart(props: {
  settings: SettingsType;
  dataConnection: DataPlugin;
  parameters: ParametersType;
  chartContainerRef: RefObject<HTMLDivElement | null>;
  store: Store;
}) {
  type RootState = ReturnType<typeof props.store.getState>;
  const dispatch = useDispatch<typeof props.store.dispatch>();
  const data = useSelector((s: RootState) => s.plugin.data as ModuleHotspot[]);
  const dataState = useSelector((s: RootState) => s.plugin.dataState);

  const [chartWidth, setChartWidth] = useState(100);
  const [chartHeight, setChartHeight] = useState(100);
  const [tooltip, setTooltip] = useState<{ x: number; y: number; point: ModuleHotspot } | null>(null);
  const svgRef = useRef<SVGSVGElement>(null);

  function resize() {
    if (!props.chartContainerRef.current) return;
    if (props.chartContainerRef.current.offsetWidth !== chartWidth) setChartWidth(props.chartContainerRef.current.offsetWidth);
    if (props.chartContainerRef.current.offsetHeight !== chartHeight) setChartHeight(props.chartContainerRef.current.offsetHeight);
  }
  useEffect(() => {
    resize();
  }, [props.chartContainerRef, chartHeight, chartWidth, resize]);
  handlePopoutResizing(props.store, resize);

  useEffect(() => {
    dispatch(setDateRange(props.parameters.parametersDateRange));
  }, [dispatch, props.parameters.parametersDateRange]);
  useEffect(() => {
    dispatch(setRepoPath(props.settings.repoPath));
  }, [dispatch, props.settings.repoPath]);
  useEffect(() => {
    dispatch({ type: 'REFRESH' });
  }, [dispatch, props.dataConnection]);
  useEffect(() => {
    dispatch(setNeededModules(props.settings.neededModules ?? []));
  }, [dispatch, props.settings.neededModules]);

  useEffect(() => {
    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove();
    if (!data || data.length === 0) return;

    const legendH = 28; // space reserved at the bottom for the color legend
    const innerW = chartWidth;
    const innerH = chartHeight - legendH;
    if (innerW <= 0 || innerH <= 0) return;

    // Only modules with a size can get an area in the treemap
    const rows = data.filter((d) => d.loc > 0);
    if (rows.length === 0) return;

    // treemap(root) returns the same tree but typed as HierarchyRectangularNode,
    // which is the type that actually carries x0/y0/x1/y1 on each node.
    const treemapLayout = d3.treemap<{ children?: ModuleHotspot[] }>().size([innerW, innerH]).paddingInner(2).round(true);
    const root = treemapLayout(
      d3
        .hierarchy<{ children?: ModuleHotspot[] }>({ children: rows })
        .sum((d) => (d as unknown as ModuleHotspot).loc ?? 0)
        .sort((a, b) => (b.value ?? 0) - (a.value ?? 0)),
    );

    // Sequential color for change frequency. sqrt because the values are heavily
    // skewed (a few very hot modules, many with 0) - linear would flatten everything.
    const maxCF = d3.max(rows, (d) => d.changeFrequency) ?? 0;
    const color = d3.scaleSequentialSqrt(d3.interpolateRgbBasis(BLUE_RAMP)).domain([0, maxCF || 1]);

    const leaves = root.leaves();
    const cells = svg
      .append('g')
      .selectAll('g')
      .data(leaves)
      .join('g')
      .attr('transform', (d) => `translate(${d.x0},${d.y0})`);

    cells
      .append('rect')
      .attr('width', (d) => Math.max(0, d.x1 - d.x0))
      .attr('height', (d) => Math.max(0, d.y1 - d.y0))
      .attr('rx', 2)
      .attr('fill', (d) => color((d.data as unknown as ModuleHotspot).changeFrequency))
      .style('cursor', 'pointer')
      .on('mouseenter', (event: MouseEvent, d) => {
        const [mx, my] = d3.pointer(event, props.chartContainerRef.current);
        setTooltip({ x: mx, y: my, point: d.data as unknown as ModuleHotspot });
      })
      .on('mouseleave', () => setTooltip(null));

    // Selective direct labels: only where the cell is big enough to hold text.
    // Ink color flips on dark fills so the label stays readable.
    cells
      .filter((d) => d.x1 - d.x0 > 70 && d.y1 - d.y0 > 22)
      .append('text')
      .attr('x', 5)
      .attr('y', 14)
      .attr('font-size', 10)
      .attr('fill', (d) => (d3.lab(color((d.data as unknown as ModuleHotspot).changeFrequency)).l < 60 ? SURFACE : INK_PRIMARY))
      // show only the last path segment, the full path is in the tooltip
      .text((d) => {
        const m = (d.data as unknown as ModuleHotspot).module;
        return m.split('/').filter(Boolean).pop() ?? m;
      });

    // Color legend - color alone must never be the only way to read the scale
    const legend = svg.append('g').attr('transform', `translate(0,${innerH + 8})`);
    const gradientId = 'hotspot-gradient';
    const stops = d3.range(0, 1.01, 0.1);
    const defs = svg.append('defs');
    const grad = defs.append('linearGradient').attr('id', gradientId);
    stops.forEach((s) =>
      grad
        .append('stop')
        .attr('offset', `${s * 100}%`)
        .attr('stop-color', color(s * maxCF)),
    );
    legend.append('rect').attr('width', 120).attr('height', 8).attr('rx', 2).attr('fill', `url(#${gradientId})`);
    legend.append('text').attr('x', 0).attr('y', 20).attr('font-size', 9).attr('fill', INK_MUTED).text('0');
    legend
      .append('text')
      .attr('x', 120)
      .attr('y', 20)
      .attr('text-anchor', 'end')
      .attr('font-size', 9)
      .attr('fill', INK_MUTED)
      .text(String(maxCF));
    legend.append('text').attr('x', 130).attr('y', 8).attr('font-size', 9).attr('fill', INK_MUTED).text('changes (area = LOC)');
  }, [data, chartWidth, chartHeight]);

  return (
    <div className={'w-full h-full flex justify-center items-center relative'} ref={props.chartContainerRef}>
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
          const openLeft = tooltip.x > chartWidth / 2;
          const openUp = tooltip.y > chartHeight / 2;
          return (
            <div
              className="absolute z-10 pointer-events-none bg-base-100 border border-base-300 rounded shadow p-2 text-xs"
              style={{
                left: tooltip.x + (openLeft ? -12 : 12),
                top: tooltip.y + (openUp ? -12 : 12),
                transform: `translate(${openLeft ? '-100%' : '0'}, ${openUp ? '-100%' : '0'})`,
                maxWidth: 280,
              }}>
              <div className="font-bold mb-1 break-all">{tooltip.point.module}</div>
              <div>LOC: {tooltip.point.loc}</div>
              <div>Changes: {tooltip.point.changeFrequency}</div>
            </div>
          );
        })()}
    </div>
  );
}
export default Chart;
