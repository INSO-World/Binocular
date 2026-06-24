import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

export enum DataState {
  EMPTY,
  FETCHING,
  COMPLETE,
}

interface DateRange {
  from: string;
  to: string;
}

export interface TopAuthor {
  gitSignature: string;
  percentage: number;
}

export interface Point {
  id: string;
  busFactor: number;
  ciErrorRate: number;
  topAuthors: TopAuthor[];
}

export interface BusFactorCIErrorState {
  data: Point[];
  repoPath: string;
  granularity: string;
  dateRange: DateRange;
  dataState: DataState;
}

const initialState: BusFactorCIErrorState = {
  data: [],
  repoPath: '',
  granularity: 'months',
  dateRange: { from: new Date().toISOString(), to: new Date().toISOString() },
  dataState: DataState.EMPTY,
};

export const reducerSlice = createSlice({
  name: 'reducer',
  initialState,
  reducers: {
    setData: (state, action: PayloadAction<Point[]>) => {
      state.data = action.payload;
    },
    setRepoPath: (state, action: PayloadAction<string>) => {
      state.repoPath = action.payload;
    },
    setGranularity: (state, action: PayloadAction<string>) => {
      state.granularity = action.payload;
    },
    setDateRange: (state, action: PayloadAction<DateRange>) => {
      state.dateRange = action.payload;
    },
    setDataState: (state, action: PayloadAction<DataState>) => {
      state.dataState = action.payload;
    },
  },
});

export const { setData, setRepoPath, setGranularity, setDateRange, setDataState } = reducerSlice.actions;
export default reducerSlice.reducer;
