'use strict';

import { ApolloClient, HttpLink, InMemoryCache } from '@apollo/client';

class GraphQL {
  public client;

  constructor(apiKey: string) {
    this.client = new ApolloClient({
      link: new HttpLink({
        uri: 'https://api.github.com/graphql',
        headers: { authorization: `Bearer ${apiKey}` },
      }),
      cache: new InMemoryCache(),
    });
  }
}

export { GraphQL };
