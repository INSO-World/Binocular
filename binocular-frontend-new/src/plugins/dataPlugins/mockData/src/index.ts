import Commits from './commits.ts';
import { DataPlugin } from '../../../interfaces/dataPlugin.ts';
import Users from './users.ts';
import General from './general.ts';
import Files from './files.ts';
import Issues from './issues.ts';
import Builds from './builds.ts';

class MockData implements DataPlugin {
  public name = 'Mock Data';
  public description = 'Mocked Data for testing purposes.';
  public capabilities = ['authors', 'commits', 'files', 'builds', 'issues'];
  public experimental = false;
  public requirements = {
    apiKey: false,
    endpoint: false,
    file: false,
  };
  public commits;
  public builds;
  public users;
  public issues;
  public general;
  public files;

  constructor() {
    this.commits = new Commits();
    this.builds = new Builds();
    this.users = new Users();
    this.issues = new Issues();
    this.general = new General();
    this.files = new Files();
  }

  public async init() {}

  public async clearRemains() {}
}

export default MockData;
