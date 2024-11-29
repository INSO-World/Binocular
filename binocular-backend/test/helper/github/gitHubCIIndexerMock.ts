import OctokitMock from './octokitMock.ts';
import GitHubMock from './gitHubMock.ts';

export default class GitHubCIIndexerMock {
  private github: any;
  private controller: any;
  private urlProvider: any;
  setupOctokit() {
    this.github = new OctokitMock();
  }

  setupGithub(config) {
    this.controller = new GitHubMock(config.testSetup.pipelineVersion);
  }

  setupUrlProvider(): any {
    this.urlProvider = {};
  }
}
