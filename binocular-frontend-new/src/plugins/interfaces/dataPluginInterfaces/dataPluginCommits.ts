import { DataPluginFileInCommit } from './dataPluginFiles.ts';
import { DataPluginUser } from './dataPluginUsers.ts';

export interface DataPluginCommits {
  getAll: (from: string, to: string) => Promise<DataPluginCommit[]>;
  getOwnershipDataForCommits: () => Promise<DataPluginOwnership[]>;
  getCommitDataForSha: (sha: string) => Promise<DataPluginCommit | undefined>;
  getByFile: (file: string) => Promise<DataPluginCommit[]>;
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
  files: DataPluginCommitFilesData;
}

export interface DataPluginCommitFilesData {
  data: DataPluginFileInCommit[];
}

export interface DataPluginOwnership {
  sha: string;
  date: string;
  parents: string[];
  files: {
    path: string;
    action: string;
    ownership: DataPluginFileOwnership[];
  }[];
}

export interface DataPluginFileOwnership {
  user: string;
  hunks: {
    originalCommit: string;
    lines: {
      from: number;
      to: number;
    }[];
  }[];
}

export interface DataPluginStats {
  additions: number;
  deletions: number;
}
