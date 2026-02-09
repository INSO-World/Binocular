import type { DataPluginCommitFile } from '../../../../interfaces/dataPluginInterfaces/dataPluginCommitsFiles';

export default {
  getAll: () => {
    return new Promise<DataPluginCommitFile[]>((resolve) => {
      const commitsFiles: DataPluginCommitFile[] = [];
      resolve(commitsFiles);
    });
  },
};
