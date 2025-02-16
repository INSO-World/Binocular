'use strict';

const gql = require('graphql-sync');
const LineType = require('./line');

module.exports = new gql.GraphQLObjectType({
  name: 'diffHunk',
  description: 'A hunk in a diff',
  fields() {
    return {
      oldStart: {
        type: gql.GraphQLInt,
        description: 'The starting line number in the old file',
      },
      oldLines: {
        type: gql.GraphQLInt,
        description: 'The number of lines in the old file',
      },
      newStart: {
        type: gql.GraphQLInt,
        description: 'The starting line number in the new file',
      },
      newLines: {
        type: gql.GraphQLInt,
        description: 'The number of lines in the new file',
      },
      lines: {
        type: new gql.GraphQLList(LineType),
        description: 'The lines in the hunk',
      },
    };
  },
});
