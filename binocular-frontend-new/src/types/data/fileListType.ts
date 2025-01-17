import { DataPluginFile } from '../../plugins/interfaces/dataPluginInterfaces/dataPluginFiles.ts';

export interface FileListElementType {
  name: string;
  id?: number;
  type: FileListElementTypeType;
  element?: DataPluginFile;
  children?: FileListElementType[];
  checked: boolean;
  foldedOut: boolean;
}

export enum FileListElementTypeType {
  Folder,
  File,
}
