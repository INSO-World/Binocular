import Builds from './visualizationPlugins/builds';

import MockData from './dataPlugins/mockData';
import BinocularBackend from './dataPlugins/binocularBackend';
import Github from './dataPlugins/github';
import PouchDb from './dataPlugins/pouchDB';

import { VisualizationPlugin } from './interfaces/visualizationPlugin.ts';
import Changes from './visualizationPlugins/changes';
import { DataPluginBuild } from './interfaces/dataPluginInterfaces/dataPluginBuilds.ts';
import createVisualizationPlugin from './visualizationPlugins/simpleVisualizationPlugin';
import { BuildSettings, ChangesSettings } from './visualizationPlugins/simpleVisualizationPlugin/src/settings/settings.tsx';
import { DataPluginCommit } from './interfaces/dataPluginInterfaces/dataPluginCommits.ts';
import ExampleComplex from './visualizationPlugins/exampleComplex';
import ExampleStats from './visualizationPlugins/exampleStats';
import ExampleVisualization from './visualizationPlugins/exampleVisualization';
import CodeOwnership from './visualizationPlugins/code-ownership';

// should currently work for commits, but fetching the data is still hardcoded to one or the other
const builds = createVisualizationPlugin<BuildSettings, DataPluginBuild>(Builds);
const changes = createVisualizationPlugin<ChangesSettings, DataPluginCommit>(Changes);

//The implicit type here has to be any because every Visualization plugin has a different settings type implied
//eslint-disable-next-line @typescript-eslint/no-explicit-any
export const visualizationPlugins: VisualizationPlugin<any, any>[] = [
  builds,
  CodeOwnership,
  changes,
  ExampleStats,
  ExampleVisualization,
  ExampleComplex,
];

//Order = priority used when nothing selected by the user.
export const dataPlugins = [MockData, BinocularBackend, PouchDb, Github];

// Separate Export for PouchDB Plugin to streamline Database loading
export const PouchDB = new PouchDb();
