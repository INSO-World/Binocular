export interface DataPluginMergeRequests {
  getAll: (from: string, to: string) => Promise<DataPluginMergeRequest[]>;
}

export interface DataPluginMergeRequest {
  id: string;
  iid: number;
  title: string;
}
