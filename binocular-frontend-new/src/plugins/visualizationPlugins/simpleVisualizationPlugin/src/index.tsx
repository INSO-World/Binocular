import { VisualizationPlugin } from '../../../interfaces/visualizationPlugin.ts';
import Chart from './chart/chart.tsx';
import { getSVGData } from './utilities/utilities.ts';
import Settings, { DefaultSettings } from './settings/settings.tsx';
import { getDataSlice } from './reducer';
import Saga from './saga';

export default function createVisualizationPlugin<SettingsType extends DefaultSettings, DataType>(
  name: string,
  components: VisualizationPlugin<SettingsType, DataType>,
): VisualizationPlugin<SettingsType, DataType> {
  // Create a wrapped settings component that merges global + plugin settings
  const wrappedSettingsComponent: VisualizationPlugin<SettingsType, DataType>['settingsComponent'] = ({ settings, setSettings }) => {
    const mergedSettings = { ...Settings, ...settings };
    return components.settingsComponent({
      settings: mergedSettings,
      setSettings: (updated) => setSettings({ ...Settings, ...updated }),
    });
  };
  return {
    name: name,
    chartComponent: Chart<SettingsType, DataType>,
    dataConverter: components.dataConverter,
    settingsComponent: wrappedSettingsComponent,
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
