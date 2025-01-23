import { createRef, useEffect, useState } from 'react';
import { throttle } from 'throttle-debounce';
import { SunburstChart } from './sunburstChart.tsx';
import { convertJacocoDataToSunburstData } from '../utilities/dataConverter.ts';

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

function Chart() {
  const chartContainerRef = createRef<HTMLDivElement>();

  const [chartWidth, setChartWidth] = useState(600);
  const [chartHeight, setChartHeight] = useState(600);
  const [chartData, setChartData] = useState<SunburstData>({ children: [], counters: [], name: '' });
  const throttledResize = throttle(
    200,
    () => {
      if (!chartContainerRef.current) return;
      if (chartContainerRef.current?.offsetWidth !== chartWidth) {
        setChartWidth(chartContainerRef.current.offsetWidth);
      }
      if (chartContainerRef.current?.offsetHeight !== chartHeight) {
        setChartHeight(chartContainerRef.current.offsetHeight);
      }
    },
    { noLeading: false, noTrailing: false },
  );

  useEffect(() => {
    if (!chartContainerRef.current) return;
    const resizeObserver = new ResizeObserver(() => {
      throttledResize();
    });
    resizeObserver.observe(chartContainerRef.current);
    return () => resizeObserver.disconnect();
  }, [chartContainerRef, chartHeight, chartWidth]);

  useEffect(() => {
    const data: SunburstData = convertJacocoDataToSunburstData();
    setChartData(data);
  }, []);

  return (
    <>
      <div className={'w-full h-full'} ref={chartContainerRef}>
        <SunburstChart width={chartWidth} height={chartHeight} data={chartData} />
      </div>
    </>
  );
}

export default Chart;
