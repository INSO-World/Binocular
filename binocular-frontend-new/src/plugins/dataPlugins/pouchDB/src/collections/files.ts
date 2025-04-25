import { DataPluginFile, DataPluginFiles, PreviousFilePaths } from '../../../../interfaces/dataPluginInterfaces/dataPluginFiles.ts';
import { binarySearch, findAll, findBranch, findBranchFileConnections, sortByAttributeString } from '../utils.ts';
import Database from '../database.ts';

export default class Files implements DataPluginFiles {
  private readonly database: Database | undefined;
  constructor(database: Database | undefined) {
    this.database = database;
  }

  public async getAll() {
    console.log(`Getting Files`);
    if (this.database && this.database.documentStore) {
      return findAll(this.database.documentStore, 'files').then((res: { docs: unknown[] }) => {
        return res.docs as DataPluginFile[];
      });
    } else {
      return new Promise<DataPluginFile[]>((resolve) => {
        const files: DataPluginFile[] = [];
        resolve(files);
      });
    }
  }

  public async getFilenamesForBranch(branchName: string): Promise<string[]> {
    if (this.database && this.database.documentStore && this.database.edgeStore) {
      const branchID = (await findBranch(this.database.documentStore, branchName)).docs[0]._id;
      const branchFiles = (await findBranchFileConnections(this.database.edgeStore)).docs;
      const files = sortByAttributeString((await findAll(this.database.documentStore, 'files')).docs, '_id');
      const fileNames: string[] = [];
      branchFiles
        .filter((bf) => bf.from == branchID)
        .forEach((bf) => {
          const file = binarySearch(files, bf.to, '_id') as DataPluginFile | null;
          if (file) fileNames.push(file.path);
        });
      return fileNames;
    } else {
      return new Promise<string[]>((resolve) => {
        const fileNames: string[] = [];
        resolve(fileNames);
      });
    }
  }
  public async getPreviousFilenamesForFilesOnBranch(branchName: string): Promise<PreviousFilePaths[]> {
    if (this.database && this.database.documentStore && this.database.edgeStore) {
      const branchID = (await findBranch(this.database.documentStore, branchName)).docs[0]._id;
      const branchFiles = (await findBranchFileConnections(this.database.documentStore)).docs;
      const files = sortByAttributeString((await findAll(this.database.documentStore, 'files')).docs, '_id');
      const fileNames: PreviousFilePaths[] = [];
      branchFiles.filter((bf) => bf._from == branchID).forEach((bf) => {});
      return fileNames;
    } else {
      return new Promise<PreviousFilePaths[]>((resolve) => {
        const fileNames: PreviousFilePaths[] = [];
        resolve(fileNames);
      });
    }
  }
}
