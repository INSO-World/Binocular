'use strict';

import { createAction } from 'redux-actions';
import { fetchFactory, timestampedActionFactory } from '../../../sagas/utils';
import Database from '../../../database/database';

export const setActiveVisualizations = createAction('SET_ACTIVE_VISUALIZATIONS');
export const setMergeRequests = createAction('SET_SHOW_MERGE_REQUESTS');
export const setGroup = createAction('CRM_SET_GROUP');
export const refresh = createAction('REFRESH');
export const requestCodeReviewMetricsData = createAction('REQUEST_CODE_REVIEW_METRICS_DATA');
export const receiveCodeReviewMetricsData = timestampedActionFactory('RECEIVE_CODE_REVIEW_METRICS_DATA');
export const receiveCodeReviewMetricsDataError = createAction('RECEIVE_CODE_REVIEW_METRICS_DATA_ERROR');

export default function* () {
  yield* fetchCodeReviewMetricsData();
}

export const fetchCodeReviewMetricsData = fetchFactory(
  function* () {
    const { firstMergeRequest, lastMergeRequest, firstComment, lastComment } = yield Database.getBounds();

    const firstMergeRequestTimestamp = Date.parse(firstMergeRequest.date);
    const lastMergeRequestTimestamp = Date.parse(lastMergeRequest.date);
    const firstCommentTimestamp = Date.parse(firstComment.date);
    const lastCommentTimestamp = Date.parse(lastComment.date);

    return yield Promise.all([
      new Promise((resolve) => {
        Database.getMergeRequestData(
          [firstMergeRequestTimestamp, lastMergeRequestTimestamp],
          [firstMergeRequestTimestamp, lastMergeRequestTimestamp],
        ).then(resolve);
      }),
      new Promise((resolve) => {
        Database.getCommentData([firstCommentTimestamp, lastCommentTimestamp], [firstCommentTimestamp, lastCommentTimestamp]).then(resolve);
      }),
      new Promise((resolve) => {
        Database.getReviewThreadData().then(resolve);
      }),
    ]).then((values) => {
      const mergeRequests = values[0];
      const comments = values[1];
      const reviewThreads = values[2];
      console.log(reviewThreads);
      return {
        mergeRequests,
        comments,
        reviewThreads,
      };
    });
  },
  requestCodeReviewMetricsData,
  receiveCodeReviewMetricsData,
  receiveCodeReviewMetricsDataError,
);
