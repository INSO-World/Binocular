import { StackedAreaChart, type ChartData, type Palette } from '../../../../../components/stackedAreaChart/StackedAreaChart.tsx';
import { ColumnChart, type ColumnChartData } from '../../../../../components/columnChart/columnChart.tsx';
import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { DataState, getDataSlice } from '../reducer';
import type { DefaultSettings } from '../settings/settings.tsx';
import { handlePopoutResizing } from '../../../../utils/resizing.ts';
import type { VisualizationPluginProperties } from '../../../../interfaces/visualizationPluginInterfaces/visualizationPluginProperties.ts';

export type { ChartData, Palette };

function Chart<SettingsType extends DefaultSettings, DataType>(props: VisualizationPluginProperties<SettingsType, DataType>) {
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
  //Redux Global State
  const data = useSelector((state: RootState) => state.plugin.data);
  const dataState = useSelector((state: RootState) => state.plugin.dataState);
  //React Component State
  const [chartWidth, setChartWidth] = useState(100);
  const [chartHeight, setChartHeight] = useState(100);

  const isSumCommits = props.dataName?.toLowerCase() === 'sum commits';

  const [chartData, setChartData] = useState<ChartData[] | ColumnChartData[]>([]);
  const [chartScale, setChartScale] = useState<number[]>([]);
  const [chartPalette, setChartPalette] = useState<Palette>({});
  const [calculating, setCalculating] = useState(false);

  /**
   * RESIZE Logic START
   */
  function resize() {
    if (!props.chartContainerRef?.current) return;
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
  /**
   * RESIZE Logic END
   */

  // Effect on data change
  useEffect(() => {
    try {
      if (props.dataConverter) {
        setCalculating(true);
        const { chartData, scale, palette } = props.dataConverter(data, props);
        setChartData(chartData);
        setChartScale(scale);
        setChartPalette(palette);
        setCalculating(false);
      }
    } catch (e) {
      console.error(e);
    }
  }, [data, props.parameters.parametersGeneral, props.settings, props.authorList, props.fileList]);

  //Set Global state when parameters change. This will also conclude in a refresh of the data.
  useEffect(() => {
    dispatch(getDataSlice(props.dataName!).actions.setDateRange(props.parameters.parametersDateRange));
  }, [props.parameters.parametersDateRange]);

  //Trigger Refresh when dataConnection changes
  useEffect(() => {
    dispatch({
      type: 'REFRESH',
    });
  }, [props.dataConnection]);

  return (
    <>
      <div className={'w-full h-full flex justify-center items-center'} ref={props.chartContainerRef}>
        {dataState === DataState.EMPTY && <div>NoData</div>}
        {(dataState === DataState.FETCHING || calculating) && (
          <div>
            <span className="loading loading-spinner loading-lg text-accent"></span>
          </div>
        )}
        {dataState === DataState.COMPLETE &&
          !calculating &&
          (chartData.length !== 0 ? (
            isSumCommits ? (
              <ColumnChart
                data={chartData as ColumnChartData[]}
                scale={chartScale}
                palette={chartPalette}
                sprintList={props.sprintList}
                width={chartWidth}
                height={chartHeight}
                settings={props.settings}
              />
            ) : (
              <StackedAreaChart
                data={chartData as ChartData[]}
                scale={chartScale}
                palette={chartPalette}
                sprintList={props.sprintList}
                width={chartWidth}
                height={chartHeight}
                settings={props.settings}
              />
            )
          ) : (
            <div>No Data matching the selected Parameters!</div>
          ))}
      </div>
    </>
  );
}

export default Chart;
