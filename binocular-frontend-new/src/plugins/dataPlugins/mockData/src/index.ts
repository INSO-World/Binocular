import Commits from './commits.ts';
import { DataPlugin } from '../../../interfaces/dataPlugin.ts';
import Users from './users.ts';
import General from './general.ts';
import Files from './files.ts';
import Builds from './builds.ts';
import CommitsFiles from './commitsFiles.ts';

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
  public users;
  public general;
  public files;
  public commitByFile;

  constructor() {
    this.commits = new Commits();
    this.builds = new Builds();
    this.users = new Users();
    this.general = new General();
    this.files = new Files();
    this.commitByFile = new CommitsFiles();
  }

  public async init() {}

  public async clearRemains() {}
}

export default MockData;
