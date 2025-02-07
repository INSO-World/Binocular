'use strict';

import * as d3 from 'd3';
import * as React from 'react';
import * as baseStyles from './verticalBarChart.module.scss';
import { ReactDOM } from 'react';
import ReactDOMServer from 'react-dom/server';
import {color} from "d3";

interface Props {
  content: any[];
  changeCommit: (commit: any) => void;
  // FROM,TO Date ????
}

interface State {
  content: any;
  componentMounted: boolean;
  // Maybe put key and value also here ...
}

// Inspired by scalable base chart but heavily customized
export default class ZoomableVerticalBarchartCommiters extends React.Component<Props, State> {
  protected styles: any;
  private svgRef: SVGSVGElement | null | undefined;
  private tooltipRef: HTMLDivElement | null | undefined;
  private key_str: string;
  private value_str: string;
  private changeCommit: (commit: any) => void;
  constructor(props: Props | Readonly<Props>, styles: any) {
    super(props);
    this.key_str = 'signature';
    this.value_str = 'bugfixes_count';

    this.styles = Object.freeze(Object.assign({}, baseStyles, styles));

    this.state = {
      content: props.content,
      componentMounted: false,
    };
    console.log('changeCommit Prop', props.changeCommit);
    this.changeCommit = props.changeCommit;
    window.addEventListener('resize', () => this.updateElement());
  }

  componentDidMount() {
    //Needed to restrict d3 to only access DOM when the component is already mounted
    this.setState({ componentMounted: true });
  }

  componentWillUnmount() {
    this.setState({ componentMounted: false });
  }

  //Draw chart after it updated
  componentDidUpdate() {
    //Only update the chart if there is data for it and the component is mounted.
    //it is not currently in a zoom transition (d3 requirement)
    if (this.state.componentMounted && this.props.content) {
      this.updateElement();
    }
  }

  render() {
    return (
      <div className={this.styles.chartDiv}>
        <svg className={this.styles.chartSvg} ref={(svg) => (this.svgRef = svg)} />
        <div
          className={this.styles.tooltip}
          ref={(div) => (this.tooltipRef = div)}
          onClick={(e) => {
            e.preventDefault();
            if (e.target['attributes']['data-sha'] !== undefined) {
              this.changeCommit(e.target['attributes']['data-sha']['value']);
            }
          }}
          id="tooltip"
        />
      </div>
    );
  }

