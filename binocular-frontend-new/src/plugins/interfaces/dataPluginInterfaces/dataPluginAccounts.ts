import type { DataPluginUser } from './dataPluginUsers.ts';

export interface DataPluginAccounts {
  getAll: () => Promise<DataPluginAccount[]>;
  //TODO add type when tested
  saveAccountUserRelation: (relation: DataPluginAccount) => Promise<unknown>;
}

export interface DataPluginAccount {
  id: string;
  name: string;
  user: DataPluginUser | null;
  platform: string;
}
