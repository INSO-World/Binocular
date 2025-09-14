import type { DataPluginBranch, DataPluginBranches } from '../../../../interfaces/dataPluginInterfaces/dataPluginBranches.ts';

export default class Branches implements DataPluginBranches {
  public async getAllBranches(): Promise<DataPluginBranch[]> {
    return Promise.resolve([
      {
        branch: 'develop',
        active: false,
        tracksFileRenames: true,
        latestCommit: '0000000002',
      },
    ]);
  }
}
