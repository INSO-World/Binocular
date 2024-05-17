import { AreaChart } from './areaChart.tsx';
import { createRef, useEffect, useState } from 'react';
import { SettingsType } from '../settings/settings.tsx';

function Chart(props: { settings: unknown }) {
  const chartContainerRef = createRef<HTMLDivElement>();

  const [chartWidth, setChartWidth] = useState(100);
  const [chartHeight, setChartHeight] = useState(100);

  useEffect(() => {
    if (chartContainerRef.current) {
      if (chartContainerRef.current?.offsetWidth !== chartWidth) {
        setChartWidth(chartContainerRef.current.offsetWidth);
      }
      if (chartContainerRef.current?.offsetHeight !== chartHeight) {
        setChartHeight(chartContainerRef.current.offsetHeight);
      }
    }
  }, [chartContainerRef, chartHeight, chartWidth]);

  return (
    <>
      <div className={'w-full h-full'} ref={chartContainerRef}>
        <AreaChart
          data={(props.settings as SettingsType).data}
          width={chartWidth}
          height={chartHeight}
          color={(props.settings as SettingsType).color}
        />
      </div>
    </>
  );
}

export default Chart;
