export interface TabType {
  selected: boolean;
  contentID: number;
  displayName: string;
  alignment: TabAlignment;
  position: number;
}

export enum TabAlignment {
  top,
  right,
  bottom,
  left,
}
