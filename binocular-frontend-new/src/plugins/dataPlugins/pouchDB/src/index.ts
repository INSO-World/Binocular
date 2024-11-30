import Commits from './collections/commits.ts';
import { DataPlugin } from '../../../interfaces/dataPlugin.ts';
import Users from './collections/users.ts';
import General from './general.ts';
import Files from './collections/files.ts';
import { Database } from './database.ts';
import Builds from './collections/builds.ts';
import Issues from './collections/issues.ts';

class PouchDb implements DataPlugin {
  public name = 'PouchDb';
  public description = 'PouchDB browser based database that is able to import a database exported by Binocular packed as a Zip File.';
  public capabilities = ['authors', 'commits', 'files', 'builds', 'issues'];
  public experimental = false;
  public requirements = {
    apiKey: false,
    endpoint: false,
    file: true,
  };
  public commits;
  public builds;
  public users;
  public issues;
  public general;
  public files;

  private database;

  constructor() {
    this.commits = new Commits(undefined);
    this.builds = new Builds(undefined);
    this.users = new Users(undefined);
    this.issues = new Issues(undefined);
    this.general = new General();
    this.files = new Files();
    this.database = new Database();
  }

  public async init(
    _apiKey: string | undefined,
    _endpoint: string | undefined,
    file: { name: string | undefined; file: File | undefined } | undefined,
  ) {
    if (file !== undefined) {
      await this.database.init(file);
      this.commits = new Commits(this.database);
      this.builds = new Builds(this.database);
      this.users = new Users(this.database);
      this.issues = new Issues(this.database);
      this.general = new General();
      this.files = new Files();
    }
  }

  public async clearRemains() {
    await this.database.delete();
  }
}

export default PouchDb;
