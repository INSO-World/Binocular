'use strict';

const gql = require('graphql-sync');

module.exports = new gql.GraphQLObjectType({
  name: 'Label',
  description: 'Label of issue',
  fields() {
    return {
      id: {
        type: gql.GraphQLString,
      },
      url: {
        type: gql.GraphQLString,
      },
      name: {
        type: gql.GraphQLString,
      },
      color: {
        type: gql.GraphQLString,
      },
      isDefault: {
        type: gql.GraphQLBoolean,
      },
      description: {
        type: gql.GraphQLString,
      },
    };
  },
});
