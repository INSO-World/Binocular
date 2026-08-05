import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { AnyActivityDataPlugin } from '../utilities/types';

export enum DataState {
  EMPTY,
  FETCHING,
  COMPLETE,
}

interface DateRange {
  from: string;
  to: string;
}

export interface RepositoryActivityState {
  data: AnyActivityDataPlugin[];
  dateRange: DateRange;
  dataState: DataState;
  showActivityTimeline: boolean;
}

const initialState: RepositoryActivityState = {
  data: [],
  dateRange: { from: new Date().toISOString(), to: new Date().toISOString() },
  dataState: DataState.EMPTY,
  showActivityTimeline: true,
};

export const reducerSlice = createSlice({
  name: 'repositoryActivity',
  initialState,
  reducers: {
    setData: (state, action: PayloadAction<AnyActivityDataPlugin[]>) => {
      state.data = action.payload;
    },
    setDateRange: (state, action: PayloadAction<DateRange>) => {
      state.dateRange = action.payload;
    },
    setDataState: (state, action: PayloadAction<DataState>) => {
      state.dataState = action.payload;
    },
    setShowActivityTimeline: (state, action: PayloadAction<boolean>) => {
      state.showActivityTimeline = action.payload;
    },
  },
});

export const { setData, setDateRange, setDataState, setShowActivityTimeline } = reducerSlice.actions;
export default reducerSlice.reducer;
