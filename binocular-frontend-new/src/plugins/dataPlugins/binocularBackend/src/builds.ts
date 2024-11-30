import { GraphQL, traversePages } from './utils';
import { gql } from '@apollo/client';
import { DataPluginBuild, DataPluginBuilds, DataPluginJob } from '../../../interfaces/dataPluginInterfaces/dataPluginBuilds.ts';
import { DataPluginUser } from '../../../interfaces/dataPluginInterfaces/dataPluginUsers.ts';

export default class Builds implements DataPluginBuilds {
  private graphQl;

  constructor(endpoint: string) {
    this.graphQl = new GraphQL(endpoint);
  }

  public async getAll(from: string, to: string) {
    console.log(`Getting Builds from ${from} to ${to}`);
    const builds: DataPluginBuild[] = [];
    const getBuildPage = (since?: number, until?: number) => async (page: number, perPage: number) => {
      const resp = await this.graphQl.client.query({
        // variable tag not queried, because it cannot be found(maybe a keyword), not needed at the moment
        query: gql`
          query ($page: Int, $perPage: Int, $since: Timestamp, $until: Timestamp) {
            builds(page: $page, perPage: $perPage, since: $since, until: $until) {
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
                commit {
                  user {
                    id
                    gitSignature
                  }
                }
              }
            }
          }
        `,
        variables: { page, perPage, since, until },
      });
      return resp.data.builds;
    };

    await traversePages(getBuildPage(new Date(from).getTime(), new Date(to).getTime()), (build: Build) => {
      builds.push(convertToDataPluginBuild(build));
    });
    return builds;
  }
}

function convertToDataPluginBuild(build: Build): DataPluginBuild {
  return {
    id: build.id,
    committedAt: build.committedAt,
    createdAt: build.createdAt,
    duration: build.duration,
    finishedAt: build.finishedAt,
    jobs: build.jobs,
    startedAt: build.startedAt,
    status: build.status,
    updatedAt: build.updatedAt,
    user: build?.commit?.user,
    userFullName: build.userFullName,
  };
}

interface Build {
  id: number;
  committedAt: string;
  createdAt: string;
  duration: string;
  finishedAt: string;
  jobs: DataPluginJob[];
  startedAt: string;
  status: string;
  updatedAt: string;
  commit: Commit;
  userFullName: string;
}

interface Commit {
  user: DataPluginUser;
}
