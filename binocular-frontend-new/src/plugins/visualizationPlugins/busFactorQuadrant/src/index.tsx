import Chart from './chart/chart.tsx';
import PreviewImage from '../assets/thumbnail.svg';
import Settings, { type SettingsType } from './settings/settings.tsx';
import { type VisualizationPlugin } from '../../../interfaces/visualizationPlugin.ts';
import Reducer, { type ModulePoint } from './reducer';
import Saga from './saga';
import Help from './help/help.tsx';
import { VisualizationPluginMetadataCategory } from '../../../interfaces/visualizationPluginInterfaces/visualizationPluginMetadata.ts';

// Plugin descriptor: wires all parts of the widget together and hands them to Binocular.
// This object is what gets registered in pluginRegistry.ts.
const BusFactorQuadrant: VisualizationPlugin<SettingsType, ModulePoint> = {
  name: 'Bus Factor / CI Quadrant', // shown in the widget picker and used as the item title
  chartComponent: Chart, // our own d3 chart (not the shared StackedAreaChart)
  settingsComponent: Settings,
  helpComponent: Help,
  // Initial settings for a freshly added widget (must match SettingsType)
  defaultSettings: { repoPath: '', busFactorThreshold: 2, ciErrorThreshold: 0.3, excludedAuthors: [], neededModules: [] },
  // Export is not implemented for this widget, so return an empty svg
  export: { getSVGData: () => '<svg></svg>' },
  capabilities: { popoutOnly: false, export: false },
  images: { thumbnail: PreviewImage }, // preview image shown in the widget picker
  reducer: Reducer, // this widget's own redux slice
  saga: Saga, // side effects (data fetching)
  metadata: {
    category: VisualizationPluginMetadataCategory.Statistics, // grouping in the widget picker
    recommended: false,
    description: 'Modules plotted by bus factor vs. CI error rate in four risk quadrants.',
    defaultSize: [10, 10], // default width/height in dashboard grid cells
    // Only the Binocular backend (and Mock Data for testing) can serve this custom query
    compatibility: { binocularBackend: true, githubAPI: false, mockData: true, pouchDB: false, github: false, gitlab: false },
  },
};
export default BusFactorQuadrant;
