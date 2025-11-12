import type { DefaultSettings } from '../../plugins/visualizationPlugins/simpleVisualizationPlugin/src/settings/settings';

export interface DashboardItemDTO {
  id: number;
  x?: number;
  y?: number;
  width: number;
  height: number;
}

export interface DashboardItemType extends DashboardItemDTO {
  pluginName?: string;
  dataPluginId: number | undefined;
  // any needed for all the different extended setting types
  //eslint-disable-next-line @typescript-eslint/no-explicit-any
  settings?: DefaultSettings & { [key: string]: any };
}
