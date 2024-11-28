'use strict';

import { Action, handleActions } from 'redux-actions';
import * as _ from 'lodash';

export default handleActions(
  {
    // TODO for fetching data
  },
  {
    // Copied from code ownership
    data: {},
    lastFetched: null,
    isFetching: null,
  },
);
