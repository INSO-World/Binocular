import Database from '../database.ts';
import { findAll } from '../utils.ts';
import { DataPluginIssue, DataPluginIssues } from '../../../../interfaces/dataPluginInterfaces/dataPluginIssue.ts';

export default class Issues implements DataPluginIssues {
  public database: Database | undefined;
  constructor(database: Database | undefined) {
    this.database = database;
  }
  // TODO implement for issues plot
  public async getAll(from: string, to: string) {
    console.log(`Getting Issues from ${from} to ${to}`);
    if (this.database && this.database.documentStore) {
      return findAll(this.database.documentStore, 'issues').then((res: { docs: unknown[] }) => {
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
