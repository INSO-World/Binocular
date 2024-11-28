'use strict';

import { mapSaga } from '../../../../sagas/utils';
import { throttle, fork, takeEvery } from 'redux-saga/effects';
import { createAction } from 'redux-actions';

// Most of the code copied and changed from the code-ownership (Changes are done mainly in state...)

//define actions
export const requestRefresh = createAction('REQUEST_REFRESH');
const refresh = createAction('REFRESH');

export const setCurrentBranch = createAction('CO_SET_CURRENT_BRANCH', (b) => b);
export const setActiveFiles = createAction('CO_SET_ACTIVE_FILES', (f) => f);
export const setFiles = createAction('CO_SET_FILES', (f) => f);

export const setAllBranches = createAction('CO_SET_ALL_BRANCHES', (s) => s); // From me
export const setBranchOptions = createAction('CO_SET_BRANCH_OPTIONS', (o) => o); // From me

export default function* () {
  yield fork(watchRefreshRequests);
  yield fork(watchSetCurrentBranch);
}

//throttle ensures that only one refresh action will be dispatched in an interval of 2000ms
function* watchRefreshRequests() {
  yield throttle(2000, 'REQUEST_REFRESH', mapSaga(refresh));
}

function* watchSetCurrentBranch() {
  yield takeEvery('CO_SET_CURRENT_BRANCH', mapSaga(requestRefresh));
}
