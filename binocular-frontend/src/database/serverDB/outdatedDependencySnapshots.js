'use strict';

import { collectPages, graphQl } from '../../utils';

export default class OutdatedDependencySnapshots {
  static getOutdatedDependencyBranches() {
    return graphQl
      .query(
        `
          query {
            outdatedDependencyBranches
          }
        `,
      )
      .then((response) => response.outdatedDependencyBranches);
  }

  static getOutdatedDependencySnapshots(branches, since, until) {
    if (!Array.isArray(branches) || !branches.length) {
      return Promise.resolve([]);
    }

    const getPage = (page, perPage) =>
      graphQl
        .query(
          `
          query (
            $page: Int,
            $perPage: Int,
            $branches: [String!]!,
            $since: Timestamp,
            $until: Timestamp
          ) {
            outdatedDependencySnapshots(
              page: $page,
              perPage: $perPage,
              branches: $branches,
              since: $since,
              until: $until
            ) {
              count
              page
              perPage
              data {
                id
                branch
                commitHash
                sequence
                date
                outdatedPercentage
                outdatedCount
                evaluatedCount
                totalCount
                unknownCount
                createdAt
              }
            }
          }
        `,
          { page, perPage, branches, since, until },
        )
        .then((response) => response.outdatedDependencySnapshots);

    return collectPages(getPage);
  }
}
