'use strict';

import { handleActions } from 'redux-actions';
import _ from 'lodash';

export default handleActions(
  {
    SET_SHOW_ISSUEBREAKDOWN: (state, action) => _.assign({}, state, { showIssues: action.payload }),
  },
  {
    chartResolution: 'months', //list bucket size, can be 'years', 'months', 'weeks' or 'days'
  },
);
