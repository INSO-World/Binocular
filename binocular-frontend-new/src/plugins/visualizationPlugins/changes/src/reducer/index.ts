import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { DataCommit } from '../../../../interfaces/dataPlugin.ts';

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
  commits: DataCommit[];
  dateRange: DateRange;
  dataState: DataState;
}

const initialState: ChangesState = {
  commits: [],
  dateRange: { from: new Date().toISOString(), to: new Date().toISOString() },
  dataState: DataState.EMPTY,
};

export const changesSlice = createSlice({
  name: 'changes',
  initialState,
  reducers: {
    setCommits: (state, action: PayloadAction<DataCommit[]>) => {
      state.commits = action.payload;
    },
    setDateRange: (state, action: PayloadAction<DateRange>) => {
      state.dateRange = action.payload;
    },
    setDataState: (state, action: PayloadAction<DataState>) => {
      state.dataState = action.payload;
    },
  },
});

export const { setCommits, setDateRange, setDataState } = changesSlice.actions;
export default changesSlice.reducer;
