import { GraphQL, traversePages } from './utils';
import { gql } from '@apollo/client';
import { DataPluginCommit, DataPluginCommits } from '../../../interfaces/dataPluginInterfaces/dataPluginCommits.ts';

export default class Commits implements DataPluginCommits {
  private graphQl;

  constructor(endpoint: string) {
    this.graphQl = new GraphQL(endpoint);
  }

  public async getAll(from: string, to: string) {
    console.log(`Getting Commits from ${from} to ${to}`);
    const commitList: DataPluginCommit[] = [];
    const getCommitsPage = (from?: string, to?: string) => async (page: number, perPage: number) => {
      const resp = await this.graphQl.client.query({
        query: gql`
          query ($page: Int, $perPage: Int, $since: Timestamp, $until: Timestamp) {
            commits(page: $page, perPage: $perPage, since: $since, until: $until) {
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
        variables: { page, perPage, from, to },
      });
      return resp.data.commits;
    };

    await traversePages(getCommitsPage(from, to), (commit: DataPluginCommit) => {
      commitList.push(commit);
    });
    const allCommits = commitList.sort((a, b) => new Date(b.date).getMilliseconds() - new Date(a.date).getMilliseconds());
    return allCommits.filter((c) => new Date(c.date) >= new Date(from) && new Date(c.date) <= new Date(to));
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
        commits.data.map((c: { sha: string; date: string; parents: string[]; files: { data: any[] } }) => {
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
        }),
      );
  }

  public async getCommitDataForSha(sha: string) {
    return this.getAll(new Date(0).toISOString(), new Date().toISOString()).then((commits) => commits.filter((c) => c.sha === sha)[0]);
  }
}
