import { findAllIssues } from '../utils.js';
import { Database } from '../database.ts';
import { DataPluginIssue, DataPluginIssues } from '../../../../interfaces/dataPluginInterfaces/dataPluginIssues.ts';

export default class Issues implements DataPluginIssues {
  private readonly database: Database | undefined;
  constructor(database: Database | undefined) {
    this.database = database;
  }

  public async getAll(from: string, to: string) {
    console.log(`Getting Issues from ${from} to ${to}`);
    // return all issues, filtering according to parameters can be added in the future
    const first = new Date(from).getTime();
    const last = new Date(to).getTime();
    if (this.database && this.database.documentStore && this.database.edgeStore) {
      return findAllIssues(this.database.documentStore, this.database.edgeStore).then((res: { docs: unknown[] }) => {
        res.docs = (res.docs as DataPluginIssue[])
          .filter((c) => new Date(c.createdAt).getTime() >= first && new Date(c.createdAt).getTime() <= last)
          .sort((a, b) => {
            return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
          });

        return res.docs as unknown as DataPluginIssue[];
      });
    } else {
      return new Promise<DataPluginIssue[]>((resolve) => {
        const issues: DataPluginIssue[] = [];
        resolve(issues);
      });
    }
  }
}
