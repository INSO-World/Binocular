import OctokitMock from './octokitMock.ts';
import GitHubMock from './gitHubMock.ts';

export default class GitHubCIIndexerMock {
  private github: any;
  private controller: any;
  private urlProvider: any;
  setupOctokit() {
    this.github = new OctokitMock();
  }

  setupGithub() {
    this.controller = new GitHubMock();
  }

  setupUrlProvider(): any {
    this.urlProvider = {};
  }
}
