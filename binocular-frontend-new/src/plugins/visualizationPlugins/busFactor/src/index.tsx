import Chart from './chart/chart.tsx';
import PreviewImage from '../assets/thumbnail.svg';
import Settings, { type SettingsType } from './settings/settings.tsx';
import { type VisualizationPlugin } from '../../../interfaces/visualizationPlugin.ts';
import Reducer, { type Point } from './reducer';
import Saga from './saga';
import Help from './help/help.tsx';
import { VisualizationPluginMetadataCategory } from '../../../interfaces/visualizationPluginInterfaces/visualizationPluginMetadata.ts';

const BusFactorCIError: VisualizationPlugin<SettingsType, Point> = {
  name: 'Bus Factor / CI Error Rate',
  chartComponent: Chart,
  settingsComponent: Settings,
  helpComponent: Help,
  defaultSettings: { repoPath: '' },
  export: {
    getSVGData: () => '<svg></svg>',
  },
  capabilities: {
    popoutOnly: false,
    export: false,
  },
  images: {
    thumbnail: PreviewImage,
  },
  reducer: Reducer,
  saga: Saga,
  metadata: {
    category: VisualizationPluginMetadataCategory.Statistics,
    recommended: false,
    description: 'Bus factor and CI error rate of the repository over time.',
    defaultSize: [12, 8],
    compatibility: { binocularBackend: true, githubAPI: false, mockData: false, pouchDB: false, github: false, gitlab: false },
  },
};
export default BusFactorCIError;
