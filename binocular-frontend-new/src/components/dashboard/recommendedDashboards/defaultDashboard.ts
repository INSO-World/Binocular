import { DashboardLayoutCategory, type DashboardLayout } from '../../../types/general/dashboardLayoutType';

const dashboard: DashboardLayout = {
  category: DashboardLayoutCategory.BASIC,
  name: 'Default',
  items: [
    {
      id: 0,
      width: 20,
      height: 12,
      pluginName: 'Changes',
      dataPluginId: undefined,
      x: 0,
      y: 0,
      settings: undefined,
    },
    {
      id: 0,
      width: 20,
      height: 12,
      pluginName: 'Builds',
      dataPluginId: undefined,
      x: 20,
      y: 0,
      settings: undefined,
    },
  ],
};

export default dashboard;
