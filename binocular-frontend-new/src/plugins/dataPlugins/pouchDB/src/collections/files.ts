import { DataPluginFile, DataPluginFiles } from '../../../../interfaces/dataPluginInterfaces/dataPluginFiles.ts';
import { findAll } from '../utils.ts';
import Database from '../database.ts';

export default class Files implements DataPluginFiles {
  private readonly database: Database | undefined;
  constructor(database: Database | undefined) {
    this.database = database;
  }

  public async getAll() {
    console.log(`Getting Authors`);
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
}
