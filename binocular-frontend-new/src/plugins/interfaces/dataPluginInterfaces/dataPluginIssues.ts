export interface DataPluginIssues {
  getAll: (from: string, to: string) => Promise<DataPluginIssue[]>;
}

export interface DataPluginIssue {
  id: string;
  iid: string;
  title: string;
  description: string;
  state: string;
  webUrl: string;
  createdAt: string;
  closedAt: string | null;
  author: { name: string };
  assignee: { name: string | null } | null;
  //notes: any;
}
