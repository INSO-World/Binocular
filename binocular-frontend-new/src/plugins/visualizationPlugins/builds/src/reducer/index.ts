import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type {DataPluginBuild} from '../../../../interfaces/dataPluginInterfaces/dataPluginBuilds.ts';

export enum DataState {
  EMPTY,
  FETCHING,
  COMPLETE,
}

interface DateRange {
  from: string;
  to: string;
}

export interface BuildsState {
  builds: DataPluginBuild[];
  dateRange: DateRange;
  dataState: DataState;
}

const initialState: BuildsState = {
  builds: [],
  dateRange: { from: new Date().toISOString(), to: new Date().toISOString() },
  dataState: DataState.EMPTY,
};

export const buildsSlice = createSlice({
  name: 'builds',
  initialState,
  reducers: {
    setBuilds: (state, action: PayloadAction<DataPluginBuild[]>) => {
      state.builds = action.payload;
    },
    setDateRange: (state, action: PayloadAction<DateRange>) => {
      state.dateRange = action.payload;
    },
    setDataState: (state, action: PayloadAction<DataState>) => {
      state.dataState = action.payload;
    },
  },
});

export const { setBuilds, setDateRange, setDataState } = buildsSlice.actions;
export default buildsSlice.reducer;
