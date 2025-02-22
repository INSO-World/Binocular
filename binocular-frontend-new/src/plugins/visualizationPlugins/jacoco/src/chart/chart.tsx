import { RefObject, useEffect, useState } from 'react';
import { throttle } from 'throttle-debounce';
import { SunburstChart } from './sunburstChart.tsx';
import { DataPlugin } from '../../../../interfaces/dataPlugin.ts';
import { ParametersType } from '../../../../../types/parameters/parametersType.ts';
import { Store } from '@reduxjs/toolkit';
import { SettingsType } from '../settings/settings.tsx';
import { useDispatch, useSelector } from 'react-redux';
import { setDateRange, setSelectedReport } from '../reducer';
import { formatDate } from '../utilities/utilities.ts';
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
  const jacocoReportData = useSelector((state: RootState) => state.jacocoReportData);
  const selectedReport = useSelector((state: RootState) => state.selectedReport);
  const isLoading = useSelector((state: RootState) => state.isLoading);
  const error = useSelector((state: RootState) => state.error);

  // React Component State
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

  // Resize Observer -> necessary for dynamically refreshing d3 chart
  useEffect(() => {
    if (!props.chartContainerRef.current) return;
    const resizeObserver = new ResizeObserver(() => {
      throttledResize();
    });
    resizeObserver.observe(props.chartContainerRef.current);
    return () => resizeObserver.disconnect();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [props.chartContainerRef, chartHeight, chartWidth]);

  // Convert Jacoco Report Data to Sunburst Data when Jacoco Report Data changes
  useEffect(() => {
    let chartData: SunburstData;
    if (selectedReport === 'first') {
      chartData = convertJacocoReportDataToSunburstChartData(jacocoReportData[1]);
    } else {
      chartData = convertJacocoReportDataToSunburstChartData(jacocoReportData[0]);
    }
    setChartData(chartData);
  }, [jacocoReportData, props.parameters]);

  // Set Global state when parameters change. This will also conclude in a refresh of the data.
  useEffect(() => {
    dispatch(setDateRange(props.parameters.parametersDateRange));
    dispatch(setSelectedReport(props.settings.selectedReport));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [props.parameters]);

  // Trigger Refresh when dataConnection changes (e.g., when the user changes the database from binocular to another)
  useEffect(() => {
    dispatch({
      type: 'REFRESH',
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [props.dataConnection]);

  return (
    <>
      <div className={'w-full h-full'} ref={props.chartContainerRef}>
        <span
          className="w-full h-full"
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            pointerEvents: 'none',
            zIndex: 1000,
          }}>
          {!isLoading && !error && jacocoReportData && (
            <div>
              Report from:{' '}
              {formatDate(props.settings.selectedReport === 'first' ? jacocoReportData[1].created_at : jacocoReportData[0].created_at)}
            </div>
          )}
          {isLoading && <div>Loading data...</div>}
          {error && <div>Error: {error}</div>}
        </span>
        {!isLoading && !error && <SunburstChart width={chartWidth} height={chartHeight} data={chartData} />}
      </div>
    </>
  );
}

export default Chart;
