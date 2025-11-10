import type { DataPluginAccount } from './dataPluginAccounts.ts';
import type { DataPluginStats } from './dataPluginCommits.ts';
import type { DataPluginNote } from './dataPluginNotes.ts';

export interface DataPluginIssues {
  getAll: (from: string, to: string) => Promise<DataPluginIssue[]>;
}

export interface DataPluginIssue {
  id: string;
  iid: number;
  title: string;
  description: string;
  state: string;
  webUrl: string;
  createdAt: string;
  closedAt: string | null;
  labels: string[];
  author: DataPluginAccount;
  assignee: DataPluginAccount | null;
  assignees: DataPluginAccount[];
  notes: DataPluginNote[];
  commits: DataPluginStats[];
}
