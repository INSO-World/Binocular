import { DataPluginUser } from './dataPluginUsers.ts';
import {DataPluginFile} from "./dataPluginFiles.ts";

export interface DataPluginCommits {
  getAll: (from: string, to: string) => Promise<DataPluginCommit[]>;
  getCommitsWithBuilds: (from: string, to: string) => Promise<DataPluginCommitBuild[]>;
  getCommitsWithFiles: (from: string, to: string) => Promise<DataPluginCommitFile[]>;
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

export interface DataPluginCommitFile extends DataPluginCommit {
  files: {
    action: string;
    file: DataPluginFile;
  }[];
}

