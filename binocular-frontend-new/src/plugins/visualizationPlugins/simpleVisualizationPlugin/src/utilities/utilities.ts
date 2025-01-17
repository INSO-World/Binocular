import { RefObject } from 'react';
import { ChartData } from '../chart/chart.tsx';

export function getSVGData(chartContainerRef: RefObject<HTMLDivElement | undefined>): string {
  const svgData = chartContainerRef.current?.children[1].outerHTML;
  if (svgData === undefined) {
    return '<svg xmlns="http://www.w3.org/2000/svg"></svg>';
  }
  return svgData;
}

export enum PositiveNegativeSide {
  POSITIVE,
  NEGATIVE,
}

export function splitPositiveNegativeData(data: ChartData[], side: PositiveNegativeSide) {
  return data.map((d) => {
    const newD: ChartData = { date: d.date };
    Object.keys(d).forEach((k) => {
      if (k !== 'date') {
        if (d[k] >= 0 && side === PositiveNegativeSide.POSITIVE) {
          newD[k] = d[k];
        } else if (d[k] < 0 && side === PositiveNegativeSide.NEGATIVE) {
          newD[k] = d[k];
        } else {
          newD[k] = 0;
        }
      }
    });
    return newD;
  });
}
