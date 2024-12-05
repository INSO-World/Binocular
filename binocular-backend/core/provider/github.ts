'use strict';

import debug from 'debug';
import { Octokit } from '@octokit/rest';
import { Octokit as OctokitCore } from '@octokit/core';
import { paginateGraphql } from '@octokit/plugin-paginate-graphql';
import { GithubArtifact, GithubJob, GithubUser } from '../../types/GithubTypes';
import JSZip from 'jszip';

const log = debug('github');

class GitHub {
  private privateToken: string;
  private requestTimeout: any;
  private count: number;
  private stopping: boolean;
  private github: any;
  private githubGraphQL: any;
  private users: { [login: string]: GithubUser };
  constructor(options: { baseUrl: string; privateToken: string; requestTimeout: any }) {
    this.privateToken = options.privateToken;
    this.requestTimeout = options.requestTimeout;
    this.count = 0;
    this.stopping = false;
    this.github = new Octokit({
      baseUrl: options.baseUrl,
      auth: this.privateToken,
    });
    const PaginatedGraphQL = OctokitCore.plugin(paginateGraphql);
    this.githubGraphQL = new PaginatedGraphQL({ auth: this.privateToken });
    this.users = {};
  }

  async loadAssignableUsers(repositoryOwner: string, repositoryName: string) {
    const { repository } = await this.githubGraphQL.graphql.paginate(
      `query paginate($cursor: String) {
         repository(owner: "${repositoryOwner}", name: "${repositoryName}") {
          name,
          assignableUsers(first: 100, after:$cursor) {
            nodes {
              login
              email
              name
              url
              avatarUrl
            }
            pageInfo {
              hasNextPage
              endCursor
            }
          }
        }
      }
      `,
    );
    repository.assignableUsers.nodes.forEach((user: GithubUser) => {
      this.users[user.login] = user;
    });
  }

  getUser(login: string) {
    if (this.users[login] !== undefined) {
      return this.users[login];
    } else {
      return { name: null };
    }
  }

  getPipelines(projectId: string) {
    log('getPipelines(%o)', projectId);
    return this.github.paginate(this.github.rest.actions.listWorkflowRunsForRepo, {
      owner: projectId.split('/')[0],
      repo: projectId.split('/')[1],
    });
  }

  getPipelineJobs(projectId: string, pipelineId: string) {
    log('getPipelineJobs(%o,%o)', projectId, pipelineId);
    return this.github.rest.actions
      .listJobsForWorkflowRun({
        owner: projectId.split('/')[0],
        repo: projectId.split('/')[1],
        run_id: pipelineId,
      })
      .then((jobs: { data: { jobs: GithubJob[] } }) => {
        return jobs.data.jobs;
      });
  }

  getPipelineArtifacts(projectId: string, pipelineId: string) {
    log('getPipelineArtifacts(%o, %o)', projectId, pipelineId);
    return this.github.rest.actions
      .listWorkflowRunArtifacts({
        owner: projectId.split('/')[0],
        repo: projectId.split('/')[1],
        run_id: pipelineId,
      })
      .then((artifacts: { data: { artifacts: GithubArtifact } }) => {
        return artifacts.data.artifacts;
      });
  }

  async downloadJacocoReport(artifact: GithubArtifact): Promise<ArrayBuffer | null> {
    const response = await fetch(artifact.archive_download_url, {
      headers: {
        Authorization: `Bearer ${this.privateToken}`,
        Accept: 'application/vnd.github+json',
      },
    });

    if (!response.ok) {
      log('Failed to download artifact: %o', response.statusText);
      return null;
    }

    return response.arrayBuffer();
  }

