export interface ProgressType {
  type: string;
  report: {
    commits: ProgressUpdate;
    issues: ProgressUpdate;
    builds: ProgressUpdate;
    files: ProgressUpdate;
    modules: ProgressUpdate;
    milestones: ProgressUpdate;
    mergeRequests: ProgressUpdate;
  };
}

interface ProgressUpdate {
  processed: number;
  total: number;
}
