export interface DataPluginBuilds {
  getAll: (from: string, to: string) => Promise<DataPluginBuild[]>;
}

export interface DataPluginBuild {
  id: number;
  committedAt: string;
  createdAt: string;
  duration: string;
  finishedAt: string;
  jobs: Job[];
  startedAt: string;
  status: string;
  updatedAt: string;
  user: string;
  userFullName: string;
}

interface Job {
  id: string;
  name: string;
  status: string;
  stage: string;
  createdAt: string;
  finishedAt: string;
}
