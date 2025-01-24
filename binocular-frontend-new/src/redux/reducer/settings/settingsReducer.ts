import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import Config from '../../../config.ts';
import { GeneralSettingsType, SettingsGeneralGridSize } from '../../../types/settings/generalSettingsType.ts';
import { DatabaseSettingsDataPluginType, DatabaseSettingsType } from '../../../types/settings/databaseSettingsType.ts';
import distinctColors from 'distinct-colors';

export interface SettingsInitialState {
  general: GeneralSettingsType;
  database: DatabaseSettingsType;
  localDatabaseLoadingState: LocalDatabaseLoadingState;
}

export enum LocalDatabaseLoadingState {
  none,
  loading,
}

const initialState: SettingsInitialState = {
  general: {
    gridSize: SettingsGeneralGridSize.medium,
  },
  database: {
    currID: 0,
    dataPlugins: [],
  },
  localDatabaseLoadingState: LocalDatabaseLoadingState.none,
};

export const settingsSlice = createSlice({
  name: 'settings',
  initialState: () => {
    const storedState = localStorage.getItem(`${settingsSlice.name}StateV${Config.localStorageVersion}`);
    if (storedState === null) {
      localStorage.setItem(`${settingsSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(initialState));
      return initialState;
    } else {
      return JSON.parse(storedState);
    }
  },
  reducers: {
    setGeneralSettings: (state, action: PayloadAction<GeneralSettingsType>) => {
      state.general = action.payload;
      localStorage.setItem(`${settingsSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    addDataPlugin: (state, action: PayloadAction<DatabaseSettingsDataPluginType>) => {
      if (action.payload.id === undefined) {
        const colors = distinctColors({ count: 100 });
        action.payload.isDefault = state.database.dataPlugins.length === 0;
        state.database.currID++;
        if (action.payload.color === '#000') {
          action.payload.color = colors[state.database.currID].hex() + '22';
        }
        action.payload.id = state.database.currID;
        state.database.dataPlugins.push(action.payload);
        console.log(`Inserted dataPlugin ${action.payload.id}`);
      } else {
        let found = false;
        state.database.dataPlugins = state.database.dataPlugins.map((dp: DatabaseSettingsDataPluginType) => {
          if (dp.id === action.payload.id) {
            found = true;
            return action.payload;
          }
          return dp;
        });
        if (!found) {
          state.database.dataPlugins.push(action.payload);
          console.log(`Inserted dataPlugin ${action.payload.id}`);
        } else {
          console.log(`Updated dataPlugin ${action.payload.id}`);
        }
      }
      localStorage.setItem(`${settingsSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    removeDataPlugin: (state, action: PayloadAction<number>) => {
      state.database.dataPlugins = state.database.dataPlugins.filter((dP: DatabaseSettingsDataPluginType) => dP.id !== action.payload);
      localStorage.setItem(`${settingsSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    setDataPluginAsDefault: (state, action: PayloadAction<number>) => {
      state.database.dataPlugins = state.database.dataPlugins.map((dP: DatabaseSettingsDataPluginType) => {
        dP.isDefault = dP.id === action.payload;
        return dP;
      });
      localStorage.setItem(`${settingsSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    clearSettingsStorage: () => {
      localStorage.removeItem(`${settingsSlice.name}StateV${Config.localStorageVersion}`);
    },
    importSettingsStorage: (state, action: PayloadAction<SettingsInitialState>) => {
      state = action.payload;
      localStorage.setItem(`${settingsSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    setLocalDatabaseLoadingState: (state, action: PayloadAction<LocalDatabaseLoadingState>) => {
      state.localDatabaseLoadingState = action.payload;
    },
  },
});

export const {
  setGeneralSettings,
  addDataPlugin,
  removeDataPlugin,
  setDataPluginAsDefault,
  clearSettingsStorage,
  importSettingsStorage,
  setLocalDatabaseLoadingState,
} = settingsSlice.actions;
export default settingsSlice.reducer;
