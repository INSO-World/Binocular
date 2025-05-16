import { GraphQL, traversePages } from '../utils.ts';
import { gql } from '@apollo/client';
import { DataPluginCommit, DataPluginCommits } from '../../../../interfaces/dataPluginInterfaces/dataPluginCommits.ts';

export default class Commits implements DataPluginCommits {
  private graphQl;

  constructor(endpoint: string) {
    this.graphQl = new GraphQL(endpoint);
  }

  public async getAll(from: string, to: string, sort: string = 'ASC') {
    console.log(`Getting Commits from ${from} to ${to}`);
    const commitList: DataPluginCommit[] = [];
    const getCommitsPage = (since?: string, until?: string, sort?: string) => async (page: number, perPage: number) => {
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
        variables: { page, perPage, since, until, sort },
      });
      return resp.data.commits;
    };

    await traversePages(getCommitsPage(from, to, sort), (commit: DataPluginCommit) => {
      commitList.push(commit);
    });
    return commitList;
  }
}
