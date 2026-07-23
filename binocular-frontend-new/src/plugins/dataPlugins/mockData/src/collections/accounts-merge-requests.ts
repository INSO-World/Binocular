import type {
  DataPluginAccountMergeRequests,
  DataPluginAccountsMergeRequests,
} from '../../../../interfaces/dataPluginInterfaces/dataPluginAccountsMergeRequests.ts';
import accountsMergeRequestsData from '../data/accountsMergeRequests.json.zip';

const accountsMergeRequests = accountsMergeRequestsData as unknown as DataPluginAccountMergeRequests[];

const dicebear = (seed: string) => `https://api.dicebear.com/9.x/avataaars/svg?seed=${encodeURIComponent(seed)}`;

export default class AccountsMergeRequests implements DataPluginAccountsMergeRequests {
  public async getAll(_from: string, _to: string): Promise<DataPluginAccountMergeRequests[]> {
    return accountsMergeRequests.map((a) => ({ ...a, avatarUrl: dicebear(a.login || a.id) }));
  }
}
