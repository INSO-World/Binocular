import Commits from './visualizationPlugins/changes';
import Builds from './visualizationPlugins/builds';
import ExampleVisualization from './visualizationPlugins/exampleVisualization';
import ExampleStats from './visualizationPlugins/exampleStats';
import ExampleComplex from './visualizationPlugins/exampleComplex';
import RepositoryStats from './visualizationPlugins/respositoryStats';

import MockData from './dataPlugins/mockData';
import BinocularBackend from './dataPlugins/binocularBackend';
import Github from './dataPlugins/github';
import PouchDb from './dataPlugins/pouchDB';

import { VisualizationPlugin } from './interfaces/visualizationPlugin.ts';

//The implicit type here has to be any because every Visualization plugin has a different settings type implied
//eslint-disable-next-line @typescript-eslint/no-explicit-any
export const visualizationPlugins: VisualizationPlugin<any>[] = [
  Commits,
  Builds,
  RepositoryStats,
  ExampleVisualization,
  ExampleStats,
  ExampleComplex,
];

//Order = priority used when nothing selected by the user.
export const dataPlugins = [MockData, BinocularBackend, PouchDb, Github];
