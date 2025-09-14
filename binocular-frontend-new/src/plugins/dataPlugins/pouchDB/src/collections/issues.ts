import Database from '../database.ts';
import { findAll } from '../utils.ts';
import type { DataPluginIssue, DataPluginIssues } from '../../../../interfaces/dataPluginInterfaces/dataPluginIssues.ts';

export default class Issues implements DataPluginIssues {
  public database: Database | undefined;
  constructor(database: Database | undefined) {
    this.database = database;
  }

  public async getAll(from: string, to: string) {
    console.log(`Getting Issues from ${from} to ${to}`);
    const first = new Date(from).getTime();
    const last = new Date(to).getTime();
    if (this.database && this.database.documentStore) {
      return findAll(this.database.documentStore, 'issues').then((res: { docs: unknown[] }) => {
        res.docs = (res.docs as unknown as DataPluginIssue[])
          .filter((c) => new Date(c.createdAt).getTime() >= first && new Date(c.createdAt).getTime() <= last)
          .sort((a, b) => {
            return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
          });
        return res.docs as unknown as DataPluginIssue[];
      });
    } else {
      return new Promise<DataPluginIssue[]>((resolve) => {
        const issue: DataPluginIssue[] = [];
        resolve(issue);
      });
    }
  }
}
