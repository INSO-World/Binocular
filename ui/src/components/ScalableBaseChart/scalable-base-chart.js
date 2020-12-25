'use strict';

import _ from 'lodash';
import React from 'react';
import * as d3 from 'd3';
import { formatDate } from '../../utils/date';

/**
 * Stacked area chart
 * Takes the following props:
 *  - content (Format: [{date: timestamp(ms), seriesName1: number, seriesName2, number, ...}, ...],
 *             e.g. array of data points with date and series values)
 *  - palette (Format: {seriesName1: color1, seriesName2: color2, ...}, color in format "#ffffff" as string)
 *  - paddings (optional) (Format: {top: number, left: number, right: number, bottom: number},
 *             number being amount of pixels) Each field in the object is optional and can be left out)
 *  - xAxisCenter (optional) (Format: true/false,
 *             whether the x axis should be at the 0 line (true), or at the bottom (false/unspecified))
 *  - yDims (Format: [topValue, bottomValue],
 *             limits of the y-Axis on top/bottom, should correspond to data.)
 *  - d3offset (Format: d3.stackOffsetNone/d3.stackOffsetDiverging/d3.stackOffsetSilhouette/...
 *             determines the way data is stacked, check d3 docs)
 *  - keys (optional) (Format: [seriesName1, seriesName2, ...])
 *             Filters the chart, only showing the provided keys and leaving everything else out.
 *  - resolution (Format: 'years'/'months'/'weeks'/'days') Needed for date format in tooltips.
 *  - displayNegative (optional) (Format: true/false) Display negative numbers on y-scale.
 *  - order (optional) (Format: [string, string, ...]) Strings containing the keys in desired order (largest to smallest).
 */
export default class ScalableBaseChart extends React.Component {
  constructor(
    props,
    styles = {
      chartDiv: false,
      chartSvg: false
    }
  ) {
    super(props);

    if (!styles.chartDiv || !styles.chartSvg || !styles.tooltip) {
      throw new Error('The provided styles are invalid!');
    }

    this.styles = Object.freeze(styles);

    this.state = {
      content: props.content, //[{name: "dev1", color: "#ffffff", checked: bool}, ...]
      palette: props.palette,
      componentMounted: false,
      zoomed: false,
      zoomedDims: [0, 0],
      zoomedVertical: false,
      verticalZoomDims: [0, 0],
      styles: props.styles || {}
    };
    window.addEventListener('resize', () => this.updateElement());
  }

  componentDidMount() {
    //Needed to restrict d3 to only access DOM when the component is already mounted
    this.setState({ componentMounted: true });
  }

  componentWillUnmount() {
    this.setState({ componentMounted: false });
  }

  /**
   *
   * @param scaleX
   * @param scaleY
   * @returns {*}
   */
  createAreaFunction(scaleX, scaleY) {
    //Area generator for the chart
    return d3
      .area()
      .x(function(d) {
        return scaleX(d.data.date);
      })
      .y0(function(d) {
        return scaleY(d[0]);
      })
      .y1(function(d) {
        return scaleY(d[1]);
      })
      .curve(d3.curveMonotoneX);
  }

  /**
   * Update the chart element. May only be called if this.props.content is not empty and the component is mounted.
   */
  updateElement() {
    //Initialization
    const data = this.props.content; //Rename for less writing
    const stackedData = this.calculateChartData(data, this.props.order); //Get d3-friendly data
    const yScale = this.props.yScale; //Multiplicator for the values on the y-scale
    const svg = d3.select(this.svgRef); //Select parent svg from render method
    const { width, height, paddings } = this.getDimsAndPaddings(svg); //Get width and height of svg in browser

    //Get X and Y scales, which translate data values into pixel values
    const { x, y } = this.createScales(this.getXDims(data), [paddings.left, width - paddings.right], this.props.yDims, [
      height - paddings.bottom,
      paddings.top
    ]);

    const area = this.createAreaFunction(x, y);

    //Brush generator for brush-zoom functionality, with referenced callback-function
    const brush = d3.brushX().extent([[paddings.left, 0], [width - paddings.right, height]]);

    //Remove old data
    svg.selectAll('*').remove();

    //Draw the chart (and brush box) using everything provided
    const { brushArea, xAxis } = this.drawChart(svg, this.props.content, stackedData, area, brush, yScale, x, y, height, width, paddings);

    //Set callback for brush-zoom functionality
    brush.on('end', event => this.updateZoom(event.selection, x, xAxis, brush, brushArea, area));
    //Set callback to reset zoom on double-click
    svg.on('dblclick', () => this.resetZoom(x, stackedData, xAxis, brushArea, area));
  }

