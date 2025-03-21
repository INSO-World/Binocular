import { put, takeEvery, fork, call, select } from 'redux-saga/effects';
import { State, DataState, getDataSlice } from '../reducer';
import { DataPlugin } from '../../../../interfaces/dataPlugin.ts';

export default function* <DataType>(dataConnection: DataPlugin, name?: string, dataConnectionName?: string) {
  yield fork(() => watchRefresh<DataType>(dataConnection, name!, dataConnectionName!));
  yield fork(() => watchDateRangeChange<DataType>(dataConnection, name!, dataConnectionName!));
}

function* watchRefresh<DataType>(dataConnection: DataPlugin, name: string, dataConnectionName: string) {
  yield takeEvery('REFRESH', () => fetchChangesData<DataType>(dataConnection, name, dataConnectionName));
}

function* watchDateRangeChange<DataType>(dataConnection: DataPlugin, name: string, dataConnectionName: string) {
  yield takeEvery(getDataSlice(name).actions.setDateRange, () => fetchChangesData<DataType>(dataConnection, name, dataConnectionName));
}

function* fetchChangesData<DataType>(dataConnection: DataPlugin, name: string, dataConnectionName: string) {
  const { setData, setDataState } = getDataSlice(name).actions;
  yield put(setDataState(DataState.FETCHING));
  const state: State<DataType> = yield select();
  const data: DataType[] = yield call(() => {
    const currentDataConnection = dataConnection[dataConnectionName as keyof DataPlugin];
    if (typeof currentDataConnection !== 'boolean' && typeof currentDataConnection !== 'string' && 'getAll' in currentDataConnection) {
      return currentDataConnection.getAll(state.dateRange.from, state.dateRange.to);
    }
  });
  yield put(setData(data));
  yield put(setDataState(DataState.COMPLETE));
}
