import type { DatabaseSettingsDataPluginType } from '../../../types/settings/databaseSettingsType.ts';
import { removeDataPlugin, setDataPluginAsDefault } from '../../../redux/reducer/settings/settingsReducer.ts';
import DataPluginStorage from '../../../utils/dataPluginStorage.ts';
import { type AppDispatch, type RootState, useAppDispatch } from '../../../redux';
import { useSelector } from 'react-redux';
import connectedDataPluginStyles from './connectedDataPlugins.module.scss';
import type { DataPlugin } from '../../../plugins/interfaces/dataPlugin.ts';
import { removeFileList } from '../../../redux/reducer/data/filesReducer.ts';
import { store as globalStore } from '../../../redux';
import { updateDashboardItem } from '../../../redux/reducer/general/dashboardReducer.ts';
import type { DashboardItemType } from '../../../types/general/dashboardItemType.ts';
import { downloadExportCompressed } from '../../../plugins/utils/export.ts';
import ColorCodedPanel from '../../colorCodedPanel/colorCodedPanel.tsx';
import { useState } from 'react';

function abbreviate(key: string) {
  if (key.length <= 10) return key;
  return `${key.slice(0, 4)}...${key.slice(-4)}`;
}

function reassignDashboardItems(deletedId: number) {
  const dashboardItems: DashboardItemType[] = globalStore.getState().dashboard.dashboardItems;
  dashboardItems.forEach((item) => {
    if (item.dataPluginId === deletedId) {
      globalStore.dispatch(updateDashboardItem({ ...item, dataPluginId: undefined }));
    }
  });
}

function ConnectedDataPlugins(props: { interactable: boolean }) {
  const dispatch: AppDispatch = useAppDispatch();
  const [copiedId, setCopiedId] = useState<number | undefined>(undefined);

  const dataPlugins = useSelector((state: RootState) => state.settings.database.dataPlugins);

  return (
    <>
      <p className={'font-bold'}>Configured Database Connections:</p>
      {dataPlugins.length === 0 ? (
        <div>No Database Connections configured! Add one from below.</div>
      ) : (
        <div className={'flex flex-wrap gap-3'}>
          {dataPlugins.map((settingsDatabaseDataPlugin: DatabaseSettingsDataPluginType) => (
            <ColorCodedPanel
              key={`settingsDatabasePlugin${settingsDatabaseDataPlugin.id}`}
              color={settingsDatabaseDataPlugin.color}
              className="w-64 min-w-64 mb-3">
              <div>
                {settingsDatabaseDataPlugin.name == 'PouchDb' && (
                  <div className="dropdown dropdown-end" style={{ float: 'inline-end' }}>
                    <div tabIndex={0} role="button" className={connectedDataPluginStyles.settingsButton} />
                    <ul tabIndex={-1} className="dropdown-content menu bg-base-100 rounded-box z-1 w-52 p-2 shadow-sm">
                      <li>
                        <a
                          onClick={() => {
                            DataPluginStorage.getDataPlugin(settingsDatabaseDataPlugin).then((dataPlugin: DataPlugin | undefined) => {
                              if (dataPlugin && dataPlugin.export) {
                                dataPlugin.export().then((data) => downloadExportCompressed(data, settingsDatabaseDataPlugin.metadata));
                              }
                            });
                          }}>
                          Download
                        </a>
                      </li>
                    </ul>
                  </div>
                )}
                <p className="card-title text-sm !mb-0">
                  {settingsDatabaseDataPlugin.name} #{settingsDatabaseDataPlugin.id}
                  {settingsDatabaseDataPlugin.id === 0 && <span className="badge badge-outline badge-sm">pre-loaded</span>}
                  {settingsDatabaseDataPlugin.isDefault && <span className="badge badge-primary badge-sm">Default</span>}
                </p>
              </div>
              {settingsDatabaseDataPlugin.parameters.apiKey && (
                <div className="text-xs">
                  <span className={'font-bold'}>API Key: </span>
                  <span
                    className="inline-flex items-center gap-1 cursor-pointer hover:underline"
                    title="Click to copy"
                    onClick={() => {
                      navigator.clipboard.writeText(settingsDatabaseDataPlugin.parameters.apiKey!).then(() => {
                        setCopiedId(settingsDatabaseDataPlugin.id);
                        setTimeout(() => setCopiedId(undefined), 1500);
                      });
                    }}>
                    {copiedId === settingsDatabaseDataPlugin.id ? (
                      'Copied!'
                    ) : (
                      <>
                        {abbreviate(settingsDatabaseDataPlugin.parameters.apiKey)}
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          width="10"
                          height="10"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                          strokeLinecap="round"
                          strokeLinejoin="round">
                          <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
                          <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                        </svg>
                      </>
                    )}
                  </span>
                </div>
              )}
              {settingsDatabaseDataPlugin.parameters.endpoint && (
                <div className="text-xs">
                  <span className={'font-bold'}>Endpoint: </span>
                  <span>{settingsDatabaseDataPlugin.parameters.endpoint}</span>
                </div>
              )}
              {settingsDatabaseDataPlugin.parameters.fileName && (
                <div className="text-xs">
                  <span className={'font-bold'}>Database: </span>
                  <span>{settingsDatabaseDataPlugin.parameters.fileName}</span>
                </div>
              )}
              {settingsDatabaseDataPlugin.parameters.progressUpdate && (
                <div className="text-xs">
                  <span className={'font-bold'}>Progress Update: </span>
                  <span className="badge badge-success badge-sm ml-1">Configured</span>
                </div>
              )}
              {settingsDatabaseDataPlugin.parameters.progressUpdate && settingsDatabaseDataPlugin.parameters.progressUpdate.endpoint && (
                <div className="text-xs">
                  <span className={'font-bold'}>Progress Update Endpoint: </span>
                  <span>{settingsDatabaseDataPlugin.parameters.progressUpdate.endpoint}</span>
                </div>
              )}
              {props.interactable && (
                <div className="flex gap-2 mt-auto justify-center">
                  <button
                    className={'btn btn-outline btn-sm'}
                    onClick={() => {
                      if (settingsDatabaseDataPlugin.id !== undefined) {
                        dispatch(setDataPluginAsDefault(settingsDatabaseDataPlugin.id));
                      }
                    }}>
                    Set Default
                  </button>
                  {settingsDatabaseDataPlugin.id !== 0 && (
                    <button
                      className={'btn btn-error btn-outline btn-sm'}
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
                                        reassignDashboardItems(settingsDatabaseDataPlugin.id);
                                        dispatch(removeDataPlugin(settingsDatabaseDataPlugin.id));
                                        dispatch(removeFileList(settingsDatabaseDataPlugin.id));
                                      }
                                    })
                                    .catch((e) => console.log(e));
                                }
                              })
                              .catch((e) => console.log(e));
                          } else {
                            reassignDashboardItems(settingsDatabaseDataPlugin.id);
                            dispatch(removeDataPlugin(settingsDatabaseDataPlugin.id));
                            dispatch(removeFileList(settingsDatabaseDataPlugin.id));
                          }
                        }
                      }}>
                      Delete
                    </button>
                  )}
                </div>
              )}
            </ColorCodedPanel>
          ))}
        </div>
      )}
    </>
  );
}

export default ConnectedDataPlugins;
