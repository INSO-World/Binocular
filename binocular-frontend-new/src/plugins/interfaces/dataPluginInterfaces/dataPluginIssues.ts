import { DataPluginUser } from './dataPluginUsers.ts';

export interface DataPluginIssues {
  getAll: (from: string, to: string) => Promise<DataPluginIssue[]>;
}

export interface DataPluginIssue {
  iid: number;
  title: string;
  description: string;
  createdAt: string;
  closedAt: string;
  updatedAt: string;
  state: string;
  webUrl: string;
  labels: string[];
  creator: DataPluginUser;
  author: DataPluginIssueAccount;
  assignees: DataPluginIssueAccount[];
  assignee: DataPluginIssueAccount;
  notes: DataPluginIssueNote;
}

export interface DataPluginIssueAccount {
  login: string;
  name: string;
}

export interface DataPluginIssueNote {
  body: string;
  createdAt: string;
  author: DataPluginIssueAccount;
}
