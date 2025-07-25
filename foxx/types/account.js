'use strict';

const gql = require('graphql-sync');
const arangodb = require('@arangodb');
const db = arangodb.db;
const aql = arangodb.aql;
const commitsToUsers = db._collection('commits-users');
const accountsToUsers = db._collection('accounts-users')
const paginated = require('./paginated.js');

module.exports = new gql.GraphQLObjectType({
  name: 'Account',
  description: 'A GitAccount',
  fields() {
    return {
      id: {
        type: new gql.GraphQLNonNull(gql.GraphQLString),
        resolve: (e) => e._key,
      },
      platform: {
        type: gql.GraphQLString,
        description: 'The platform of this account',
      },
      login: {
        type: gql.GraphQLString,
        description: 'The username of this account',
      },
      name: {
        type: gql.GraphQLString,
        description: 'The full name of the user this account belongs to',
      },
      url: {
        type: gql.GraphQLString,
        description: 'A link to the user profile on the respective platform',
      },
      avatarUrl: {
        type: gql.GraphQLString,
        description: 'A link to the profile picture of this account',
      },
      user: {
        type: require('./user.js'),
        description: 'The user details related to this GitHub account',
        resolve: function(account, args) {
          return db
            ._query(
              aql`
                FOR user 
                IN 
                OUTBOUND ${account} ${accountsToUsers}
                RETURN user`
            ).next();
        },
      }
    };
  },
});
