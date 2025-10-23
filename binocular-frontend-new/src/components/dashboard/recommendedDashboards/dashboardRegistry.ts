import type { DashboardLayout } from '../../../types/general/dashboardLayoutType';
import defaultDashboard from './defaultDashboard';
import ownershipDashboard from './ownershipDashboard';

export const recommendLayouts: DashboardLayout[] = [defaultDashboard, ownershipDashboard];
