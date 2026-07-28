import { DashboardLayoutCategory, type DashboardLayout } from '../../../types/general/dashboardLayoutType';

const dashboard: DashboardLayout = {
  category: DashboardLayoutCategory.ADVANCED,
  name: 'Roadmap Progress',
  items: [
    {
      id: 0,
      width: 20,
      height: 12,
      pluginName: 'Burndown',
      dataPluginId: undefined,
      x: 0,
      y: 0,
      settings: { showSprints: true },
    },
    {
      id: 0,
      width: 20,
      height: 12,
      pluginName: 'Issues Timeline',
      dataPluginId: undefined,
      x: 20,
      y: 0,
      settings: { showSprints: false, coloringMode: 'time-spent', maxNumberOfDifferencesBetweenLabels: 5, minNumberOfLabelsPerGroup: 2 },
    },
    {
      id: 0,
      width: 40,
      height: 12,
      pluginName: 'Issues',
      dataPluginId: undefined,
      x: 0,
      y: 12,
      settings: { splitIssuesPerAuthor: true, visualizationStyle: 'curved', showSprints: false, breakdown: true },
    },
    {
      id: 0,
      width: 20,
      height: 16,
      pluginName: 'Change Frequency',
      dataPluginId: undefined,
      x: 0,
      y: 24,
      settings: undefined,
    },
    {
      id: 0,
      width: 20,
      height: 16,
      pluginName: 'Merge Requests',
      dataPluginId: undefined,
      x: 20,
      y: 24,
      settings: { splitMergeRequestsPerAuthor: false, breakdown: true, visualizationStyle: 'linear', showSprints: true },
    },
  ],
};

export default dashboard;
