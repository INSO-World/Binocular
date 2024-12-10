import Chart from './chart/chart.tsx';
import PreviewImage from '../assets/thumbnail.svg';
import Settings, { SettingsType } from './settings/settings.tsx';
import { VisualizationPlugin } from '../../../interfaces/visualizationPlugin.ts';
import { getSVGData } from './utilities/utilities.ts';
import Reducer from '../../simpleVisualizationPlugin/src/reducer';
import Saga from './saga';
import Help from './help/help.tsx';
import { convertCommitDataToChangesChartData } from './utilities/dataConverter.ts';
import { DataPluginCommit } from '../../../interfaces/dataPluginInterfaces/dataPluginCommits.ts';

const Changes: VisualizationPlugin<SettingsType, DataPluginCommit> = {
  name: 'Changes',
  chartComponent: Chart,
  settingsComponent: Settings,
  helpComponent: Help,
  dataConverter: convertCommitDataToChangesChartData,
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
  reducer: Reducer,
  saga: Saga,
};

export default Changes;
