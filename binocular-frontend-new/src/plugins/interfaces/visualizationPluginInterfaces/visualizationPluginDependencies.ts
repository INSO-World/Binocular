// Optional information on what kinds of dependencies a visualization has
export interface VisualizationPluginDependencies {
  authors: VisualizationPluginDependencyType;
  dateRange: VisualizationPluginDependencyType;
  files: VisualizationPluginDependencyType;
  generalParameters: VisualizationPluginDependencyType;
}

export enum VisualizationPluginDependencyType {
  Refresh,
  Recalculate,
  None,
}
