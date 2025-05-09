import Commits from './dataConnections/commits.ts';
import { DataPlugin } from '../../../interfaces/dataPlugin.ts';
import Users from './dataConnections/users.ts';
import General from './dataConnections/general.ts';
import Files from './dataConnections/files.ts';
import Builds from './dataConnections/builds.ts';
import Issues from './dataConnections/issues.ts';

class MockData implements DataPlugin {
  public name = 'Mock Data';
  public description = 'Mocked Data for testing purposes.';
  public capabilities = ['authors', 'commits', 'files'];
  public experimental = false;
  public requirements = {
    apiKey: false,
    endpoint: false,
    file: false,
    progressUpdate: false,
  };
  public commits;
  public builds;
  public issues;
  public users;
  public general;
  public files;

  constructor() {
    this.commits = new Commits();
    this.builds = new Builds();
    this.issues = new Issues();
    this.users = new Users();
    this.general = new General();
    this.files = new Files();
  }

  public async init() {}

  public async clearRemains() {}
}

export default MockData;
