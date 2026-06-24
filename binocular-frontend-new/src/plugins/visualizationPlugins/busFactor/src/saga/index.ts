import { put, takeEvery, fork, call, select } from 'redux-saga/effects';
import { ApolloClient, HttpLink, InMemoryCache, gql } from '@apollo/client';
import type { DataPlugin } from '../../../../interfaces/dataPlugin.ts';

import {
  type BusFactorCIErrorState,
  type Point,
  DataState,
  setData,
  setDateRange,
  setRepoPath,
  setGranularity,
  setDataState,
} from '../reducer';

const client = new ApolloClient({
  link: new HttpLink({ uri: '/graphQl' }),
  cache: new InMemoryCache(),
  defaultOptions: { watchQuery: { fetchPolicy: 'no-cache' } },
});

function toBackendGranularity(g: string): string {
  switch (g) {
    case 'years':
      return 'YEAR';
    case 'months':
    default:
      return 'MONTH';
  }
}

export default function* (dataConnection: DataPlugin) {
  yield fork(() => watchRefresh(dataConnection));
  yield fork(() => watchReload(dataConnection));
}

function* watchRefresh(dataConnection: DataPlugin) {
  yield takeEvery('REFRESH', () => fetchData(dataConnection));
}

function* watchReload(dataConnection: DataPlugin) {
  yield takeEvery([setDateRange, setRepoPath, setGranularity], () => fetchData(dataConnection));
}

function* fetchData(dataConnection: DataPlugin) {
  if (dataConnection.name === 'Mock Data') {
    const mock: Point[] = [
      {
        id: '09/2024',
        busFactor: 4,
        ciErrorRate: 0,
        topAuthors: [
          { gitSignature: 'Maximilian Zenz <e1633058@student.tuwien.ac.at>', percentage: 0.256 },
          { gitSignature: 'Manuel Stöger <manuel.stoeger@inso.tuwien.ac.at>', percentage: 0.102 },
          { gitSignature: 'Roman Decker <roman.decker@gmail.com>', percentage: 0.076 },
        ],
      },
      {
        id: '10/2024',
        busFactor: 4,
        ciErrorRate: 0.17,
        topAuthors: [
          { gitSignature: 'Maximilian Zenz <e1633058@student.tuwien.ac.at>', percentage: 0.256 },
          { gitSignature: 'Manuel Stöger <manuel.stoeger@inso.tuwien.ac.at>', percentage: 0.102 },
          { gitSignature: 'Roman Decker <roman.decker@gmail.com>', percentage: 0.076 },
        ],
      },
      {
        id: '11/2024',
        busFactor: 4,
        ciErrorRate: 0.21,
        topAuthors: [
          { gitSignature: 'Maximilian Zenz <e1633058@student.tuwien.ac.at>', percentage: 0.256 },
          { gitSignature: 'Manuel Stöger <manuel.stoeger@inso.tuwien.ac.at>', percentage: 0.102 },
          { gitSignature: 'Roman Decker <roman.decker@gmail.com>', percentage: 0.076 },
        ],
      },
      {
        id: '12/2024',
        busFactor: 5,
        ciErrorRate: 0.38,
        topAuthors: [
          { gitSignature: 'Maximilian Zenz <e1633058@student.tuwien.ac.at>', percentage: 0.256 },
          { gitSignature: 'Manuel Stöger <manuel.stoeger@inso.tuwien.ac.at>', percentage: 0.102 },
          { gitSignature: 'Roman Decker <roman.decker@gmail.com>', percentage: 0.076 },
        ],
      },
      {
        id: '01/2025',
        busFactor: 5,
        ciErrorRate: 0.31,
        topAuthors: [
          { gitSignature: 'Maximilian Zenz <e1633058@student.tuwien.ac.at>', percentage: 0.256 },
          { gitSignature: 'Manuel Stöger <manuel.stoeger@inso.tuwien.ac.at>', percentage: 0.102 },
          { gitSignature: 'Roman Decker <roman.decker@gmail.com>', percentage: 0.076 },
        ],
      },
      {
        id: '02/2025',
        busFactor: 5,
        ciErrorRate: 0.28,
        topAuthors: [
          { gitSignature: 'Maximilian Zenz <e1633058@student.tuwien.ac.at>', percentage: 0.256 },
          { gitSignature: 'Manuel Stöger <manuel.stoeger@inso.tuwien.ac.at>', percentage: 0.102 },
          { gitSignature: 'Roman Decker <roman.decker@gmail.com>', percentage: 0.076 },
        ],
      },
      {
        id: '03/2025',
        busFactor: 6,
        ciErrorRate: 0.25,
        topAuthors: [
          { gitSignature: 'Maximilian Zenz <e1633058@student.tuwien.ac.at>', percentage: 0.256 },
          { gitSignature: 'Manuel Stöger <manuel.stoeger@inso.tuwien.ac.at>', percentage: 0.102 },
          { gitSignature: 'Roman Decker <roman.decker@gmail.com>', percentage: 0.076 },
        ],
      },
    ];
    yield put(setData(mock));
    yield put(setDataState(DataState.COMPLETE));
    return;
  }

  const state: { plugin: BusFactorCIErrorState } = yield select();
  const { repoPath, granularity, dateRange } = state.plugin;

  if (!repoPath) {
    yield put(setData([]));
    yield put(setDataState(DataState.COMPLETE));
    return;
  }

  yield put(setDataState(DataState.FETCHING));

  const resp: { data: { busFactorCIErrorRate: Point[] } } = yield call(() =>
    client.query({
      query: gql`
        query ($repoPath: String!, $since: Timestamp!, $until: Timestamp!, $granularity: Granularity!) {
          busFactorCIErrorRate(repoPath: $repoPath, since: $since, until: $until, granularity: $granularity) {
            id
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
        since: new Date(dateRange.from).getTime(),
        until: new Date(dateRange.to).getTime(),
        granularity: toBackendGranularity(granularity),
      },
    }),
  );

  const data: Point[] = resp.data.busFactorCIErrorRate;
  yield put(setData(data));
  yield put(setDataState(DataState.COMPLETE));
}
