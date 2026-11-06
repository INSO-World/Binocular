import { DashboardLayoutCategory, type DashboardLayout } from '../../../types/general/dashboardLayoutType';

const dashboard: DashboardLayout = {
  category: DashboardLayoutCategory.BASIC,
  name: 'Issue',
  items: [
    {
      id: 0,
      width: 40,
      height: 8,
      pluginName: 'Issues',
      dataPluginId: undefined,
      x: 0,
      y: 0,
      settings: undefined,
    },
    {
      id: 0,
      width: 10,
      height: 12,
      pluginName: 'Repository Stats',
      dataPluginId: undefined,
      x: 0,
      y: 8,
      settings: undefined,
    },
    {
      id: 0,
      width: 30,
      height: 12,
      pluginName: 'Collaboration',
      dataPluginId: undefined,
      x: 10,
      y: 8,
      settings: undefined,
    },
    {
      id: 0,
      width: 40,
      height: 8,
      pluginName: 'Merge Requests',
      dataPluginId: undefined,
      x: 0,
      y: 20,
      settings: undefined,
    },
  ],
};

export default dashboard;
