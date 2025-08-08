import {
  type DataPluginGeneral,
  type DataPluginIndexer,
  DataPluginIndexerState,
} from '../../../../interfaces/dataPluginInterfaces/dataPluginGeneral.ts';
import type { ProgressUpdateConfig } from '../../../../../types/settings/databaseSettingsType.ts';
//import {GraphQL} from "./utils.ts";

export default class General implements DataPluginGeneral {
  //private graphQl;
  private progressUpdateConfig: ProgressUpdateConfig;
  constructor(_endpoint: string, progressUpdateConfig: ProgressUpdateConfig | undefined) {
    //this.graphQl = new GraphQL(endpoint);
    if (progressUpdateConfig) {
      this.progressUpdateConfig = progressUpdateConfig;
    } else {
      this.progressUpdateConfig = { useAutomaticUpdate: false };
    }
  }

  public getIndexer(): DataPluginIndexer {
    return { vcs: 'ArangoDB', its: 'ArangoDB', ci: 'ArangoDB' };
  }
  public getIndexerState(): DataPluginIndexerState {
    return DataPluginIndexerState.IDLE;
  }
  public getRepositoryName(): Promise<string> {
    return new Promise<string>((resolve) => {
      resolve('[RepositoryName]');
    });
  }

  public getProgressUpdateConfig(): ProgressUpdateConfig {
    if (this.progressUpdateConfig.endpoint) {
      return this.progressUpdateConfig;
    }
    return { useAutomaticUpdate: this.progressUpdateConfig.useAutomaticUpdate, endpoint: '' };
  }
}
