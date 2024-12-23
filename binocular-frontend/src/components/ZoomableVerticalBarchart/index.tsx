'use strict';

import * as d3 from 'd3';
import * as React from 'react';
import * as baseStyles from './verticalBarChart.module.scss';

interface Props {
  content: any[];
  // FROM,TO Date ????
}

interface State {
  content: any;
  componentMounted: boolean;
  // Maybe put key and value also here ...
}

// Inspired by scalable base chart but heavily customized
export default class ZoomableVerticalBarchart extends React.Component<Props, State> {
  protected styles: any;
  private svgRef: SVGSVGElement | null | undefined;
  private tooltipRef: HTMLDivElement | null | undefined;
  private key_str: string;
  private value_str: string;
  private aggregatedDataByYear: any[];
  private aggregatedDataByYearMonth: any[];
  private aggregatedDataByYearMonthDay: any[];
  constructor(props: Props | Readonly<Props>, styles: any) {
    super(props);
    this.key_str = 'year';
    this.value_str = 'bugfixes_count';

    this.styles = Object.freeze(Object.assign({}, baseStyles, styles));

    this.state = {
      content: props.content,
      componentMounted: false,
    };
    window.addEventListener('resize', () => this.updateElement());

    const results = this.aggregateData();
    this.aggregatedDataByYear = results[0];
    this.aggregatedDataByYearMonth = results[1];
    this.aggregatedDataByYearMonthDay = results[2];
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
      </div>
    );
  }

  // Aggregate data based on year, year + month and based on year + month + day ... this allows for granularity when zooming in
  aggregateData() {
    let data = JSON.parse(JSON.stringify(this.state.content)); // Deep copy the contents
    data = data.map((d: any) => {
      return { date: new Date(d.date), bugfixes_count: d.bugfixes_count };
    });
    const value_str = this.value_str;
    // Preparing data: Array elements with value 0 if there is missing day... this is needed for working zoom

    // eslint-disable-next-line max-len
    const minDay: any = new Date().setFullYear(d3.min(data, (d) => new Date(d.date).getFullYear()) as number, 0, 1);
    // minDay is 1.1. of the min Year
    const maxDay: any = new Date().setFullYear(d3.max(data, (d) => new Date(d.date).getFullYear()) as number, 11, 31);
    // maxDay is 31.12. of the max Year
    console.log('Min ... ' + minDay);
    console.log('Max ... ' + maxDay);
    // TODO: Probably even create data for each day in all years ...
    // Create data for each possible day in all years that the dataset is in (So that zoom is uniform)
    const temp: any = new Date(minDay);
    console.log('First ... ' + temp);
    while (temp < maxDay) {
      data.push({
        date: new Date(`${temp.getFullYear()}-${temp.getMonth() + 1}-${temp.getUTCDate()}`),
        year: temp.getFullYear(),
        month: temp.getMonth(),
        day: temp.getUTCDate(),
        bugfixes_count: 0,
      });
      temp.setTime(temp.getTime() + 24 * 60 * 60 * 1000); // Add 24 hours
    }

    // 1. Group data by year (using d3.group in D3 v6+)
    const groupedDataYear = d3.group(data, (d) => d.date.getFullYear());

    const aggregatedDataYear = Array.from(groupedDataYear, ([year, values]) => ({
      aggregatedBy: year,
      total: d3.sum(values, (d) => d[value_str]),
    }));

    console.log(aggregatedDataYear);

    // 2. Group data by year + month
    const groupedDataYearMonth = d3.group(data, (d) => {
      return `${d.date.getFullYear()}-${d.date.getMonth() + 1}`;
    });

    const aggregatedDataYearMonth = Array.from(groupedDataYearMonth, ([yearMonth, values]) => ({
      aggregatedBy: yearMonth,
      total: d3.sum(values, (d) => d[value_str]),
    }));

    console.log(aggregatedDataYearMonth);

    // 3. Group data by year + month + day
    const groupedDataYearMonthDay = d3.group(data, (d) => {
      return `${d.date.getFullYear()}-${d.date.getMonth() + 1}-${d.date.getDate()}`;
    });

    const aggregatedDataYearMonthDay = Array.from(groupedDataYearMonthDay, ([yearMonthDay, values]) => ({
      aggregatedBy: yearMonthDay,
      total: d3.sum(values, (d) => d[value_str]),
    }));

    console.log(aggregatedDataYearMonthDay);

    return [aggregatedDataYear, aggregatedDataYearMonth, aggregatedDataYearMonthDay];
  }
  // TODO: Even for days and months with no datapoints there needs to be data (at least empty data)...

  updateElement() {
    const key_str = 'aggregatedBy';
    const value_str = 'total';

    // Basic chart dimensions
    const width = 945;
    const height = 525;
    const marginTop = 10;
    const marginRight = 10;
    const marginBottom = 10;
    const marginLeft = 50;

    // Specify different comparators for different granularity
    const dataYear = this.aggregatedDataByYear;
    const yearComparator = (d) => d[key_str];
    const dataMonth = this.aggregatedDataByYearMonth;
    const monthComparator = (d) => new Date(d[key_str] + '-1');
    const dataDay = this.aggregatedDataByYearMonthDay;
    const dayComaparator = (d) => new Date(d[key_str]);
    // Specifying x-axis
    const x = d3
      .scaleBand()
      .range([marginLeft, width - marginRight])
      .padding(0.05); // Padding between bars

    x.domain(d3.sort(dataYear, yearComparator).map(d => d[key_str])); // at first, set the x-axis to year

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

    svg
      .append('g')
      .attr('class', 'y-axis')
      .attr('transform', `translate(${marginLeft},0)`)
      .call(yAxis)
      .call((g) => g.select('.domain').remove());

    // mode: 'year', 'month' or 'day'
    function updateBarsAndAxes(data, mode) {
      if (mode === 'year') {
        x.domain(d3.sort(data, yearComparator).map(d => d[key_str]));
      } else if (mode === 'month') {
        x.domain(d3.sort(data, monthComparator).map(d => d[key_str]));
      } else {
        x.domain(d3.sort(data, dayComaparator).map(d => d[key_str]));
      }
      y.domain([0, d3.max(data, d => d[value_str])]).nice();

      // Transition y-axis using effects to different scaling
      svg
        .select('.y-axis')
        .transition()
        .call(yAxis)
        .call((g) => g.select('.domain').remove());
      // Append bars
      svg
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
    }

    function zoom(svg) {
      const extent = [
        [marginLeft, marginTop],  // x0, y0
        [width - marginRight, height - marginTop],  // x1, y1
      ];

      svg.call(d3.zoom().scaleExtent([1, 50]).translateExtent(extent).extent(extent).on('zoom', zoomed));
      let currentLevel = 'year';
      function zoomed(event) {
        const transform = event.transform;
        // Determine the zoom level
        if (transform.k > 200 && currentLevel !== 'day') {
          // Optimisation... change mode only once
          // TODO: For days, it is really really slow
          // TODO: Allow day view only with max 2 years of data (or hovewer many so that it is still quick)
          svg.selectAll('rect').remove(); // Remove old bars
          updateBarsAndAxes(dataDay, 'day'); // Set granularity of bars to month
          currentLevel = 'day';
          // TODO: Chose these values automatically for edge cases
        } else if (transform.k > 5 && currentLevel !== 'month') {
          // TODO: Allow this view only with max 10 years of data
          svg.selectAll('rect').remove(); // Remove old bars
          updateBarsAndAxes(dataMonth, 'month'); // Set granularity of bars to month
          currentLevel = 'month';
        } else if (transform.k <= 5 && currentLevel !== 'year') {
          // TODO: Only do this once .... (e.g. by setting some state to ...)
          svg.selectAll('rect').remove(); // Remove old bars
          updateBarsAndAxes(dataYear, 'year');
          currentLevel = 'year';
        }

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
