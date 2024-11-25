'use strict';

import {
  issuesWithEvents,
  pipelineJobsVersion0,
  pipelineJobsVersion1,
  singlePipeline,
  pipelinesVersion0,
  pipelinesVersion1,
  singlePipelineJob,
  pullRequestsWithEvents,
  users,
} from './gitHubTestData';

class GitHubMock {
  private pipelineId: number;
  private pipelineVersion: number;

  constructor(pipelineVersion) {
    this.pipelineId = 0;
    this.pipelineVersion = pipelineVersion;
  }

  loadAssignableUsers() {}

  getPipelines() {
    return new Promise((resolve) => {
      if (this.pipelineVersion === 1) {
        resolve(pipelinesVersion1);
      } else if (this.pipelineVersion === 2) {
        resolve(
          Array.from({ length: 201 }, (_, index) => ({
            ...singlePipeline,
            id: index + 1, // Assign a unique id for each item
          })),
        );
      }
      resolve(pipelinesVersion0);
    });
  }

  getPipeline() {
    this.pipelineId++;
    return new Promise((resolve) => {
      resolve({
        id: this.pipelineId - 1,
        head_sha: '1234567890',
        head_commit: { id: 0, sha: '1234567890', committed_at: '1970-01-01T07:00:00.000Z', timestamp: '1970-01-01T07:00:00.000Z' },
        conclusion: 'success',
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

  getPipelineJobs(projectId, pipelineId) {
    if (pipelineId === 0) {
      if (this.pipelineVersion === 1) {
        return pipelineJobsVersion1;
      }
      return pipelineJobsVersion0;
    } else if (pipelineId === 1) {
      if (this.pipelineVersion === 1) {
        return pipelineJobsVersion1;
      } else {
        return [];
      }
    } else {
      return singlePipelineJob;
    }
  }

  getIssuesWithEvents() {
    return new Promise((resolve) => {
      resolve(issuesWithEvents);
    });
  }

  getPullRequestsWithEvents() {
    return new Promise((resolve) => {
      resolve(pullRequestsWithEvents);
    });
  }

  getUser(login) {
    const res = users.filter((u) => u.login === login)[0];
    if (res === undefined) {
      return { name: null };
    }
    return res;
  }
}

export default GitHubMock;
