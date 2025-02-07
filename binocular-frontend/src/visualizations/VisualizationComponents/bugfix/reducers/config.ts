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
    // Copied from changes component to get the graph running
    SET_SELECTED_AUTHORS: (state, action: Action<any>) => _.assign({}, state, { selectedAuthors: [...action.payload] }),
    SET_DISPLAY_METRIC: (state, action: Action<any>) => _.assign({}, state, { displayMetric: action.payload }),
    SET_GRAPH_STYLE: (state, action: Action<any>) => _.assign({}, state, { graphSwitch: action.payload }),
  },
  {
    //initial state (these are used for dahboard)
    currentBranch: null,
    allBranches: [],
    files: [], // Files for the currently selected branch
    activeFiles: [],
    branchOptions: [],
    mode: 'absolute',
    // These are used for graph (copied from changes component to get the graph running)
    chartResolution: 'months', //chart bucket size, can be 'years', 'months', 'weeks' or 'days'
    selectedAuthors: [], //Authors checked in the CheckBoxLegend, Array of objects: [{id: 1234, gitSignature: 'Dev1 <Dev1@email.com>'}, ...]
    availableAuthors: [], //All authors that should be displayed in CheckBoxLegend, Same format as above
    displayMetric: 'linesChanged', //display metric for Empty-Chart, can be 'linesChanged' or 'commits'
    graphSwitch: false,
  },
);
