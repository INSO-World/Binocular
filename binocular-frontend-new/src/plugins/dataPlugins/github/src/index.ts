import Commits from './commits.ts';
import type { DataPlugin } from '../../../interfaces/dataPlugin.ts';
import Users from './users.ts';
import General from './general.ts';
import Files from './files.ts';
import Builds from './builds.ts';

class Github implements DataPlugin {
  public name = 'Github';
  public description = 'Connect directly to the github API.';
  public capabilities = ['authors', 'commits'];
  public experimental = true;
  public requirements = {
    apiKey: true,
    endpoint: false,
    file: false,
    progressUpdate: false,
  };
  public commits;
  public builds;
  public users;
  public general;
  public files = Files;

  constructor() {
    this.commits = new Commits('', '');
    this.builds = new Builds(); // Not implemented (questionable if needed in future)
    this.users = new Users('', '');
    this.general = new General('');
  }

  public async init(apiKey: string | undefined, endpoint: string | undefined) {
    console.log(`Init GitHub Backend with ApiKey: ${apiKey} and Endpoint ${endpoint}`);
    if (apiKey !== undefined) {
      this.commits = new Commits(apiKey, 'INSO-TUWien/Binocular');
      this.builds = new Builds(); // Not implemented (questionable if needed in future)
      this.users = new Users(apiKey, 'INSO-TUWien/Binocular');
      this.general = new General('INSO-TUWien/Binocular');
    }
  }

  public async clearRemains() {}
}

export default Github;
