import { createSlice } from '@reduxjs/toolkit';
import Config from '../../../config';
import type { DashboardLayout } from '../../../types/general/dashboardLayoutType.ts';

export interface LayoutsInitialState {
  customLayouts: DashboardLayout[];
  customLayoutCount: number;
}

const initialState: LayoutsInitialState = {
  customLayouts: [],
  customLayoutCount: 0,
};

export const layoutSlice = createSlice({
  name: 'layout',
  initialState: () => {
    const storedState = localStorage.getItem(`${layoutSlice.name}StateV${Config.localStorageVersion}`);
    if (storedState === null) {
      localStorage.setItem(`${layoutSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(initialState));
      return initialState;
    } else {
      return JSON.parse(storedState);
    }
  },

  reducers: {
    addCustomLayout(state, action: { payload: DashboardLayout }) {
      action.payload.id = state.customLayoutCount;
      state.customLayouts = [...state.customLayouts, action.payload];
      state.customLayoutCount++;
      localStorage.setItem(`${layoutSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    saveChanges(state, action: { payload: DashboardLayout }) {
      state.customLayouts = state.customLayouts.map((layout: DashboardLayout) => {
        if (layout.id === action.payload.id) {
          return action.payload;
        }
        return layout;
      });
      localStorage.setItem(`${layoutSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    deleteCustomLayout(state, action: { payload: number }) {
      state.customLayouts = state.customLayouts.filter((layout: DashboardLayout) => layout.id !== action.payload);
      localStorage.setItem(`${layoutSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
  },
});

export const { addCustomLayout, saveChanges, deleteCustomLayout } = layoutSlice.actions;
export default layoutSlice.reducer;
