'use strict';

const gql = require('graphql-sync');
const requiredString = { type: new gql.GraphQLNonNull(gql.GraphQLString) };

module.exports = new gql.GraphQLObjectType({
  name: 'OutdatedDependencyDetail',
  fields: () => ({
    library: requiredString,
    component: requiredString,
    installedVersion: { type: gql.GraphQLString },
    latestVersion: { type: gql.GraphQLString },
    freshness: requiredString,
    knownVulnerabilities: { type: new gql.GraphQLList(new gql.GraphQLNonNull(gql.GraphQLString)) },
  }),
});
