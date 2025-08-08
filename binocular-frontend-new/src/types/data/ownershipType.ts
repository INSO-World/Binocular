import type { DataPluginFileOwnership } from '../../plugins/interfaces/dataPluginInterfaces/dataPluginCommits.ts';

export interface OwnershipData {
  sha: string;
  date: Date;
  parents?: string[];
  files?: FileData[];
  ownership: { [id: string]: number };
}

export interface OwnershipResult {
  user: string;
  ownedLines: number;
}

export interface FileData {
  path: string;
  action: string;
  filename: string;
  ownership: DataPluginFileOwnership[];
}

export interface FileOwnershipCollection {
  [id: string]: OwnershipResult[];
}

export interface PreviousFileData {
  oldFilePath: string;
  hasThisNameFrom: Date;
  hasThisNameUntil: Date;
}
