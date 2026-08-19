import Chart from './chart/chart.tsx';
import PreviewImage from '../assets/thumbnail.svg';
import Settings, { type SettingsType } from './settings/settings.tsx';
import { type VisualizationPlugin } from '../../../interfaces/visualizationPlugin.ts';
import Reducer, { type ModuleHotspot } from './reducer';
import Saga from './saga';
import Help from './help/help.tsx';
import { VisualizationPluginMetadataCategory } from '../../../interfaces/visualizationPluginInterfaces/visualizationPluginMetadata.ts';

const LocChangeFrequency: VisualizationPlugin<SettingsType, ModuleHotspot> = {
  name: 'Module Hotspots',
  chartComponent: Chart,
  settingsComponent: Settings,
  helpComponent: Help,
  defaultSettings: { repoPath: '', parentModule: '.', neededModules: [] },
  export: { getSVGData: () => '<svg></svg>' },
  capabilities: { popoutOnly: false, export: false },
  images: { thumbnail: PreviewImage },
  reducer: Reducer,
  saga: Saga,
  metadata: {
    category: VisualizationPluginMetadataCategory.Statistics,
    recommended: false,
    description: 'Treemap of modules: area = size in LOC, color = change frequency.',
    defaultSize: [12, 9],
    compatibility: { binocularBackend: true, githubAPI: false, mockData: true, pouchDB: false, github: false, gitlab: false },
  },
};
export default LocChangeFrequency;
