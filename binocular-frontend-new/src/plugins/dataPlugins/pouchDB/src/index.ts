import Commits from './collections/commits.ts';
import { DataPlugin } from '../../../interfaces/dataPlugin.ts';
import Users from './collections/users.ts';
import General from './general.ts';
import Files from './collections/files.ts';
import Database from './database.ts';
import { FileConfig } from '../../../interfaces/dataPluginInterfaces/dataPluginFiles.ts';
import Builds from './collections/builds.ts';
import Notes from './collections/notes.ts';
import Issues from './collections/issues.ts';
import Accounts from './collections/accounts.ts';

class PouchDb implements DataPlugin {
  public name = 'PouchDb';
  public description =
    'PouchDB browser based database that is able to import a database exported by Binocular packed as a Zip File. It is also possible to pre compile this database into Binocular through the frontend build process.';
  public capabilities = ['authors', 'commits', 'files'];
  public experimental = false;
  public requirements = {
    apiKey: false,
    endpoint: false,
    file: true,
    progressUpdate: false,
  };
  public commits;
  public builds;
  public users;
  public accounts;
  public general;
  public files;
  public notes;
  public issues;

  private readonly database;

  constructor() {
    this.commits = new Commits(undefined);
    this.builds = new Builds(undefined);
    this.notes = new Notes(undefined);
    this.issues = new Issues(undefined);
    this.users = new Users(undefined);
    this.accounts = new Accounts(undefined);
    this.general = new General();
    this.files = new Files(undefined);
    this.database = new Database();
  }

  public async init(_apiKey: string | undefined, _endpoint: string | undefined, file: FileConfig | undefined) {
    if (file !== undefined) {
      await this.database.initDB(file);
      this.commits = new Commits(this.database);
      this.builds = new Builds(this.database);
      this.notes = new Notes(this.database);
      this.issues = new Issues(this.database);
      this.users = new Users(this.database);
      this.accounts = new Accounts(this.database);
      this.general = new General();
      this.files = new Files(this.database);
    }
  }

  public async clearRemains() {
    await this.database.delete();
  }
}

export default PouchDb;
