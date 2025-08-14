import Database from '../database.ts';
import type { DataPluginBranch, DataPluginBranches } from '../../../../interfaces/dataPluginInterfaces/dataPluginBranches.ts';
import { findAll } from '../utils.ts';

export default class Branches implements DataPluginBranches {
  private readonly database: Database | undefined;

  constructor(database: Database | undefined) {
    this.database = database;
  }

  public async getAllBranches(): Promise<DataPluginBranch[]> {
    if (this.database && this.database.documentStore) {
      const branches = (await findAll(this.database.documentStore, 'branches')).docs;
      return branches as unknown as DataPluginBranch[];
    } else {
      return new Promise<DataPluginBranch[]>((resolve) => {
        const branches: DataPluginBranch[] = [];
        resolve(branches);
      });
    }
  }
}
