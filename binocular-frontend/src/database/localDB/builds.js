'use strict';

import PouchDB from 'pouchdb-browser';
import PouchDBFind from 'pouchdb-find';
import PouchDBAdapterMemory from 'pouchdb-adapter-memory';
import moment from 'moment/moment';
import _ from 'lodash';
import { findAll, findCommitBuildConnections } from './utils';
PouchDB.plugin(PouchDBFind);
PouchDB.plugin(PouchDBAdapterMemory);

export default class Builds {
  static getBuildData(db, relations, commitSpan, significantSpan) {
    const from = significantSpan ? significantSpan[0] : new Date(0);
    const to = significantSpan ? significantSpan[1] : new Date();

    // add stats object to each build
    return findAll(db, 'builds').then(async (res) => {
      const allCommits = (await findAll(db, 'commits')).docs;
      const commitBuildConnections = (await findCommitBuildConnections(relations)).docs;

      const emptyStats = { success: 0, failed: 0, pending: 0, cancelled: 0 };

      return res.docs
        .filter((build) => new Date(build.createdAt) >= from && new Date(build.createdAt) <= to)
        .map((build) => {
          const stats = Object.assign({}, emptyStats);
          if (build.status === 'success') {
            stats.success = 1;
          } else if (build.status === 'failed' || build.status === 'errored') {
            stats.failed = 1;
          } else if (build.status === 'cancelled') {
            stats.cancelled = 1;
          }
          build.stats = stats;
          build.commit = { sha: null };

          const relevantConnection = commitBuildConnections.filter((cb) => cb.to === build._id);
          if (relevantConnection.length !== 0) {
            const relevantCommit = allCommits.filter((c) => c._id === relevantConnection[0].from);
            if (relevantCommit.length !== 0) {
              build.commit.sha = relevantCommit[0].sha;
            }
          }

          return build;
        });
    });
  }
}
