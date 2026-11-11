'use strict';

import debug from 'debug';
import { Octokit } from '@octokit/rest';
import { paginateGraphQL } from '@octokit/plugin-paginate-graphql';
import { retry } from '@octokit/plugin-retry';
import { throttling } from '@octokit/plugin-throttling';
import type { ThrottlingOptions } from '@octokit/plugin-throttling';
import type { EndpointDefaults } from '@octokit/types';
import { GithubJob, GithubUser } from '../../types/GithubTypes';

const log = debug('github');

// @octokit/plugin-retry@8.1.1 declares no options type (its octokitOptions parameter is any), so this names the
// options below; the plugin reads both off the client options in its request hook
type RetryOptions = {
  enabled?: boolean;
  retries?: number;
  retryAfterBaseValue?: number;
  doNotRetry?: number[];
};

// every status outside this list is retried up to `retries` times, so 403 and 429 — the secondary rate limit
// answers — stay retryable; the plugin backs off (retryCount + 1)² seconds and does not read `retry-after`
const retryRateLimitedRequests: RetryOptions = {
  retries: 3,
  doNotRetry: [400, 401, 404],
};

// the plugin calls both handlers as (retryAfter, options, octokit, retryCount) and retries the request only
// when the handler returns true; options is the resolved request, so it carries the plugin's own retryCount
const throttleRateLimitedRequests: ThrottlingOptions = {
  // pause every indexer using this client when the primary limit is hit, then retry once the window resets
  onRateLimit: (retryAfter: number, requestOptions: EndpointDefaults, _octokit: unknown, retryCount: number) => {
    log('Hit primary rate limit for %o %o — pausing every indexer, retry %o', requestOptions.method, requestOptions.url, retryCount);
    return retryCount < 3;
  },
  // back off on the secondary limit, retry once, then give up on this request
  onSecondaryRateLimit: (retryAfter: number, requestOptions: EndpointDefaults) => {
    // the plugin stores how often it retried this very request on options.request
    if ((requestOptions.request?.retryCount ?? 0) > 0) {
      console.error('Secondary rate limit hit again for %o %o — dropping request', requestOptions.method, requestOptions.url);
      return false;
    }
    log(
      'Hit secondary rate limit for %o %o — pausing every indexer, retrying after %o s',
      requestOptions.method,
      requestOptions.url,
      retryAfter,
    );
    return true;
  },
};

// One Octokit per (baseUrl, token) for the whole process. @octokit/plugin-throttling keeps its limiter and its
// backoff state inside the client, so sharing the client is what makes the rate limit handling global: the ITS
// indexer and the CI job fetch run concurrently but share one budget and one backoff.
const sharedClients: { [cacheKey: string]: any } = {};
// assignable users of a repository, requested at most once per process even when indexers ask in parallel
const sharedAssignableUsers: { [cacheKey: string]: Promise<GithubUser[]> } = {};

function sharedGitHubClient(options: { baseUrl: string; privateToken: string }) {
  const cacheKey = `${options.baseUrl}|${options.privateToken ?? ''}`;
  if (sharedClients[cacheKey] === undefined) {
    log('Creating the shared GitHub client for %o', options.baseUrl);
    const RateLimitedOctokit = Octokit.plugin(retry, throttling, paginateGraphQL);
    sharedClients[cacheKey] = new RateLimitedOctokit({
      baseUrl: options.baseUrl,
      auth: options.privateToken,
      // honor GitHub's secondary rate limits instead of failing on them
      retry: retryRateLimitedRequests,
      throttle: throttleRateLimitedRequests,
    });
  }
  return sharedClients[cacheKey];
}

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
    this.github = sharedGitHubClient(options);
    // one client for REST and GraphQL, so both share the single rate limit budget
    this.githubGraphQL = this.github;
    this.users = {};
  }

  async loadAssignableUsers(repositoryOwner: string, repositoryName: string) {
    const cacheKey = `${this.privateToken ?? ''}:${repositoryOwner}/${repositoryName}`;
    if (sharedAssignableUsers[cacheKey] === undefined) {
      sharedAssignableUsers[cacheKey] = this.loadAssignableUsersFromApi(repositoryOwner, repositoryName).catch((error: unknown) => {
        // a failed lookup must not stay cached
        delete sharedAssignableUsers[cacheKey];
        throw error;
      });
    }
    const nodes = await sharedAssignableUsers[cacheKey];
    nodes.forEach((user: GithubUser) => {
      this.users[user.login] = user;
    });
  }

  private loadAssignableUsersFromApi(repositoryOwner: string, repositoryName: string): Promise<GithubUser[]> {
    log('Loading assignable users of %o/%o', repositoryOwner, repositoryName);
    return this.githubGraphQL.graphql
      .paginate(
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
      )
      .then((result: any) => result.repository.assignableUsers.nodes as GithubUser[]);
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
