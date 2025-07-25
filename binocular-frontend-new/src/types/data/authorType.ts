import { UserType } from './userType.ts';

export interface AuthorType {
  user: UserType;
  id: number;
  parent: number;
  color: { main: string; secondary: string };
  selected: boolean;
  displayName?: string;
}

export interface Palette {
  [signature: string]: string;
}
