export interface VisualizationPluginMetadata {
  category: VisualizationPluginMetadataCategory;
  recommended?: boolean;
  description?: string;
  defaultSize?: [number, number];
  compatibility?: VisualizationPluginCompatibility;
}

export interface VisualizationPluginCompatibility {
  binocularBackend: boolean;
  githubAPI: boolean;
  mockData: boolean;
  pouchDB: boolean;
  github: boolean;
  gitlab: boolean;
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
  Expertise = 'Expertise',
}
