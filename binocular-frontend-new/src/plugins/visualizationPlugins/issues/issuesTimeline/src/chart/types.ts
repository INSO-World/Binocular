import type { Moment } from 'moment';
import type { DataPluginIssue } from '../../../../../interfaces/dataPluginInterfaces/dataPluginIssues';
import type { DataPluginMergeRequest } from '../../../../../interfaces/dataPluginInterfaces/dataPluginMergeRequests';

export type MappedDataPluginIssue = Omit<DataPluginIssue, 'createdAt' | 'closedAt' | 'labels'> & {
  createdAt: Moment;
  closedAt?: Moment;
  labels: { name: string; color: string }[];
};

export type MappedDataPluginMergeRequest = Omit<DataPluginMergeRequest, 'createdAt' | 'closedAt'> &
  Record<'createdAt' | 'closedAt', Moment>;
