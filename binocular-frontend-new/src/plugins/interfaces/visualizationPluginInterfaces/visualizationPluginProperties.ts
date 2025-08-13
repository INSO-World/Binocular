import type { RefObject } from 'react';
import type { Store } from '@reduxjs/toolkit';
import type { DataPlugin } from '../dataPlugin.ts';
import type { AuthorType } from '../../../types/data/authorType.ts';
import type { SprintType } from '../../../types/data/sprintType.ts';
import type { ParametersType } from '../../../types/parameters/parametersType.ts';
import type { ChartData, Palette } from '../../visualizationPlugins/simpleVisualizationPlugin/src/chart/chart.tsx';
import type { FileListElementType } from '../../../types/data/fileListType.ts';

export interface VisualizationPluginProperties<SettingsType, DataType> {
  settings: SettingsType; // Interface for settings defines which settings are transported
  // between the settingsComponent and Chart Component
  dataConnection: DataPlugin; // Data connection of the type DataPlugin provided by Binocular.
  // !!
  // Not every dataPlugin has all capabilities.
  // !!
  dataConverter?: (
    data: DataType[],
    props: VisualizationPluginProperties<SettingsType, DataType>,
  ) => { chartData: ChartData[]; scale: number[]; palette: Palette };
  authorList: AuthorType[]; //list of Users set by Binocular
  fileList: FileListElementType[]; //list of Users set by Binocular
  sprintList: SprintType[]; //list of Sprints set by Binocular
  parameters: ParametersType; // General Parameters Provided By Binocular
  chartContainerRef: RefObject<HTMLDivElement | null>; //forwarded ref that should reference the chart div
  store: Store; //Redux store is needed
  // for creating the redux dispatch within the chart component so that it can change the store.
  // The store gets dynamically created for each visualization item within the components/dashboard/dashboardItem component
  dataName?: string | undefined;
}
