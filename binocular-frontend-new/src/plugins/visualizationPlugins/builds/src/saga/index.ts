import { put, takeEvery, fork, call, select } from 'redux-saga/effects';
import { BuildsState, DataState, setBuilds, setDataState, setDateRange } from '../reducer';
import { DataPlugin } from '../../../../interfaces/dataPlugin.ts';
import { DataPluginBuild } from '../../../../interfaces/dataPluginInterfaces/dataPluginBuilds.ts';

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
  const state: BuildsState = yield select();
  const builds: DataPluginBuild[] = yield call(() => dataConnection.builds.getAll(state.dateRange.from, state.dateRange.to));
  yield put(setBuilds(builds));
  yield put(setDataState(DataState.COMPLETE));
}
