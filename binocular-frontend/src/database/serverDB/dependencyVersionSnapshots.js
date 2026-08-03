'use strict';

import { collectPages, graphQl } from '../../utils';

export default class DependencyVersionSnapshots {
  static getDependencyVersionBranches() {
    return graphQl
      .query(
        `
          query {
            dependencyVersionBranches
          }
        `,
      )
      .then((response) => response.dependencyVersionBranches);
  }

  static getDependencyVersionSnapshots(branch) {
    const getPage = (page, perPage) =>
      graphQl
        .query(
          `
          query ($page: Int, $perPage: Int, $branch: String!) {
            dependencyVersionSnapshots(page: $page, perPage: $perPage, branch: $branch) {
              count
              page
              perPage
              data {
                id
                branch
                component
                library
                commitHash
                sequence
                date
                author
                oldVersion
                newVersion
                dependencyType
                wasDependencyType
                sourceType
                highestSeverity
                vulnerabilityCount
                vulnerabilities {
                  id
                  severity
                  title
                  advisoryUrl
                  cve
                }
                createdAt
              }
            }
          }
        `,
          { page, perPage, branch },
        )
        .then((response) => response.dependencyVersionSnapshots);

    return collectPages(getPage);
  }
}
