import { DashboardLayoutCategory, type DashboardLayout } from '../../../types/general/dashboardLayoutType';

const dashboard: DashboardLayout = {
  category: DashboardLayoutCategory.BASIC,
  name: 'Default',
  items: [
    {
      id: 0,
      width: 20,
      height: 20,
      pluginName: 'Changes',
      dataPluginId: undefined,
      x: 0,
      y: 0,
      settings: { splitAdditionsDeletions: true, visualizationStyle: 'curved', showSprints: false },
    },
    {
      id: 0,
      width: 20,
      height: 20,
      pluginName: 'Builds',
      dataPluginId: undefined,
      x: 20,
      y: 0,
      settings: { splitBuildsPerAuthor: false, visualizationStyle: 'stepped', showSprints: false },
    },
    {
      id: 0,
      width: 20,
      height: 20,
      pluginName: 'Issues',
      dataPluginId: undefined,
      x: 0,
      y: 20,
      settings: { splitIssuesPerAuthor: false, breakdown: true, visualizationStyle: 'curved', showSprints: false },
    },
    {
      id: 0,
      width: 20,
      height: 20,
      pluginName: 'Merge Requests',
      dataPluginId: undefined,
      x: 20,
      y: 20,
      settings: { splitMergeRequestsPerAuthor: false, breakdown: true, visualizationStyle: 'curved', showSprints: false },
    },
  ],
};

export default dashboard;
