import * as d3 from 'd3';
import { useEffect, useRef } from 'react';
import { data as data, SunburstData } from './jacocoSunburstData.ts';
import { HierarchyRectangularNode } from 'd3';

type SunburstChartProps = {
  width: number;
  height: number;
};

export const SunburstChart = ({ width, height }: SunburstChartProps) => {
  const d3Container = useRef(null);

  useEffect(() => {
    if (d3Container.current && data.children?.length) {
      // Specify the chart’s colors and approximate radius (it will be adjusted at the end).
      const color = d3.scaleOrdinal(d3.quantize(d3.interpolateRainbow, data.children.length + 1));
      const radius = width / 6;

      // Compute the layout (sort the data). https://d3js.org/d3-hierarchy/hierarchy
      const hierarchy = d3
        .hierarchy(data)
        .sum((d) => {
          const counters = d.counters?.[0];
          return (
            (counters?.INSTRUCTION?.covered || 0) +
            (counters?.INSTRUCTION?.missed || 0) +
            (counters?.LINE?.covered || 0) +
            (counters?.LINE?.missed || 0) +
            (counters?.COMPLEXITY?.covered || 0) +
            (counters?.COMPLEXITY?.missed || 0) +
            (counters?.METHOD?.covered || 0) +
            (counters?.METHOD?.missed || 0) +
            (counters?.CLASS?.covered || 0) +
            (counters?.CLASS?.missed || 0)
          );
        })
        .sort((a, b) => (b.value as number) - (a.value as number));
      // root node
      const root = d3.partition<SunburstData>().size([2 * Math.PI, hierarchy.height + 1])(hierarchy);
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      root.each((d) => (d.current = d));

      // Create the arc generator.
      const arc = d3
        .arc<{ x0: number; y0: number; x1: number; y1: number }>()
        .startAngle((d) => d.x0)
        .endAngle((d) => d.x1)
        .padAngle((d) => Math.min((d.x1 - d.x0) / 2, 0.005))
        .padRadius(radius * 1.5)
        .innerRadius((d) => d.y0 * radius)
        .outerRadius((d) => Math.max(d.y0 * radius, d.y1 * radius - 1));

      // Create the SVG container.
      const svg = d3
        .select(d3Container.current)
        .attr('viewBox', [-height / 2, -width / 2, height, width])
        .style('font', '30px sans-serif');
      svg.selectAll('*').remove();

      // Append the arcs.
      const path = svg
        .append('g')
        .selectAll('path')
        .data(root.descendants().slice(1))
        .join('path')
        .attr('fill', (d) => {
          while (d.depth > 1) {
            if (d.parent !== null) {
              d = d.parent;
            }
          }
          return color(d.data.name);
        })
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-expect-error
        .attr('fill-opacity', (d) => (arcVisible(d.current) ? (d.children ? 0.6 : 0.4) : 0))
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-expect-error
        .attr('pointer-events', (d) => (arcVisible(d.current) ? 'auto' : 'none'))
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-expect-error
        .attr('d', (d) => arc(d.current));

      // Make them clickable if they have children.
      path
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-expect-error
        .filter((d) => d.children)
        .style('cursor', 'pointer')
        .on('click', (e, p) => clicked(e, p, parent, svg, root, arc, path, label, radius));

      // on hover display coverage data
      const format = d3.format(',d');
      path.append('title').text((d) => {
        const aggregatedCounters = calculateAggregatedCounters(d);
        return `${d
          .ancestors()
          .map((d) => d.data.name)
          .reverse()
          .join('/')}
            \nINSTRUCTION: ${format(aggregatedCounters.INSTRUCTION?.covered ?? 0)} covered, ${format(aggregatedCounters.INSTRUCTION?.missed ?? 0)} missed
            \nLINE: ${format(aggregatedCounters.LINE?.covered ?? 0)} covered, ${format(aggregatedCounters.LINE?.missed ?? 0)} missed
            \nCOMPLEXITY: ${format(aggregatedCounters.COMPLEXITY?.covered ?? 0)} covered, ${format(aggregatedCounters.COMPLEXITY?.missed ?? 0)} missed
            \nMETHOD: ${format(aggregatedCounters.METHOD?.covered ?? 0)} covered, ${format(aggregatedCounters.METHOD?.missed ?? 0)} missed
            \nCLASS: ${format(aggregatedCounters.CLASS?.covered ?? 0)} covered, ${format(aggregatedCounters.CLASS?.missed ?? 0)} missed;
          `;
      });

      const label = svg
        .append('g')
        .attr('pointer-events', 'none')
        .attr('text-anchor', 'middle')
        .style('user-select', 'none')
        .selectAll('text')
        .data(root.descendants().slice(1))
        .join('text')
        .attr('dy', '0.35em')
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-expect-error
        .attr('fill-opacity', (d) => +labelVisible(d.current))
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-expect-error
        .attr('transform', (d) => labelTransform(d.current, radius))
        .text((d) => d.data.name);

      const parent: d3.Selection<SVGCircleElement, d3.HierarchyRectangularNode<SunburstData>, HTMLElement, unknown> = svg
        .append('circle')
        .datum(root)
        .attr('r', radius)
        .attr('fill', 'none')
        .attr('pointer-events', 'all')
        .on('click', (e, p) => clicked(e, p, parent, svg, root, arc, path, label, radius));
    }
  }, [d3Container.current]);

  return (
    <div>
      <svg ref={d3Container} width={width} height={height}></svg>
    </div>
  );
};

