import type { DataPluginUser } from './dataPluginUsers.ts';

export interface DataPluginAccounts {
  getAll: () => Promise<DataPluginAccount[]>;
  saveAccountUserRelation: (relation: DataPluginAccount) => Promise<DataPluginAccount[]>;
}

export interface DataPluginAccount {
  id: string;
  name: string;
  user: DataPluginUser | null;
  platform: string;
}
