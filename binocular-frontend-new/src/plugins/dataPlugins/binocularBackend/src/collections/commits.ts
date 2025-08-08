import { GraphQL, traversePages } from '../utils';
import { gql } from '@apollo/client';
import type {
  DataPluginCommit,
  DataPluginCommits,
  DataPluginFileOwnership,
} from '../../../../interfaces/dataPluginInterfaces/dataPluginCommits.ts';

export default class Commits implements DataPluginCommits {
  private graphQl;

  constructor(endpoint: string) {
    this.graphQl = new GraphQL(endpoint);
  }

  public async getAll(from: string, to: string, sort: string = 'ASC') {
    console.log(`Getting Commits from ${from} to ${to}`);
    try {
      const commitList: DataPluginCommit[] = [];
      const getCommitsPage = (from?: string, to?: string, sort?: string) => async (page: number, perPage: number) => {
        const resp = await this.graphQl.client.query({
          query: gql`
            query ($page: Int, $perPage: Int, $since: Timestamp, $until: Timestamp, $sort: Sort) {
              commits(page: $page, perPage: $perPage, since: $since, until: $until, sort: $sort) {
                count
                page
                perPage
                data {
                  sha
                  shortSha
                  message
                  messageHeader
                  user {
                    id
                    gitSignature
                  }
                  branch
                  parents
                  date
                  webUrl
                  stats {
                    additions
                    deletions
                  }
                }
              }
            }
          `,
          variables: { page, perPage, from, to, sort },
        });
        return resp.data.commits;
      };

      await traversePages(getCommitsPage(from, to, sort), (commit: DataPluginCommit) => {
        commitList.push(commit);
      });
      return commitList;
    } catch (e) {
      console.log(e);
      return [];
    }
  }

  public async getOwnershipDataForCommits() {
    return await this.graphQl.client
      .query({
        query: gql`
          query {
            commits {
              data {
                sha
                date
                parents
                files {
                  data {
                    file {
                      path
                    }
                    action
                    ownership {
                      user
                      hunks {
                        originalCommit
                        lines {
                          from
                          to
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        `,
      })
      .then((res) => res.data.commits)
      .then((commits) =>
        commits.data.map(
          (c: {
            sha: string;
            date: string;
            parents: string[];
            files: { data: { action: string; file: { path: string }; ownership: DataPluginFileOwnership }[] };
          }) => {
            return {
              sha: c.sha,
              date: c.date,
              parents: c.parents,
              files: c.files.data.map((fileData) => {
                return {
                  path: fileData.file.path,
                  action: fileData.action,
                  ownership: fileData.ownership,
                };
              }),
            };
          },
        ),
      );
  }

  public async getCommitDataForSha(sha: string) {
    return this.getAll(new Date(0).toISOString(), new Date().toISOString()).then((commits) => commits.filter((c) => c.sha === sha)[0]);
  }

  public async getByFile(file: string) {
    console.log(`Getting Commits for file ${file}`);
    const commitList: DataPluginCommit[] = [];
    const getCommitsPage = (file?: string) => async (page: number, perPage: number) => {
      const resp = await this.graphQl.client.query({
        query: gql`
          query ($file: String!, $page: Int, $perPage: Int) {
            file(path: $file) {
              commits(page: $page, perPage: $perPage) {
                data {
                  commit {
                    sha
                    shortSha
                    message
                    messageHeader
                    user {
                      id
                      gitSignature
                    }
                    branch
                    parents
                    date
                    webUrl
                    stats {
                      additions
                      deletions
                    }
                    files(page: 1, perPage: 1000) {
                      data {
                        file {
                          path
                        }
                        hunks {
                          newStart
                          newLines
                          oldStart
                          oldLines
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        `,
        variables: { page, perPage, file },
      });
      return resp.data.file.commits;
    };

    await traversePages(getCommitsPage(file), (data: { commit: DataPluginCommit }) => {
      commitList.push(data.commit);
    });
    return commitList.sort((a, b) => new Date(b.date).getMilliseconds() - new Date(a.date).getMilliseconds());
  }

  public async getDateOfFirstCommit() {
    console.log(`Getting Date of First Commit`);
    const resp = await this.graphQl.client.query({
      query: gql`
        query {
          commits(page: 1, perPage: 1) {
            data {
              date
            }
          }
        }
      `,
    });
    return resp.data.commits.data[0].date;
  }

  public async getDateOfLastCommit() {
    console.log(`Getting Date of Last Commit`);
    const resp = await this.graphQl.client.query({
      query: gql`
        query {
          commits(page: 1, perPage: 1, sort: "desc") {
            data {
              date
            }
          }
        }
      `,
    });
    return resp.data.commits.data[0].date;
  }
}
