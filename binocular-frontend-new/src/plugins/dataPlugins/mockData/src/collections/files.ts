import type { DataPluginFile, DataPluginFiles, PreviousFilePaths } from '../../../../interfaces/dataPluginInterfaces/dataPluginFiles.ts';

export default class Files implements DataPluginFiles {
  constructor() {}

  public async getAll() {
    return new Promise<DataPluginFile[]>((resolve) => {
      const files: DataPluginFile[] = [
        {
          path: 'index.js',
          webUrl: 'https://github.com/INSO-TUWien/Binocular',
          maxLength: 5,
        },
        {
          path: 'src/app.js',
          webUrl: 'https://github.com/INSO-TUWien/Binocular',
          maxLength: 10,
        },
        {
          path: 'src/app.css',
          webUrl: 'https://github.com/INSO-TUWien/Binocular',
          maxLength: 8,
        },
      ];
      resolve(files);
    });
  }

  getFilenamesForBranch(_branchName: string): Promise<string[]> {
    return Promise.resolve(['index.js', 'src/app.js', 'src/app.css']);
  }

  getPreviousFilenamesForFilesOnBranch(_branchName: string): Promise<PreviousFilePaths[]> {
    return Promise.resolve([]);
  }
}