function calculateAggregatedCounters(node: HierarchyRectangularNode<SunburstData>) {
  const aggregatedCounters = {
    INSTRUCTION: { covered: 0, missed: 0 },
    LINE: { covered: 0, missed: 0 },
    COMPLEXITY: { covered: 0, missed: 0 },
    METHOD: { covered: 0, missed: 0 },
    CLASS: { covered: 0, missed: 0 },
  };

  // Add current node's counters
  if (node.data.children) {
    for (const key in aggregatedCounters) {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      if (node.data.counters?.[0][key]) {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-expect-error
        aggregatedCounters[key].covered += node.counters[0][key].covered || 0;
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-expect-error
        aggregatedCounters[key].missed += node.counters[0][key].missed || 0;
      }
    }
  } else {
    for (const key in aggregatedCounters) {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      aggregatedCounters[key].covered += node.data.counters[0][key].covered || 0;
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      aggregatedCounters[key].missed += node.data.counters[0][key].missed || 0;
    }
  }

  // Recurse through children
  if (node.children) {
    for (const child of node.children) {
      const childCounters = calculateAggregatedCounters(child);
      for (const key in aggregatedCounters) {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-expect-error
        aggregatedCounters[key].covered += childCounters[key].covered;
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-expect-error
        aggregatedCounters[key].missed += childCounters[key].missed;
      }
    }
  }

  return aggregatedCounters;
}

// Handle zoom on click.
function clicked(
  event: MouseEvent,
  p: d3.HierarchyRectangularNode<SunburstData>,
  parent: d3.Selection<SVGCircleElement, d3.HierarchyRectangularNode<SunburstData>, HTMLElement, unknown>,
  svg: d3.Selection<d3.BaseType, unknown, HTMLElement, unknown>,
  root: d3.HierarchyRectangularNode<SunburstData>,
  arc: d3.Arc<unknown, { x0: number; y0: number; x1: number; y1: number }>,
  path: d3.Selection<d3.BaseType | SVGPathElement, d3.HierarchyRectangularNode<SunburstData>, SVGGElement, unknown>,
  label: d3.Selection<d3.BaseType | SVGTextElement, d3.HierarchyRectangularNode<SunburstData>, SVGGElement, unknown>,
  radius: number,
) {
  event.stopPropagation();
  parent.datum(p.parent || root);

  root.each(
    (d) =>
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      (d.target = {
        x0: Math.max(0, Math.min(1, (d.x0 - p.x0) / (p.x1 - p.x0))) * 2 * Math.PI,
        x1: Math.max(0, Math.min(1, (d.x1 - p.x0) / (p.x1 - p.x0))) * 2 * Math.PI,
        y0: Math.max(0, d.y0 - p.depth),
        y1: Math.max(0, d.y1 - p.depth),
      }),
  );

  const t = svg.transition().duration(750);

  // Transition the data on all arcs, even the ones that aren’t visible,
  // so that if this transition is interrupted, entering arcs will start
  // the next transition from the desired position.
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  path
    .transition(t)
    .tween('data', (d) => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      const i = d3.interpolate(d.current, d.target);
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      return (t) => (d.current = i(t));
    })
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    .filter(function (d) {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      return +this.getAttribute('fill-opacity') || arcVisible(d.target);
    })
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    .attr('fill-opacity', (d) => (arcVisible(d.target) ? (d.children ? 0.6 : 0.4) : 0))
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    .attr('pointer-events', (d) => (arcVisible(d.target) ? 'auto' : 'none'))
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    .attrTween('d', (d) => () => arc(d.current));

  label
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    .filter(function (d) {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      return +this.getAttribute('fill-opacity') || labelVisible(d.target);
    })
    .transition(t)
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    .attr('fill-opacity', (d) => +labelVisible(d.target))
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    .attrTween('transform', (d) => () => labelTransform(d.current, radius));
}

function arcVisible(d: { x0: number; y0: number; x1: number; y1: number }) {
  return d.y1 <= 3 && d.y0 >= 1 && d.x1 > d.x0;
}

function labelVisible(d: { x0: number; y0: number; x1: number; y1: number }) {
  return d.y1 <= 3 && d.y0 >= 1 && (d.y1 - d.y0) * (d.x1 - d.x0) > 0.03;
}

function labelTransform(d: { x0: number; y0: number; x1: number; y1: number }, radius: number) {
  const x = (((d.x0 + d.x1) / 2) * 180) / Math.PI;
  const y = ((d.y0 + d.y1) / 2) * radius;
  return `rotate(${x - 90}) translate(${y},0) rotate(${x < 180 ? 0 : 180})`;
}
