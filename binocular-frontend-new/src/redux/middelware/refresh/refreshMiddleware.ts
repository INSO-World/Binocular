import { Middleware } from 'redux';
import { Action, Store } from '@reduxjs/toolkit';
import { DatabaseSettingsDataPluginType } from '../../../types/settings/databaseSettingsType.ts';

const refreshMiddleware = (globalStore: Store, dataPlugin: DatabaseSettingsDataPluginType): Middleware => {
  return () => {
    return (next) => (action) => {
      if ((action as Action).type == 'progress/setProgress') {
        globalStore.dispatch({ type: 'REFRESH_PLUGIN', payload: { pluginId: dataPlugin.id } });
      }
      next(action);
    };
  };
};

export default refreshMiddleware;
