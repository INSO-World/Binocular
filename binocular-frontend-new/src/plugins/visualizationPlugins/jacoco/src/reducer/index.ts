import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { DataPluginJacocoReport } from '../../../../interfaces/dataPluginInterfaces/dataPluginArtifacts.ts';

interface DateRange {
  from: string;
  to: string;
}

export interface JacocoState {
  jacocoReportData: DataPluginJacocoReport[];
  dateRange: DateRange;
  lastUpdated: number;
  isLoading: boolean;
  error?: string;
  selectedReport: string;
}

const initialState: JacocoState = {
  jacocoReportData: [
    {
      id: -1,
      created_at: '',
      xmlContent: '',
    },
    {
      id: -2,
      created_at: '',
      xmlContent: '',
    },
  ],
  dateRange: { from: '', to: '' },
  lastUpdated: -1,
  isLoading: false,
  error: undefined,
  selectedReport: 'last',
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
    fetchSunburstDataSuccess: (state, action: PayloadAction<DataPluginJacocoReport[]>) => {
      state.jacocoReportData = action.payload;
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
    setSelectedReport: (state, action: PayloadAction<string>) => {
      state.selectedReport = action.payload;
    },
  },
});

export const { fetchSunburstDataStart, fetchSunburstDataSuccess, fetchSunburstDataFailure, setDateRange, setSelectedReport } =
  jacocoSlice.actions;
export default jacocoSlice.reducer;
