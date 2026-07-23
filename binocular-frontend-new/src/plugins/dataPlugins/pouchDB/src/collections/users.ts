import type { DataPluginUser, DataPluginUsers } from '../../../../interfaces/dataPluginInterfaces/dataPluginUsers.ts';
import { findAllUsers } from '../utils';
import Database from '../database';

export default class Users implements DataPluginUsers {
  public database: Database | undefined;
  constructor(database: Database | undefined) {
    this.database = database;
  }

  public async getAll() {
    console.log(`Getting Authors`);
    if (this.database && this.database.documentStore && this.database.edgeStore) {
      return findAllUsers(this.database.documentStore, this.database.edgeStore).then((res: { docs: unknown[] }) => {
        res.docs = res.docs as DataPluginUser[];
        return res.docs as unknown as DataPluginUser[];
      });
    } else {
      return new Promise<DataPluginUser[]>((resolve) => {
        const users: DataPluginUser[] = [];
        resolve(users);
      });
    }
  }
}
