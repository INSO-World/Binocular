import Commits from './commits.ts';
import { DataPlugin } from '../../../interfaces/dataPlugin.ts';
import General from './general.ts';
import Files from './files.ts';
import Users from './users.ts';
import JacocoReports from './jacocoReports.ts';

class BinocularBackend implements DataPlugin {
  public name = 'Binocular Backend';
  public description = 'Connection to the Binocular GraphQL Backend.';
  public capabilities = ['authors', 'commits', 'files', 'artifacts'];
  public experimental = false;
  public requirements = {
    apiKey: false,
    endpoint: true,
    file: false,
  };
  public commits;
  public users;
  public general;
  public files;
  public jacocoReports;

  constructor() {
    this.commits = new Commits('/graphQl');
    this.users = new Users('/graphQl');
    this.general = new General(/*'/graphQl'*/);
    this.files = new Files('/graphQl');
    this.jacocoReports = new JacocoReports('/graphQl');
  }

  public async init(apiKey: string | undefined, endpoint: string | undefined) {
    console.log(`Init Binocular Backend with ApiKey: ${apiKey} and Endpoint ${endpoint}`);
    if (endpoint === undefined || endpoint.length === 0) {
      endpoint = '/graphQl';
    }
    this.commits = new Commits(endpoint);
    this.users = new Users(endpoint);
    this.general = new General(/*endpoint*/);
    this.files = new Files(endpoint);
    this.jacocoReports = new JacocoReports(endpoint);
  }

  public async clearRemains() {}
}

export default BinocularBackend;
