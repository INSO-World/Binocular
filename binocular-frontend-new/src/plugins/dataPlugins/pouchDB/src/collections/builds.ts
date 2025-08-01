import { findAllBuilds } from '../utils.js';
import Database from '../database.ts';
import type {DataPluginBuild, DataPluginBuilds} from '../../../../interfaces/dataPluginInterfaces/dataPluginBuilds.ts';

export default class Builds implements DataPluginBuilds {
  private readonly database: Database | undefined;
  constructor(database: Database | undefined) {
    this.database = database;
  }

  public async getAll(from: string, to: string) {
    console.log(`Getting Builds from ${from} to ${to}`);
    // return all builds, filtering according to parameters can be added in the future
    const first = new Date(from).getTime();
    const last = new Date(to).getTime();
    if (this.database && this.database.documentStore && this.database.edgeStore) {
      return findAllBuilds(this.database.documentStore, this.database.edgeStore).then((res: { docs: unknown[] }) => {
        res.docs = (res.docs as DataPluginBuild[])
          .filter((c) => new Date(c.createdAt).getTime() >= first && new Date(c.createdAt).getTime() <= last)
          .sort((a, b) => {
            return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
          });

        return res.docs as unknown as DataPluginBuild[];
      });
    } else {
      return new Promise<DataPluginBuild[]>((resolve) => {
        const users: DataPluginBuild[] = [];
        resolve(users);
      });
    }
  }
}
