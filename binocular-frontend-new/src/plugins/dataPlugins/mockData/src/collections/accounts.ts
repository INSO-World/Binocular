import type { DataPluginAccount, DataPluginAccounts } from '../../../../interfaces/dataPluginInterfaces/dataPluginAccounts.ts';
import accountData from '../data/accounts.json.zip';

const accounts = accountData as unknown as DataPluginAccount[];

export default class Accounts implements DataPluginAccounts {
  public async getAll() {
    return accounts;
  }

  public async saveAccountUserRelation(_relation: DataPluginAccount): Promise<boolean> {
    return true;
  }
}
