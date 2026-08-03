'use strict';

const gql = require('graphql-sync');
const Timestamp = require('./Timestamp.js');

function countField(name, description) {
  return {
    type: new gql.GraphQLNonNull(gql.GraphQLInt),
    description,
    resolve: (doc) => doc[name] || 0,
  };
}

module.exports = new gql.GraphQLObjectType({
  name: 'LicenseComplianceSnapshot',
  description: 'License compliance counts for direct dependencies at a lockfile change',
  fields: () => ({
    id: {
      type: new gql.GraphQLNonNull(gql.GraphQLString),
      description: 'Document key or logical id',
      resolve: (doc) => doc._key || doc.id,
    },
    branch: {
      type: new gql.GraphQLNonNull(gql.GraphQLString),
      description: 'Analyzed branch name',
    },
    commitHash: {
      type: new gql.GraphQLNonNull(gql.GraphQLString),
      description: 'Commit containing the lockfile state',
    },
    sequence: {
      type: gql.GraphQLInt,
      description: 'Position among lockfile changes on the branch',
      resolve: (doc) => doc.sequence,
    },
    date: {
      type: new gql.GraphQLNonNull(Timestamp),
      description: 'Commit timestamp used for the snapshot',
      resolve: (doc) => doc.date,
    },
    compliantCount: countField('compliantCount', 'Permissively licensed dependencies'),
    partiallyCompliantCount: countField('partiallyCompliantCount', 'Weak-copyleft dependencies requiring review'),
    nonCompliantCount: countField('nonCompliantCount', 'Restricted or strong-copyleft dependencies'),
    unknownCount: countField('unknownCount', 'Dependencies without recognized license metadata'),
    totalCount: countField('totalCount', 'Direct dependencies evaluated in this snapshot'),
    createdAt: {
      type: Timestamp,
      description: 'When this snapshot was created',
      resolve: (doc) => doc.createdAt,
    },
  }),
});
