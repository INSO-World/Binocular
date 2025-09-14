import { type VisualizationPlugin } from '../../../interfaces/visualizationPlugin.ts';
import Chart from './chart/chart.tsx';
import { getSVGData } from './utilities/utilities.ts';
import Settings, { type DefaultSettings } from './settings/settings.tsx';
import { getDataSlice } from './reducer';
import Saga from './saga';
export default function createVisualizationPlugin<SettingsType extends DefaultSettings, DataType>(
  component: VisualizationPlugin<SettingsType, DataType>,
): VisualizationPlugin<SettingsType, DataType> {
  // Create a wrapped settings component that merges global + plugin settings
  const wrappedSettingsComponent: VisualizationPlugin<SettingsType, DataType>['settingsComponent'] = ({ settings, setSettings }) => {
    const mergedSettings = { ...Settings, ...settings };
    return component.settingsComponent({
      settings: mergedSettings,
      setSettings: (updated) => setSettings({ ...Settings, ...updated }),
    });
  };
  return {
    name: component.name,
    chartComponent: Chart<SettingsType, DataType>,
    dataConnectionName: component.dataConnectionName,
    dataConverter: component.dataConverter,
    settingsComponent: wrappedSettingsComponent,
    helpComponent: component.helpComponent,
    defaultSettings: component.defaultSettings,
    export: {
      getSVGData: getSVGData,
    },
    capabilities: {
      popoutOnly: false,
      export: true,
    },
    images: {
      thumbnail: component.images.thumbnail,
    },
    metadata: component.metadata,
    reducer: getDataSlice(component.name).reducer,
    saga: Saga,
  };
}
