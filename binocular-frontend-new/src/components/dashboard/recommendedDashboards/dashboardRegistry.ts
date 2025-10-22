import type { DashboardLayout } from '../../../types/general/dashboardLayoutType';
import defaultDashboard from './defaultDashboard';
import ownershipDashboard from './ownershipDashboard';

export const recommendedDashboards: DashboardLayout[] = [defaultDashboard, ownershipDashboard, ownershipDashboard, ownershipDashboard];
// TODO replace duplicates with new dashboards
