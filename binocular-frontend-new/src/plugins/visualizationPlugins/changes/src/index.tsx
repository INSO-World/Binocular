import PreviewImage from '../assets/thumbnail.svg';
import Settings, { type ChangesSettings } from './settings/settings.tsx';
import type { VisualizationPlugin } from '../../../interfaces/visualizationPlugin.ts';
import { getSVGData } from './utilities/utilities.ts';
import Reducer from '../../simpleVisualizationPlugin/src/reducer';
import Saga from './saga';
import Help from './help/help.tsx';
import { convertToChartData } from './utilities/dataConverter.ts';
import type { DataPluginCommit } from '../../../interfaces/dataPluginInterfaces/dataPluginCommits.ts';
import { VisualizationPluginMetadataCategory } from '../../../interfaces/visualizationPluginInterfaces/visualizationPluginMetadata.ts';

const Changes: VisualizationPlugin<ChangesSettings, DataPluginCommit> = {
  name: 'Changes',
  chartComponent: undefined,
  settingsComponent: Settings,
  helpComponent: Help,
  dataConnectionName: 'commits',
  dataConverter: convertToChartData,
  defaultSettings: { splitAdditionsDeletions: true, visualizationStyle: 'curved' },
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
    category: VisualizationPluginMetadataCategory.Commits,
    recommended: true,
    description: 'A line chart that visualizes the amount of additions and deletion and what author is responsible for them over time.',
  },
  reducer: Reducer,
  saga: Saga,
};

export default Changes;
