import { put, takeEvery, fork, call, select } from 'redux-saga/effects';
import { State, DataState, getDataSlice } from '../reducer';
import { DataPlugin } from '../../../../interfaces/dataPlugin.ts';

export default function* <DataType>(dataConnection: DataPlugin, name?: string) {
  yield fork(() => watchRefresh<DataType>(dataConnection, name!));
  yield fork(() => watchDateRangeChange<DataType>(dataConnection, name!));
}

function* watchRefresh<DataType>(dataConnection: DataPlugin, name: string) {
  yield takeEvery('REFRESH', () => fetchChangesData<DataType>(dataConnection, name));
}

function* watchDateRangeChange<DataType>(dataConnection: DataPlugin, name: string) {
  yield takeEvery(getDataSlice(name).actions.setDateRange, () => fetchChangesData<DataType>(dataConnection, name));
}

function* fetchChangesData<DataType>(dataConnection: DataPlugin, name: string) {
  const { setData, setDataState } = getDataSlice(name).actions;
  yield put(setDataState(DataState.FETCHING));
  const state: State<DataType> = yield select();
  // how do we get our wanted type here?
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-expect-error
  const data: DataType[] = yield call(() => dataConnection[name].getAll(state.dateRange.from, state.dateRange.to));
  yield put(setData(data));
  yield put(setDataState(DataState.COMPLETE));
}
