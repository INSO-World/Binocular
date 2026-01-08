import type { DataPluginCommit } from '../../../../../interfaces/dataPluginInterfaces/dataPluginCommits';
import type { DataPluginBuild } from '../../../../../interfaces/dataPluginInterfaces/dataPluginBuilds';
import type { DataPluginIssue } from '../../../../../interfaces/dataPluginInterfaces/dataPluginIssues';
import type { DataPluginMergeRequest } from '../../../../../interfaces/dataPluginInterfaces/dataPluginMergeRequests';
import type { DataPluginNote } from '../../../../../interfaces/dataPluginInterfaces/dataPluginNotes';
import type { DataPluginBranch } from '../../../../../interfaces/dataPluginInterfaces/dataPluginBranches';

export interface ActivityTimelineProps {
  data: Array<{ date: Date; value: number; tooltip?: string }>;
  startDate: Date;
  endDate: Date;
  minCellSize?: number;
  color?: string;
  cellPadding?: number;
  onCellClick?: ((cell: HeatmapCell) => void) | null;
  showLegend?: boolean;
  legendTitle?: string;
  width?: number;
  height?: number;
  scaleHorizontal?: boolean;
  scaleVertical?: boolean;
  containerWidth?: number;
  containerHeight?: number;
}

export interface HeatmapProps {
  data: HeatmapCell[];
  rowLabels: string[];
  colLabels: string[];
  minCellSize?: number;
  color?: string;
  cellPadding?: number;
  scaleHorizontal?: boolean;
  scaleVertical?: boolean;
  onCellClick?: ((cell: HeatmapCell) => void) | null;
  showLegend?: boolean;
  legendTitle?: string;
  containerWidth?: number;
  containerHeight?: number;
}
export interface HeatmapCell {
  row: number;
  col: number;
  value: number;
  tooltip?: string;
  metadata?: any; // TODO define a proper type here
}

export type AnyActivityDataPlugin =
  | DataPluginCommit
  | DataPluginBuild
  | DataPluginIssue
  | DataPluginMergeRequest
  | DataPluginNote
  | DataPluginBranch;

export function isDataPluginCommit(d: any): d is DataPluginCommit {
  return d && typeof d === 'object' && 'sha' in d && 'messageHeader' in d && 'stats' in d;
}

export function isDataPluginBuild(d: any): d is DataPluginBuild {
  return d && typeof d === 'object' && 'status' in d && 'webUrl' in d;
}

export function isDataPluginIssue(d: any): d is DataPluginIssue {
  return d && typeof d === 'object' && 'iid' in d && 'title' in d && 'state' in d && !('mergedAt' in d);
}

export function isDataPluginMergeRequest(d: any): d is DataPluginMergeRequest {
  return d && typeof d === 'object' && 'iid' in d && 'title' in d && 'mergedAt' in d;
}

export function isDataPluginNote(d: any): d is DataPluginNote {
  return d && typeof d === 'object' && 'body' in d && 'noteableType' in d;
}

export function isDataPluginBranch(d: any): d is DataPluginBranch {
  return d && typeof d === 'object' && 'branch' in d && 'active' in d;
}

export type ActivityType = 'commit' | 'build' | 'issue' | 'mergeRequest' | 'note' | 'branch' | 'unknown';

export interface ActivityTypeInfo {
  type: ActivityType;
  singular: string;
  plural: string;
}

export const ACTIVITY_TYPES: Record<ActivityType, ActivityTypeInfo> = {
  commit: { type: 'commit', singular: 'commit', plural: 'commits' },
  build: { type: 'build', singular: 'build', plural: 'builds' },
  issue: { type: 'issue', singular: 'issue', plural: 'issues' },
  mergeRequest: { type: 'mergeRequest', singular: 'merge request', plural: 'merge requests' },
  note: { type: 'note', singular: 'note', plural: 'notes' },
  branch: { type: 'branch', singular: 'branch', plural: 'branches' },
  unknown: { type: 'unknown', singular: 'activity', plural: 'activities' },
};

export function getActivityType(d: AnyActivityDataPlugin): ActivityType {
  if (isDataPluginCommit(d)) return 'commit';
  if (isDataPluginBuild(d)) return 'build';
  if (isDataPluginMergeRequest(d)) return 'mergeRequest'; // Check before issue since MR has more specific fields
  if (isDataPluginIssue(d)) return 'issue';
  if (isDataPluginNote(d)) return 'note';
  if (isDataPluginBranch(d)) return 'branch';
  return 'unknown';
}

export function getActivityDate(d: AnyActivityDataPlugin): Date | null {
  if (isDataPluginCommit(d)) return new Date(d.date);
  if (isDataPluginBuild(d)) return new Date(d.createdAt);
  if (isDataPluginIssue(d)) return new Date(d.createdAt);
  if (isDataPluginMergeRequest(d)) return new Date(d.createdAt);
  if (isDataPluginNote(d)) return new Date(d.createdAt);
  if (isDataPluginBranch(d)) return d.latestCommit ? new Date(d.latestCommit) : null;
  return null;
}

export function formatActivityCounts(counts: Record<ActivityType, number>): string {
  const parts: string[] = [];
  for (const [type, count] of Object.entries(counts)) {
    if (count > 0) {
      const info = ACTIVITY_TYPES[type as ActivityType];
      parts.push(`${count} ${count === 1 ? info.singular : info.plural}`);
    }
  }
  return parts.length > 0 ? parts.join(', ') : '0 activities';
}
