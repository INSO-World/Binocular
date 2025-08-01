import type {DataPluginUser} from './dataPluginUsers.ts';

export interface DataPluginBuilds {
  getAll: (from: string, to: string) => Promise<DataPluginBuild[]>;
}

export interface DataPluginBuild {
  id: number;
  committedAt: string;
  createdAt: string;
  duration: string;
  finishedAt: string;
  jobs: DataPluginJob[];
  startedAt: string;
  status: string;
  updatedAt: string;
  user: DataPluginUser;
  userFullName: string;
}

export interface DataPluginJob {
  id: string;
  name: string;
  status: string;
  stage: string;
  createdAt: string;
  finishedAt: string;
}
