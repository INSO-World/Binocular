import Commits from './commits.ts';
import { DataPlugin } from '../../../interfaces/dataPlugin.ts';
import General from './general.ts';
import Files from './files.ts';
import Users from './users.ts';
import Issues from './issues.ts';
import Builds from './builds.ts';

class BinocularBackend implements DataPlugin {
  public name = 'Binocular Backend';
  public description = 'Connection to the Binocular GraphQL Backend.';
  public capabilities = ['authors', 'commits', 'builds', 'files', 'issues'];
  public experimental = false;
  public requirements = {
    apiKey: false,
    endpoint: true,
    file: false,
  };
  public commits;
  public builds;
  public users;
  public issues;
  public general;
  public files;

  constructor() {
    this.commits = new Commits('/graphQl');
    this.builds = new Builds('/graphQl');
    this.users = new Users('/graphQl');
    this.issues = new Issues('/graphQl');
    this.general = new General(/*'/graphQl'*/);
    this.files = new Files('/graphQl');
  }

  public async init(apiKey: string | undefined, endpoint: string | undefined) {
    console.log(`Init Binocular Backend with ApiKey: ${apiKey} and Endpoint ${endpoint}`);
    if (endpoint === undefined || endpoint.length === 0) {
      endpoint = '/graphQl';
    }
    this.commits = new Commits(endpoint);
    this.builds = new Builds(endpoint);
    this.users = new Users(endpoint);
    this.issues = new Issues(endpoint);
    this.general = new General(/*endpoint*/);
    this.files = new Files(endpoint);
  }

  public async clearRemains() {}
}

export default BinocularBackend;