  /**
   *
   * @param data
   * @returns {[]}
   */
  getXDims(data) {
    return [d3.min(data, d => d.date), d3.max(data, d => d.date)];
  }

  /**
   *
   * @param x
   * @param stackedData
   * @param xAxis
   * @param brushArea
   * @param area
   */
  resetZoom(x, stackedData, xAxis, brushArea, area) {
    x.domain([stackedData[0][0].data.date, stackedData[0][stackedData[0].length - 1].data.date]);
    xAxis.call(d3.axisBottom(x));
    brushArea.selectAll('.layer').attr('d', area);
    this.setState({ zoomed: false });
  }

  /**
   * Calculate data for the chart.
   * @param data Chart data in the format [{date: timestamp(ms), series1: value, series2: value, series3: value, series4: value, ...}, ...]
   * @param order
   * @returns Stacked chart data for d3 functions
   */
  calculateChartData(data, order) {
    //Keys are the names of the developers, date is excluded
    let keys;

    if (this.props.keys) {
      keys = this.props.keys.length > 0 ? this.props.keys : Object.keys(data[0]).slice(1);
    } else keys = Object.keys(data[0]).slice(1);

    let orderedKeys = [];
    if (order) {
      _.each(order, orderElem => {
        if (keys.includes('(Additions) ' + orderElem) && keys.includes('(Deletions) ' + orderElem)) {
          orderedKeys.push('(Additions) ' + orderElem);
          orderedKeys.push('(Deletions) ' + orderElem);
        } else if (keys.includes(orderElem)) {
          orderedKeys.push(orderElem);
        }
      });
    } else {
      orderedKeys = keys;
    }

    //Stack function for a ThemeRiver chart, using the keys provided
    const stack = d3.stack().offset(this.props.d3offset).order(d3.stackOrderReverse).keys(orderedKeys);

    //Data formatted for d3
    return stack(data);
  }

  /**
   * Get dimensions and padding of element
   * @param svg Svg-Element to get dimensions and paddings of
   * @returns {width, height, {paddings: {top: *, left: *, bottom: *, right: *}, width: *, height: *}}
   *          Values self-explanatory. All values in pixels.
   */
  getDimsAndPaddings(svg) {
    const node = !svg || typeof svg.node !== 'function' ? { getBoundingClientRec: () => ({}) } : svg.node();
    const clientRect = node ? node.getBoundingClientRect() : {};
    const width = clientRect.width ? clientRect.width : 0;
    const height = clientRect.height ? clientRect.height : 0;
    const paddingLeft = this.props.paddings.left ? this.props.paddings.left : 0;
    const paddingBottom = this.props.paddings.bottom ? this.props.paddings.bottom : 0;
    const paddingTop = this.props.paddings.top ? this.props.paddings.top : 0;
    const paddingRight = this.props.paddings.right ? this.props.paddings.right : 0;

    return { width, height, paddings: { left: paddingLeft, bottom: paddingBottom, top: paddingTop, right: paddingRight } };
  }

