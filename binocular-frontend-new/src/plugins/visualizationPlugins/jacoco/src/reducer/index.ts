import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { DataPluginJacocoReport } from '../../../../interfaces/dataPluginInterfaces/dataPluginArtifacts.ts';

interface DateRange {
  from: string;
  to: string;
}

// Define an interface for the state structure
export interface JacocoState {
  sunburstData: DataPluginJacocoReport;
  dateRange: DateRange;
  lastUpdated: number;
  isLoading: boolean;
  error?: string;
}

// Define the initial state
const initialState: JacocoState = {
  sunburstData: { id: -1, xmlContent: '' },
  dateRange: { from: '', to: '' },
  lastUpdated: -1,
  isLoading: false,
  error: undefined,
};

// Create Redux slice
export const jacocoSlice = createSlice({
  name: 'jacoco',
  initialState,
  reducers: {
    fetchSunburstDataStart: (state) => {
      state.isLoading = true;
      state.error = undefined;
    },
    fetchSunburstDataSuccess: (state, action: PayloadAction<DataPluginJacocoReport>) => {
      state.sunburstData = action.payload;
      state.lastUpdated = Date.now();
      state.isLoading = false;
    },
    fetchSunburstDataFailure: (state, action: PayloadAction<string>) => {
      state.isLoading = false;
      state.error = action.payload;
    },
    setDateRange: (state, action: PayloadAction<DateRange>) => {
      state.dateRange = action.payload;
    },
  },
});

export const { fetchSunburstDataStart, fetchSunburstDataSuccess, fetchSunburstDataFailure, setDateRange } = jacocoSlice.actions;
export default jacocoSlice.reducer;
