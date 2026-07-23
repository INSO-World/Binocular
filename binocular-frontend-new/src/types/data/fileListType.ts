import type { DataPluginFile } from '../../plugins/interfaces/dataPluginInterfaces/dataPluginFiles.ts';

export interface FileListElementType {
  element: DataPluginFile;
  checked: boolean;
}
export interface FileTreeElementType {
  name: string;
  id?: number;
  type: FileTreeElementTypeType;
  element?: DataPluginFile;
  children?: FileTreeElementType[];
  searchTerm?: string;
  checked: boolean;
  foldedOut: boolean;
  isRoot: boolean;
}

export enum FileTreeElementTypeType {
  Folder = 'Folder',
  File = 'File',
}