  /**
   *
   * @param xDims Dimensions of x-data in format [timestamp, timestamp] (timestamp = .getTime(),
   *               e.g. timestamp in milliseconds)
   * @param xRange Range of x-data in format [xLeft, xRight] (values in pixels relative to parent,
   *               e.g. insert left padding/right padding to have spacing)
   * @param yDims Dimensions of y-data in format [lowestNumber, highestNumber] (number = e.g. max/min values of data)
   * @param yRange Range of y-data in format [yBottom, yTop] (values in pixels relative to parent,
   *               e.g. insert top padding/bottom padding to have spacing.
   *        CAUTION: y-pixels start at the top, so yBottom should be
   *               e.g. width-paddingBottom and yTop should be e.g. paddingTop)
   * @returns {{x: *, y: *}} d3 x and y scales. x scale as time scale, y scale as linear scale.
   */
  createScales(xDims, xRange, yDims, yRange) {
    const x = d3.scaleTime().domain(xDims).range(xRange);

    if (this.state.zoomed === true) {
      x.domain(this.state.zoomedDims);
    }

    //Y axis scaled with the maximum amount of change (half in each direction)
    const y = d3.scaleLinear().domain(yDims).range(yRange);

    if (this.state.zoomedVertical === true) {
      y.domain(this.state.verticalZoomDims);
    }

    return { x, y };
  }

  /**
   * Draw the chart onto the svg element.
   * @param svg element to draw on (e.g. d3.select(this.ref))
   * @param rawData raw, unstacked data, needed for tooltips
   * @param data stacked data from the calculateChartData function
   * @param area d3 area generator
   * @param brush d3 brush generator
   * @param yScale yScale factor from props (for applying formatting to the y axis)
   * @param x d3 x-scale from method createScales
   * @param y d3 y-scale from method createScales
   * @param height element height from getDimsAndPaddings method (required for axis formatting)
   * @param width element width from getDimsAndPaddings method (required for axis formatting)
   * @param paddings paddings of element from getDimsAndPaddings method ()
   * @returns {{brushArea: *, xAxis: *}} brushArea: Area that has all the contents appended to it,
   *               xAxis: d3 x-Axis for later transitioning (for zooming)
   */
  drawChart(svg, rawData, data, area, brush, yScale, x, y, height, width, paddings) {
    //Color palette with the form {author1: color1, ...}
    const palette = this.props.palette;
    const brushArea = svg.append('g');
    const resolution = this.props.resolution;

    const yAxis = brushArea.append('g').attr('transform', 'translate(' + paddings.left + ',0)').call(
      d3.axisLeft(y).tickFormat(d => {
        if (this.props.displayNegative) {
          return d;
        } else return Math.abs(d);
      })
    );

    const scrollEvent = this.createScrollEvent(svg, y, yAxis, brushArea, area);
    svg.on('wheel', scrollEvent);

    const tooltip = d3.select(this.tooltipRef);

    this.setBrushArea(brushArea, brush, data, palette, area, tooltip, svg, x, rawData, resolution, y);

    //Append visible x-axis on the bottom, with an offset so it's actually visible
    let xAxis;
    if (this.props.xAxisCenter) {
      xAxis = brushArea.append('g').attr('transform', 'translate(0,' + y(0) + ')').call(d3.axisBottom(x));
    } else {
      xAxis = brushArea.append('g').attr('transform', 'translate(0,' + (height - paddings.bottom) + ')').call(d3.axisBottom(x));
    }

    return { brushArea, xAxis };
  }

  /**
   *
   * @param brushArea
   * @param brush
   * @param data
   * @param palette
   * @param area
   * @param tooltip
   * @param svg
   * @param x
   * @param rawData
   * @param resolution
   * @param y
   */
  setBrushArea(brushArea, brush, data, palette, area, tooltip, svg, x, rawData, resolution, y) {
    brushArea.append('g').attr('class', 'brush').call(brush);

    const bisectDate = d3.bisector(d => d.date).left;
    const _this = this;

    //Append data to svg using the area generator and palette
    brushArea
      .selectAll()
      .data(data)
      .enter()
      .append('path')
      .attr('class', 'layer')
      .attr('id', d => d.key)
      .style('fill', d => palette[d.key])
      .attr('d', area)
      .attr('clip-path', 'url(#clip)')
      .on('mouseover', () => tooltip.style('display', 'inline'))
      .on('mouseout', () => {
        tooltip.style('display', 'none');
        brushArea.select('.' + this.styles.indicatorLine).remove();
        brushArea.selectAll('.' + this.styles.indicatorCircle).remove();
      })
      .on('mousemove', function(event) {
        if (event === undefined || event === null) {
          return;
        }

        //Calculate values and text for tooltip
        const node = svg.node();
        const pointer = d3.pointer(event, node);
        const mouseoverDate = x.invert(pointer[0]);
        _this.createdTooltipNode(
          this,
          bisectDate,
          rawData,
          mouseoverDate,
          data,
          resolution,
          tooltip,
          palette,
          event,
          node,
          brushArea,
          x,
          y
        );
      });
  }

