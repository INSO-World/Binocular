'use strict';

import { fetchFactory, mapSaga, timestampedActionFactory } from '../../../../sagas/utils';
import { throttle, fork, takeEvery, select } from 'redux-saga/effects';
import { createAction } from 'redux-actions';
import { Commit } from '../../../../types/commitTypes.ts';
import { Palette } from '../../../../types/authorTypes.ts';
import { Bounds } from '../../../../types/boundsTypes.ts';
import Database from '../../../../database/database';
import { GlobalState } from '../../../../types/globalTypes.ts';
import chroma from 'chroma-js';
import _ from 'lodash';

// Most of the code copied and changed from the code-ownership (Changes are done mainly in state...)

//define actions
export const requestRefresh = createAction('REQUEST_REFRESH');
const refresh = createAction('REFRESH');

export const setCurrentBranch = createAction('CO_SET_CURRENT_BRANCH', (b) => b);
export const setActiveFiles = createAction('CO_SET_ACTIVE_FILES', (f) => f);
export const setFiles = createAction('CO_SET_FILES', (f) => f);

export const setAllBranches = createAction('CO_SET_ALL_BRANCHES', (s) => s); // From me
export const setBranchOptions = createAction('CO_SET_BRANCH_OPTIONS', (o) => o); // From me
export const setGraphStyle = createAction('SET_GRAPH_STYLE', (o) => o);
export const setShowFilterMenu = createAction('SET_SHOW_FILTER_MENU', (o) => o);
export const setRegexConfig = createAction('SET_REGEX_CONFIG', (o) => o);

//throttle ensures that only one refresh action will be dispatched in an interval of 2000ms
function* watchRefreshRequests() {
  yield throttle(2000, 'REQUEST_REFRESH', mapSaga(refresh));
}

function* watchSetCurrentBranch() {
  yield takeEvery('CO_SET_CURRENT_BRANCH', mapSaga(requestRefresh));
}

export const requestChangesData = createAction('REQUEST_CHANGES_DATA');
export const receiveChangesData = timestampedActionFactory('RECEIVE_CHANGES_DATA');
export const receiveChangesDataError = createAction('RECEIVE_CHANGES_DATA_ERROR');

interface ChangesData {
  otherCount: number;
  filteredCommits: Commit[];
  commits: Commit[];
  committers: string[];
  palette: Palette;
  firstCommitTimestamp: number;
  lastCommitTimestamp: number;
  firstSignificantTimestamp: number;
  lastSignificantTimestamp: number;
  graphSwitch: boolean;
  showFilterMenu: boolean;
}

export default function* () {
  // fetch data once on entry
  yield* fetchChangesData();

  yield fork(watchRefreshRequests);
  yield fork(watchSetCurrentBranch);
  yield fork(watchMessages);

  // keep looking for viewport changes to re-fetch
  yield fork(watchRefresh);
  yield fork(watchToggleHelp);

  // keep looking for universal settings changes
  yield fork(watchTimeSpan);
  yield fork(watchSelectedAuthorsGlobal);
  yield fork(watchMergedAuthors);
  yield fork(watchOtherAuthors);
}

function* watchTimeSpan() {
  yield takeEvery('SET_TIME_SPAN', fetchChangesData);
}

function* watchSelectedAuthorsGlobal() {
  yield takeEvery('SET_SELECTED_AUTHORS_GLOBAL', fetchChangesData);
}

function* watchOtherAuthors() {
  yield takeEvery('SET_OTHER_AUTHORS', fetchChangesData);
}

function* watchMergedAuthors() {
  yield takeEvery('SET_MERGED_AUTHORS', fetchChangesData);
}

function* watchMessages() {
  yield takeEvery('message', mapSaga(requestRefresh));
}

function* watchToggleHelp() {
  yield takeEvery('TOGGLE_HELP', mapSaga(refresh));
}

function* watchRefresh() {
  yield takeEvery('REFRESH', fetchChangesData);
}

/**
 * Fetch data for dashboard, this still includes old functions that were copied over.
 */
export const fetchChangesData = fetchFactory(
  function* () {
    const bounds: Bounds = yield Database.getBounds();
    const firstCommitTimestamp = Date.parse(bounds.firstCommit.date);
    const lastCommitTimestamp = Date.parse(bounds.lastCommit.date);

    const firstIssueTimestamp = bounds.firstIssue ? Date.parse(bounds.firstIssue.createdAt) : firstCommitTimestamp;
    const lastIssueTimestamp = bounds.lastIssue ? Date.parse(bounds.lastIssue.createdAt) : lastCommitTimestamp;

    const state: GlobalState = yield select();
    const viewport = state.visualizations.bugfix.state.config.viewport || [0, null];
    let firstSignificantTimestamp = Math.max(viewport[0], Math.min(firstCommitTimestamp, firstIssueTimestamp));
    let lastSignificantTimestamp = viewport[1] ? viewport[1].getTime() : Math.max(lastCommitTimestamp, lastIssueTimestamp);
    const timeSpan = state.universalSettings.chartTimeSpan;
    firstSignificantTimestamp = timeSpan.from === undefined ? firstSignificantTimestamp : new Date(timeSpan.from).getTime();
    lastSignificantTimestamp = timeSpan.to === undefined ? lastSignificantTimestamp : new Date(timeSpan.to).getTime();
    const changesData: ChangesData = yield Promise.all([
      Database.getCommitData([firstCommitTimestamp, lastCommitTimestamp], [firstSignificantTimestamp, lastSignificantTimestamp]),
      Database.getCommitData([firstCommitTimestamp, lastCommitTimestamp], [firstCommitTimestamp, lastCommitTimestamp]),
    ])
      .then((result) => {
        const filteredCommits = result[0];
        const commits = result[1];

        const palette = getPalette(commits, 15, bounds.committers.length);

        return {
          otherCount: 0,
          filteredCommits,
          commits,
          committers: bounds.committers,
          palette,
          firstCommitTimestamp,
          lastCommitTimestamp,
          firstSignificantTimestamp,
          lastSignificantTimestamp,
        };
      })
      .catch(function (e) {
        console.error(e.stack);
        throw e;
      });
    // console.log(changesData);
    return changesData;
  },
  requestChangesData,
  receiveChangesData,
  receiveChangesDataError,
);

function getPalette(commits: Commit[], maxNumberOfColors: number, numOfCommitters: number) {
  function chartColors(band: string, maxLength: number, length: number): string[] {
    const len = length > maxLength ? maxLength : length;
    return chroma.scale(band).mode('lch').colors(len);
  }

  const palette = chartColors('spectral', maxNumberOfColors, numOfCommitters);

  const totals: { [signature: string]: number } = {};
  _.each(commits, (commit) => {
    const changes = commit.stats.additions + commit.stats.deletions;
    if (totals[commit.signature]) {
      totals[commit.signature] += changes;
    } else {
      totals[commit.signature] = changes;
    }
  });

  const sortable: (string | number)[][] = [];
  _.each(Object.keys(totals), (key) => {
    sortable.push([key, totals[key]]);
  });

  sortable.sort((a: (string | number)[], b: (string | number)[]) => {
    return Number(b[1]) - Number(a[1]);
  });

  const returnPalette: Palette = {};

  for (let i = 0; i < Math.min(sortable.length, palette.length) - 1; i++) {
    returnPalette[sortable[i][0]] = palette[i];
  }
  if (sortable.length > maxNumberOfColors) {
    returnPalette['others'] = palette[maxNumberOfColors - 1];
  } else {
    returnPalette[sortable[sortable.length - 1][0]] = palette[palette.length - 1];
  }

  return returnPalette;
}
