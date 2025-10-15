import { findAllAccountsIssues } from '../utils';
import Database from '../database';
import type {
  DataPluginAccountIssues,
  DataPluginAccountsIssues,
} from '../../../../interfaces/dataPluginInterfaces/dataPluginAccountsIssues';

export default class AccountsIssues implements DataPluginAccountsIssues {
  public database: Database | undefined;
  constructor(database: Database | undefined) {
    this.database = database;
  }

  public async getAll() {
    if (this.database && this.database.documentStore && this.database.edgeStore) {
      return findAllAccountsIssues(this.database.documentStore, this.database.edgeStore).then((res: { docs: unknown[] }) => {
        res.docs = res.docs as DataPluginAccountIssues[];
        return res.docs as unknown as DataPluginAccountIssues[];
      });
    } else {
      return new Promise<DataPluginAccountIssues[]>((resolve) => {
        const accountsIssues: DataPluginAccountIssues[] = [];
        resolve(accountsIssues);
      });
    }
  }
}
