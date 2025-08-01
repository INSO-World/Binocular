import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type {ProgressType} from '../../../types/general/progressType.ts';
import { SocketConnectionStatusType, type SocketConnectionType } from '../../../types/general/socketConnectionType.ts';

export interface ProgressInitialState {
  socketConnection: SocketConnectionType;
  progress: ProgressType | null;
}

const initialState: ProgressInitialState = {
  socketConnection: {
    status: SocketConnectionStatusType.Idle,
  },
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
    setConnectionStatus: (state, action: PayloadAction<SocketConnectionType>) => {
      state.socketConnection = action.payload;
    },
  },
});

export const { setProgress, setConnectionStatus } = progressSlice.actions;
export default progressSlice.reducer;
