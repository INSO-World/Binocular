import OctokitMock from './octokitMock.js';
import GitHubMock from './gitHubMock.js';

export default class GitHubCIIndexerMock {
  setupOctokit() {
    this.github = new OctokitMock();
  }

  setupGithub(config) {
    this.controller = new GitHubMock(config.testSetup.pipelineVersion);
  }

  setupUrlProvider() {
    this.urlProvider = {};
  }
}
