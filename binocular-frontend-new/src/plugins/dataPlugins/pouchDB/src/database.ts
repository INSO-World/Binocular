import PouchDB from 'pouchdb-browser';
import PouchDBFind from 'pouchdb-find';
import PouchDBAdapterMemory from 'pouchdb-adapter-memory';
import JSZip from 'jszip';

import { WorkerPouchDB } from './worker/WorkerPouchDB';
import { decompressJson } from '../../../../../../utils/json-utils';
import type { FileConfig, JSONObject } from '../../../interfaces/dataPluginInterfaces/dataPluginFiles';

PouchDB.plugin(PouchDBFind);
PouchDB.plugin(PouchDBAdapterMemory);

export default class Database {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public documentStore: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public edgeStore: any;

  async initDB(file: FileConfig, startTime?: number) {
    if (!file.name) return;

    const initialized = await this.createDB(file.name);

    if (!initialized) return false;

    if (file.file) {
      return this.importFromZip(file.file, startTime);
    }

    if (file.dbObjects) {
      return this.importFromObjects(file.dbObjects, startTime);
    }
  }

  async delete() {
    await this.documentStore?.destroy();
    await this.edgeStore?.destroy();
  }

  private async createDB(name: string) {
    const useWorker = typeof Worker !== 'undefined';

    if (useWorker) {
      this.documentStore = new WorkerPouchDB(`${name}_documents`);
      this.edgeStore = new WorkerPouchDB(`${name}_edges`);
    } else {
      this.documentStore = new PouchDB(`${name}_documents`, { adapter: 'memory' });
      this.edgeStore = new PouchDB(`${name}_edges`, { adapter: 'memory' });
    }

    const d = await this.documentStore.info();
    const e = await this.edgeStore.info();

    return !(d.doc_count > 0 && e.doc_count > 0);
  }

  preprocess(coll: JSONObject[]) {
    return coll.map((row) => {
      // key and rev not needed for pouchDB
      delete row._key;
      delete row._rev;
      // rename _from/_to if this is a connection
      if (row._from) {
        row.from = row._from;
        row.to = row._to;
        delete row._from;
        delete row._to;
      }
      return row;
    });
  }

  async importDocument(name: string, content: JSONObject[]) {
    // first decompress the json file, then remove attributes that are not needed by PouchDB
    const docs = this.preprocess(decompressJson(name, content));
    await this.documentStore.bulkDocs(docs);
  }

  async importEdge(name: string, content: JSONObject[]) {
    // first decompress the json file, then remove attributes that are not needed by PouchDB
    const docs = this.preprocess(decompressJson(name, content));
    await this.edgeStore.bulkDocs(docs);
  }

  // both import functions are not running in parallel to avoid overloading pouchDB(testing necessary before changing to parallel)
  async importFromZip(file: Blob, startTime?: number) {
    const zip = await new JSZip().loadAsync(file);

    // Filter out folders
    const fileEntries = Object.values(zip.files).filter((f) => !f.dir);
    const totalFiles = fileEntries.length;
    let imported = 0;

    for (const fileEntry of fileEntries) {
      const raw = await fileEntry.async('string');
      const json = JSON.parse(raw);

      const name = fileEntry.name.split('/')[1].replace('.json', '');

      if (name.includes('-')) {
        await this.importEdge(name, json);
      } else {
        await this.importDocument(name, json);
      }

      imported++;
      const end = performance.now();
      console.log(`${imported}/${totalFiles} ${name} imported in ${Math.trunc(end - (startTime ?? end))} ms`);
    }
    return true; // all files processed
  }

  async importFromObjects(dbObjects: Record<string, JSONObject[]>, startTime?: number) {
    const keys = Object.keys(dbObjects);
    let imported = 0;

    return new Promise((resolve) => {
      keys.forEach(async (name) => {
        if (name.includes('-')) {
          await this.importEdge(name, dbObjects[name]);
        } else {
          await this.importDocument(name, dbObjects[name]);
        }

        imported++;
        const end = performance.now();
        console.log(`${imported}/${keys.length} ${name} imported in ${Math.trunc(end - (startTime ?? end))} ms`);

        if (imported >= keys.length) resolve(true);
      });
    });
  }
}
