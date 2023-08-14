'use strict';

const Paginator = require('../../../../lib/paginator');

class GitHubMock {
  constructor() {
    this.pipelineId = 0;
  }

  getPipelines() {
    return new Paginator(
      () => {
        return new Promise((resolve) => {
          resolve([{ id: '0' }, { id: '1' }, { id: '2' }]);
        });
      },
      (resp) => {
        return resp;
      },
      () => {
        this.count = 3;
        return this.count;
      }
    );
  }

  getPipeline() {
    this.pipelineId++;
    return new Promise((resolve) => {
      resolve({
        id: '' + (this.pipelineId - 1),
        head_sha: '1234567890',
        head_commit: { id: 0, sha: '1234567890', committed_at: '1970-01-01T07:00:00.000Z', timestamp: '1970-01-01T07:00:00.000Z' },
        status: 'success',
        display_title: 'test',
        run_number: 0,
        created_at: '1970-01-01T07:00:00.000Z',
        started_at: '1970-01-01T07:00:00.000Z',
        updated_at: '1970-01-01T07:00:00.000Z',
        run_started_at: '1970-01-01T07:00:00.000Z',
        actor: { login: 'tester1' },
      });
    });
  }

  getPipelineJobs() {
    return [
      { id: '0', conclusion: 'success', created_at: '1970-01-01T07:00:00.000Z', completed_at: '1970-01-01T07:00:00.000Z' },
      { id: '1', conclusion: 'success', created_at: '1970-01-01T07:00:00.000Z', completed_at: '1970-01-01T07:00:00.000Z' },
      { id: '2', conclusion: 'failure', created_at: '1970-01-01T07:00:00.000Z', completed_at: '1970-01-01T07:00:00.000Z' },
    ];
  }
}

module.exports = GitHubMock;