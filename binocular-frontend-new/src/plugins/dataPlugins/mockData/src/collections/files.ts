import type { DataPluginFile, DataPluginFiles, PreviousFilePaths } from '../../../../interfaces/dataPluginInterfaces/dataPluginFiles.ts';
import fileData from '../data/files.json.zip';

const files = fileData as unknown as DataPluginFile[];

export default class Files implements DataPluginFiles {
  public async getAll() {
    return files;
  }

  public async getFilenamesForBranch(_branchName: string): Promise<string[]> {
    return files.map((f) => f.path);
  }

  public async getPreviousFilenamesForFilesOnBranch(_branchName: string): Promise<PreviousFilePaths[]> {
    return [];
  }
}
