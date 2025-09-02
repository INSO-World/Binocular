import type { DataPluginStats } from './dataPluginCommits.ts';
import type { PreviousFileData } from '../../../types/data/ownershipType.ts';

export interface DataPluginFiles {
  getAll: () => Promise<DataPluginFile[]>;
  getFilenamesForBranch: (branchName: string) => Promise<string[]>;
  getPreviousFilenamesForFilesOnBranch: (branchName: string) => Promise<PreviousFilePaths[]>;
}

export interface DataPluginFile {
  path: string;
  webUrl: string;
  maxLength: number;
}

export interface DataPluginFileInCommit {
  file: DataPluginFile;
  hunks: DataPluginHunk[];
}

export interface DataPluginHunk {
  oldStart: number;
  oldLines: number;
  newStart: number;
  newLines: number;
}

export interface FileConfig {
  name: string | undefined;
  file: File | undefined;
  dbObjects: { [key: string]: JSONObject[] } | undefined;
}

export interface JSONObject {
  [key: string]: string | string[] | boolean | number | DataPluginStats | object | null;
}

export interface PreviousFilePaths {
  path: string;
  previousFileNames: PreviousFileData[];
}
