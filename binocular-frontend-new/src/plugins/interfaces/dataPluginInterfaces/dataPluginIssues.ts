export interface DataPluginIssue {
  id: string;
  iid: string;
  title: string;
  description: string;
  createdAt: string;
  closedAt: string;
  state: string;
  webUrl: string;
}

export interface DataPluginIssues {
  getAll: () => Promise<DataPluginIssue[]>;
}
