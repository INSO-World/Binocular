import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { NotificationType } from '../../../types/general/alertType.ts';
import { cloneDeep } from 'lodash';

export interface NotificationsInitialState {
  notificationList: NotificationType[];
  currID: number;
}

const initialState: NotificationsInitialState = {
  notificationList: [],
  currID: 0,
};

export const notificationsSlice = createSlice({
  name: 'notifications',
  initialState,
  reducers: {
    addNotification: (state, action: PayloadAction<NotificationType>) => {
      const newNotification = cloneDeep(action.payload);
      newNotification.id = state.currID;
      state.notificationList.push(newNotification);
      state.currID++;
    },
    removeNotification: (state, action: PayloadAction<number>) => {
      state.notificationList = state.notificationList.filter((notification) => notification.id !== action.payload);
    },
  },
});

export const { addNotification, removeNotification } = notificationsSlice.actions;
export default notificationsSlice.reducer;
