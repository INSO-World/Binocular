import type { ProgressUpdateConfig } from '../../../types/settings/databaseSettingsType.ts';

export interface DataPluginGeneral {
  getIndexer: () => DataPluginIndexer;
  getIndexerState: () => DataPluginIndexerState;
  getRepositoryName: () => Promise<string>;
  getProgressUpdateConfig: () => ProgressUpdateConfig;
}

export interface DataPluginIndexer {
  vcs: string;
  its: string;
  ci: string;
}

export enum DataPluginIndexerState {
  IDLE,
  INDEXING,
  FINISHED,
  CONNECTED,
  CONNECTION_FAILED,
}
