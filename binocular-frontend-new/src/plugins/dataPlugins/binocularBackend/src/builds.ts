import { GraphQL, traversePages } from './utils';
import { gql } from '@apollo/client';
import { DataPluginBuild, DataPluginBuilds } from '../../../interfaces/dataPluginInterfaces/dataPluginBuilds.ts';

export default class Builds implements DataPluginBuilds {
  private graphQl;

  constructor(endpoint: string) {
    this.graphQl = new GraphQL(endpoint);
  }

  public async getAll(from: string, to: string) {
    console.log(`Getting Builds from ${from} to ${to}`);
    const builds: DataPluginBuild[] = [];
    const getBuildPage = (to?: string) => async (page: number, perPage: number) => {
      const resp = await this.graphQl.client.query({
        // variable tag not queried, because it cannot be found(maybe a keyword), not needed at the moment
        query: gql`
          query ($page: Int, $perPage: Int, $until: Timestamp) {
            builds(page: $page, perPage: $perPage, until: $until) {
              count
              page
              perPage
              data {
                id
                committedAt
                createdAt
                duration
                finishedAt
                jobs {
                  id
                  name
                  status
                  stage
                  createdAt
                  finishedAt
                }
                startedAt
                status
                updatedAt
                user
                userFullName
              }
            }
          }
        `,
        variables: { page, perPage, to },
      });
      return resp.data.builds;
    };

    await traversePages(getBuildPage(to), (build: DataPluginBuild) => {
      builds.push(build);
    });
    const allBuilds = builds.sort((a, b) => new Date(b.createdAt).getMilliseconds() - new Date(a.createdAt).getMilliseconds());
    return allBuilds.filter((c) => new Date(c.createdAt) >= new Date(from) && new Date(c.createdAt) <= new Date(to));
  }
}
