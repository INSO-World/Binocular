import { put, takeEvery, fork, call, select } from 'redux-saga/effects';
import { ApolloClient, HttpLink, InMemoryCache, gql } from '@apollo/client';
import type { DataPlugin } from '../../../../interfaces/dataPlugin.ts';
import {
  type HotspotState,
  type ModuleHotspot,
  DataState,
  setData,
  setDateRange,
  setRepoPath,
  setDataState,
  setNeededModules,
} from '../reducer';

const client = new ApolloClient({
  link: new HttpLink({ uri: '/graphQl' }),
  cache: new InMemoryCache(),
  defaultOptions: { watchQuery: { fetchPolicy: 'no-cache' } },
});

export default function* (dataConnection: DataPlugin) {
  yield fork(() => watchRefresh(dataConnection));
  yield fork(() => watchReload(dataConnection));
}
function* watchRefresh(dataConnection: DataPlugin) {
  yield takeEvery('REFRESH', () => fetchData(dataConnection));
}
function* watchReload(dataConnection: DataPlugin) {
  yield takeEvery([setDateRange, setRepoPath, setNeededModules], () => fetchData(dataConnection));
}

function* fetchData(dataConnection: DataPlugin) {
  yield put(setDataState(DataState.FETCHING));

  // Mock branch so the widget can be tested without a backend
  if (dataConnection.name === 'Mock Data') {
    const mock: ModuleHotspot[] = [
      { module: './ui', loc: 43, changeFrequency: 184 },
      { module: './ui/src', loc: 296, changeFrequency: 177 },
      { module: './foxx/types', loc: 6229, changeFrequency: 22 },
      { module: './docs', loc: 5387, changeFrequency: 5 },
      { module: './lib/endpoints', loc: 737, changeFrequency: 11 },
      { module: './ui/components', loc: 165, changeFrequency: 0 },
    ];
    yield put(setData(mock));
    yield put(setDataState(DataState.COMPLETE));
    return;
  }

  const state: { plugin: HotspotState } = yield select();
  const { repoPath, dateRange, neededModules } = state.plugin;
  if (!repoPath) {
    yield put(setData([]));
    yield put(setDataState(DataState.COMPLETE));
    return;
  }

  const resp: { data: { moduleSizeChangeFrequency: ModuleHotspot[] } } = yield call(() =>
    client.query({
      query: gql`
        query ($repoPath: String!, $since: Timestamp!, $until: Timestamp!, $neededModules: [String!]!) {
          moduleSizeChangeFrequency(repoPath: $repoPath, since: $since, until: $until, neededModules: $neededModules) {
            module
            loc
            changeFrequency
          }
        }
      `,
      variables: {
        repoPath,
        since: new Date(dateRange.from).getTime(),
        until: new Date(dateRange.to).getTime(),
        neededModules,
      },
    }),
  );

  yield put(setData(resp.data.moduleSizeChangeFrequency));
  yield put(setDataState(DataState.COMPLETE));
}
