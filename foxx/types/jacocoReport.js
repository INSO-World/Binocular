'use strict';

const gql = require('graphql-sync');
const Timestamp = require('./Timestamp.js');

module.exports = new gql.GraphQLObjectType({
  name: 'JacocoReport',
  description: 'A single jacoco report',
  fields() {
    return {
      id: {
        type: new gql.GraphQLNonNull(gql.GraphQLString),
        description: 'The id of the jacoco report',
      },
      node_id: {
        type: new gql.GraphQLNonNull(gql.GraphQLString),
        description: 'The node_id of the jacoco report',
      },
      created_at: {
        type: Timestamp,
        description: 'The creation date of the jacoco report',
      },
      updated_at: {
        type: Timestamp,
        description: 'The update date of the jacoco report',
      },
      xmlContent: {
        type: new gql.GraphQLNonNull(gql.GraphQLString),
        description: 'The xml content of the jacoco report',
      }
    };
  }
});
