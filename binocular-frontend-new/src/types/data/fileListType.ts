import { DataPluginFile } from '../../plugins/interfaces/dataPluginInterfaces/dataPluginFiles.ts';

export interface FileListElementType {
  name: string;
  id?: number;
  type: FileListElementTypeType;
  element?: DataPluginFile;
  children?: FileListElementType[];
  searchTerm?: string;
  checked: boolean;
  foldedOut: boolean;
  isRoot: boolean;
}

export enum FileListElementTypeType {
  Folder,
  File,
}