  async extractJacocoXMLReportContent(buffer: ArrayBuffer): Promise<any> {
    const zip = new JSZip();
    const zipContent = await zip.loadAsync(buffer);

    // Find the Jacoco report XML file
    let xmlReportContent: string | null = null;
    await Promise.all(
      Object.keys(zipContent.files).map(async (relativePath) => {
        const file = zipContent.files[relativePath];

        // Check if the file is an XML report file
        if (relativePath.endsWith('.xml') && !file.dir) {
          // Read file content as text
          xmlReportContent = await file.async('text');
        }
      }),
    );

    if (!xmlReportContent) {
      log('Jacoco XML report not found in ZIP archive');
      return;
    }

    return xmlReportContent;
  }

  /**
   * Currently the number of timeline items is fixed to 200 items.
   * This could improve in the future through nested pagination.
   * Nested pagination is not possible with @octokit/plugin-paginate-graphql
   */
  getIssuesWithEvents(repositoryOwner: string, repositoryName: string) {
    return this.githubGraphQL.graphql
      .paginate(
        `query paginate($cursor: String) {
           repository(owner: "${repositoryOwner}", name: "${repositoryName}") {
              issues(first: 100, after:$cursor) {
                  totalCount
                  nodes {
                      id
                      number
                      title
                      body
                      state
                      url
                      closedAt
                      createdAt
                      updatedAt
                      labels(first: 100) {
                          nodes {
                              id
                              url
                              name
                              color
                              isDefault
                              description
                          }
                      }
                      milestone {
                          id
                          url
                          number
                          state
                          title
                          description
                          creator {
                              login
                          }
                          createdAt
                          updatedAt
                          closedAt
                          dueOn
                      }
                      author {
                          login
                      }
                      assignees(first: 100) {
                          nodes {
                              login
                          }
                      }
                      timelineItems(first: 200, itemTypes: [CLOSED_EVENT, REFERENCED_EVENT]) {
                          totalCount
                          nodes {
                              ... on ReferencedEvent {
                                  createdAt
                                  commit {
                                      oid
                                  }
                              }
                              ... on ClosedEvent {
                                  createdAt
                              }
                          }
                      }
                  }
                  pageInfo {
                    hasNextPage
                    endCursor
                  }
              }
          }
        }
      `,
      )
      .then((repository: any) => repository.repository.issues.nodes);
  }

  /**
   * Currently the number of timeline items is fixed to 200 items.
   * This could improve in the future through nested pagination.
   * Nested pagination is not possible with @octokit/plugin-paginate-graphql
   */
  getPullRequestsWithEvents(repositoryOwner: string, repositoryName: string) {
    return this.githubGraphQL.graphql
      .paginate(
        `query paginate($cursor: String) {
           repository(owner: "${repositoryOwner}", name: "${repositoryName}") {
              pullRequests(first: 100, after:$cursor) {
                  totalCount
                  nodes {
                      id
                      number
                      title
                      body
                      state
                      url
                      closedAt
                      createdAt
                      updatedAt
                      labels(first: 100) {
                          nodes {
                              id
                              url
                              name
                              color
                              isDefault
                              description
                          }
                      }
                      milestone {
                          id
                          url
                          number
                          state
                          title
                          description
                          creator {
                              login
                          }
                          createdAt
                          updatedAt
                          closedAt
                          dueOn
                      }
                      author {
                          login
                      }
                      assignees(first: 100) {
                          nodes {
                              login
                          }
                      }
                      timelineItems(first: 200, itemTypes: [CLOSED_EVENT, REFERENCED_EVENT]) {
                          totalCount
                          nodes {
                              ... on ReferencedEvent {
                                  createdAt
                                  commit {
                                      oid
                                  }
                              }
                              ... on ClosedEvent {
                                  createdAt
                              }
                          }
                      }
                  }
                  pageInfo {
                    hasNextPage
                    endCursor
                  }
              }
          }
        }
      `,
      )
      .then((repository: any) => repository.repository.pullRequests.nodes);
  }

  isStopping() {
    return this.stopping;
  }

  stop() {
    this.stopping = true;
  }
}

export default GitHub;
