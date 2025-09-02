export interface DataPluginBranches {
  getAllBranches: () => Promise<DataPluginBranch[]>;
}

export interface DataPluginBranch {
  branch: string;
  active: boolean;
  tracksFileRenames: boolean;
  latestCommit: string;
}
