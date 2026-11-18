'use strict';

const gql = require('graphql-sync');

module.exports = new gql.GraphQLObjectType({
  name: 'PatchLagObservation',
  fields: () => ({
    library: { type: new gql.GraphQLNonNull(gql.GraphQLString) },
    component: { type: new gql.GraphQLNonNull(gql.GraphQLString) },
    vulnerabilityId: { type: new gql.GraphQLNonNull(gql.GraphQLString) },
    lagDays: { type: new gql.GraphQLNonNull(gql.GraphQLFloat) },
  }),
});
