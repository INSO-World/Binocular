import { createRef, useEffect, useState } from 'react';
import { throttle } from 'throttle-debounce';
import { SunburstChart } from './sunburstChart.tsx';

function Chart() {
  const chartContainerRef = createRef<HTMLDivElement>();

  const [chartWidth, setChartWidth] = useState(100);
  const [chartHeight, setChartHeight] = useState(100);
  const throttledResize = throttle(
    1000,
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

  return (
    <>
      <div className={'w-full h-full'} ref={chartContainerRef}>
        <SunburstChart width={chartWidth} height={chartHeight} />
      </div>
    </>
  );
}

export default Chart;
