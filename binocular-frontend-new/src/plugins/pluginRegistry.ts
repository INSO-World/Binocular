import Changes from './visualizationPlugins/changes';
import Builds from './visualizationPlugins/builds';
import Issues from './visualizationPlugins/issues';
import TimeSpent from './visualizationPlugins/timeSpent';
import CodeOwnership from './visualizationPlugins/code-ownership';
import FileChanges from './visualizationPlugins/fileChanges';

import ExampleComplex from './visualizationPlugins/exampleComplex';
import ExampleStats from './visualizationPlugins/exampleStats';
import ExampleVisualization from './visualizationPlugins/exampleVisualization';

import { DataPluginCommit } from './interfaces/dataPluginInterfaces/dataPluginCommits.ts';
import { DataPluginBuild } from './interfaces/dataPluginInterfaces/dataPluginBuilds.ts';
import { DataPluginIssue } from './interfaces/dataPluginInterfaces/dataPluginIssues.ts';
import { DataPluginNote } from './interfaces/dataPluginInterfaces/dataPluginNotes.ts';

import MockData from './dataPlugins/mockData';
import BinocularBackend from './dataPlugins/binocularBackend';
import Github from './dataPlugins/github';
import PouchDb from './dataPlugins/pouchDB';

import { VisualizationPlugin } from './interfaces/visualizationPlugin.ts';
import createVisualizationPlugin from './visualizationPlugins/simpleVisualizationPlugin';
import { IssueSettings } from './visualizationPlugins/issues/src/settings/settings.tsx';
import { BuildSettings } from './visualizationPlugins/builds/src/settings/settings.tsx';
import { TimeSpentSettings } from './visualizationPlugins/timeSpent/src/settings/settings.tsx';
import { ChangesSettings } from './visualizationPlugins/changes/src/settings/settings.tsx';
import KnowledgeRadar from './visualizationPlugins/knowledge-radar';
import CodeExpertise from './visualizationPlugins/code-expertise';

// should currently work for commits, but fetching the data is still hardcoded to one or the other
const changes = createVisualizationPlugin<ChangesSettings, DataPluginCommit>(Changes);
const builds = createVisualizationPlugin<BuildSettings, DataPluginBuild>(Builds);
const issues = createVisualizationPlugin<IssueSettings, DataPluginIssue>(Issues);
const timeSpent = createVisualizationPlugin<TimeSpentSettings, DataPluginNote>(TimeSpent);

//The implicit type here has to be any because every Visualization plugin has a different settings type implied
//eslint-disable-next-line @typescript-eslint/no-explicit-any
export const visualizationPlugins: VisualizationPlugin<any, any>[] = [
  changes,
  builds,
  issues,
  timeSpent,
  CodeOwnership,
  FileChanges,
  ExampleStats,
  ExampleVisualization,
  ExampleComplex,
  KnowledgeRadar,
  CodeExpertise,
];

//Order = priority used when nothing selected by the user.
export const dataPlugins = [BinocularBackend, PouchDb, MockData, Github];

// Separate Export for PouchDB Plugin to streamline Database loading
export const PouchDB = new PouchDb();
