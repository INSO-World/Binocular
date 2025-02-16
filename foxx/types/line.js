'use strict';

const gql = require('graphql-sync');

module.exports = new gql.GraphQLObjectType({
  name: 'Line',
  description: 'A line in a diff hunk',
  fields() {
    return {
      content: {
        type: gql.GraphQLString,
        description: 'The content of the line',
      },
      origin: {
        type: gql.GraphQLString,
        description: 'The origin of the line ("+", "-", " ")',
      },
    };
  },
});
