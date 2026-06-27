import type {
  DataPluginAccountIssues,
  DataPluginAccountsIssues,
} from '../../../../interfaces/dataPluginInterfaces/dataPluginAccountsIssues.ts';
import accountsIssuesData from '../data/accountsIssues.json.zip';

const accountsIssues = accountsIssuesData as unknown as DataPluginAccountIssues[];

export default class AccountsIssues implements DataPluginAccountsIssues {
  public async getAll(_from: string, _to: string): Promise<DataPluginAccountIssues[]> {
    return accountsIssues;
  }
}