  /**
   *
   * @param path
   * @param bisectDate
   * @param rawData
   * @param mouseoverDate
   * @param data
   * @param resolution
   * @param tooltip
   * @param palette
   * @param event
   * @param node
   * @param brushArea
   * @param x
   * @param y
   */
  createdTooltipNode(path, bisectDate, rawData, mouseoverDate, data, resolution, tooltip, palette, event, node, brushArea, x, y) {
    const nearestDateIndex = bisectDate(rawData, mouseoverDate);
    const candidate1 = rawData[nearestDateIndex];
    const candidate2 = rawData[nearestDateIndex - 1];
    let nearestDataPoint;
    if (Math.abs(mouseoverDate - candidate1.date) < Math.abs(mouseoverDate - candidate2.date)) {
      nearestDataPoint = candidate1;
    } else {
      nearestDataPoint = candidate2;
    }
    const key = d3.select(path).attr('id');
    const text = key.split(' <', 1); //Remove git signature email
    let value = nearestDataPoint[key];
    const chartValues = this.findChartValues(data, key, nearestDataPoint.date);
    const formattedDate = formatDate(new Date(nearestDataPoint.date), resolution);
    if (value < 0) {
      value *= -1;
    }

    //Render tooltip
    tooltip
      .html(formattedDate + '<hr/>' + '<div style="background: ' + palette[key] + '">' + '</div>' + text + ': ' + Math.round(value))
      .style('position', 'absolute')
      .style('left', event.layerX - 20 + 'px')
      .style('top', event.layerY + (node.getBoundingClientRect() || { y: 0 }).y - 70 + 'px');
    brushArea.select('.' + this.styles.indicatorLine).remove();
    brushArea.selectAll('.' + this.styles.indicatorCircle).remove();
    brushArea
      .append('line')
      .attr('class', this.styles.indicatorLine)
      .attr('x1', x(nearestDataPoint.date))
      .attr('x2', x(nearestDataPoint.date))
      .attr('y1', y(chartValues.y1))
      .attr('y2', y(chartValues.y2))
      .attr('clip-path', 'url(#clip)');

    brushArea
      .append('circle')
      .attr('class', this.styles.indicatorCircle)
      .attr('cx', x(nearestDataPoint.date))
      .attr('cy', y(chartValues.y2))
      .attr('r', 5)
      .attr('clip-path', 'url(#clip)')
      .style('fill', palette[key]);

    brushArea
      .append('circle')
      .attr('class', this.styles.indicatorCircle)
      .attr('cx', x(nearestDataPoint.date))
      .attr('cy', y(chartValues.y1))
      .attr('r', 5)
      .attr('clip-path', 'url(#clip)')
      .style('fill', palette[key]);
  }

  /**
   * Finds the chart values (for the displayed line) for the moused-over data-point
   * @param data
   * @param key
   * @param timeValue
   * @returns {{y1: *, y2: *}}
   */
  findChartValues(data, key, timeValue) {
    let foundValues = [];
    _.each(data, series => {
      if (series.key === key) {
        _.each(series, dataPoint => {
          if (dataPoint.data.date === timeValue) {
            foundValues = dataPoint;
            return false;
          }
        });
        return false;
      }
    });
    return { y1: foundValues[0], y2: foundValues[1] };
  }

