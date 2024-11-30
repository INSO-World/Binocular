import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { DataPluginBuild } from '../../../../interfaces/dataPluginInterfaces/dataPluginBuilds.ts';
import { DataPluginCommit } from '../../../../interfaces/dataPluginInterfaces/dataPluginCommits.ts';
import { dataName } from '../index.tsx';

export enum DataState {
  EMPTY,
  FETCHING,
  COMPLETE,
}

interface DateRange {
  from: string;
  to: string;
}

export interface State {
  builds: DataPluginBuild[];
  commits: DataPluginCommit[];
  dateRange: DateRange;
  dataState: DataState;
}

const initialState: State = {
  builds: [],
  commits: [],
  dateRange: { from: new Date().toISOString(), to: new Date().toISOString() },
  dataState: DataState.EMPTY,
};

export const dataSlice = createSlice({
  name: dataName,
  initialState,
  reducers: {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    setData: (state, action: PayloadAction<any[]>) => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      state[dataName] = action.payload;
    },
    setDateRange: (state, action: PayloadAction<DateRange>) => {
      state.dateRange = action.payload;
    },
    setDataState: (state, action: PayloadAction<DataState>) => {
      state.dataState = action.payload;
    },
  },
});

export const { setData, setDateRange, setDataState } = dataSlice.actions;
export default dataSlice.reducer;
