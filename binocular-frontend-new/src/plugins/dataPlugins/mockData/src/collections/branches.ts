import type { DataPluginBranch, DataPluginBranches } from '../../../../interfaces/dataPluginInterfaces/dataPluginBranches.ts';
import branchData from '../data/branches.json.zip';

const branches = branchData as unknown as DataPluginBranch[];

export default class Branches implements DataPluginBranches {
  public async getAll(): Promise<DataPluginBranch[]> {
    return branches;
  }
}
