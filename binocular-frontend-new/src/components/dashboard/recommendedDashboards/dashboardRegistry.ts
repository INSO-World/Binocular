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
import allVisualizationsDashboard from './allVisualizationsDashboard';

// Flip to true to surface Debug-category dashboards (e.g. allVisualizationsDashboard) in the layout picker.
const SHOW_DEBUG_DASHBOARDS = false;

export const recommendLayouts: DashboardLayout[] = [
  // Basic: frequently used, general-purpose dashboards
  defaultDashboard,
  ownershipDashboard,
  issueDashboard,
  codeActivityDashboard,
  buildQualityDashboard,
  sprintHealthDashboard,
  // Advanced: more specialized dashboards
  teamCollaborationDashboard,
  roadmapProgressDashboard,
  deliveryHealthDashboard,
  cicdPipelineDashboard,
  engineeringHealthDashboard,
  // Debug: development-only dashboards, hidden unless SHOW_DEBUG_DASHBOARDS is enabled
  ...(SHOW_DEBUG_DASHBOARDS ? [allVisualizationsDashboard] : []),
];
