import { FileType } from './fileType.ts';

export interface FileListElementType {
  name: string;
  id?: number;
  type: FileListElementTypeType;
  element: FileType;
  children?: FileListElementType[];
}

export enum FileListElementTypeType {
  Folder,
  File,
}
