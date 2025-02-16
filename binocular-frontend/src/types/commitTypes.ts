export interface Commit {
  tags: string[] | undefined;
  sha: string;
  shortSha: string;
  messageHeader: string;
  message: string;
  signature: string;
  branch: string;
  date: string;
  parents: string[];
  webUrl: string;
  stats: Stats;
}

interface Stats {
  additions: number;
  deletions: number;
}
