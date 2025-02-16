'use strict';

const gql = require('graphql-sync');
const diffHunkType = require('./diffHunk');

module.exports = new gql.GraphQLObjectType({
  name: 'DiffFile',
  description: 'A file in a diff',
  fields() {
    return {
      oldFilePath: {
        type: gql.GraphQLString,
        description: 'The path of the file in the parent commit',
      },
      newFilePath: {
        type: gql.GraphQLString,
        description: 'The path of the file in the current commit',
      },
      status: {
        type: gql.GraphQLString,
        description: 'The status of the file ("added", "deleted", "modified")',
      },
      hunks: {
        type: new gql.GraphQLList(diffHunkType),
        description: 'The hunks in the file',
      },
    };
  },
});
