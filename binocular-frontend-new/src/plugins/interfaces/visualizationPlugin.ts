import { ReactNode, RefObject } from 'react';
import { DataPlugin } from './dataPlugin.ts';
import { Reducer } from '@reduxjs/toolkit';
import { Properties } from './visualizationPluginInterfaces/properties.ts';
import { ChartData, Palette } from '../visualizationPlugins/simpleVisualizationPlugin/src/chart/chart.tsx';

export interface VisualizationPlugin<SettingsType, DataType> {
  name: string;
  chartComponent?: (props: Properties<SettingsType, DataType>) => ReactNode;
  settingsComponent: (props: { settings: SettingsType; setSettings: (newSettings: SettingsType) => void }) => ReactNode;
  helpComponent: () => ReactNode;
  dataConnectionName?: string; //
  dataConverter?: (
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
  saga: (dataConnection: DataPlugin, name?: string, dataConnectionName?: string) => Generator;
}
