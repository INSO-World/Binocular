import type {
  DataPluginAccountIssues,
  DataPluginAccountsIssues,
} from '../../../../interfaces/dataPluginInterfaces/dataPluginAccountsIssues.ts';
import accountsIssuesData from '../data/accountsIssues.json.zip';

const accountsIssues = accountsIssuesData as unknown as DataPluginAccountIssues[];

const dicebear = (seed: string) => `https://api.dicebear.com/9.x/avataaars/svg?seed=${encodeURIComponent(seed)}`;

export default class AccountsIssues implements DataPluginAccountsIssues {
  public async getAll(_from: string, _to: string): Promise<DataPluginAccountIssues[]> {
    return accountsIssues.map((a) => ({ ...a, avatarUrl: dicebear(a.login || a.id) }));
  }
}
