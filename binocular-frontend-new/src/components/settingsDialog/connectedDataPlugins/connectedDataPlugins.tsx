import type {DatabaseSettingsDataPluginType} from '../../../types/settings/databaseSettingsType.ts';
import { removeDataPlugin, setDataPluginAsDefault } from '../../../redux/reducer/settings/settingsReducer.ts';
import DataPluginStorage from '../../../utils/dataPluginStorage.ts';
import {type AppDispatch, type RootState, useAppDispatch } from '../../../redux';
import { useSelector } from 'react-redux';

function ConnectedDataPlugins(props: { interactable: boolean }) {
  const dispatch: AppDispatch = useAppDispatch();

  const dataPlugins = useSelector((state: RootState) => state.settings.database.dataPlugins);

  return (
    <>
      <h2 className={'font-bold'}>Configured Database Connections:</h2>
      {dataPlugins.length === 0 ? (
        <div>No Database Connections configured! Add one from below.</div>
      ) : (
        <div className={'flex overflow-x-auto'}>
          {dataPlugins.map((settingsDatabaseDataPlugin: DatabaseSettingsDataPluginType) => (
            <div
              className={'card w-96 bg-base-100 shadow-md mb-3 mr-3 border border-base-300 min-w-96'}
              style={{ background: settingsDatabaseDataPlugin.color }}
              key={`settingsDatabasePlugin${settingsDatabaseDataPlugin.id}`}>
              <div className="card-body">
                <h2 className="card-title">
                  {settingsDatabaseDataPlugin.name} #{settingsDatabaseDataPlugin.id}
                  {settingsDatabaseDataPlugin.id === 0 && <div className="badge badge-outline">pre-loaded</div>}
                  {settingsDatabaseDataPlugin.isDefault && <div className="badge badge-accent">Default</div>}
                </h2>
                {settingsDatabaseDataPlugin.parameters.apiKey && (
                  <div>
                    <span className={'font-bold'}>API Key:</span>
                    <span>{settingsDatabaseDataPlugin.parameters.apiKey}</span>
                  </div>
                )}
                {settingsDatabaseDataPlugin.parameters.endpoint && (
                  <div>
                    <span className={'font-bold'}>Endpoint:</span>
                    <span>{settingsDatabaseDataPlugin.parameters.endpoint}</span>
                  </div>
                )}
                {settingsDatabaseDataPlugin.parameters.fileName && (
                  <div>
                    <span className={'font-bold'}>Database:</span>
                    <span>{settingsDatabaseDataPlugin.parameters.fileName}</span>
                  </div>
                )}
                {settingsDatabaseDataPlugin.parameters.progressUpdate && (
                  <div>
                    <span className={'font-bold'}>Progress Update:</span>
                    <span className="badge badge-success ml-1">Configured</span>
                  </div>
                )}
                {settingsDatabaseDataPlugin.parameters.progressUpdate && settingsDatabaseDataPlugin.parameters.progressUpdate.endpoint && (
                  <div>
                    <span className={'font-bold'}>Use Progress Update Endpoint:</span>
                    <span>{settingsDatabaseDataPlugin.parameters.progressUpdate.endpoint}</span>
                  </div>
                )}
                {props.interactable && (
                  <button
                    className={'btn btn-outline'}
                    onClick={() => {
                      if (settingsDatabaseDataPlugin.id !== undefined) {
                        dispatch(setDataPluginAsDefault(settingsDatabaseDataPlugin.id));
                      }
                    }}>
                    Set Default
                  </button>
                )}
                {props.interactable && settingsDatabaseDataPlugin.id !== 0 && (
                  <button
                    className={'btn btn-error btn-outline'}
                    onClick={() => {
                      if (settingsDatabaseDataPlugin.id !== undefined) {
                        if (settingsDatabaseDataPlugin.parameters.fileName) {
                          DataPluginStorage.getDataPlugin(settingsDatabaseDataPlugin)
                            .then((dataPlugin) => {
                              if (dataPlugin) {
                                dataPlugin
                                  .clearRemains()
                                  .then(() => {
                                    console.log(`${settingsDatabaseDataPlugin.name} #${settingsDatabaseDataPlugin.id} cleared`);
                                    if (settingsDatabaseDataPlugin.id !== undefined) {
                                      dispatch(removeDataPlugin(settingsDatabaseDataPlugin.id));
                                    }
                                  })
                                  .catch((e) => console.log(e));
                              }
                            })
                            .catch((e) => console.log(e));
                        } else {
                          dispatch(removeDataPlugin(settingsDatabaseDataPlugin.id));
                        }
                      }
                    }}>
                    Delete
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  );
}

export default ConnectedDataPlugins;
