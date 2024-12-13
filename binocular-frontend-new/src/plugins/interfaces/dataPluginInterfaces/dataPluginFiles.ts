import { DataPluginStats } from './dataPluginCommits.ts';

export interface DataPluginFiles {
  getAll: () => Promise<DataPluginFile[]>;
}

export interface DataPluginFile {
  path: string;
  webUrl: string;
  maxLength: number;
}

export interface FileConfig {
  name: string | undefined;
  file: File | undefined;
  dbObjects: { [key: string]: JSONObject[] } | undefined;
}

export interface JSONObject {
  [key: string]: string | string[] | boolean | number | DataPluginStats | object | null;
}
