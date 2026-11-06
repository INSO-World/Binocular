'use strict';

import Connection from '../Connection.ts';
import Account, { AccountDataType } from '../models/Account.ts';
import User, { UserDataType } from '../models/User.ts';

export interface AccountUserConnectionDataType {}

class AccountUserConnection extends Connection<AccountUserConnectionDataType, AccountDataType, UserDataType> {
  constructor() {
    super();
  }

  ensureCollection() {
    return super.ensureCollection(Account, User);
  }
}
export default new AccountUserConnection();
