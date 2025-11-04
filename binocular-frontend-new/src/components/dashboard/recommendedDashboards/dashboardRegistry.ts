import type { DashboardLayout } from '../../../types/general/dashboardLayoutType';
import defaultDashboard from './defaultDashboard';
import ownershipDashboard from './ownershipDashboard';
import issueDashboard from './issueDashboard';

export const recommendLayouts: DashboardLayout[] = [defaultDashboard, ownershipDashboard, issueDashboard];
