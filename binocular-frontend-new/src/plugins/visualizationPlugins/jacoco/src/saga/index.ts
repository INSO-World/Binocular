import { put, takeEvery, fork, call, select } from 'redux-saga/effects';
import { fetchSunburstDataStart, fetchSunburstDataSuccess, fetchSunburstDataFailure, JacocoState } from '../reducer';
import { DataPlugin } from '../../../../interfaces/dataPlugin.ts';
import { DataPluginJacocoReport } from '../../../../interfaces/dataPluginInterfaces/dataPluginArtifacts.ts';

// Root saga to initialize watchers
export default function* (dataConnection: DataPlugin) {
  yield fork(() => watchRefresh(dataConnection));
}

// Watcher saga to listen for refresh action
function* watchRefresh(dataConnection: DataPlugin) {
  yield takeEvery('REFRESH', () => fetchSunburstChartData(dataConnection));
}

// Define the data fetching saga
function* fetchSunburstChartData(dataConnection: DataPlugin) {
  yield put(fetchSunburstDataStart());

  try {
    // Get the current parameters (e.g., date range) from state if needed
    const state: JacocoState = yield select();

    // Replace with actual data fetching logic from the data plugin
    const data: DataPluginJacocoReport[] = yield call(() => dataConnection.jacocoReports.getAll(state.dateRange.from, state.dateRange.to));
    yield put(fetchSunburstDataSuccess(data[0]));
  } catch (error) {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    yield put(fetchSunburstDataFailure(error.toString()));
  }
}
