import statusBarDataPluginElementStyles from './statusBarDataPluginElement.module.scss';
import { DatabaseSettingsDataPluginType } from '../../../../types/settings/databaseSettingsType.ts';
import { DataPlugin } from '../../../../plugins/interfaces/dataPlugin.ts';
import { useDispatch, useSelector } from 'react-redux';
import { Store } from '@reduxjs/toolkit';
import { useEffect } from 'react';

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
          {props.dataPluginConfig.name} #{props.dataPluginConfig.id}
        </div>
        {props.dataPluginConfig.parameters.progressUpdate?.useAutomaticUpdate ? (
          <div className={'p-1'}>
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
          <div>No Data avaliable</div>
        )}
      </div>
    </>
  );
}

export default StatusBarDataPlugin;
