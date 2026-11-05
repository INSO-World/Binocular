import type { DataPluginCommitFile, DataPluginCommitsFiles } from '../../../../interfaces/dataPluginInterfaces/dataPluginCommitsFiles';

export default class CommitsFiles implements DataPluginCommitsFiles {
  // TODO
  public async getAll(): Promise<DataPluginCommitFile[]> {
    // not yet implemented
    return Promise.resolve([]);
  }
}