  /**
   *
   * @param svg
   * @param y
   * @param yAxis
   * @param brushArea
   * @param area
   * @returns {function(*): (undefined)}
   */
  createScrollEvent(svg, y, yAxis, brushArea, area) {
    return event => {
      const direction = event.deltaY > 0 ? 'down' : 'up';
      let zoomedDims = [...this.props.yDims];
      let top = zoomedDims[1],
        bottom = zoomedDims[0];
      if (this.props.keys && this.props.keys.length === 0) {
        //If everything is filtered, do nothing
        return;
      }

      if (this.state.zoomedVertical) {
        top = this.state.verticalZoomDims[1];
        bottom = this.state.verticalZoomDims[0];
        if (direction === 'up' && top / 2 > 1 && (bottom / 2 < -1 || bottom === 0)) {
          //Zoom limit
          this.updateVerticalZoom([bottom / 2, top / 2], y, yAxis, brushArea, area);
        } else if (direction === 'down') {
          if (top * 2 > zoomedDims[1] && (bottom * 2 < zoomedDims[0] || bottom === 0)) {
            this.resetVerticalZoom(y, yAxis, brushArea, area);
          } else {
            this.updateVerticalZoom([bottom * 2, top * 2], y, yAxis, brushArea, area);
          }
        }
        return;
      }

      if (direction === 'up') {
        if (bottom === 0) {
          zoomedDims = [bottom, top / 2];
        } else if (top > Math.abs(bottom)) {
          zoomedDims = [top / -1, top];
        } else if (Math.abs(bottom) >= top) {
          zoomedDims = [bottom, Math.abs(bottom)];
        }
        this.updateVerticalZoom(zoomedDims, y, yAxis, brushArea, area);
      }
    };
  }

  /**
   * Update the vertical zoom (mouse wheel zoom) with new values
   * @param dims Y-Dimensions for new zoom level
   * @param y Y-Scale from d3
   * @param yAxis Y-Axis from d3
   * @param area Area that the paths are drawn on
   * @param areaGenerator Area generator for those paths
   */
  updateVerticalZoom(dims, y, yAxis, area, areaGenerator) {
    y.domain(dims);

    yAxis.call(d3.axisLeft(y));
    area.selectAll('.layer').attr('d', areaGenerator);
    this.setState({ zoomedVertical: true, verticalZoomDims: dims });
  }

  /**
   * Reset the vertical zoom to default values.
   * @param y Y-Scale from d3
   * @param yAxis Y-Axis from d3
   * @param area Area that the paths are drawn on
   * @param areaGenerator Area generator for those paths
   */
  resetVerticalZoom(y, yAxis, area, areaGenerator) {
    y.domain(this.props.yDims);

    yAxis.call(d3.axisLeft(y));
    area.selectAll('.layer').attr('d', areaGenerator);

    this.setState({ zoomedVertical: false, verticalZoomDims: [0, 0] });
  }

  /**
   * Callback function for brush-zoom functionality. Should be called when brush ends. (.on("end"...)
   * @param extent Call event.selection inside an anonymous/arrow function, put that anonymous/arrow function as the .on callback method
   * @param x d3 x Scale provided by createScales function
   * @param xAxis d3 x-Axis provided by drawChart function
   * @param brush brush generator
   * @param brushArea Area that the path, x/y-Axis and brush-functionality live on (see drawChart)
   * @param area d3 Area generator (for area graphs)
   */
  updateZoom(extent, x, xAxis, brush, brushArea, area) {
    let zoomedDims;
    if (extent) {
      zoomedDims = [x.invert(extent[0]), x.invert(extent[1])];
      x.domain(zoomedDims);
      brushArea.select('.brush').call(brush.move, null);
    } else {
      return;
    }

    xAxis.call(d3.axisBottom(x));
    brushArea.selectAll('.layer').attr('d', area);
    this.setState({ zoomed: true, zoomedDims: zoomedDims });
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
        <svg className={this.styles.chartSvg} ref={svg => (this.svgRef = svg)} />
        <div className={this.styles.tooltip} ref={div => (this.tooltipRef = div)} />
      </div>
    );
  }
}
