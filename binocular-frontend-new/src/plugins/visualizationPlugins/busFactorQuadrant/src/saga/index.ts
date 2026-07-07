import { put, takeEvery, fork, call, select } from 'redux-saga/effects';
import { ApolloClient, HttpLink, InMemoryCache, gql } from '@apollo/client';
import type { DataPlugin } from '../../../../interfaces/dataPlugin.ts';
import {
  type QuadrantState,
  type ModulePoint,
  DataState,
  setData,
  setDateRange,
  setRepoPath,
  setDataState,
  setExcludedAuthors,
  setNeededModules,
} from '../reducer';

// Own Apollo client that talks to the backend GraphQL endpoint (proxied by the backend).
// no-cache so we always get fresh data when parameters change.
const client = new ApolloClient({
  link: new HttpLink({ uri: '/graphQl' }),
  cache: new InMemoryCache(),
  defaultOptions: { watchQuery: { fetchPolicy: 'no-cache' } },
});

// Root saga: start the two watchers in parallel.
// dataConnection is the currently selected data source (e.g. Binocular Backend or Mock Data).
export default function* (dataConnection: DataPlugin) {
  yield fork(() => watchRefresh(dataConnection));
  yield fork(() => watchReload(dataConnection));
}

// React to a manual/initial REFRESH (fired by the chart on mount / data source change)
function* watchRefresh(dataConnection: DataPlugin) {
  yield takeEvery('REFRESH', () => fetchData(dataConnection));
}
// Re-fetch whenever one of the query inputs changes (date range, repo, filters)
function* watchReload(dataConnection: DataPlugin) {
  yield takeEvery([setDateRange, setRepoPath, setExcludedAuthors, setNeededModules], () => fetchData(dataConnection));
}

// Loads the module data and writes it into the reducer
function* fetchData(dataConnection: DataPlugin) {
  yield put(setDataState(DataState.FETCHING));

  // When "Mock Data" is selected we don't hit the backend and just return hardcoded points.
  // This lets the widget be tested without a running backend / repoPath.
  if (dataConnection.name === 'Mock Data') {
    const mock: ModulePoint[] = [
      {
        module: '.',
        busFactor: 6,
        ciErrorRate: 0.28,
        topAuthors: [{ gitSignature: 'Maximilian Zenz <e1633058@student.tuwien.ac.at>', percentage: 0.24 }],
      },
      {
        module: './binocular-backend-new',
        busFactor: 0,
        ciErrorRate: 0.32,
        topAuthors: [{ gitSignature: 'Markus <markus.gumpoltsberger@gmail.com>', percentage: 0.23 }],
      },
    ];
    yield put(setData(mock));
    yield put(setDataState(DataState.COMPLETE));
    return;
  }

  // Read the current query inputs from the reducer
  const state: { plugin: QuadrantState } = yield select();
  const { repoPath, dateRange, excludedAuthors, neededModules } = state.plugin;
  // Without a repository we can't query anything -> stop early with empty data
  if (!repoPath) {
    yield put(setData([]));
    yield put(setDataState(DataState.COMPLETE));
    return;
  }

  const resp: { data: { busFactorCIErrorRateModules: ModulePoint[] } } = yield call(() =>
    client.query({
      query: gql`
        query ($repoPath: String!, $since: Timestamp!, $until: Timestamp!, $excludedAuthors: String!, $neededModules: String!) {
          busFactorCIErrorRateModules(
            repoPath: $repoPath
            since: $since
            until: $until
            excludedAuthors: $excludedAuthors
            neededModules: $neededModules
          ) {
            module
            busFactor
            ciErrorRate
            topAuthors {
              gitSignature
              percentage
            }
          }
        }
      `,
      variables: {
        repoPath,
        // backend expects a Timestamp (milliseconds), so convert the ISO date strings
        since: new Date(dateRange.from).getTime(),
        until: new Date(dateRange.to).getTime(),
        // backend takes comma-separated strings, so join the arrays ("" = nothing selected)
        excludedAuthors: excludedAuthors.join(','),
        neededModules: neededModules.join(','),
      },
    }),
  );

  // Store the result -> the chart re-renders from the reducer data
  yield put(setData(resp.data.busFactorCIErrorRateModules));
  yield put(setDataState(DataState.COMPLETE));
}
