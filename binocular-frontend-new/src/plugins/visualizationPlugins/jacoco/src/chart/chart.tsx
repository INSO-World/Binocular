import { RefObject, useEffect, useState } from 'react';
import { throttle } from 'throttle-debounce';
import { SunburstChart } from './sunburstChart.tsx';
import { DataPlugin } from '../../../../interfaces/dataPlugin.ts';
import { ParametersType } from '../../../../../types/parameters/parametersType.ts';
import { Store } from '@reduxjs/toolkit';
import { SettingsType } from '../settings/settings.tsx';
import { useDispatch, useSelector } from 'react-redux';
import { setDateRange } from '../reducer';
import { DataPluginJacocoReport } from '../../../../interfaces/dataPluginInterfaces/dataPluginArtifacts.ts';
import { convertJacocoReportDataToSunburstChartData } from '../utilities/dataConverter.ts';

export interface Counter {
  missed?: number;
  covered?: number;
}

export interface Counters {
  INSTRUCTION?: Counter;
  LINE?: Counter;
  COMPLEXITY?: Counter;
  METHOD?: Counter;
  CLASS?: Counter;
}

export interface SunburstData {
  name: string;
  counters?: Counters[];
  children?: SunburstData[];
}

function Chart(props: {
  settings: SettingsType;
  dataConnection: DataPlugin;
  parameters: ParametersType;
  chartContainerRef: RefObject<HTMLDivElement>;
  store: Store;
}) {
  /*
   * Creating Dispatch and Root State for interaction with the reducer State
   */
  type RootState = ReturnType<typeof props.store.getState>;
  type AppDispatch = typeof props.store.dispatch;
  const useAppDispatch = () => useDispatch<AppDispatch>();
  const dispatch: AppDispatch = useAppDispatch();
  /*
   * -----------------------------
   */
  // Redux Global State
  const jacocoReportData: DataPluginJacocoReport = useSelector((state: RootState) => state.sunburstData);
  const isLoading: boolean = useSelector((state: RootState) => state.isLoading);
  const error: string = useSelector((state: RootState) => state.error);

  const [chartWidth, setChartWidth] = useState(100);
  const [chartHeight, setChartHeight] = useState(100);
  const [chartData, setChartData] = useState<SunburstData>({ name: '', children: [] });

  /*
  Throttle the resize of the svg (refresh rate) to every 1s to not overwhelm the renderer,
  This isn't really necessary for this visualization, but for bigger visualization this can be quite essential
   */
  const throttledResize = throttle(
    1000,
    () => {
      if (!props.chartContainerRef.current) return;
      if (props.chartContainerRef.current?.offsetWidth !== chartWidth) {
        setChartWidth(props.chartContainerRef.current.offsetWidth);
      }
      if (props.chartContainerRef.current?.offsetHeight !== chartHeight) {
        setChartHeight(props.chartContainerRef.current.offsetHeight);
      }
    },
    { noLeading: false, noTrailing: false },
  );

  //Resize Observer -> necessary for dynamically refreshing d3 chart
  useEffect(() => {
    if (!props.chartContainerRef.current) return;
    const resizeObserver = new ResizeObserver(() => {
      throttledResize();
    });
    resizeObserver.observe(props.chartContainerRef.current);
    return () => resizeObserver.disconnect();
  }, [props.chartContainerRef, chartHeight, chartWidth]);

  // Effect on data change
  useEffect(() => {
    const chartData: SunburstData = convertJacocoReportDataToSunburstChartData(jacocoReportData);
    setChartData(chartData);
  }, [jacocoReportData]);

  //Set Global state when parameters change. This will also conclude in a refresh of the data.
  //TODO when the time changes, get the new last report in the specific timeframe
  useEffect(() => {
    dispatch(setDateRange(props.parameters.parametersDateRange));
  }, [props.parameters]);

  //Trigger Refresh when dataConnection changes
  useEffect(() => {
    dispatch({
      type: 'REFRESH',
    });
  }, [props.dataConnection]);

  return (
    <>
      <div className="w-full h-full" ref={props.chartContainerRef}>
        {isLoading && <div>Loading data...</div>}
        {error && <div>Error: {error}</div>}
        {!isLoading && !error && <SunburstChart width={chartWidth} height={chartHeight} data={chartData} />}
      </div>
    </>
  );
}

export default Chart;
