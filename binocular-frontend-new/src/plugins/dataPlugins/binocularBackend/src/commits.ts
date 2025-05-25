import { GraphQL, traversePages } from './utils';
import { gql } from '@apollo/client';
import { DataPluginCommit, DataPluginCommits } from '../../../interfaces/dataPluginInterfaces/dataPluginCommits.ts';
import {DataPluginCommitBuild} from "../../../interfaces/dataPluginInterfaces/dataPluginCommitsBuilds.ts";

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
        variables: {page, perPage, from, to},
      });
      return resp.data.commits;
    };

    await traversePages(getCommitsPage(from, to), (commit: DataPluginCommit) => {
      commitList.push(commit);
    });
    const allCommits = commitList.sort((a, b) => new Date(b.date).getMilliseconds() - new Date(a.date).getMilliseconds());
    return allCommits.filter((c) => new Date(c.date) >= new Date(from) && new Date(c.date) <= new Date(to));
  }


  public async getCommitsWithBuilds(from: string, to: string): Promise<DataPluginCommitBuild[]> {
    console.log(`Getting Commits with Build data from ${from} to ${to}`);
    const commitBuildList: DataPluginCommitBuild[] = [];

    // Query that leverages the commits-builds connection in the database
    const getCommitsBuildsPage = (from?: string, to?: string) => async (page: number, perPage: number) => {
      const resp = await this.graphQl.client.query({
        query: gql`
          query ($since: Timestamp, $until: Timestamp, $page: Int, $perPage: Int) {
            commits(since: $since, until: $until, page: $page, perPage: $perPage) {
              count
              page
              perPage
              data {
                sha
                shortSha
                messageHeader
                message
                user {
                  id
                  gitSignature
                }
                branch
                date
                parents
                webUrl
                stats {
                  additions
                  deletions
                }
                builds {
                  id
                  status
                  duration
                  startedAt
                  finishedAt
                  jobs {
                    id
                    name
                    status
                    stage
                  }
                }
              }
            }
          }
        `,
        variables: {
          page,
          perPage,
          since: new Date(from).getTime(),
          until: new Date(to).getTime()
        },
      });
      return resp.data.commits;
    };

    // Fetch data using the connection-based query
    await traversePages(getCommitsBuildsPage(from, to), (commitBuild: DataPluginCommitBuild) => {
      commitBuildList.push(commitBuild);
    });
    return commitBuildList;
  }
}
