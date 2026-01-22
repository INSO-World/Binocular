import Database from '../database.ts';
import { findAll } from '../utils.ts';
import type {
  DataPluginMergeRequest,
  DataPluginMergeRequests,
} from '../../../../interfaces/dataPluginInterfaces/dataPluginMergeRequests.ts';

export default class MergeRequests implements DataPluginMergeRequests {
  public database: Database | undefined;
  constructor(database: Database | undefined) {
    this.database = database;
  }

  public async getAll(from: string, to: string) {
    console.log(`Getting MergeRequests from ${from} to ${to}`);
    const first = new Date(from).getTime();
    const last = new Date(to).getTime();
    if (this.database && this.database.documentStore) {
      return findAll(this.database.documentStore, 'mergeRequests').then((res: { docs: unknown[] }) => {
        res.docs = (res.docs as DataPluginMergeRequest[])
          .filter((c) => new Date(c.createdAt).getTime() >= first && new Date(c.createdAt).getTime() <= last)
          .sort((a, b) => {
            return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
          });

        return res.docs as unknown as DataPluginMergeRequest[];
      });
    } else {
      return new Promise<DataPluginMergeRequest[]>((resolve) => {
        const mergeRequest: DataPluginMergeRequest[] = [];
        resolve(mergeRequest);
      });
    }
  }
}
