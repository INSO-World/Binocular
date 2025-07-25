import Commits from './dataConnections/commits.ts';
import { DataPlugin } from '../../../interfaces/dataPlugin.ts';
import General from './dataConnections/general.ts';
import Files from './dataConnections/files.ts';
import Users from './dataConnections/users.ts';
import { FileConfig } from '../../../interfaces/dataPluginInterfaces/dataPluginFiles.ts';
import { ProgressUpdateConfig } from '../../../../types/settings/databaseSettingsType.ts';
import Builds from './dataConnections/builds.ts';
import Branches from './dataConnections/branches.ts';
import Issues from './dataConnections/issues.ts';
import Notes from './dataConnections/notes.ts';
import Accounts from './dataConnections/accounts.ts';

class BinocularBackend implements DataPlugin {
  public name = 'Binocular Backend';
  public description = 'Connection to the Binocular GraphQL Backend.';
  public capabilities = ['authors', 'commits', 'files'];
  public experimental = false;
  public requirements = {
    apiKey: false,
    endpoint: true,
    file: false,
    progressUpdate: true,
  };
  public commits;
  public builds;
  public users;
  public accounts;
  public issues;
  public notes;
  public general;
  public files;
  public branches;

  constructor() {
    this.commits = new Commits('/graphQl');
    this.builds = new Builds('/graphQl');
    this.users = new Users('/graphQl');
    this.accounts = new Accounts('/graphQl');
    this.issues = new Issues('/graphQl');
    this.notes = new Notes('/graphQl');
    this.general = new General('/graphQl', undefined);
    this.files = new Files('/graphQl');
    this.branches = new Branches('/graphQl');
  }

  // eslint-disable-next-line @typescript-eslint/require-await
  public async init(
    apiKey: string | undefined,
    endpoint: string | undefined,
    _file: FileConfig | undefined,
    progressUpdateConfig: ProgressUpdateConfig | undefined,
  ) {
    console.log(`Init Binocular Backend with ApiKey: ${apiKey} and Endpoint ${endpoint}`);
    if (endpoint === undefined || endpoint.length === 0) {
      endpoint = '/graphQl';
    }
    this.commits = new Commits(endpoint);
    this.builds = new Builds(endpoint);
    this.users = new Users(endpoint);
    this.accounts = new Accounts(endpoint);
    this.issues = new Issues(endpoint);
    this.notes = new Notes(endpoint);
    this.general = new General(endpoint, progressUpdateConfig);
    this.files = new Files(endpoint);
  }

  public async clearRemains() {}
}

export default BinocularBackend;
