'use strict';

import { collectPages, graphQl, traversePages } from '../../utils';
import _ from 'lodash';
import moment from 'moment/moment';

export default class Commits {
  static getCommitData(commitSpan, significantSpan) {
    const commitList = [];

    const getCommitsPage = (since, until) => (page, perPage) => {
      return graphQl
        .query(
          `query($page: Int, $perPage: Int, $since: Timestamp, $until: Timestamp) {
             commits(page: $page, perPage: $perPage, since: $since, until: $until) {
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
          { page, perPage, since, until }
        )
        .then((resp) => resp.commits);
    };
    return traversePages(getCommitsPage(significantSpan[0], significantSpan[1]), (commit) => {
      commitList.push(commit);
    }).then(function () {
      return commitList;
    });
  }

  static getCommitsWithFileTree(commitSpan, significantSpan) {
    const commitList = [];
    const commitsWithFileTree = (since, until) => (page, perPage) => {
      return graphQl
        .query(
          `query($page: Int, $perPage: Int, $since: Timestamp, $until: Timestamp) {
             commits(page: $page, perPage: $perPage, since: $since, until: $until) {
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
                 files {
              data {
                stats {
                  additions
                  deletions
                }
                file {
                  path,
                }
                lineCount
              }
            }
               }
             }
          }`,
          { page, perPage, since, until }
        )
        .then((resp) => resp.commits);
    };

    return traversePages(commitsWithFileTree(significantSpan[0], significantSpan[1]), (commit) => {
      commitList.push(commit);
    }).then(function () {
      return commitList;
    });
  }
  static searchCommits(text) { //TODO richtige Query schreiben
    /*
    const commitList = [];

    const getCommitPageSearch = (text) => (page, perPage) => {
      return graphQl
        .query(
          `
                  query($q: String) {
                    commits(page: 1, perPage: 50, q: $q, sort: "DESC") {
                      data { iid title createdAt closedAt }
                    }
                  }`,
          { q: text }
        )
        .then((resp) => resp.data);
    };

    return traversePages(getCommitPageSearch(text), (commit) => {
      commitList.push(commit);
    }).then(function () {
      return commitList;
    });*/
  }



  static getCommitDataOwnershipRiver(commitSpan, significantSpan, granularity, interval) {
    const statsByAuthor = {};

    const totals = {
      count: 0,
      additions: 0,
      deletions: 0,
      changes: 0,
    };

    const data = [
      {
        date: new Date(significantSpan[0]),
        totals: _.cloneDeep(totals),
        statsByAuthor: {},
      },
    ];

    let next = moment(significantSpan[0]).startOf(granularity.unit).toDate().getTime();

    const getCommitsPage = (since, until) => (page, perPage) => {
      return graphQl
        .query(
          `query($page: Int, $perPage: Int, $since: Timestamp, $until: Timestamp) {
             commits(page: $page, perPage: $perPage, since: $since, until: $until) {
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
          { page, perPage, since, until }
        )
        .then((resp) => resp.commits);
    };

    function group(data) {
      const lastDatum = _.last(data);

      if (_.keys(lastDatum.statsByAuthor).length < 50) {
        return data;
      }

      const meanCommitCount = _.meanBy(_.values(lastDatum.statsByAuthor), 'count');
      const threshhold = meanCommitCount;

      const applyGroupBy = (stats, predicate) => {
        const groupStats = {
          count: 0,
          additions: 0,
          deletions: 0,
          changes: 0,
        };
        const groupedCommitters = [];

        const groupedStats = _.omitBy(stats, (stats, author) => {
          if (predicate(stats, author)) {
            groupStats.count += stats.count;
            groupStats.additions += stats.additions;
            groupStats.deletions += stats.deletions;
            groupStats.changes += stats.changes;
            groupedCommitters.push(author);
            return true;
          }
        });

        groupedStats.other = groupStats;

        return {
          groupedStats,
          groupedCommitters,
        };
      };

      const { groupedCommitters } = applyGroupBy(lastDatum.statsByAuthor, (stats) => {
        return stats.count <= threshhold;
      });

      _.each(data, (datum) => {
        const { groupedStats } = applyGroupBy(datum.statsByAuthor, (stats, author) => _.includes(groupedCommitters, author));

        datum.statsByAuthor = groupedStats;
      });

      return data;
    }

    return traversePages(getCommitsPage(significantSpan[0], significantSpan[1]), (commit) => {
      const dt = Date.parse(commit.date);
      let stats = statsByAuthor[commit.signature];
      if (!stats) {
        stats = statsByAuthor[commit.signature] = {
          count: 0,
          additions: 0,
          deletions: 0,
          changes: 0,
        };
      }

      totals.count++;
      totals.additions += commit.stats.additions;
      totals.deletions += commit.stats.deletions;
      totals.changes += commit.stats.additions + commit.stats.deletions;

      stats.count++;
      stats.additions += commit.stats.additions;
      stats.deletions += commit.stats.deletions;
      stats.changes += commit.stats.additions + commit.stats.deletions;

      while (dt >= next) {
        const dataPoint = {
          date: new Date(next),
          totals: _.cloneDeep(totals),
          statsByAuthor: _.cloneDeep(statsByAuthor),
        };

        data.push(dataPoint);
        next += interval;
      }
    }).then(function () {
      data.push({
        date: new Date(significantSpan[1]),
        totals: _.cloneDeep(totals),
        statsByAuthor: _.cloneDeep(statsByAuthor),
      });

      return group(data);
    });
  }

  static getRelatedCommitDataOwnershipRiver(issue) {
    if (!issue) {
      return [];
    }

    const getRelatedCommitsPage = (issue) => (page, perPage) => {
      return graphQl
        .query(
          `query($page: Int, $perPage: Int, $iid: Int!){
         issue (iid: $iid){
           commits (page: $page, perPage: $perPage) {
             count
             data {
               sha
               shortSha
               message
               messageHeader
               signature
               webUrl
               date
             }
           }
         }
       }`,
          { page, perPage, iid: issue.iid }
        )
        .then((resp) => resp.issue.commits);
    };
    return collectPages(getRelatedCommitsPage(issue)).map((commit) => {
      commit.date = new Date(commit.date);
      return commit;
    });
  }

  static getCommitDateHistogram(granularity, dateField, since, until) {
    return graphQl.query(
      `query($granularity: DateGranularity!, $dateField: String!, $since: Timestamp, $until: Timestamp) {
           commitDateHistogram(granularity: $granularity, since: $since, until: $until) {
             category
             count
           }
           goodCommits: commitDateHistogram(granularity: $granularity, buildFilter: successful, since: $since, until: $until) {
             category
             count
           }
           badCommits: commitDateHistogram(granularity: $granularity, buildFilter: failed, since: $since, until: $until) {
             category
             count
           }
           issueDateHistogram(granularity: $granularity, dateField: $dateField, since: $since, until: $until) {
             category
             count
           }
         }`,
      {
        granularity: granularity,
        dateField: dateField,
        since: since,
        until: until,
      }
    );
  }

  static getCodeHotspotsChangeData(file) {
    return graphQl.query(
      `
        query($file: String!) {
          file(path: $file){
              path
              maxLength
              commits{
                  data{
                      message
                      sha
                      signature
                      branch
                      history
                      parents
                      date
                      stats{
                          additions
                          deletions
                      }
                      file(path: $file){
                          file{
                              path
                          }
                          lineCount
                          hunks{
                              newStart
                              newLines
                              oldStart
                              oldLines
                          }
                      }
                  }
              }
          }
      }
      `,
      { file: file }
    );
  }
}
