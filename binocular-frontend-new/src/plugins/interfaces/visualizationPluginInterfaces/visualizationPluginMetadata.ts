export interface VisualizationPluginMetadata {
  category: VisualizationPluginMetadataCategory;
  recommended?: boolean;
  description?: string;
}

export enum VisualizationPluginMetadataCategory {
  Commits = 'Commits',
  Issues = 'Issues',
  Builds = 'Builds',
  Files = 'Files',
  Ownership = 'Ownership',
  AuthorBehaviour = 'Author Behaviour',
  Statistics = 'Statistics',
  Examples = 'Examples',
  Unknown = 'Unknown',
}
