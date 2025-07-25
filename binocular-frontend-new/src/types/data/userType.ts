import { AccountType } from './accountType.ts';

export interface UserType {
  id: string;
  gitSignature: string;
  account: AccountType | null;
}
