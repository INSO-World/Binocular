import { DataPluginUser } from './dataPluginUsers.ts';

export interface DataPluginCommits {
  getAll: (from: string, to: string) => Promise<DataPluginCommit[]>;
  getAllShort: () => Promise<DataPluginCommitShort[]>;
}

export interface DataPluginCommit {
  sha: string;
  shortSha: string;
  messageHeader: string;
  message: string;
  user: DataPluginUser;
  branch: string;
  date: string;
  parents: string[];
  webUrl: string;
  stats: DataPluginStats;
}

export interface DataPluginStats {
  additions: number;
  deletions: number;
}

export interface DataPluginCommitShort {
  sha: string;
  date: string;
  messageHeader: string;
}
