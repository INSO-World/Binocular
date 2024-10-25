import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { ProgressType } from '../../types/general/progressType.ts';

export interface ProgressInitialState {
  progress: ProgressType | null;
}

const initialState: ProgressInitialState = {
  progress: {
    type: '',
    report: {
      commits: {
        processed: 0,
        total: 0,
      },
      issues: {
        processed: 0,
        total: 0,
      },
      builds: {
        processed: 0,
        total: 0,
      },
      files: {
        processed: 0,
        total: 0,
      },
      modules: {
        processed: 0,
        total: 0,
      },
      milestones: {
        processed: 0,
        total: 0,
      },
      mergeRequests: {
        processed: 0,
        total: 0,
      },
    },
  },
};

export const progressSlice = createSlice({
  name: 'progress',
  initialState,
  reducers: {
    setProgress: (state, action: PayloadAction<ProgressType>) => {
      state.progress = action.payload;
    },
  },
});

export const { setProgress } = progressSlice.actions;
export default progressSlice.reducer;
