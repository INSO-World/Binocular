import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { ProgressType } from '../../types/general/progressType.ts';

export interface ProgressInitialState {
  progress: ProgressType | null;
}

const initialState: ProgressInitialState = {
  progress: null,
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
