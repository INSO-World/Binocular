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
}
