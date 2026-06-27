import type {
  DataPluginAccountMergeRequests,
  DataPluginAccountsMergeRequests,
} from '../../../../interfaces/dataPluginInterfaces/dataPluginAccountsMergeRequests.ts';
import accountsMergeRequestsData from '../data/accountsMergeRequests.json.zip';

const accountsMergeRequests = accountsMergeRequestsData as unknown as DataPluginAccountMergeRequests[];

export default class AccountsMergeRequests implements DataPluginAccountsMergeRequests {
  public async getAll(_from: string, _to: string): Promise<DataPluginAccountMergeRequests[]> {
    return accountsMergeRequests;
  }
}
