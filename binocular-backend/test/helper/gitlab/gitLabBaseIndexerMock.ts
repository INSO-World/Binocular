'use strict';

import GitLabMock from './gitLabMock.ts';
import Repository from '../../../core/provider/git';
import ReporterMock from '../reporter/reporterMock.ts';
import ProgressReporter from '../../../utils/progress-reporter.ts';

class GitLabBaseIndexerMock {
  private repo: Repository;
  private stopping: boolean;
  private reporter: typeof ReporterMock | typeof ProgressReporter;
  private urlProvider: any;
  private gitlab: any;

  constructor(repo, reporter) {
    this.repo = repo;
    this.stopping = false;
    this.reporter = reporter;

    this.urlProvider = {
      getJobUrl: (id) => 'https://gitlab.com/Test/Test-Project/jobs/' + id,
      getPipelineUrl: (id) => 'https://gitlab.com/Test/Test-Project/pipelines/' + id,
    };
  }

  setupUrlProvider(): any {
    this.urlProvider = {
      getJobUrl: (id) => 'https://gitlab.com/Test/Test-Project/jobs/' + id,
      getPipelineUrl: (id) => 'https://gitlab.com/Test/Test-Project/pipelines/' + id,
    };
    this.setupGitlab();
  }

  setupGitlab() {
    this.gitlab = new GitLabMock();
  }

  getProject() {
    return new GitLabMock().getProject();
  }

  stop() {
    this.stopping = true;
  }

  isStopping() {
    return this.stopping;
  }
}

export default GitLabBaseIndexerMock;
