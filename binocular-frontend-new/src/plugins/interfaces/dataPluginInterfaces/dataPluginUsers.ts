import type { DataPluginAccount } from './dataPluginAccounts.ts';

export interface DataPluginUsers {
  getAll: () => Promise<DataPluginUser[]>;
}

export interface DataPluginUser {
  id: string;
  gitSignature: string;
  account: DataPluginAccount | null;
}
