'use strict';

import debug from 'debug';
import { Octokit } from '@octokit/rest';
import { Octokit as OctokitCore } from '@octokit/core';
import { paginateGraphql } from '@octokit/plugin-paginate-graphql';
import { retry } from '@octokit/plugin-retry';
import { throttling } from '@octokit/plugin-throttling';
import type { OctokitOptions } from '@octokit/core/dist-types/types';
import type { RequestParameters } from '@octokit/types';
import { GithubJob, GithubUser } from '../../types/GithubTypes';

const log = debug('github');

// retry plugin types only retry 5xx; shouldRetryMethod additionally retries secondary rate limit responses (403/429 with Retry-After)
const retryRateLimitedRequests: NonNullable<OctokitOptions['retry']> & { doNotRetry: number[] } = {
  doNotRetry: [400, 401, 404],
  shouldRetryMethod: (error: { status: number; response?: { headers?: Record<string, string> } }) =>
    (error.status === 403 || error.status === 429) && error.response?.headers?.['retry-after'] !== undefined,
};

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
    // cast: @octokit/rest@19 ships its own nested @octokit/types copy, so the plugin types clash structurally
    const ThrottledOctokit = (Octokit as any).plugin(retry, throttling);
    this.github = new ThrottledOctokit({
      baseUrl: options.baseUrl,
      auth: this.privateToken,
      // honor GitHub's secondary rate limits instead of failing on them
      retry: retryRateLimitedRequests,
      throttle: {
        // pause the whole client when the primary limit is hit until x-ratelimit-reset
        onRateLimit: (retryAfter: number, options: RequestParameters) => {
          log('Hit primary rate limit for %o %o — pausing %o s', options.method, options.url, retryAfter);
          return false;
        },
        // back off on the secondary limit, retry once, then give up on this request
        onSecondaryRateLimit: (retryAfter: number, options: RequestParameters) => {
          if ((options as { __rateLimitRetries?: number }).__rateLimitRetries) {
            console.error('Secondary rate limit hit again for %o %o — dropping request', options.method, options.url);
            return false;
          }
          (options as { __rateLimitRetries?: number }).__rateLimitRetries = 1;
          log('Hit secondary rate limit for %o %o — retrying after %o s', options.method, options.url, retryAfter);
          return true;
        },
      },
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
              id
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
    // per_page 100 keeps the paginated run list at ~1 request per 100 runs instead of 30
    return this.github.paginate(this.github.rest.actions.listWorkflowRunsForRepo, {
      owner: projectId.split('/')[0],
      repo: projectId.split('/')[1],
      per_page: 100,
    });
  }

  async getPipelineJobs(projectId: string, pipelineId: number): Promise<GithubJob[]> {
    log('getPipelineJobs(%o,%o)', projectId, pipelineId);
    // paginate, because a single REST response is capped at one page and would truncate the job list
    return this.github
      .paginate(this.github.rest.actions.listJobsForWorkflowRun, {
        owner: projectId.split('/')[0],
        repo: projectId.split('/')[1],
        run_id: pipelineId,
        per_page: 100,
      })
      .then((jobs: GithubJob[]) => jobs);
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
              issues(first: 50, after:$cursor) {
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
   *
   * For the same reason, the commits and closing issue references of each pull request are not paginated.
   * Instead, they are fixed to 250 commits and 100 closingIssuesReferences
   */
  getPullRequestsWithEvents(repositoryOwner: string, repositoryName: string) {
    return this.githubGraphQL.graphql
      .paginate(
        `query paginate($cursor: String) {
           repository(owner: "${repositoryOwner}", name: "${repositoryName}") {
              pullRequests(first: 50, after:$cursor) {
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
                      closingIssuesReferences (first: 100) {
                        nodes {
                          id
                        }
                      }
                      commits (first: 250) {
                        nodes{
                          commit{
                            oid
                          }
                        }
                      }
                      mergeCommit {
                        oid
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
