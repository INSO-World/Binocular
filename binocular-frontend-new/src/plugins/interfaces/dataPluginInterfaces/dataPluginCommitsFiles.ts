import { DataPluginStats } from './dataPluginCommits.ts';

export interface DataPluginCommitsFiles {
  getAll: (sha: string) => Promise<DataPluginCommitFile[]>;
}

export interface DataPluginCommitFile {
  file: {
    path: string;
  };
  stats: DataPluginStats;
}
