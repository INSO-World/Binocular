import PreviewImage from '../assets/thumbnail.svg';
import Settings, { SettingsType } from './settings/settings.tsx';
import { VisualizationPlugin } from '../../../interfaces/visualizationPlugin.ts';
import { getSVGData } from './utilities/utilities.ts';
import Reducer from './reducer';
import { convertToChartData } from './utilities/dataConverter.ts';
import Saga from './saga';
import Help from './help/help.tsx';
import { DataPluginBuild } from '../../../interfaces/dataPluginInterfaces/dataPluginBuilds.ts';

const Builds: VisualizationPlugin<SettingsType, DataPluginBuild> = {
  name: 'Builds',
  chartComponent: undefined,
  settingsComponent: Settings,
  dataConnectionName: 'builds',
  dataConverter: convertToChartData,
  helpComponent: Help,
  defaultSettings: { splitBuildsPerAuthor: false, visualizationStyle: 'curved', showSprints: false },
  export: {
    getSVGData: getSVGData,
  },
  capabilities: {
    popoutOnly: false,
    export: true,
  },
  images: {
    thumbnail: PreviewImage,
  },
  reducer: Reducer,
  saga: Saga,
};

export default Builds;
