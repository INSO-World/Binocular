import { put, takeEvery, fork, call, select } from 'redux-saga/effects';
import {type BuildsState, DataState, setBuilds, setDataState, setDateRange } from '../reducer';
import type {DataPlugin} from '../../../../interfaces/dataPlugin.ts';
import type {DataPluginBuild} from '../../../../interfaces/dataPluginInterfaces/dataPluginBuilds.ts';

export default function* (dataConnection: DataPlugin) {
  yield fork(() => watchRefresh(dataConnection));
  yield fork(() => watchDateRangeChange(dataConnection));
}

function* watchRefresh(dataConnection: DataPlugin) {
  yield takeEvery('REFRESH', () => fetchChangesData(dataConnection));
}

function* watchDateRangeChange(dataConnection: DataPlugin) {
  yield takeEvery(setDateRange, () => fetchChangesData(dataConnection));
}

function* fetchChangesData(dataConnection: DataPlugin) {
  yield put(setDataState(DataState.FETCHING));
  const state: { plugin: BuildsState } = yield select();
  const builds: DataPluginBuild[] = yield call(() => dataConnection.builds.getAll(state.plugin.dateRange.from, state.plugin.dateRange.to));
  yield put(setBuilds(builds));
  yield put(setDataState(DataState.COMPLETE));
}
