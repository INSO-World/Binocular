import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import Config from '../../../config.ts';
import type {DashboardItemType} from '../../../types/general/dashboardItemType.ts';

export interface DashboardInitialState {
  dashboardItems: DashboardItemType[];
  placeableItem?: DashboardItemType;
  dashboardItemCount: number;
  popupCount: number;
  dashboardState: number[][];
  initialized: boolean;
}

enum DashboardStateUpdateType {
  place,
  move,
  delete,
}

const initialState: DashboardInitialState = {
  dashboardItems: [],
  placeableItem: undefined,
  dashboardItemCount: 0,
  popupCount: 0,
  dashboardState: Array.from(Array(40), () => new Array(40).fill(0)),
  initialized: false,
};

export const dashboardSlice = createSlice({
  name: 'dashboard',
  initialState: () => {
    const storedState = localStorage.getItem(`${dashboardSlice.name}StateV${Config.localStorageVersion}`);
    if (storedState === null) {
      localStorage.setItem(`${dashboardSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(initialState));
      return initialState;
    } else {
      return JSON.parse(storedState);
    }
  },
  reducers: {
    addDashboardItem: (state, action: PayloadAction<DashboardItemType>) => {
      state.placeableItem = undefined;
      const nextFreePosition = findNextFreePosition(state.dashboardState, action.payload);
      if (nextFreePosition !== null) {
        state.dashboardItemCount++;
        action.payload.id = state.dashboardItemCount;
        if (action.payload.x === undefined) {
          action.payload.x = nextFreePosition.x;
        }
        if (action.payload.y === undefined) {
          action.payload.y = nextFreePosition.y;
        }
        state.dashboardItems = [...state.dashboardItems, action.payload];
        updateDashboardState(state.dashboardState, action.payload, DashboardStateUpdateType.place);
        localStorage.setItem(`${dashboardSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
      }
      state.initialized = true;
    },
    moveDashboardItem: (state, action: PayloadAction<DashboardItemType>) => {
      state.placeableItem = undefined;
      if (checkIfSpaceIsFree(state.dashboardState, action.payload)) {
        state.dashboardItems = state.dashboardItems.map((item: DashboardItemType) => {
          if (item.id === action.payload.id) {
            item.x = action.payload.x;
            item.y = action.payload.y;
            item.width = action.payload.width;
            item.height = action.payload.height;
          }
          return item;
        });
        updateDashboardState(state.dashboardState, action.payload, DashboardStateUpdateType.move);
      }
      localStorage.setItem(`${dashboardSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    placeDashboardItem: (state, action: PayloadAction<DashboardItemType | undefined>) => {
      state.placeableItem = action.payload;
      localStorage.setItem(`${dashboardSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
      state.initialized = true;
    },
    deleteDashboardItem: (state, action: PayloadAction<DashboardItemType>) => {
      state.dashboardItems = state.dashboardItems.filter((item: DashboardItemType) => item.id !== action.payload.id);
      updateDashboardState(state.dashboardState, action.payload, DashboardStateUpdateType.delete);
      localStorage.setItem(`${dashboardSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    updateDashboardItem: (state, action: PayloadAction<DashboardItemType>) => {
      state.dashboardItems = state.dashboardItems.map((item: DashboardItemType) => {
        if (item.id === action.payload.id) {
          return action.payload;
        }
        return item;
      });
      localStorage.setItem(`${dashboardSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    increasePopupCount: (state) => {
      state.popupCount++;
      localStorage.setItem(`${dashboardSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    clearDashboardStorage: () => {
      localStorage.removeItem(`${dashboardSlice.name}StateV${Config.localStorageVersion}`);
    },
    importDashboardStorage: (state, action: PayloadAction<DashboardInitialState>) => {
      state = action.payload;
      localStorage.setItem(`${dashboardSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    setDashboardState: (state, action: PayloadAction<DashboardItemType[]>) => {
      const dashboardItems = action.payload.map((item, id) => {
        item.id = id + 1;
        state.dashboardItemCount = item.id;
        return item;
      });
      state.dashboardState = Array.from(Array(40), () => new Array(40).fill(0));
      dashboardItems.forEach((item: DashboardItemType) => {
        if (item.x !== undefined && item.y !== undefined) {
          for (let x = item.x; x < item.x + item.width; x++) {
            for (let y = item.y; y < item.y + item.height; y++) {
              state.dashboardState[y][x] = item.id;
            }
          }
        }
      });
      state.dashboardItems = dashboardItems;
      state.initialized = true;
      localStorage.setItem(`${dashboardSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
  },
});

export const {
  addDashboardItem,
  moveDashboardItem,
  placeDashboardItem,
  deleteDashboardItem,
  updateDashboardItem,
  increasePopupCount,
  clearDashboardStorage,
  importDashboardStorage,
  setDashboardState,
} = dashboardSlice.actions;
export default dashboardSlice.reducer;

function checkIfSpaceIsFree(dashboardState: number[][], item: DashboardItemType) {
  if (item.x !== undefined && item.y !== undefined) {
    for (let y = item.y; y < item.y + item.height; y++) {
      for (let x = item.x; x < item.x + item.width; x++) {
        if (dashboardState[y][x] !== 0 && dashboardState[y][x] !== item.id) {
          return false;
        }
      }
    }
  }
  return true;
}

function updateDashboardState(dashboardState: number[][], item: DashboardItemType, dashboardStateUpdateType: DashboardStateUpdateType) {
  if (item.x !== undefined && item.y !== undefined) {
    for (let y = 0; y < dashboardState.length; y++) {
      for (let x = 0; x < dashboardState[y].length; x++) {
        if (
          (dashboardStateUpdateType === DashboardStateUpdateType.move || dashboardStateUpdateType === DashboardStateUpdateType.delete) &&
          dashboardState[y][x] === item.id
        ) {
          dashboardState[y][x] = 0;
        }
        if (
          (dashboardStateUpdateType === DashboardStateUpdateType.move || dashboardStateUpdateType === DashboardStateUpdateType.place) &&
          x >= item.x &&
          x < item.x + item.width &&
          y >= item.y &&
          y < item.y + item.height
        ) {
          dashboardState[y][x] = item.id;
        }
      }
    }
  }
  return dashboardState;
}

function findNextFreePosition(dashboardState: number[][], item: DashboardItemType): { x: number; y: number } | null {
  for (let y = 0; y < dashboardState.length; y++) {
    for (let x = 0; x < dashboardState[y].length; x++) {
      if (
        checkIfSpaceIsFree(dashboardState, {
          id: 0,
          x: x,
          y: y,
          width: item.width,
          height: item.height,
          pluginName: '',
          dataPluginId: undefined,
        })
      ) {
        return { x: x, y: y };
      }
    }
  }
  return null;
}
