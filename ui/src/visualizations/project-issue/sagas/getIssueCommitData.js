'use strict';

import { traversePages, graphQl } from '../../../utils';

/**
 * Get issue commit data from the database.
 * @param commitSpan Array of two time values (ms), first commit and last commit.
 * @param significantSpan Array of two time values (ms), first significant and last significant commit
 * (only these will actually be returned, used for zooming, the rest of the time will be empty data).
 * @returns {*} (see below)
 */
export default function getIssueCommitData(issueCommitSpan, significantSpan) {
  const issueCommitList = [];

  return traversePages(getIssueCommitsPage(significantSpan[1]), commit => {
    issueCommitList.push(commit);
  }).then(function() {
    return issueCommitList;
  });
}

const getIssueCommitsPage = until => (page, perPage) => {
  return graphQl
    .query(
      `query($page: Int, $perPage: Int, $until: Timestamp) {
             commits(page: $page, perPage: $perPage, until: $until) {
               count
               page
               perPage
               data {
                 sha
                 date
                 messageHeader
                 signature
                 stats {
                   additions
                   deletions
                 }
               }
             }
          }`,
      { page, perPage, until }
    )
    .then(resp => resp.commits);
};
