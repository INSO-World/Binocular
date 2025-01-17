import statusBarDataPluginElementStyles from './statusBarDataPluginElement.module.scss';
import { DatabaseSettingsDataPluginType } from '../../../../types/settings/databaseSettingsType.ts';
import { DataPlugin } from '../../../../plugins/interfaces/dataPlugin.ts';
import { useDispatch, useSelector } from 'react-redux';
import { Store } from '@reduxjs/toolkit';
import { useEffect } from 'react';
import { SocketConnectionStatusType } from '../../../../types/general/socketConnectionType.ts';
import ConnectedToApi from '../../../../assets/connected_to_api_blue.svg';
import ConnectedToApiFailed from '../../../../assets/connected_to_api_failed_red.svg';
import Idle from '../../../../assets/idle_blue.svg';
function StatusBarDataPlugin(props: {
  dataPluginConfig: DatabaseSettingsDataPluginType;
  dataPlugin: DataPlugin | undefined;
  store: Store;
}) {
  type RootState = ReturnType<typeof props.store.getState>;
  type AppDispatch = typeof props.store.dispatch;
  const useAppDispatch = () => useDispatch<AppDispatch>();
  const dispatch: AppDispatch = useAppDispatch();

  const progress = useSelector((state: RootState) => state.progress);
  const socketConnection = useSelector((state: RootState) => state.socketConnection);

  //Trigger Refresh when dataConnection changes
  useEffect(() => {
    dispatch({
      type: 'REFRESH',
    });
  }, [props.dataPlugin]);

  return (
    <>
      <div className={statusBarDataPluginElementStyles.dataPluginElement}>
        <div className={statusBarDataPluginElementStyles.dataPluginLabel} style={{ background: props.dataPluginConfig.color }}>
          {props.dataPluginConfig.id === 0 ? (
            <span className={'flex items-center gap-3'}>
              {props.dataPluginConfig.name}
              <div className={statusBarDataPluginElementStyles.dataPluginLabelBadge}>pre-loaded</div>
            </span>
          ) : (
            <span>
              {props.dataPluginConfig.name} #{props.dataPluginConfig.id}
            </span>
          )}
          {socketConnection.status === SocketConnectionStatusType.Idle && <img className={'inline h-4 ml-2'} src={Idle} alt={'idle'} />}
          {socketConnection.status === SocketConnectionStatusType.Connected && (
            <img className={'inline h-4 ml-2'} src={ConnectedToApi} alt={'idle'} />
          )}
          {socketConnection.status === SocketConnectionStatusType.Disconnected && (
            <img className={'inline h-4 ml-2'} src={ConnectedToApiFailed} alt={'idle'} />
          )}
        </div>
        {props.dataPluginConfig.parameters.progressUpdate?.useAutomaticUpdate ? (
          <div className={'p-1'}>
            <div>
              <div>Connection Status: </div>
              <div>
                {socketConnection.status === SocketConnectionStatusType.Idle && (
                  <span className={statusBarDataPluginElementStyles.connectionStatus}>
                    <img className={'inline h-4 mr-2'} src={Idle} alt={'idle'} />
                    Idle
                  </span>
                )}
                {socketConnection.status === SocketConnectionStatusType.Connected && (
                  <span className={statusBarDataPluginElementStyles.connectionStatus}>
                    <img className={'inline h-4 mr-2'} src={ConnectedToApi} alt={'idle'} />
                    Connected
                  </span>
                )}
                {socketConnection.status === SocketConnectionStatusType.Disconnected && (
                  <span className={statusBarDataPluginElementStyles.connectionStatus}>
                    <img className={'inline h-4 mr-2'} src={ConnectedToApiFailed} alt={'idle'} />
                    Disconnected
                  </span>
                )}
              </div>
            </div>
            <hr className={'mb-3 mt-3'} />
            <div>
              <div>
                Commits: {progress.report.commits.processed}/{progress.report.commits.total}
              </div>
              <progress
                className="progress w-56 progress-accent"
                value={progress.report.commits.processed}
                max={progress.report.commits.total}></progress>
            </div>
            <div>
              <div>
                Issues: {progress.report.issues.processed}/{progress.report.issues.total}
              </div>
              <progress
                className="progress w-56 progress-accent"
                value={progress.report.issues.processed}
                max={progress.report.issues.total}></progress>
            </div>
            <div>
              <div>
                Builds: {progress.report.builds.processed}/{progress.report.builds.total}
              </div>
              <progress
                className="progress w-56 progress-accent"
                value={progress.report.builds.processed}
                max={progress.report.builds.total}></progress>
            </div>
            <div>
              <div>
                Files: {progress.report.files.processed}/{progress.report.files.total}
              </div>
              <progress
                className="progress w-56 progress-accent"
                value={progress.report.files.processed}
                max={progress.report.files.total}></progress>
            </div>
            <div>
              <div>
                Modules: {progress.report.modules.processed}/{progress.report.modules.total}
              </div>
              <progress
                className="progress w-56 progress-accent"
                value={progress.report.modules.processed}
                max={progress.report.modules.total}></progress>
            </div>
            <div>
              <div>
                Milestones: {progress.report.milestones.processed}/{progress.report.milestones.total}
              </div>
              <progress
                className="progress w-56 progress-accent"
                value={progress.report.milestones.processed}
                max={progress.report.milestones.total}></progress>
            </div>
            <div>
              <div>
                Merge Requests: {progress.report.mergeRequests.processed}/{progress.report.mergeRequests.total}
              </div>
              <progress
                className="progress w-56 progress-accent"
                value={progress.report.mergeRequests.processed}
                max={progress.report.mergeRequests.total}></progress>
            </div>
          </div>
        ) : (
          <div>{props.dataPlugin?.description}</div>
        )}
      </div>
    </>
  );
}

export default StatusBarDataPlugin;
