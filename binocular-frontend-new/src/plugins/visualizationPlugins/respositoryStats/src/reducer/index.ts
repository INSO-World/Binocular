import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export enum DataState {
  EMPTY,
  FETCHING,
  COMPLETE,
}

interface DateRange {
  from: string;
  to: string;
}

export interface ChangesState {
  commitNumber: number;
  userNumber: number;
  issueNumber: number;
  buildNumber: number;
  dateRange: DateRange;
  dataState: DataState;
}

const initialState: ChangesState = {
  commitNumber: 0,
  userNumber: 0,
  issueNumber: 0,
  buildNumber: 0,
  dateRange: { from: new Date().toISOString(), to: new Date().toISOString() },
  dataState: DataState.EMPTY,
};

export const reducerSlice = createSlice({
  name: 'reducer',
  initialState,
  reducers: {
    setCommits: (state, action: PayloadAction<number>) => {
      state.commitNumber = action.payload;
    },
    setUsers: (state, action: PayloadAction<number>) => {
      state.userNumber = action.payload;
    },
    setIssues: (state, action: PayloadAction<number>) => {
      state.issueNumber = action.payload;
    },
    setBuilds: (state, action: PayloadAction<number>) => {
      state.buildNumber = action.payload;
    },
    setDateRange: (state, action: PayloadAction<DateRange>) => {
      state.dateRange = action.payload;
    },
    setDataState: (state, action: PayloadAction<DataState>) => {
      state.dataState = action.payload;
    },
  },
});

export const { setCommits, setIssues, setDateRange, setDataState, setUsers, setBuilds } = reducerSlice.actions;
export default reducerSlice.reducer;
