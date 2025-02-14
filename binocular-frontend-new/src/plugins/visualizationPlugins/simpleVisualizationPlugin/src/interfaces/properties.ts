import { RefObject } from 'react';
import { Store } from '@reduxjs/toolkit';
import { DataPlugin } from '../../../../interfaces/dataPlugin.ts';
import { AuthorType } from '../../../../../types/data/authorType.ts';
import { SprintType } from '../../../../../types/data/sprintType.ts';
import { ParametersType } from '../../../../../types/parameters/parametersType.ts';
import { ChartData, Palette } from '../chart/chart.tsx';

export interface Properties<SettingsType, DataType> {
  settings: SettingsType;
  dataConnection: DataPlugin;
  dataConverter: (
    data: DataType[],
    props: Properties<SettingsType, DataType>,
  ) => { chartData: ChartData[]; scale: number[]; palette: Palette };
  authorList: AuthorType[];
  sprintList: SprintType[];
  parameters: ParametersType;
  chartContainerRef: RefObject<HTMLDivElement>;
  store: Store;
  dataName?: string;
}
