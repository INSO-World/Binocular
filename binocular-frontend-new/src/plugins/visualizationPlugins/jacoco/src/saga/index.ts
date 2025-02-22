import { put, takeEvery, fork, call, select } from 'redux-saga/effects';
import { fetchSunburstDataStart, fetchSunburstDataSuccess, fetchSunburstDataFailure, JacocoState, setDateRange } from '../reducer';
import { DataPlugin } from '../../../../interfaces/dataPlugin.ts';
import { DataPluginJacocoReport } from '../../../../interfaces/dataPluginInterfaces/dataPluginArtifacts.ts';

// Root saga to initialize watchers
export default function* (dataConnection: DataPlugin) {
  yield fork(() => watchRefresh(dataConnection));
  yield fork(() => watchDateRangeChange(dataConnection));
}

// Watcher saga to listen for refresh action
function* watchRefresh(dataConnection: DataPlugin) {
  yield takeEvery('REFRESH', () => fetchSunburstChartData(dataConnection));
}

// Watcher saga to listen for date range change action
function* watchDateRangeChange(dataConnection: DataPlugin) {
  yield takeEvery(setDateRange, () => fetchSunburstChartData(dataConnection));
}

// The data fetching saga
function* fetchSunburstChartData(dataConnection: DataPlugin) {
  yield put(fetchSunburstDataStart());

  try {
    // Get the current parameters (e.g., date range) from state if needed
    const state: JacocoState = yield select();

    // Replace with actual data fetching logic from the data plugin
    const data: DataPluginJacocoReport[] = yield call(() =>
      dataConnection.jacocoReports.getAll(state.dateRange.from, state.dateRange.to, 'DESC'),
    );
    // Dispatch the data to the reducer
    if (data.length === 0) {
      yield put(fetchSunburstDataFailure('No data available from: ' + state.dateRange.from + ' to: ' + state.dateRange.to + '.'));
    } else if (data.length === 2) {
      yield put(fetchSunburstDataSuccess(data));
    } else {
      const tempData: DataPluginJacocoReport[] = [];
      tempData.push(data[0]);
      tempData.push(data[data.length - 1]);
      yield put(fetchSunburstDataSuccess(tempData));
    }
  } catch (error) {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    yield put(fetchSunburstDataFailure(error.toString()));
  }
}
