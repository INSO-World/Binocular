import { ReactNode, RefObject } from 'react';
import { DataPlugin } from './dataPlugin.ts';
import { AuthorType } from '../../types/data/authorType.ts';
import { SprintType } from '../../types/data/sprintType.ts';
import { Action, Store } from '@reduxjs/toolkit';
import { ParametersType } from '../../types/parameters/parametersType.ts';

export interface VisualizationPlugin<SettingsType> {
  name: string;
  chartComponent: (props: {
    settings: SettingsType; // Interface for settings defines which settings are transported
    // between the settingsComponent and Chart Component
    dataConnection: DataPlugin; // Data connection of the type DataPlugin provided by Binocular.
    // !!
    // Not every dataPlugin has all capabilities.
    // !!
    authorList: AuthorType[]; //list of Authors set by Binocular
    sprintList: SprintType[]; //list of Sprints set by Binocular
    parameters: ParametersType; // General Parameters Provided By Binocular
    chartContainerRef: RefObject<HTMLDivElement>; //forwarded ref that should reference the chart div
  }) => ReactNode;
  settingsComponent: (props: { settings: SettingsType; setSettings: (newSettings: SettingsType) => void }) => ReactNode;
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
  store: Store<unknown, Action, unknown>;
}
