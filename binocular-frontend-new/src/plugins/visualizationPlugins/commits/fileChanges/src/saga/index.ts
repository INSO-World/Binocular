import { put, takeLatest, fork, call, throttle, select } from 'redux-saga/effects';
import {
  DataState,
  setCurrentFileTotalCommits,
  setDataState,
  setDateOfOverallFirstCommit,
  setDateOfOverallLastCommit,
  setCurrentFile,
  type ChangesState,
} from '../reducer';
import type { DataPlugin } from '../../../../../interfaces/dataPlugin.ts';
import type { DataPluginCommit } from '../../../../../interfaces/dataPluginInterfaces/dataPluginCommits.ts';

export default function* (dataConnection: DataPlugin) {
  yield fork(() => watchRefresh(dataConnection));
  yield fork(() => watchFileChange(dataConnection));
}

function* watchFileChange(dataConnection: DataPlugin) {
  yield takeLatest(setCurrentFile, () => fetchChangesData(dataConnection));
}

function* watchRefresh(dataConnection: DataPlugin) {
  yield throttle(5000, 'REFRESH', () => fetchChangesData(dataConnection));
}

function* fetchChangesData(dataConnection: DataPlugin) {
  yield put(setDataState(DataState.FETCHING));

  const state: { plugin: ChangesState } = yield select();
  const current_file_total_commits: DataPluginCommit[] = yield call(() => dataConnection.commits.getByFile(state.plugin.current_file));

  const dateOfOverallFirstCommit: string = yield call(() => dataConnection.commits.getDateOfFirstCommit());
  const dateOfOverallLastCommit: string = yield call(() => dataConnection.commits.getDateOfLastCommit());

  yield put(setDateOfOverallFirstCommit(dateOfOverallFirstCommit));
  yield put(setDateOfOverallLastCommit(dateOfOverallLastCommit));
  yield put(setCurrentFileTotalCommits(current_file_total_commits));
  yield put(setDataState(DataState.COMPLETE));
}
