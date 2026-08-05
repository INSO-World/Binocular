import { GraphQL, traversePages } from '../utils.ts';
import { gql } from '@apollo/client';
import type {
  DataPluginMergeRequest,
  DataPluginMergeRequests,
} from '../../../../interfaces/dataPluginInterfaces/dataPluginMergeRequests.ts';

export default class MergeRequests implements DataPluginMergeRequests {
  private graphQl;

  constructor(endpoint: string) {
    this.graphQl = new GraphQL(endpoint);
  }

  public async getAll(from: string, to: string, sort: string = 'ASC') {
    console.log(`Getting Merge Requests from ${from} to ${to}`);
    const mergeRequests: DataPluginMergeRequest[] = [];
    const getMergeRequestsPage = (since?: string, until?: string, sort?: string) => async (page: number, perPage: number) => {
      const resp = await this.graphQl.client.query({
        query: gql`
          query ($page: Int, $perPage: Int, $since: Timestamp, $until: Timestamp) {
            mergeRequests(page: $page, perPage: $perPage, since: $since, until: $until) {
              page
              perPage
              count
              data {
                iid
                title
                state
                webUrl
                createdAt
                closedAt
                updatedAt
                sourceBranch
                targetBranch
                author {
                  login
                  name
                  user {
                    gitSignature
                    id
                  }
                }
                assignee {
                  login
                  name
                  user {
                    gitSignature
                    id
                  }
                }
                assignees {
                  login
                  name
                  user {
                    gitSignature
                    id
                  }
                }
                notes {
                  author {
                    id
                    name
                    user {
                      gitSignature
                      id
                    }
                  }
                  body
                  createdAt
                  updatedAt
                }
              }
            }
          }
        `,
        variables: { page, perPage, since, until, sort },
      });
      return resp.data.mergeRequests;
    };
    await traversePages(getMergeRequestsPage(from, to, sort), (issue: DataPluginMergeRequest) => {
      mergeRequests.push(issue);
    });
    return mergeRequests;
  }
}
