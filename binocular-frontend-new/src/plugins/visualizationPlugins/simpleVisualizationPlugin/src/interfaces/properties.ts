import { SettingsType } from '../settings/settings.tsx';
import { RefObject } from 'react';
import { Store } from '@reduxjs/toolkit';
import { DataPlugin } from '../../../../interfaces/dataPlugin.ts';
import { AuthorType } from '../../../../../types/data/authorType.ts';
import { SprintType } from '../../../../../types/data/sprintType.ts';
import { ParametersType } from '../../../../../types/parameters/parametersType.ts';

export interface Properties {
  settings: SettingsType;
  dataConnection: DataPlugin;
  authorList: AuthorType[];
  sprintList: SprintType[];
  parameters: ParametersType;
  chartContainerRef: RefObject<HTMLDivElement>;
  store: Store;
}
