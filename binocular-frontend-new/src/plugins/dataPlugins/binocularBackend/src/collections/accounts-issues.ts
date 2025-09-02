import { gql } from "@apollo/client";
import { GraphQL, traversePages } from "../utils.ts";
import type {
  DataPluginAccountIssues,
  DataPluginAccountsIssues,
} from "../../../../interfaces/dataPluginInterfaces/dataPluginAccountsIssues.ts";

export default class AccountsIssues implements DataPluginAccountsIssues {
  private graphQl: GraphQL;

  constructor(endpoint: string) {
    this.graphQl = new GraphQL(endpoint);
  }

  /**
   * Retrieves all accounts with their related issues from the backend.
   */
  public async getAll(
    from: string,
    to: string,
  ): Promise<DataPluginAccountIssues[]> {
    console.log(`Getting all Accounts with Issues from:${from} to:${to}:`);
    const relationships: DataPluginAccountIssues[] = [];
    const getAccountsIssuesPage =
      (from?: string, to?: string) =>
      async (page: number, perPage: number = 50) => {
        const response = await this.graphQl.client.query({
          query: gql`
            query (
              $page: Int
              $perPage: Int
              $from: Timestamp
              $to: Timestamp
            ) {
              accounts(page: $page, perPage: $perPage) {
                count
                page
                perPage
                data {
                  platform
                  login
                  name
                  url
                  avatarUrl
                  issues(from: $from, to: $to) {
                    id
                    iid
                    title
                    description
                    createdAt
                    closedAt
                    state
                    webUrl
                  }
                }
              }
            }
          `,
          variables: { page, perPage, from, to },
        });
        console.log("FETCHED !!!!!!!!!");
        return response.data.accounts;
      };

    await traversePages(getAccountsIssuesPage(from, to), (record: any) => {
      relationships.push({
        id: record.login,
        login: record.login,
        name: record.name,
        avatarUrl: record.avatarUrl,
        url: record.url,
        issues: record.issues.map((issue: any) => ({
          id: issue.id,
          iid: String(issue.iid),
          title: issue.title,
          description: issue.description,
          createdAt: issue.createdAt,
          closedAt: issue.closedAt,
          state: issue.state,
          webUrl: issue.webUrl,
        })),
      });
    });

    return relationships;
  }
}
