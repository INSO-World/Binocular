import type { DataPluginAccount } from './dataPluginAccounts.ts';
import type { DataPluginNote } from './dataPluginNotes.ts';

export interface DataPluginMergeRequests {
  getAll: (from: string, to: string) => Promise<DataPluginMergeRequest[]>;
}

export interface DataPluginMergeRequest {
  id: string;
  iid: number;
  title: string;
  //description: string;
  state: string;
  webUrl: string;
  createdAt: string;
  closedAt: string | null;
  updatedAt: string | null;
  sourceBranch: string;
  targetBranch: string;
  author: DataPluginAccount;
  assignee: DataPluginAccount | null;
  assignees: DataPluginAccount[];
  notes: DataPluginNote[];
}
