import PreviewImage from '../assets/thumbnail.svg';
import Settings, {type SettingsType } from './settings/settings.tsx';
import type {VisualizationPlugin} from '../../../interfaces/visualizationPlugin.ts';
import { getSVGData } from './utilities/utilities.ts';
import Reducer from './reducer';
import { convertToChartData } from './utilities/dataConverter.ts';
import Saga from './saga';
import Help from './help/help.tsx';
import type {DataPluginBuild} from '../../../interfaces/dataPluginInterfaces/dataPluginBuilds.ts';
import { VisualizationPluginMetadataCategory } from '../../../interfaces/visualizationPluginInterfaces/visualizationPluginMetadata.ts';

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
  metadata: {
    category: VisualizationPluginMetadataCategory.Builds,
    recommended: true,
    description: 'A line chart that visualizes the amount of builds/pipeline runs and their state over time.',
  },
  reducer: Reducer,
  saga: Saga,
};

export default Builds;
