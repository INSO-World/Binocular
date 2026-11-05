import { GraphQL } from '../utils.ts';
import { type ApolloQueryResult, gql } from '@apollo/client';
import type {
  DataPluginCommit,
  DataPluginCommitBuild,
  DataPluginCommitShort,
  DataPluginCommits,
  DataPluginOwnership,
} from '../../../../interfaces/dataPluginInterfaces/dataPluginCommits.ts';

interface CommitQueryResult {
  repository: {
    defaultBranchRef: {
      target: {
        history: {
          totalCount: number;
          pageInfo: { endCursor: string; hasNextPage: boolean };
          nodes: {
            oid: string;
            messageHeadline: string;
            message: string;
            committedDate: string;
            url: string;
            deletions: number;
            additions: number;
            author: { user: { id: string; login: string } };
            parents: { totalCount: number; nodes: { oid: string }[] };
          }[];
        };
      };
    };
  };
}

export default class Commits implements DataPluginCommits {
  private graphQl;
  private owner;
  private name;

  constructor(apiKey: string, endpoint: string) {
    this.graphQl = new GraphQL(apiKey);
    this.owner = endpoint.split('/')[0];
    this.name = endpoint.split('/')[1];
  }
  public async getAll(from: string, to: string) {
    return await Promise.resolve(this.getCommits(100, new Date(from).toISOString(), new Date(to).toISOString()));
  }
  private async getCommits(perPage: number, from: string, to: string): Promise<DataPluginCommit[]> {
    let hasNextPage: boolean = true;
    let nextPageCursor: string | null = null;

    const commitNodes: DataPluginCommit[] = [];

    while (hasNextPage) {
      const resp: void | ApolloQueryResult<CommitQueryResult> = await this.graphQl.client
        .query<
          CommitQueryResult,
          {
            nextPageCursor: string | null;
            perPage: number;
            from: string;
            to: string;
            owner: string;
            name: string;
          }
        >({
          query: gql`
            query ($nextPageCursor: String, $perPage: Int, $from: GitTimestamp, $to: GitTimestamp, $owner: String!, $name: String!) {
              repository(owner: $owner, name: $name) {
                defaultBranchRef {
                  target {
                    ... on Commit {
                      history(after: $nextPageCursor, first: $perPage, since: $from, until: $to) {
                        pageInfo {
                          endCursor
                          hasNextPage
                        }
                        totalCount
                        nodes {
                          oid
                          messageHeadline
                          message
                          committedDate
                          url
                          deletions
                          additions
                          author {
                            user {
                              id
                              login
                            }
                          }
                          parents(first: 100) {
                            totalCount
                            nodes {
                              oid
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          `,
          variables: {
            nextPageCursor,
            perPage,
            from,
            to,
            owner: this.owner,
            name: this.name,
          },
        })
        .catch((e) => console.log(e));

      if (resp) {
        resp.data.repository.defaultBranchRef.target.history.nodes.forEach((commit) => {
          if (commit.author.user === null) {
            return;
          }
          commitNodes.push({
            sha: commit.oid,
            shortSha: '',
            files: undefined,
            messageHeader: commit.messageHeadline,
            message: commit.message,
            user: {
              id: commit.author.user.id,
              gitSignature: commit.author.user.login,
              account: null,
            },
            branch: '',
            date: commit.committedDate,
            parents: commit.parents.nodes.map((parent) => parent.oid),
            webUrl: commit.url,
            stats: {
              additions: commit.additions,
              deletions: commit.deletions,
            },
          });
        });
        nextPageCursor = resp.data.repository.defaultBranchRef.target.history.pageInfo.endCursor;
        hasNextPage = resp.data.repository.defaultBranchRef.target.history.pageInfo.hasNextPage;
      } else {
        hasNextPage = false;
      }
    }

    return commitNodes;
  }

  public async getOwnershipDataForCommits(): Promise<DataPluginOwnership[]> {
    return Promise.resolve([]);
  }

  public async getCommitDataForSha(_sha: string): Promise<DataPluginCommit> {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    return Promise.resolve({});
  }

  public async getByFile(file: string): Promise<DataPluginCommit[]> {
    console.log(`Getting Commits for file ${file}`);
    let hasNextPage: boolean = true;
    let nextPageCursor: string | null = null;
    const commitList: DataPluginCommit[] = [];
    const perPage = 100; // You can adjust this value or make it a parameter

    while (hasNextPage) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const resp: any = await this.graphQl.client
        .query({
          query: gql`
            query ($file: String!, $nextPageCursor: String, $perPage: Int) {
              file(path: $file) {
                commits(after: $nextPageCursor, first: $perPage) {
                  pageInfo {
                    endCursor
                    hasNextPage
                  }
                  data {
                    commit {
                      sha
                      message
                      messageHeader
                      date
                      stats {
                        additions
                        deletions
                      }
                    }
                    files(page: 1, perPage: 1000) {
                      data {
                        file {
                          path
                        }
                      }
                    }
                  }
                }
              }
            }
          `,
          variables: { nextPageCursor, perPage, file },
        })
        .catch((e) => {
          console.log(e);
          return null;
        });

      if (resp && resp.data.file.commits) {
        resp.data.file.commits.data.forEach((data: { commit: DataPluginCommit }) => {
          commitList.push(data.commit);
        });

        nextPageCursor = resp.data.file.commits.pageInfo.endCursor;
        hasNextPage = resp.data.file.commits.pageInfo.hasNextPage;
      } else {
        hasNextPage = false;
      }
    }

    const sortedCommits = commitList.sort((a, b) => {
      return new Date(a.date).getTime() - new Date(b.date).getTime();
    });

    return sortedCommits;
  }

  public async getDateOfFirstCommit() {
    console.log(`Getting Date of First Commit`);
    const resp = await this.graphQl.client.query({
      query: gql`
        query ($owner: String!, $name: String!) {
          repository(owner: $owner, name: $name) {
            defaultBranchRef {
              target {
                ... on Commit {
                  history(first: 1) {
                    nodes {
                      committedDate
                    }
                  }
                }
              }
            }
          }
        }
      `,
      variables: { owner: this.owner, name: this.name },
    });
    return resp.data.repository.defaultBranchRef.target.history.nodes[0].committedDate;
  }

  public async getDateOfLastCommit() {
    console.log(`Getting Date of Last Commit`);
    const resp = await this.graphQl.client.query({
      query: gql`
        query ($owner: String!, $name: String!) {
          repository(owner: $owner, name: $name) {
            defaultBranchRef {
              target {
                ... on Commit {
                  history(last: 1) {
                    nodes {
                      committedDate
                    }
                  }
                }
              }
            }
          }
        }
      `,
      variables: { owner: this.owner, name: this.name },
    });
    return resp.data.repository.defaultBranchRef.target.history.nodes[0].committedDate;
  }

  public async getCommitsWithBuilds(_from: string, _to: string): Promise<DataPluginCommitBuild[]> {
    // not yet implemented
    return Promise.resolve([]);
  }

  public async getCommitsWithFiles(_from: string, _to: string): Promise<DataPluginCommit[]> {
    // not yet implemented
    return Promise.resolve([]);
  }

  public async getAllShort(): Promise<DataPluginCommitShort[]> {
    // not yet implemented
    return Promise.resolve([]);
  }
}