  // TODO: Even for days and months with no datapoints there needs to be data (at least empty data)...
  tooltipFunc(bars, changeCommit) {
    // TODO: Use tooltipRef
    // Select tooltip
    const tooltip = d3.select('#tooltip');
    console.log('tooltip', tooltip);
    let isInTooltip = false; // indicates if the pointer is in the tooltip

    // TODO: After you structure data in tooltip, make tooltip stay on top of the bar for a while and then disappear

    let tooltipTimeout;
    bars
      .on('mouseover', (event, data) => {
        const bar = event.target.getBoundingClientRect();
        console.log('mouseover bar', bar); // The hovered over bar

        console.log('mouseover', data);
        tooltip.transition().duration(300).style('opacity', 0.9);
        let htmlTooltip = `<b>Commiter: ${data['signature']}</b>`;
        for (const c of data['commits']) {
          const dateFormatted = new Intl.DateTimeFormat('en-GB', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(
            new Date(c.date),
          ); // TODO: use more universal formatting based on locale or use month as word ?
          // eslint-disable-next-line max-len
          htmlTooltip += `<table border="0"><tr><th style="color: white;">${dateFormatted}</th><th></th></tr><tr><th style="color: white;"><a data-sha="${c.sha}">${c.shortSha}</a></th><th style="color: white;">${c.messageHeader}</th></tr><tr><th></th><th></th></tr></table><br>`;
        }
        const yCoordinate = bar.y - 250 < 0 ? 0 : bar.y - 250; // Prevent overflow of tooltip
        console.log('xCoordinate', event.layerX + 300 + 512);
        console.log('xCoordinate max', window.innerWidth);
        // eslint-disable-next-line max-len
        const xCoordinate = event.layerX + 300 + 512 > window.innerWidth ? event.layerX - 300 : event.layerX; // Prevent overflow of tooltip, 512 is width of the dashboard: TODO: Supply iwht of dasboard different way

        tooltip // TODO: add html the react way
          .html(htmlTooltip)
          .style('left', xCoordinate + 'px')
          .style('top', yCoordinate + 'px');
      })
      .on('mousemove', function (event) {
        // const bar = event.target.getBoundingClientRect();
        // tooltip
        //   .style('left', event.layerX + 'px')
        //   .style('top', bar.y - 250 + 'px');
        clearTimeout(tooltipTimeout);
      })
      .on('mouseout', function () {
        tooltipTimeout = setTimeout(function () {
          tooltip.transition().duration(200).style('opacity', 0);
        }, 750);
      });

    tooltip
      .on('mouseover', (event, data) => {
        isInTooltip = true;
        tooltip.transition().duration(300).style('opacity', 0.9);
        clearTimeout(tooltipTimeout);
      })
      .on('mouseout', function (event) {
        tooltipTimeout = setTimeout(function () {
          tooltip.transition().duration(200).style('opacity', 0);
        }, 750);
        isInTooltip = false;
      });
  }

  updateElement() {
    d3.select(this.svgRef).selectAll('*').remove(); // Delete old graph
    const key_str = 'signature';
    const value_str = 'bugfixes_count';

    // Basic chart dimensions
    const width = 945;
    const height = 525;
    const marginTop = 10;
    const marginRight = 10;
    const marginBottom = 10;
    const marginLeft = 50;

    // Specify different comparators for different granularity
    const dataYear = this.state.content;
    const yearComparator = (d) => d[value_str];
    // Specifying x-axis
    const x = d3
      .scaleBand()
      .range([marginLeft, width - marginRight])
      .padding(0.05); // Padding between bars

    x.domain(d3.sort(dataYear, yearComparator).map(d => d[key_str]));

    const xAxis = d3.axisBottom(x).tickSizeOuter(0);

    // Specifying y-axis
    const y = d3
      .scaleLinear()
      .domain([0, d3.max(dataYear, d => d[value_str])])
      .nice()
      .range([height - marginBottom, marginTop]);

    const yAxis = d3.axisLeft(y);

    // SVG Container with zoom function
    const svg = d3
      .select(this.svgRef)
      .attr('viewBox', [0, 0, width, height])
      .attr('width', width)
      .attr('height', height)
      .call(zoom);

    // Append both axes
    svg
      .append('g')
      .attr('class', 'x-axis')
      .attr('transform', `translate(0,${height - marginBottom})`)
      .call(xAxis);
    // Make the text on x-axis rotated
    svg.selectAll('.x-axis').selectAll('text').attr("transform", "translate(0,20)rotate(-35)");

    svg
      .append('g')
      .attr('class', 'y-axis')
      .attr('transform', `translate(${marginLeft},0)`)
      .call(yAxis)
      .call((g) => g.select('.domain').remove());

    const tooltipFunc = this.tooltipFunc;
    const changeCommit = this.changeCommit;
    // mode: 'year', 'month' or 'day'
    function updateBarsAndAxes(data, mode) {
      x.domain(d3.sort(data, yearComparator).map(d => d[key_str]));

      y.domain([0, d3.max(data, d => d[value_str])]).nice();

      // Transition y-axis using effects to different scaling
      svg
        .select('.y-axis')
        .transition()
        .call(yAxis)
        .call((g) => g.select('.domain').remove());
      // Append bars
      const bars = svg
        .append('g')
        .attr('class', 'bars')
        .attr('fill', 'grey')
        .selectAll('rect')
        .data(data)
        .join('rect')
        .attr('x', (d) => x(d[key_str]))
        .attr('y', (d) => y(d[value_str]))
        .attr('height', (d) => y(0) - y(d[value_str]))
        .attr('width', x.bandwidth());

      svg.selectAll('rect').attr('fill', (d) => {
        console.log(d);
        if (d['color'] !== undefined) {
          return d['color'];
        } else return 'grey';
      });

      tooltipFunc(bars, changeCommit);
    }

    function zoom(svg) {
      const extent = [
        [marginLeft, marginTop], // x0, y0
        [width - marginRight, height - marginTop], // x1, y1
      ];

      svg.call(d3.zoom().scaleExtent([1, 50]).translateExtent(extent).extent(extent).on('zoom', zoomed));
      function zoomed(event) {
        const transform = event.transform;
        // TODO: Only do this once .... (e.g. by setting some state to ...)
        svg.selectAll('rect').remove(); // Remove old bars
        updateBarsAndAxes(dataYear, 'year');

        x.range([marginLeft, width - marginRight].map(d => event.transform.applyX(d)));
        svg
          .selectAll('.bars rect')
          .attr('x', (d) => x(d[key_str]))
          .attr('width', x.bandwidth()); // Reapply new bars (show only the ones we have zoomed into)
        svg.selectAll('.x-axis').call(xAxis); // Transform x-axis
      }
    }

    updateBarsAndAxes(dataYear, 'year'); // Appending the first bars
  }
}
