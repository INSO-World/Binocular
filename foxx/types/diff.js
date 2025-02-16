'use strict';

const gql = require('graphql-sync');
const DiffFileType = require('./diffFile');
const Timestamp = require("./Timestamp");

module.exports = new gql.GraphQLObjectType({
  name: 'Diff',
  description: 'The diff between a commit and its parent',
  fields() {
    return {
      sha: {
        type: new gql.GraphQLNonNull(gql.GraphQLString),
        description: 'The SHA of the parent commit',
      },
      shortSha: {
        type: new gql.GraphQLNonNull(gql.GraphQLString),
        resolve: (e) => e.sha.substring(0, 7),
        description: 'The shortSHA of the parent commit',
      },
      message: {
        type: gql.GraphQLString,
        description: 'The parent commit message',
      },
      messageHeader: {
        type: gql.GraphQLString,
        description: 'Header of the parent commit message',
        resolve: (c) => c.message.split('\n')[0],
      },
      branch: {
        type: gql.GraphQLString,
        description: 'The parent commit branch',
      },
      date: {
        type: Timestamp,
        description: 'The date of the parent commit',
      },
      webUrl: {
        type: gql.GraphQLString,
        description: 'Web-url (if available) of the parent commit',
      },
      files: {
        type: new gql.GraphQLList(DiffFileType),
        description: 'The files changed in the diff',
      },
    };
  },
});
