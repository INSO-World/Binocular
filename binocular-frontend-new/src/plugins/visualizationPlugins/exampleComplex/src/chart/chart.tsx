import { useEffect, useRef, useState } from 'react';
import { SunburstChart } from './sunburstChart.tsx';
import { Store } from '@reduxjs/toolkit';
import { handelPopoutResizing } from '../../../../utils/resizing.ts';

function Chart(props: { store: Store }) {
  const chartContainerRef = useRef<HTMLDivElement>(null);

  const [chartWidth, setChartWidth] = useState(100);
  const [chartHeight, setChartHeight] = useState(100);

  /**
   * RESIZE Logic START
   */
  function resize() {
    if (!chartContainerRef.current) return;
    if (chartContainerRef.current?.offsetWidth !== chartWidth) {
      setChartWidth(chartContainerRef.current.offsetWidth);
    }
    if (chartContainerRef.current?.offsetHeight !== chartHeight) {
      setChartHeight(chartContainerRef.current.offsetHeight);
    }
  }

  useEffect(() => {
    resize();
  }, [chartContainerRef, chartHeight, chartWidth]);

  handelPopoutResizing(props.store, resize);
  /**
   * RESIZE Logic END
   */

  return (
    <>
      <div className={'w-full h-full'} ref={chartContainerRef}>
        <SunburstChart width={chartWidth} height={chartHeight} />
      </div>
    </>
  );
}

export default Chart;
