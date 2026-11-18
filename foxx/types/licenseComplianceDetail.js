'use strict';

const gql = require('graphql-sync');
const requiredString = { type: new gql.GraphQLNonNull(gql.GraphQLString) };

module.exports = new gql.GraphQLObjectType({
  name: 'LicenseComplianceDetail',
  fields: () => ({
    library: requiredString,
    component: requiredString,
    license: { type: gql.GraphQLString },
    category: requiredString,
    reason: requiredString,
  }),
});
