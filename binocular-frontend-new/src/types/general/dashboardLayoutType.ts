import type { DashboardItemType } from './dashboardItemType.ts';

export interface DashboardLayout {
  id?: number;
  name: string;
  category: DashboardLayoutCategory;
  items: DashboardItemType[];
}

export enum DashboardLayoutCategory {
  BASIC = 'Basic',
  CUSTOM = 'Custom',
}
