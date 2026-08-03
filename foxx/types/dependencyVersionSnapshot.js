'use strict';

const gql = require('graphql-sync');
const Timestamp = require('./Timestamp.js');

const DependencyVulnerability = new gql.GraphQLObjectType({
  name: 'DependencyVersionVulnerability',
  description: 'Vulnerability affecting a dependency version at a commit',
  fields: () => ({
    id: { type: new gql.GraphQLNonNull(gql.GraphQLString) },
    severity: { type: new gql.GraphQLNonNull(gql.GraphQLString) },
    title: { type: gql.GraphQLString },
    advisoryUrl: { type: gql.GraphQLString },
    cve: { type: gql.GraphQLString },
  }),
});

module.exports = new gql.GraphQLObjectType({
  name: 'DependencyVersionSnapshot',
  description: 'Dependency version-change event enriched with the vulnerabilities active after the change',
  fields: () => ({
    id: {
      type: new gql.GraphQLNonNull(gql.GraphQLString),
      resolve: (doc) => doc._key || doc.id,
    },
    branch: { type: new gql.GraphQLNonNull(gql.GraphQLString) },
    component: { type: new gql.GraphQLNonNull(gql.GraphQLString) },
    library: { type: new gql.GraphQLNonNull(gql.GraphQLString) },
    commitHash: { type: new gql.GraphQLNonNull(gql.GraphQLString) },
    sequence: { type: gql.GraphQLInt },
    date: { type: new gql.GraphQLNonNull(Timestamp) },
    author: { type: gql.GraphQLString },
    oldVersion: { type: gql.GraphQLString },
    newVersion: { type: gql.GraphQLString },
    dependencyType: { type: new gql.GraphQLNonNull(gql.GraphQLString) },
    wasDependencyType: { type: new gql.GraphQLNonNull(gql.GraphQLString) },
    sourceType: { type: new gql.GraphQLNonNull(gql.GraphQLString) },
    highestSeverity: { type: new gql.GraphQLNonNull(gql.GraphQLString) },
    vulnerabilityCount: { type: new gql.GraphQLNonNull(gql.GraphQLInt) },
    vulnerabilities: {
      type: new gql.GraphQLNonNull(new gql.GraphQLList(new gql.GraphQLNonNull(DependencyVulnerability))),
      resolve: (doc) => doc.vulnerabilities || [],
    },
    createdAt: { type: Timestamp },
  }),
});
