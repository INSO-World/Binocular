import type { DataPluginAccount } from './dataPluginAccounts.ts';
import type { DataPluginIssue } from './dataPluginIssues.ts';
import type { DataPluginMergeRequest } from './dataPluginMergeRequests.ts';

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
