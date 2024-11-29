interface GitlabUser {
  id: string;
  username: string;
  name: string;
  state: string;
  // avatarUrl, webUrl
}

export interface GitlabPipeline {
  id: number;
  iid: number;
  project: GitlabProject;
  path: string;
  sha: string;
  ref: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  beforeSha: string;
  user: GitlabUser;
  jobs: GitlabJob;
  startedAt: string;
  finishedAt: string;
  duration: number;
  queueDuration: number;
  // detailedStatus: GitlabStatus
}

export interface GitlabProject {
  id: string;
  webUrl: string;
}

export interface GitlabJob {
  edges: GitlabEdge[];
}

export interface GitlabEdge {
  node: GitlabNode;
}

export interface GitlabNode {
  id: string;
  name: string;
  createdAt: string;
  finishedAt: string;
  status: string;
  stage: GitlabStage;
}

export interface GitlabStage {
  name: string;
}
