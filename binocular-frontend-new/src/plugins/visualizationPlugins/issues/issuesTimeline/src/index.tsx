import type { DataPluginIssue } from '../../../../interfaces/dataPluginInterfaces/dataPluginIssues.ts';
import type { VisualizationPlugin } from '../../../../interfaces/visualizationPlugin.ts';
import { VisualizationPluginMetadataCategory } from '../../../../interfaces/visualizationPluginInterfaces/visualizationPluginMetadata.ts';
import PreviewImage from '../assets/thumbnail.svg';
import Chart from './chart/Chart.tsx';
import { Help } from './help/help.tsx';
import Reducer from './reducer';
import Saga from './saga';
import { Settings, type IssuesTimelineSettings } from './settings/settings.tsx';

const IssuesTimeline: VisualizationPlugin<IssuesTimelineSettings, DataPluginIssue> = {
  name: 'Issues Timeline',
  chartComponent: Chart,
  settingsComponent: Settings,
  dataConnectionName: 'issues',
  helpComponent: Help,
  defaultSettings: {
    showSprints: true,
    maxNumberOfDifferencesBetweenLabels: 3,
    minNumberOfLabelsPerGroup: 1,
    coloringMode: 'author',
  },
  export: {
    getSVGData: () => '',
  },
  capabilities: {
    popoutOnly: false,
    export: false,
  },
  images: {
    thumbnail: PreviewImage,
  },
  metadata: {
    category: VisualizationPluginMetadataCategory.Issues,
    recommended: false,
    description: 'A bar chart that visualizes issues in relation to the defined sprints.',
    compatibility: {
      binocularBackend: true,
      github: true,
      githubAPI: false,
      gitlab: true,
      mockData: true,
      pouchDB: true,
    },
  },
  reducer: Reducer,
  saga: Saga,
};

export default IssuesTimeline;
