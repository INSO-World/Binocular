import Changes from './visualizationPlugins/changes';
import Builds from './visualizationPlugins/builds';
import CommitByFile from './visualizationPlugins/commitByFile';
import Issues from './visualizationPlugins/issues';
import TimeSpent from './visualizationPlugins/timeSpent';
import CodeOwnership from './visualizationPlugins/code-ownership';
import FileChanges from './visualizationPlugins/fileChanges';
import RepositoryStats from './visualizationPlugins/respositoryStats';
import MergeRequests from './visualizationPlugins/mergeRequests';
import CodeHotspots from './visualizationPlugins/codeHotspots';

import ExampleComplex from './visualizationPlugins/exampleComplex';
import ExampleStats from './visualizationPlugins/exampleStats';
import ExampleVisualization from './visualizationPlugins/exampleVisualization';

import type { DataPluginCommit } from './interfaces/dataPluginInterfaces/dataPluginCommits.ts';
import type { DataPluginBuild } from './interfaces/dataPluginInterfaces/dataPluginBuilds.ts';
import type { DataPluginIssue } from './interfaces/dataPluginInterfaces/dataPluginIssues.ts';
import type { DataPluginNote } from './interfaces/dataPluginInterfaces/dataPluginNotes.ts';

import MockData from './dataPlugins/mockData';
import BinocularBackend from './dataPlugins/binocularBackend';
import Github from './dataPlugins/github';
import PouchDb from './dataPlugins/pouchDB';

import type { VisualizationPlugin } from './interfaces/visualizationPlugin.ts';
import createVisualizationPlugin from './visualizationPlugins/simpleVisualizationPlugin';
import type { IssueSettings } from './visualizationPlugins/issues/src/settings/settings.tsx';
import type { BuildSettings } from './visualizationPlugins/builds/src/settings/settings.tsx';
import type { TimeSpentSettings } from './visualizationPlugins/timeSpent/src/settings/settings.tsx';
import type { ChangesSettings } from './visualizationPlugins/changes/src/settings/settings.tsx';
import type { DataPluginMergeRequest } from './interfaces/dataPluginInterfaces/dataPluginMergeRequests.ts';
import type { MergeRequestsSettings } from './visualizationPlugins/mergeRequests/src/settings/settings.tsx';
import CollaborationVisualization from './visualizationPlugins/collaboration';
import KnowledgeRadar from './visualizationPlugins/knowledge-radar';
import CodeExpertise from './visualizationPlugins/code-expertise';

// should currently work for commits, but fetching the data is still hardcoded to one or the other
const changes = createVisualizationPlugin<ChangesSettings, DataPluginCommit>(Changes);
const builds = createVisualizationPlugin<BuildSettings, DataPluginBuild>(Builds);
const issues = createVisualizationPlugin<IssueSettings, DataPluginIssue>(Issues);
const mergeRequest = createVisualizationPlugin<MergeRequestsSettings, DataPluginMergeRequest>(MergeRequests);
const timeSpent = createVisualizationPlugin<TimeSpentSettings, DataPluginNote>(TimeSpent);

//The implicit type here has to be any because every Visualization plugin has a different settings type implied
//eslint-disable-next-line @typescript-eslint/no-explicit-any
export const visualizationPlugins: VisualizationPlugin<any, any>[] = [
  changes,
  builds,
  issues,
  mergeRequest,
  timeSpent,
  RepositoryStats,
  CodeOwnership,
  FileChanges,
  ExampleStats,
  ExampleVisualization,
  ExampleComplex,
  CommitByFile,
  CollaborationVisualization,
  KnowledgeRadar,
  CodeExpertise,
  CodeHotspots,
];

//Order = priority used when nothing selected by the user.
export const dataPlugins = [BinocularBackend, PouchDb, MockData, Github];

// Separate Export for PouchDB Plugin to streamline Database loading
export const PouchDB = new PouchDb();
