import Commits from './dataConnections/commits.ts';
import { DataPlugin } from '../../../interfaces/dataPlugin.ts';
import Users from './dataConnections/users.ts';
import General from './dataConnections/general.ts';
import Files from './dataConnections/files.ts';
import Builds from './dataConnections/builds.ts';
import Issues from './dataConnections/issues.ts';
import Issues from './dataConnections/notes.ts';

class MockData implements DataPlugin {
  public name = 'Mock Data';
  public description = 'Mocked Data for testing purposes.';
  public capabilities = ['authors', 'commits', 'files', 'issues', 'builds'];
  public experimental = false;
  public requirements = {
    apiKey: false,
    endpoint: false,
    file: false,
    progressUpdate: false,
  };
  public commits;
  public builds;
  public users;
  public general;
  public files;
  public issues;
  public notes;

  constructor() {
    this.commits = new Commits();
    this.builds = new Builds();
    this.users = new Users();
    this.general = new General();
    this.files = new Files();
    this.issues = new Issues();
    this.notes = new Notes();
  }

  public async init() {}

  public async clearRemains() {}
}

export default MockData;
