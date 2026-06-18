import type { DataPluginFileInCommit } from './dataPluginFiles.ts';
import type { DataPluginUser } from './dataPluginUsers.ts';

export interface DataPluginCommits {
  getAll: (from: string, to: string) => Promise<DataPluginCommit[]>;
  getAllShort: () => Promise<DataPluginCommitShort[]>;
  getCommitsWithBuilds: (from: string, to: string) => Promise<DataPluginCommitBuild[]>;
  getCommitsWithFiles: (from: string, to: string) => Promise<DataPluginCommit[]>;
  getOwnershipDataForCommits: () => Promise<DataPluginOwnership[]>;
  getCommitDataForSha: (sha: string) => Promise<DataPluginCommit | undefined>;
  getByFile: (file: string) => Promise<DataPluginCommit[]>;
  getDateOfFirstCommit: () => Promise<string>;
  getDateOfLastCommit: () => Promise<string>;
  getCommitDataWithFilesAndOwnership: (from: string, to: string) => Promise<CommitWithFileChanges[]>;
}

// Per-file change information for a single commit, in the shape consumed by the
// change-frequency visualization. `signature` is the commit author's git signature and
// `lineCount` is the file's current size where the data source can provide it.
export interface CommitFileChange {
  file: { path: string };
  lineCount?: number;
  stats: DataPluginStats;
}

export interface CommitWithFileChanges {
  sha?: string;
  date: string;
  signature?: string;
  branch?: string;
  message?: string;
  webUrl?: string;
  parents?: string[];
  stats?: DataPluginStats;
  files: { data: CommitFileChange[] };
  // True when the commit falls inside the significant (visible) window. Commits loaded only for
  // cumulative line-count context (outside the window) are returned with isSignificant: false.
  // Absent is treated as true.
  isSignificant?: boolean;
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
  files?: DataPluginCommitFilesData;
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

export interface DataPluginCommitShort {
  sha: string;
  date: string;
  messageHeader: string;
}

export interface DataPluginCommitBuild extends DataPluginCommit {
  builds: {
    id: number;
    status: string;
    duration: string;
    startedAt: string;
    finishedAt: string;
    jobs: {
      id: number;
      name: string;
      status: string;
      stage: string;
    }[];
  }[];
}
