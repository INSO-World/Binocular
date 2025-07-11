'use strict';

const gql = require('graphql-sync');
const arangodb = require('@arangodb');
const Timestamp = require('./Timestamp');
const db = arangodb.db;
const aql = arangodb.aql;
const notesToAccounts = db._collection('notes-accounts');
const issuesToNotes = db._collection('issues-notes');
const mergeRequestsToNotes = db._collection('mergeRequests-notes')

module.exports = new gql.GraphQLObjectType({
  name: 'GitLabNote',
  description: 'A GitLabNote',
  fields() {
    return {
      author: {
        type: require('./account.js'),
        description: 'The github author of this issue',
        resolve(note /*, args*/) {
          return db
            ._query(
              aql`
              FOR author, edge
              IN outbound ${note} ${notesToAccounts}
              RETURN author
              `
            )
            .toArray()[0];
        },
      },
      issue: {
        type: require('./issue.js'),
        description: 'The github author of this issue',
        resolve(note /*, args*/) {
          const result = db._query(
            aql`
              FOR issue, edge
              IN INBOUND ${note} ${issuesToNotes}
              RETURN issue
            `
          ).toArray();

          return result.length > 0 ? result[0] : null;
        },
      },
      mergeRequest: {
        type: require('./mergeRequest.js'),
        description: 'The github author of this issue',
        resolve(note /*, args*/) {
          const result = db
            ._query(
              aql`
              FOR mergeRequest, edge
              IN INBOUND ${note} ${mergeRequestsToNotes}
              RETURN mergeRequest
              `
            )
            .toArray();

          return result.length > 0 ? result[0] : null;
        },
      },
      body: {
        type: gql.GraphQLString,
        description: 'body of the note',
      },
      createdAt: {
        type: Timestamp,
        description: 'Creation date of the issue',
      },
      updatedAt: {
        type: Timestamp,
        description: 'Sate the issue was updated',
      },
      system: {
        type: gql.GraphQLBoolean,
      },
      resolvable: {
        type: gql.GraphQLBoolean,
      },
      confidential: {
        type: gql.GraphQLBoolean,
      },
      internal: {
        type: gql.GraphQLBoolean,
      }
    };
  },
});
