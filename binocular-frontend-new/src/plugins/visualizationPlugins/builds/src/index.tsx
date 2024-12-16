import Chart from './chart/chart.tsx';
import PreviewImage from '../assets/thumbnail.svg';
import Settings, { SettingsType } from './settings/settings.tsx';
import { VisualizationPlugin } from '../../../interfaces/visualizationPlugin.ts';
import { getSVGData } from './utilities/utilities.ts';
import Reducer from './reducer';
import Saga from './saga';
import Help from './help/help.tsx';

const Builds: VisualizationPlugin<SettingsType> = {
  name: 'Builds',
  chartComponent: Chart,
  settingsComponent: Settings,
  helpComponent: Help,
  defaultSettings: { splitBuildsPerAuthor: false, visualizationStyle: 'curved' },
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