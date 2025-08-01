import type {DatabaseSettingsDataPluginType} from '../../../types/settings/databaseSettingsType.ts';
import { useEffect, useState } from 'react';
import type {DataPlugin} from '../../../plugins/interfaces/dataPlugin.ts';
import DataPluginStorage from '../../../utils/dataPluginStorage.ts';
import { configureStore } from '@reduxjs/toolkit';
import { createLogger } from 'redux-logger';
import ProgressReducer from '../../../redux/reducer/general/progressReducer.ts';
import socketMiddleware from '../../../redux/middelware/socket/socketMiddleware.ts';
import StatusBarDataPluginElement from './statusBarDataPluginElement/statusBarDataPluginElement.tsx';
import { Provider } from 'react-redux';
import refreshMiddleware from '../../../redux/middelware/refresh/refreshMiddleware.ts';

import { store as globalStore } from '../../../redux';

const logger = createLogger({
  collapsed: () => true,
});

function StatusBarDataPlugin(props: { dataPlugin: DatabaseSettingsDataPluginType }) {
  const [dataPlugin, setDataPlugin] = useState<DataPlugin | undefined>(undefined);

  useEffect(() => {
    if (props.dataPlugin && props.dataPlugin.id !== undefined) {
      DataPluginStorage.getDataPlugin(props.dataPlugin)
        .then((newDataPlugin) => {
          if (newDataPlugin) {
            setDataPlugin(newDataPlugin);
          }
        })
        .catch((e) => console.log(e));
    }
  }, [props.dataPlugin]);

  /**
   * Create Redux Store from Reducer for individual Item and run saga
   */
  let store;
  if (dataPlugin) {
    const progressUpdateConfig = dataPlugin.general.getProgressUpdateConfig();
    if (progressUpdateConfig.useAutomaticUpdate) {
      store = configureStore({
        reducer: ProgressReducer,
        middleware: (getDefaultMiddleware) =>
          getDefaultMiddleware().concat(
            socketMiddleware(progressUpdateConfig.endpoint || ''),
            refreshMiddleware(globalStore, props.dataPlugin),
            logger,
          ),
      });
    } else {
      store = configureStore({
        reducer: ProgressReducer,
        middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(refreshMiddleware(globalStore, props.dataPlugin), logger),
      });
    }
  } else {
    store = undefined;
  }

  return (
    <>
      {store ? (
        <Provider store={store}>
          <StatusBarDataPluginElement dataPluginConfig={props.dataPlugin} dataPlugin={dataPlugin} store={store} />
        </Provider>
      ) : (
        <div style={{ background: props.dataPlugin.color }}>Loading Data Plugin</div>
      )}
    </>
  );
}

export default StatusBarDataPlugin;
