import { Store } from '@reduxjs/toolkit';
import { RefObject } from 'react';
import { AuthorType } from '../../../../types/data/authorType.ts';
import { DataPlugin } from '../../../interfaces/dataPlugin.ts';
import { SprintType } from '../../../../types/data/sprintType.ts';
import { ParametersType } from '../../../../types/parameters/parametersType.ts';
import { Reducer } from '@reduxjs/toolkit';

let dataName: string;

export interface VisualizationPlugin<SettingsType> {
  name: string;
  chartComponent: (props: {
    settings: SettingsType;
    dataConnection: DataPlugin;
    authorList: AuthorType[];
    sprintList: SprintType[];
    parameters: ParametersType;
    chartContainerRef: RefObject<HTMLDivElement>;
    store: Store;
  }) => React.ReactNode;
  settingsComponent: (props: { settings: SettingsType; setSettings: (newSettings: SettingsType) => void }) => React.ReactNode;
  helpComponent: () => React.ReactNode;
  defaultSettings: SettingsType;
  export: {
    getSVGData: (chartContainerRef: RefObject<HTMLDivElement>) => string;
  };
  capabilities: {
    popoutOnly: boolean;
    export: boolean;
  };
  images: {
    thumbnail: string;
  };
  getSVGData: (chartContainerRef: RefObject<HTMLDivElement>) => string;
  reducer: Reducer;
  saga: (dataConnection: DataPlugin) => Generator;
  thumbnail: string;
}
/*
export function createVisualizationPlugin<SettingsType>(name: string, components: VisualizationPlugin<SettingsType>): VisualizationPlugin<SettingsType> {
  dataName = name;
  return {
    name,
    chartComponent: Chart,
    settingsComponent: components.settingsComponent,
    helpComponent: components.helpComponent,
    defaultSettings: components.defaultSettings,
    export: {
      getSVGData: components.getSVGData,
    },
    capabilities: {
      popoutOnly: false,
      export: true,
    },
    images: {
      thumbnail: components.thumbnail,
    },
    reducer: Reducer,
    saga: components.saga,
    getSVGData: components.getSVGData,
    thumbnail: components.thumbnail,
  };
}
*/
export { dataName };
