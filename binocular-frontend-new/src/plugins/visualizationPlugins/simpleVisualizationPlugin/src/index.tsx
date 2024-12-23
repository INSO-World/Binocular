import { VisualizationPlugin } from '../../../interfaces/visualizationPlugin.ts';
import Chart from './chart/chart.tsx';
import { getSVGData } from './utilities/utilities.ts';
import { DefaultSettings } from './settings/settings.tsx';
import { getDataSlice } from './reducer';
import Saga from './saga';

export default function createVisualizationPlugin<SettingsType extends DefaultSettings, DataType>(
  name: string,
  components: VisualizationPlugin<SettingsType, DataType>,
): VisualizationPlugin<SettingsType, DataType> {
  return {
    name: name,
    chartComponent: Chart<SettingsType, DataType>,
    dataConverter: components.dataConverter,
    settingsComponent: components.settingsComponent,
    helpComponent: components.helpComponent,
    defaultSettings: components.defaultSettings,
    export: {
      getSVGData: getSVGData,
    },
    capabilities: {
      popoutOnly: false,
      export: true,
    },
    images: {
      thumbnail: components.images.thumbnail,
    },
    reducer: getDataSlice(name).reducer,
    saga: Saga,
  };
}
