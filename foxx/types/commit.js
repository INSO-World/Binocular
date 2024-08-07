'use strict';

const gql = require('graphql-sync');
const arangodb = require('@arangodb');
const db = arangodb.db;
const aql = arangodb.aql;
const commitsToCommits = db._collection('commits-commits');
const commitsToFiles = db._collection('commits-files');
const commitsToFilesToUsers = db._collection('commits-files-users');
const commitsToUsers = db._collection('commits-users');
const CommitsToModules = db._collection('commits-modules');
const commitsToBuilds = db._collection('commits-builds');
const paginated = require('./paginated.js');
const Timestamp = require('./Timestamp.js');

module.exports = new gql.GraphQLObjectType({
  name: 'Commit',
  description: 'A single git commit',
  fields() {
    return {
      sha: {
        type: new gql.GraphQLNonNull(gql.GraphQLString),
      },
      shortSha: {
        type: new gql.GraphQLNonNull(gql.GraphQLString),
        resolve: (e) => e.sha.substring(0, 7),
      },
      message: {
        type: gql.GraphQLString,
        description: 'The commit message',
      },
      messageHeader: {
        type: gql.GraphQLString,
        description: 'Header of the commit message',
        resolve: (c) => c.message.split('\n')[0],
      },
      signature: {
        type: gql.GraphQLString,
        description: "The commit author's signature",
        resolve(commit) {
          return db
            ._query(
              aql`
              FOR user, edge
              IN OUTBOUND ${commit} ${commitsToUsers}
              return user.gitSignature
          `
            )
            .toArray()[0];
        },
      },
      branch: {
        type: gql.GraphQLString,
        description: 'The commit branch',
      },
      parents: {
        type: new gql.GraphQLList(gql.GraphQLString),
        description: 'Parents of the commit',
        resolve(commit) {
          return db
            ._query(
              aql`
            FOR c, edge
            IN OUTBOUND ${commit} ${commitsToCommits}
            RETURN c.sha`
            )
            .toArray();
        },
      },
      date: {
        type: Timestamp,
        description: 'The date of the commit',
      },
      webUrl: {
        type: gql.GraphQLString,
        description: 'Web-url (if available) of this commit',
      },
      stats: {
        type: require('./stats'),
        description: 'The changing stats of the commit',
      },
      files: paginated({
        type: require('./fileInCommit.js'),
        description: 'The files touched by this commit',
        query: (commit, args, limit) => aql`
          FOR file, edge
            IN OUTBOUND ${commit} ${commitsToFiles}
            let o = (
              FOR user, conn
                IN OUTBOUND edge ${commitsToFilesToUsers}
                    RETURN {
                      user: user.gitSignature,
                      hunks: conn.hunks,
                    }
            )
            ${limit}
            RETURN {
              file: file,
              lineCount: edge.lineCount,
              stats: edge.stats,
              hunks: edge.hunks,
              action: edge.action,
              ownership: o,
            }`,
      }),
      file: {
        type: require('./fileInCommit.js'),
        args: {
          path: {
            description: 'Path of the file',
            type: new gql.GraphQLNonNull(gql.GraphQLString),
          },
        },
        description: 'The file with path touched by this commit',
        resolve(commit, args) {
          return db
            ._query(
              aql`
          FOR file, edge
            IN OUTBOUND ${commit} ${commitsToFiles}
            FILTER file.path == ${args.path}
            RETURN {
              file,
              lineCount: edge.lineCount,
              hunks: edge.hunks
            }`
            )
            .toArray()[0];
        },
      },
      user: {
        type: require('./user.js'),
        description: 'The author of this commit',
        resolve(commit /*, args*/) {
          return db
            ._query(
              aql`
            FOR
            user
            IN
            OUTBOUND ${commit} ${commitsToUsers}
              RETURN user
          `
            )
            .toArray()[0];
        },
      },
      builds: {
        type: new gql.GraphQLList(require('./build.js')),
        description: 'Builds started on this commit',
        resolve(commit) {
          return db
            ._query(
              aql`
              FOR build, edge
              IN OUTBOUND ${commit} ${commitsToBuilds}
              RETURN build`
            )
            .toArray();
        },
      },
      modules: paginated({
        type: require('./moduleInCommit'),
        description: 'modules modified in the particular commit',
        query: (commit, args, limit) => aql`
          FOR module, edge
            IN OUTBOUND ${commit} ${CommitsToModules}
            ${limit}
            RETURN {
              module,
              webUrl: edge.webUrl,
              stats: edge.stats
            }`,
      }),
    };
  },
});
