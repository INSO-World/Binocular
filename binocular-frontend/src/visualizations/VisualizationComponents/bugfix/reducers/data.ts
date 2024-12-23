'use strict';

import { Action, handleActions } from 'redux-actions';
import * as _ from 'lodash';

export default handleActions(
  {
    // TODO for fetching data (right now all of these actions are from changes)
    REQUEST_CHANGES_DATA: (state) => _.assign({}, state, { isFetching: true }),
    RECEIVE_CHANGES_DATA: (state, action: Action<any>) => {
      return _.assign({}, state, {
        data: action.payload,
        isFetching: false,
      });
    },
  },
  {
    // Copied from code ownership
    data: {},
    lastFetched: null,
    isFetching: null,
  },
);
