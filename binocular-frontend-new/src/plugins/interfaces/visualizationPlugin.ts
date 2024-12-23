import { ReactNode, RefObject } from 'react';
import { DataPlugin } from './dataPlugin.ts';
import { AuthorType } from '../../types/data/authorType.ts';
import { SprintType } from '../../types/data/sprintType.ts';
import { Reducer, Store } from '@reduxjs/toolkit';
import { ParametersType } from '../../types/parameters/parametersType.ts';
import { Properties } from '../visualizationPlugins/simpleVisualizationPlugin/src/interfaces/properties.ts';
import { ChartData, Palette } from '../visualizationPlugins/simpleVisualizationPlugin/src/chart/chart.tsx';

export interface VisualizationPlugin<SettingsType, DataType> {
  name: string;
  chartComponent: (props: {
    settings: SettingsType; // Interface for settings defines which settings are transported
    // between the settingsComponent and Chart Component
    dataConnection: DataPlugin; // Data connection of the type DataPlugin provided by Binocular.
    // !!
    // Not every dataPlugin has all capabilities.
    // !!
    dataConverter: (
      data: DataType[],
      props: Properties<SettingsType, DataType>,
    ) => { chartData: ChartData[]; scale: number[]; palette: Palette };
    authorList: AuthorType[]; //list of Users set by Binocular
    sprintList: SprintType[]; //list of Sprints set by Binocular
    parameters: ParametersType; // General Parameters Provided By Binocular
    chartContainerRef: RefObject<HTMLDivElement>; //forwarded ref that should reference the chart div
    store: Store; //Redux store is needed
    // for creating the redux dispatch within the chart component so that it can change the store.
    // The store gets dynamically created for each visualization item within the components/dashboard/dashboardItem component
    dataName?: string | undefined;
  }) => ReactNode;
  settingsComponent: (props: { settings: SettingsType; setSettings: (newSettings: SettingsType) => void }) => ReactNode;
  helpComponent: () => ReactNode;
  dataConverter: (
    data: DataType[],
    props: Properties<SettingsType, DataType>,
  ) => { chartData: ChartData[]; scale: number[]; palette: Palette };
  defaultSettings: unknown;
  export: {
    getSVGData: (chartContainerRef: RefObject<HTMLDivElement>) => string; // method that extracts and returns a svg element as a string from a RefObject
  };
  capabilities: {
    //capabilities that the visualization can fulfill
    popoutOnly: boolean;
    export: boolean;
  };
  images: {
    // media a visualization provides for Binocular
    thumbnail: string;
  };
  reducer: Reducer;
  saga: (dataConnection: DataPlugin, name?: string) => Generator;
}
