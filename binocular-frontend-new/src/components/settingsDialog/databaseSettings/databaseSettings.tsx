import { dataPlugins } from '../../../plugins/pluginRegistry.ts';
import { useSelector } from 'react-redux';
import { AppDispatch, RootState, useAppDispatch } from '../../../redux';
import { DataPlugin } from '../../../plugins/interfaces/dataPlugin.ts';
import { useEffect } from 'react';
import { removeDataPlugin, setDataPluginAsDefault } from '../../../redux/reducer/settings/settingsReducer.ts';
import { DatabaseSettingsDataPluginType } from '../../../types/settings/databaseSettingsType.ts';
import DataPluginStorage from '../../../utils/dataPluginStorage.ts';
import AddDataPluginCard from '../addDataPluginCard/addDataPluginCard.tsx';

function DatabaseSettings() {
  const dispatch: AppDispatch = useAppDispatch();

  const settingsDatabaseDataPlugins = useSelector((state: RootState) => state.settings.database.dataPlugins);

  useEffect(() => {
    settingsDatabaseDataPlugins.forEach((dP: DatabaseSettingsDataPluginType) => {
      DataPluginStorage.addDataPlugin(dP)
        .then(() => console.log(`${dP.name} #${dP.id} created`))
        .catch((e) => console.log(e));
    });
  }, [settingsDatabaseDataPlugins]);

  return (
    <>
      <div className={'h-4/5 overflow-x-hidden overflow-y-scroll'}>
        <h2 className={'font-bold'}>Current Configured Database Connections:</h2>
        {settingsDatabaseDataPlugins.length === 0 ? (
          <div>No Database Connections configured! Add one from below.</div>
        ) : (
          <div className={'flex'}>
            {settingsDatabaseDataPlugins.map((settingsDatabaseDataPlugin: DatabaseSettingsDataPluginType) => (
              <div
                className={'card w-96 bg-base-100 shadow-xl mb-3 mr-3 border-2 border-base-300'}
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
                  {settingsDatabaseDataPlugin.parameters.progressUpdate &&
                    settingsDatabaseDataPlugin.parameters.progressUpdate.endpoint && (
                      <div>
                        <span className={'font-bold'}>Use Progress Update Endpoint:</span>
                        <span>{settingsDatabaseDataPlugin.parameters.progressUpdate.endpoint}</span>
                      </div>
                    )}
                  <button
                    className={'btn btn-outline'}
                    onClick={() => {
                      if (settingsDatabaseDataPlugin.id !== undefined) {
                        dispatch(setDataPluginAsDefault(settingsDatabaseDataPlugin.id));
                      }
                    }}>
                    Set Default
                  </button>
                  {settingsDatabaseDataPlugin.id !== 0 && (
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
        <h2 className={'font-bold'}>Add Database Connection:</h2>
        <div className={'flex'}>
          {dataPlugins
            .map((dataPlugin) => new dataPlugin())
            .map((dataPlugin: DataPlugin) => (
              <AddDataPluginCard key={dataPlugin.name} dataPlugin={dataPlugin}></AddDataPluginCard>
            ))}
        </div>
      </div>
    </>
  );
}

export default DatabaseSettings;
