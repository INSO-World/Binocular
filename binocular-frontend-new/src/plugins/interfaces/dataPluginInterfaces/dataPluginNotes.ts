import { DataPluginAccount } from './dataPluginAccounts.ts';
import { DataPluginIssue } from './dataPluginIssues.ts';
import { DataPluginMergeRequest } from './dataPluginMergeRequests.ts';

export interface DataPluginNotes {
  getAll: (from: string, to: string) => Promise<DataPluginNote[]>;
}

export interface DataPluginNote {
  createdAt: string;
  updatedAt: string;
  issue: DataPluginIssue | null;
  mergeRequest: DataPluginMergeRequest | null;
  body: string;
  author: DataPluginAccount;
}
