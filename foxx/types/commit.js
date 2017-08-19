'use strict';

const gql = require('graphql-sync');
const hunkType = require('./hunk.js');
const arangodb = require('@arangodb');
const db = arangodb.db;
const aql = arangodb.aql;
const commitsToHunks = db._collection('commits-hunks');
const filesToHunks = db._collection('files-hunks');
const commitsToStakeholders = db._collection('commits-stakeholders');

module.exports = new gql.GraphQLObjectType({
  name: 'Commit',
  description: 'A single git commit',
  fields() {
    return {
      sha: {
        type: new gql.GraphQLNonNull(gql.GraphQLString),
        resolve: e => e._key
      },
      shortSha: {
        type: new gql.GraphQLNonNull(gql.GraphQLString),
        resolve: e => e._key.substring(0, 7)
      },
      message: {
        type: gql.GraphQLString,
        description: 'The commit message'
      },
      messageHeader: {
        type: gql.GraphQLString,
        description: 'Header of the commit message',
        resolve: c => c.message.split('\n')[0]
      },
      signature: {
        type: gql.GraphQLString,
        description: "The commit author's signature"
      },
      date: {
        type: gql.GraphQLString,
        description: 'The date of the commit'
      },
      stats: {
        type: new gql.GraphQLObjectType({
          name: 'Stats',
          fields: {
            additions: {
              type: gql.GraphQLInt
            },
            deletions: {
              type: gql.GraphQLInt
            }
          }
        })
      },
      hunks: {
        type: new gql.GraphQLList(hunkType),
        description: 'The hunks in this commit',
        args: {},
        resolve(commit /*, args*/) {
          return db
            ._query(
              aql`FOR hunk
                  IN
                  INBOUND ${commit} ${commitsToHunks}
                  SORT hunk._key ASC
                    RETURN hunk`
            )
            .toArray();
        }
      },
      files: {
        type: new gql.GraphQLList(require('./file.js')),
        description: 'The files touched by this commit',
        args: {},
        resolve(commit /*, args*/) {
          return db
            ._query(
              aql`FOR hunk
                  IN INBOUND ${commit} ${commitsToHunks}
                    FOR file
                    IN
                    INBOUND
                    hunk ${filesToHunks}
                      RETURN DISTINCT file`
            )
            .toArray();
        }
      },
      stakeholder: {
        type: require('./stakeholder.js'),
        description: 'The author of this commit',
        resolve(commit /*, args*/) {
          return db
            ._query(
              aql`
            FOR
            stakeholder
            IN
            INBOUND ${commit} ${commitsToStakeholders}
              RETURN stakeholder
          `
            )
            .toArray()[0];
        }
      }
    };
  }
});
