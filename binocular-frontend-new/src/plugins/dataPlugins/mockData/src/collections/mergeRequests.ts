import type {
  DataPluginMergeRequest,
  DataPluginMergeRequests,
} from '../../../../interfaces/dataPluginInterfaces/dataPluginMergeRequests.ts';
import mergeRequestData from '../data/mergeRequests.json.zip';

const mergeRequests = mergeRequestData as unknown as DataPluginMergeRequest[];

export default class MergeRequests implements DataPluginMergeRequests {
  public async getAll(_from: string, _to: string): Promise<DataPluginMergeRequest[]> {
    return mergeRequests;
  }
}
