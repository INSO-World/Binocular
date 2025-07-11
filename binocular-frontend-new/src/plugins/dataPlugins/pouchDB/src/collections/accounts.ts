import { DataPluginAccount, DataPluginAccounts } from '../../../../interfaces/dataPluginInterfaces/dataPluginAccounts.ts';
import { findAllAccounts } from '../utils';
import Database from '../database';

export default class Accounts implements DataPluginAccounts {
  public database: Database | undefined;
  constructor(database: Database | undefined) {
    this.database = database;
  }

  public async getAll() {
    console.log(`Getting Accounts`);
    if (this.database && this.database.documentStore && this.database.edgeStore) {
      return findAllAccounts(this.database.documentStore, this.database.edgeStore).then((res: { docs: unknown[] }) => {
        res.docs = res.docs as DataPluginAccount[];
        return res.docs as unknown as DataPluginAccount[];
      });
    } else {
      return new Promise<DataPluginAccount[]>((resolve) => {
        const users: DataPluginAccount[] = [];
        resolve(users);
      });
    }
  }
}
