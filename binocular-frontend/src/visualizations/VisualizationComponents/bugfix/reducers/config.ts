'use strict';

import { Action, handleActions } from 'redux-actions';
import _ from 'lodash';

// Parts copied from code-ownership
export default handleActions(
  {
    //actions
    CO_SET_CURRENT_BRANCH: (state, action: Action<any>) => _.assign({}, state, { currentBranch: action.payload ? action.payload : null }),
    CO_SET_ACTIVE_FILES: (state, action: Action<any>) => _.assign({}, state, { activeFiles: action.payload ? action.payload : [] }),
    // These actions are from me
    CO_SET_ALL_BRANCHES: (state, action: Action<any>) => _.assign({}, state, { allBranches: action.payload ? action.payload : [] }),
    CO_SET_BRANCH_OPTIONS: (state, action: Action<any>) => _.assign({}, state, { branchOptions: action.payload ? action.payload : [] }),
    CO_SET_FILES: (state, action: Action<any>) => _.assign({}, state, { files: action.payload ? action.payload : [] }),
  },
  {
    //initial state
    currentBranch: null,
    allBranches: [],
    files: [], // Files for the currently selected branch
    activeFiles: [],
    branchOptions: [],
    mode: 'absolute',
  },
);
