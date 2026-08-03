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
  name: 'OutdatedDependencySnapshot',
  description: 'Commit-level percentage of dependencies outdated at the time of the commit',
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
      description: 'Commit hash for this snapshot',
    },
    sequence: {
      type: gql.GraphQLInt,
      description: 'First-parent position of the commit in the branch',
      resolve: (doc) => doc.sequence,
    },
    date: {
      type: new gql.GraphQLNonNull(Timestamp),
      description: 'Commit timestamp used for the snapshot',
      resolve: (doc) => doc.date,
    },
    outdatedPercentage: {
      type: gql.GraphQLFloat,
      description: 'Percentage of evaluable dependencies that were outdated',
      resolve: (doc) => doc.outdatedPercentage,
    },
    outdatedCount: countField('outdatedCount', 'Dependencies older than the latest stable release available at this date'),
    evaluatedCount: countField('evaluatedCount', 'Dependencies with comparable installed and registry versions'),
    totalCount: countField('totalCount', 'Direct dependencies found in package-lock.json'),
    unknownCount: countField('unknownCount', 'Dependencies excluded because version or registry data was unavailable'),
    createdAt: {
      type: Timestamp,
      description: 'When this snapshot was created',
      resolve: (doc) => doc.createdAt,
    },
  }),
});
