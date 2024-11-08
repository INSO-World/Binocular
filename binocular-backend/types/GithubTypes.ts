export interface GithubUser {
  login: string;
  email: string;
  name: string;
  url: string;
  avatarUrl: string;
}

// In GitHub, it is called Workflow Run
export interface GithubRun {
  // some variables are not implemented, but commented if needed anytime
  id: number;
  name: string | null;
  node_id: string;
  head_branch: string | null;
  head_sha: string;
  // path
  display_title: string;
  run_number: number;
  status: string | null;
  conclusion: string | null;
  // workflow_id, check_suite_id, check_suite_node_id, url
  html_url: string;
  // pull_requests
  created_at: string | null;
  updated_at: string | null;
  actor: GithubActor;
  run_attempt: number;
  // referenced_workflows
  run_started_at: string | null;
  // triggering_actor, some urls
  head_commit: GithubCommit;
}

export interface GithubJob {
  id: number;
  run_id: number;
  workflow_name: string;
  head_branch: string;
  run_url: string;
  run_attempt: number;
  node_id: string;
  head_sha: string;
  url: string;
  html_url: string;
  status: string;
  conclusion: string;
  created_at: string;
  started_at: string;
  completed_at: string;
  name: string;
  steps: GithubJobStep[];
  check_run_url: string;
  labels: string[];
  runner_id: number;
  runner_name: string;
  runner_group_id: number;
  runner_group_name: string;
}

export interface GithubJobStep {
  name: string;
  status: string;
  conclusion: string;
  number: number;
  started_at: string | null;
  completed_at: string | null;
}

export interface GithubActor {
  login: string;
  id: number;
  node_id: string;
  avatar_url: string;
  url: string;
  html_url: string;
  type: string;
  site_admin: boolean;
  name: string;
}

export interface GithubMilestone {
  id: number;
  url: string;
  number: number;
  state: string;
  title: string;
  description: string;
  creator: GithubActor;
  createdAt: string;
  updatedAt: string;
  closedAt: string;
  dueOn: string;
}

export interface GithubCommit {
  id: number;
  message: string;
  timestamp: string; // probably type: date-time
  // other variables are not implemented, because they are not used and documented
}
