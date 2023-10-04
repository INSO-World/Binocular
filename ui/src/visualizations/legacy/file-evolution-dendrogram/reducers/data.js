'use strict';

import { handleActions } from 'redux-actions';
import _ from 'lodash';

export default handleActions(
  {
    REQUEST_FILE_EVOLUTION_DENDROGRAM_DATA: (state) => _.assign({}, state, { isFetching: true }),
    RECEIVE_FILE_EVOLUTION_DENDROGRAM_DATA: (state, action) => {
      return _.assign({}, state, {
        data: action.payload,
        isFetching: false,
        receivedAt: action.meta.receivedAt,
      });
    },
  },
  {
    data: {
      files: [],
    },
    lastFetched: null,
    isFetching: null,
  }
);