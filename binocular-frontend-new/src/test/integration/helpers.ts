// Shared test store factory for online integration tests (I1–I5).
// Mirrors src/redux/index.ts but omits redux-logger to keep test output clean.

import { configureStore } from '@reduxjs/toolkit';
import type { Middleware } from 'redux';

import DashboardReducer from '../../redux/reducer/general/dashboardReducer.ts';
import LayoutReducer from '../../redux/reducer/general/layoutReducer.ts';
import AuthorsReducer from '../../redux/reducer/data/authorsReducer.ts';
import AccountsReducer from '../../redux/reducer/data/accountsReducer.ts';
import FilesReducer from '../../redux/reducer/data/filesReducer.ts';
import SettingsReducer from '../../redux/reducer/settings/settingsReducer.ts';
import ExportReducer from '../../redux/reducer/export/exportReducer.ts';
import ParametersReducer from '../../redux/reducer/parameters/parametersReducer.ts';
import SprintsReducer from '../../redux/reducer/data/sprintsReducer.ts';
import NotificationsReducer from '../../redux/reducer/general/notificationsReducer.ts';
import TabsReducer from '../../redux/reducer/general/tabsReducer.ts';
import ActionsReducer from '../../redux/reducer/general/actionsReducer.ts';
import actionsMiddleware from '../../redux/middleware/actions/actionsMiddleware.ts';

export function createTestStore() {
  return configureStore({
    reducer: {
      dashboard: DashboardReducer,
      layout: LayoutReducer,
      authors: AuthorsReducer,
      accounts: AccountsReducer,
      files: FilesReducer,
      settings: SettingsReducer,
      export: ExportReducer,
      parameters: ParametersReducer,
      sprints: SprintsReducer,
      notifications: NotificationsReducer,
      tabs: TabsReducer,
      actions: ActionsReducer,
    },
    middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(actionsMiddleware() as Middleware),
  });
}
