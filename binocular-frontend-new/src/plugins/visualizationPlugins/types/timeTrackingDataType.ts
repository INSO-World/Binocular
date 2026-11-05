import { type DataPluginAccount } from '../../interfaces/dataPluginInterfaces/dataPluginAccounts';
import { type DataPluginIssue } from '../../interfaces/dataPluginInterfaces/dataPluginIssues';
import { type DataPluginMergeRequest } from '../../interfaces/dataPluginInterfaces/dataPluginMergeRequests';

export interface TimeTrackingData {
  author: DataPluginAccount;
  timeSpent: number;
  createdAt: string;
  issue: DataPluginIssue | null;
  mergeRequest: DataPluginMergeRequest | null;
}
