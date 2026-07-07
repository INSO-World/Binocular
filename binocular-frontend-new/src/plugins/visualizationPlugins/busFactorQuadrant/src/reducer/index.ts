import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

// Loading state of the widget, used to switch between spinner / chart / "no data"
export enum DataState {
  EMPTY,
  FETCHING,
  COMPLETE,
}

// Selected time window (ISO date strings)
interface DateRange {
  from: string;
  to: string;
}

// A single author and how much of the module they contributed (0..1)
export interface TopAuthor {
  gitSignature: string;
  percentage: number;
}

// One data point = one module with its metrics and its top authors
export interface ModulePoint {
  module: string;
  busFactor: number;
  ciErrorRate: number;
  topAuthors: TopAuthor[];
}

// Everything this widget keeps in its own redux store
export interface QuadrantState {
  data: ModulePoint[];
  repoPath: string;
  excludedAuthors: string[]; // authors whose commits should not count
  neededModules: string[]; // which modules to show (empty = all)
  dateRange: DateRange;
  dataState: DataState;
}

// Default values before anything is loaded
const initialState: QuadrantState = {
  data: [],
  repoPath: '',
  excludedAuthors: [],
  neededModules: [],
  dateRange: { from: new Date().toISOString(), to: new Date().toISOString() },
  dataState: DataState.EMPTY,
};

// Redux slice: holds the state and the actions that change it.
// The saga reads these values and reacts to some of the actions to re-fetch data.
export const reducerSlice = createSlice({
  name: 'reducer',
  initialState,
  reducers: {
    // Store the modules returned by the backend
    setData: (state, action: PayloadAction<ModulePoint[]>) => {
      state.data = action.payload;
    },
    // Repository to query (set from the settings)
    setRepoPath: (state, action: PayloadAction<string>) => {
      state.repoPath = action.payload;
    },
    // Time window (set from the global/local dashboard parameters)
    setDateRange: (state, action: PayloadAction<DateRange>) => {
      state.dateRange = action.payload;
    },
    // Switch the loading state (EMPTY / FETCHING / COMPLETE)
    setDataState: (state, action: PayloadAction<DataState>) => {
      state.dataState = action.payload;
    },
    // Authors to exclude from the calculation (set from the settings checkboxes)
    setExcludedAuthors: (state, action: PayloadAction<string[]>) => {
      state.excludedAuthors = action.payload;
    },
    // Modules to keep (set from the settings checkboxes)
    setNeededModules: (state, action: PayloadAction<string[]>) => {
      state.neededModules = action.payload;
    },
  },
});

// Export the auto-generated action creators and the reducer itself
export const { setData, setRepoPath, setExcludedAuthors, setNeededModules, setDateRange, setDataState } = reducerSlice.actions;
export default reducerSlice.reducer;
