'use strict';

import { collectPages, graphQl } from '../../utils';

export default class LicenseComplianceSnapshots {
  static getDetails(branch, snapshotId) {
    return graphQl
      .query(
        `
          query ($branch: String!, $snapshotId: String!) {
            licenseComplianceDetails(branch: $branch, snapshotId: $snapshotId) {
              library
              component
              license
              category
              reason
            }
          }
        `,
        { branch, snapshotId },
      )
      .then((response) => response.licenseComplianceDetails);
  }

  static getLicenseComplianceBranches() {
    return graphQl
      .query(
        `
          query {
            licenseComplianceBranches
          }
        `,
      )
      .then((response) => response.licenseComplianceBranches);
  }

  static getLicenseComplianceSnapshots(branch, since, until) {
    const getPage = (page, perPage) =>
      graphQl
        .query(
          `
          query (
            $page: Int,
            $perPage: Int,
            $branch: String!,
            $since: Timestamp,
            $until: Timestamp
          ) {
            licenseComplianceSnapshots(
              page: $page,
              perPage: $perPage,
              branch: $branch,
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
                compliantCount
                partiallyCompliantCount
                nonCompliantCount
                unknownCount
                totalCount
                createdAt
              }
            }
          }
        `,
          { page, perPage, branch, since, until },
        )
        .then((response) => response.licenseComplianceSnapshots);

    return collectPages(getPage);
  }
}
