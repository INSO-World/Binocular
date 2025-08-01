import { DataPluginNote, DataPluginNotes } from '../../../../interfaces/dataPluginInterfaces/dataPluginNotes.ts';
import Database from '../database';
import { findAllNotes } from '../utils.ts';

export default class Notes implements DataPluginNotes {
  public database: Database | undefined;
  constructor(database: Database | undefined) {
    this.database = database;
  }

  public async getAll(from: string, to: string) {
    console.log(`Getting Notes from ${from} to ${to}`);
    const first = new Date(from).getTime();
    const last = new Date(to).getTime();
    if (this.database && this.database.documentStore && this.database.edgeStore) {
      return findAllNotes(this.database.documentStore, this.database.edgeStore).then((res: { docs: unknown[] }) => {
        res.docs = (res.docs as DataPluginNote[])
          .filter((c) => new Date(c.createdAt).getTime() >= first && new Date(c.createdAt).getTime() <= last)
          .sort((a, b) => {
            return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
          });
        return res.docs as unknown as DataPluginNote[];
      });
    } else {
      return new Promise<DataPluginNote[]>((resolve) => {
        const notes: DataPluginNote[] = [];
        resolve(notes);
      });
    }
  }
}
