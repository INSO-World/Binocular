import { put, takeEvery, fork, call, select } from 'redux-saga/effects';
import { State, DataState, setData, setDataState, setDateRange } from '../reducer';
import { DataPlugin } from '../../../../interfaces/dataPlugin.ts';

export default function* <DataType>(dataConnection: DataPlugin) {
  yield fork(() => watchRefresh<DataType>(dataConnection));
  yield fork(() => watchDateRangeChange<DataType>(dataConnection));
}

function* watchRefresh<DataType>(dataConnection: DataPlugin) {
  yield takeEvery('REFRESH', () => fetchChangesData<DataType>(dataConnection));
}

function* watchDateRangeChange<DataType>(dataConnection: DataPlugin) {
  yield takeEvery(setDateRange, () => fetchChangesData<DataType>(dataConnection));
}

function* fetchChangesData<DataType>(dataConnection: DataPlugin) {
  yield put(setDataState(DataState.FETCHING));
  const state: State<DataType> = yield select();
  // how do we get our wanted type here?
  const data: DataType[] = yield call(() => dataConnection['commits'].getAll(state.dateRange.from, state.dateRange.to));
  yield put(setData(data));
  yield put(setDataState(DataState.COMPLETE));
}
