import { Middleware } from 'redux';
import { Action, Store } from '@reduxjs/toolkit';
import { DatabaseSettingsDataPluginType } from '../../../types/settings/databaseSettingsType.ts';

const refreshMiddleware = (globalStore: Store, dataPlugin: DatabaseSettingsDataPluginType): Middleware => {
  return () => {
    return (next) => (action) => {
      if ((action as Action).type == 'progress/setProgress') {
        next(action);
        globalStore.dispatch({ type: 'REFRESH_PLUGIN', payload: { pluginId: dataPlugin.id } });
      } else {
        next(action);
      }
    };
  };
};

export default refreshMiddleware;
