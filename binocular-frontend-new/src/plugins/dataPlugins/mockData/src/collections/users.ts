import type { DataPluginUser, DataPluginUsers } from '../../../../interfaces/dataPluginInterfaces/dataPluginUsers.ts';
import userData from '../data/users.json.zip';

const users = userData as unknown as DataPluginUser[];

export default class Users implements DataPluginUsers {
  public async getAll() {
    return users;
  }
}
