'use strict';

import { fetchFactory, mapSaga, timestampedActionFactory } from '../../../../sagas/utils.ts';
import { Bounds } from '../../../../types/boundsTypes.ts';
import Database from '../../../../database/database';
import { GlobalState } from '../../../../types/globalTypes.ts';
import { fork, select, takeEvery, throttle, call, put, take } from 'redux-saga/effects';
import { Commit } from '../../../../types/commitTypes.ts';
import { createAction } from 'redux-actions';

export const requestTagsData = createAction('REQUEST_TAGS_DATA', (payload: any) => payload);
export const receiveTagsData = timestampedActionFactory('RECEIVE_TAGS_DATA');
export const receiveTagsDataError = createAction('RECEIVE_TAGS_DATA_ERROR', (error: any) => error);
export const changeTagsPage = createAction('CHANGE_TAGS_PAGE');
export const receiveCommitTags = createAction('RECEIVE_COMMIT_TAGS');

export const requestRefresh = createAction('REQUEST_REFRESH');
const refresh = createAction('REFRESH');

export const requestTagCommits = createAction('REQUEST_TAG_COMMITS', (payload: { commitShas: string[]; provider?: string }) => payload);
export const receiveTagCommits = timestampedActionFactory('RECEIVE_TAG_COMMITS');
export const receiveTagCommitsError = createAction('RECEIVE_TAG_COMMITS_ERROR', (error: any) => error);

interface TagsData {
  otherCount: number;
  commits: Commit[];
  committers: string[];
  firstCommitTimestamp: number;
  lastCommitTimestamp: number;
  firstSignificantTimestamp: number;
  lastSignificantTimestamp: number;
}

export default function* () {
  yield* fetchTagsData();

  yield fork(watchRefreshRequests);
  yield fork(watchMessages);
  yield fork(watchToggleHelp);

  yield fork(watchTimeSpan);
  yield fork(watchSelectedAuthorsGlobal);
  yield fork(watchMergedAuthors);
  yield fork(watchOtherAuthors);
  yield fork(watchExcludeMergeCommits);
  yield fork(watchChangeTagsPage);
  yield fork(watchTagCommits);
  yield fork(watchReceiveCommitTags);
}

function* watchChangeTagsPage() {
  yield takeEvery('CHANGE_TAGS_PAGE', fetchTagsData);
}

function* watchReceiveCommitTags() {
  yield takeEvery('RECEIVE_COMMIT_TAGS', fetchTagsData);
}

function* watchTimeSpan() {
  yield takeEvery('SET_TIME_SPAN', fetchTagsData);
}

function* watchSelectedAuthorsGlobal() {
  yield takeEvery('SET_SELECTED_AUTHORS_GLOBAL', fetchTagsData);
}

function* watchOtherAuthors() {
  yield takeEvery('SET_OTHER_AUTHORS', fetchTagsData);
}

function* watchExcludeMergeCommits() {
  yield takeEvery('SET_EXCLUDE_MERGE_COMMITS', fetchTagsData);
}

function* watchMergedAuthors() {
  yield takeEvery('SET_MERGED_AUTHORS', fetchTagsData);
}

function* watchRefreshRequests() {
  yield throttle(5000, 'REQUEST_REFRESH', mapSaga(refresh));
}

function* watchMessages() {
  yield takeEvery('message', mapSaga(requestRefresh));
}

function* watchToggleHelp() {
  yield takeEvery('TOGGLE_HELP', mapSaga(refresh));
}

function* watchTagCommits() {
  yield takeEvery('REQUEST_TAG_COMMITS', handleTagCommitsSaga);
}

export const fetchTagsData = fetchFactory(
  function* (action: any) {
    console.log('REQUEST_TAGS_DATA');
    const { page, pageSize, tags, withoutTags } = (action && action.payload) || {};

    const bounds: Bounds = yield Database.getBounds();
    const firstCommitTimestamp = new Date(bounds.firstCommit.date).getTime();
    const lastCommitTimestamp = new Date(bounds.lastCommit.date).getTime();

    const state: GlobalState = yield select();
    const timeSpan = state.universalSettings.chartTimeSpan;
    const firstSignificantTimestamp = timeSpan.from ? new Date(timeSpan.from).getTime() : firstCommitTimestamp;
    const lastSignificantTimestamp = timeSpan.to ? new Date(timeSpan.to).getTime() : lastCommitTimestamp;

    const result = yield Database.getCommitDataWithCategoriesAndWebUrl(
      [firstCommitTimestamp, lastCommitTimestamp],
      [firstSignificantTimestamp, lastSignificantTimestamp],
      page,
      pageSize,
      tags,
      withoutTags,
    )
      .then((res) => {
        const sortedData = res.data.sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
        return { data: sortedData, totalCount: res.totalCount };
      })
      .catch((e) => {
        console.error(e.stack);
        throw e;
      });

    return {
      commits: result.data,
      totalCount: result.totalCount,
      committers: bounds.committers,
      firstCommitTimestamp,
      lastCommitTimestamp,
      firstSignificantTimestamp,
      lastSignificantTimestamp,
    };
  },
  requestTagsData,
  receiveTagsData,
  receiveTagsDataError,
);

function* handleTagCommitsSaga(action: ReturnType<typeof requestTagCommits>) {
  try {
    const { commitShas, provider } = action.payload;

    let url = 'http://localhost:48763/api/tag-commits';
    if (provider) {
      url += `?provider=${encodeURIComponent(provider)}`;
    }

    const response: Response = yield call(fetch, url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ commitShas }),
    });
    const data = yield call([response, response.json]);

    if (!response.ok) {
      throw new Error(data.error || 'Request error');
    }

    yield put(receiveTagCommits(data));

    yield put(receiveCommitTags({}));
  } catch (error: any) {
    console.error('Error tagging commits:', error);
    yield put(receiveTagCommitsError(error.message));
  }
}
