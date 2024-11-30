export interface DataPluginIssues {
  getAll: (from: string, to: string) => Promise<DataPluginIssue[]>;
}

export interface DataPluginIssue {
  iid: string;
  title: string;
  //description: string;
  state: string;
  webUrl: string;
  createdAt: string;
  closedAt: string | null;
  author: DataPluginGitHubUser;
  assignee: DataPluginGitHubUser | null;
  assignees: DataPluginGitHubUser[];
  //notes?: DataPluginNotes[];
}

export interface DataPluginGitHubUser {
  login: string | null;
  name: string | null;
}

export interface DataPluginNotes {
  body: any;
  createdAt: string;
  author: DataPluginGitHubUser;
}
