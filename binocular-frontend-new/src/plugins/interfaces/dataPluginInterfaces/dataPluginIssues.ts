import { DataPluginAccount } from './dataPluginAccounts.ts';
import { DataPluginNote } from './dataPluginNotes.ts';

export interface DataPluginIssues {
  getAll: (from: string, to: string) => Promise<DataPluginIssue[]>;
}

export interface DataPluginIssue {
  id: string;
  iid: number;
  title: string;
  //description: string;
  state: string;
  webUrl: string;
  createdAt: string;
  closedAt: string | null;
  author: DataPluginAccount;
  assignee: DataPluginAccount | null;
  assignees: DataPluginAccount[];
  notes: DataPluginNote[];
}
