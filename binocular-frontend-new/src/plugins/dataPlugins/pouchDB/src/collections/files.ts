import type {
  DataPluginFile,
  DataPluginFiles,
  PreviousFilePaths
} from '../../../../interfaces/dataPluginInterfaces/dataPluginFiles.ts';
import {
  binarySearch,
  binarySearchArray,
  findAll,
  findBranch,
  findBranchFileConnections,
  findBranchFileFileConnections,
  sortByAttributeString,
} from '../utils.ts';
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
      return findBranch(this.database.documentStore, branchName).then(async (resBranch) => {
        const result: PreviousFilePaths[] = [];
        const branch = resBranch.docs[0];
        //find all branch-file-file connections. These are connections between a branch-file edge and a file.
        // They tell us which files on a branch had another name in the past (since renaming a file effectively creates a new file)
        const branchFileFileConnections = sortByAttributeString(
          (await findBranchFileFileConnections(this.database!.edgeStore!)).docs,
          'from',
        );
        //find all files and extract the ones that are on this branch
        const files = (await findAll(this.database!.documentStore!, 'files')).docs;
        // find connections from this branch to files
        const branchFileConnections = (await findBranchFileConnections(this.database!.edgeStore!)).docs.filter(
          (connection) => connection.from === branch._id,
        );

        // for each connection, extract the file object and find all connections to other files (previous names)
        for (const branchFileConnection of branchFileConnections) {
          const currentFile = binarySearch(files, branchFileConnection.to, '_id') as unknown as DataPluginFile | null;
          if (currentFile !== null) {
            //get connections to other files
            const connectionsToOtherFiles = binarySearchArray(branchFileFileConnections, branchFileConnection._id, 'from') as {
              hasThisNameFrom: Date;
              hasThisNameUntil: Date;
              to: string;
            }[];

            //if there are no connections, go to the next bf connection
            if (connectionsToOtherFiles.length === 0) {
              continue;
            }
            //this object says the file with this path has had these previous filenames
            const resultObject: PreviousFilePaths = {
              path: currentFile.path,
              previousFileNames: [],
            };

            for (const branchFileFileConnection of connectionsToOtherFiles) {
              // get referenced file
              const referencedFile = binarySearch(files, branchFileFileConnection.to, '_id') as unknown as DataPluginFile | null;
              if (referencedFile !== null) {
                resultObject.previousFileNames.push({
                  oldFilePath: referencedFile.path,
                  hasThisNameFrom: branchFileFileConnection.hasThisNameFrom,
                  hasThisNameUntil: branchFileFileConnection.hasThisNameUntil,
                });
              }
            }
            result.push(resultObject);
          }
        }
        return result;
      });
    } else {
      return new Promise<PreviousFilePaths[]>((resolve) => {
        const fileNames: PreviousFilePaths[] = [];
        resolve(fileNames);
      });
    }
  }
}
