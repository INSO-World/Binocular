import type { DashboardLayout } from '../../../types/general/dashboardLayoutType';
import defaultDashboard from './defaultDashboard';
import ownershipDashboard from './ownershipDashboard';
import issueDashboard from './issueDashboard';
import codeActivityDashboard from './codeActivityDashboard';
import deliveryHealthDashboard from './deliveryHealthDashboard';
import sprintHealthDashboard from './sprintHealthDashboard';
import teamCollaborationDashboard from './teamCollaborationDashboard';
import roadmapProgressDashboard from './roadmapProgressDashboard';
import buildQualityDashboard from './buildQualityDashboard';
import cicdPipelineDashboard from './cicdPipelineDashboard';
import engineeringHealthDashboard from './engineeringHealthDashboard';

export const recommendLayouts: DashboardLayout[] = [
  // Basic: frequently used, general-purpose dashboards
  defaultDashboard,
  ownershipDashboard,
  issueDashboard,
  codeActivityDashboard,
  deliveryHealthDashboard,
  sprintHealthDashboard,
  // Advanced: more specialized dashboards, used less often
  teamCollaborationDashboard,
  roadmapProgressDashboard,
  buildQualityDashboard,
  cicdPipelineDashboard,
  engineeringHealthDashboard,
];
