import type { DataPluginIssue } from './dataPluginIssues.ts';

export interface DataPluginAccountsIssues {
  getAll: (from: string, to: string) => Promise<DataPluginAccountIssues[]>;
}

export interface DataPluginAccountIssues {
  id: string;
  login: string;
  name: string;
  avatarUrl: string;
  url: string;
  issues: DataPluginIssue[];
}
