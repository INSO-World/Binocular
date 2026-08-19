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

// One data point = one module with its size and how often it changed
export interface ModuleHotspot {
  module: string;
  loc: number;
  changeFrequency: number;
}

export interface HotspotState {
  data: ModuleHotspot[];
  repoPath: string;
  neededModules: string[];
  dateRange: DateRange;
  dataState: DataState;
}

const initialState: HotspotState = {
  data: [],
  repoPath: '',
  neededModules: [],
  dateRange: { from: new Date().toISOString(), to: new Date().toISOString() },
  dataState: DataState.EMPTY,
};

export const reducerSlice = createSlice({
  name: 'reducer',
  initialState,
  reducers: {
    setData: (state, action: PayloadAction<ModuleHotspot[]>) => {
      state.data = action.payload;
    },
    setRepoPath: (state, action: PayloadAction<string>) => {
      state.repoPath = action.payload;
    },
    setDateRange: (state, action: PayloadAction<DateRange>) => {
      state.dateRange = action.payload;
    },
    setDataState: (state, action: PayloadAction<DataState>) => {
      state.dataState = action.payload;
    },
    setNeededModules: (state, action: PayloadAction<string[]>) => {
      state.neededModules = action.payload;
    },
  },
});

export const { setData, setRepoPath, setNeededModules, setDateRange, setDataState } = reducerSlice.actions;
export default reducerSlice.reducer;
