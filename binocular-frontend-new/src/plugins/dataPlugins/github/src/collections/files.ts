import type { DataPluginFile, PreviousFilePaths } from '../../../../interfaces/dataPluginInterfaces/dataPluginFiles.ts';

export default {
  getAll: () => {
    console.log(`Getting Files`);
    return new Promise<DataPluginFile[]>((resolve) => {
      const files: DataPluginFile[] = [];
      resolve(files);
    });
  },
  getFilenamesForBranch: (branchName: string) => {
    console.log(`Getting Filenames For ` + branchName);
    return new Promise<string[]>((resolve) => {
      const files: string[] = [];
      resolve(files);
    });
  },
  getPreviousFilenamesForFilesOnBranch: (branchName: string) => {
    console.log(`Getting Previous Filenames For ` + branchName);
    return new Promise<PreviousFilePaths[]>((resolve) => {
      const names: PreviousFilePaths[] = [];
      resolve(names);
    });
  },
};
