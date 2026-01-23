import fileListStyles from './fileList.module.scss';
import { useSelector } from 'react-redux';
import { type AppDispatch, type RootState, store as globalStore, useAppDispatch } from '../../../../redux';
import { useEffect } from 'react';
import { filterFileTree, loadFileList, refreshFileList } from './fileListUtilities/fileTreeUtilities.tsx';
import FileListFolder from './fileListElements/fileListFolder.tsx';
import type { DatabaseSettingsDataPluginType } from '../../../../types/settings/databaseSettingsType.ts';
import {
  setFilesDataPluginId,
  checkAllFiles,
  uncheckAllFiles,
  switchAllFileSelection,
} from '../../../../redux/reducer/data/filesReducer.ts';

function FileList(props: { orientation?: string; search: string }) {
  const dispatch: AppDispatch = useAppDispatch();
  const currentDataPlugins = useSelector((state: RootState) => state.settings.database.dataPlugins);
  const fileTrees = useSelector((state: RootState) => state.files.fileTrees);
  const fileCounts = useSelector((state: RootState) => state.files.fileCounts);

  const filesDataPluginId = useSelector((state: RootState) => state.files.dataPluginId);

  function refreshFileTree(dP: DatabaseSettingsDataPluginType) {
    if (dP && dP.id !== undefined) {
      loadFileList(dP, dispatch);
    } else {
      if (currentDataPlugins.length > 0) {
        dispatch(setFilesDataPluginId(currentDataPlugins[0].id));
      }
    }
  }

  useEffect(() => {
    const dataPlugin = currentDataPlugins.filter((p: DatabaseSettingsDataPluginType) => p.id === filesDataPluginId)[0];
    refreshFileTree(dataPlugin);
  }, [currentDataPlugins, filesDataPluginId]);

  globalStore.subscribe(() => {
    if (filesDataPluginId) {
      if (globalStore.getState().actions.lastAction === 'REFRESH_PLUGIN') {
        if ((globalStore.getState().actions.payload as { pluginId: number }).pluginId === filesDataPluginId) {
          const dataPlugin = currentDataPlugins.filter((p: DatabaseSettingsDataPluginType) => p.id === filesDataPluginId)[0];
          refreshFileTree(dataPlugin);
        }
      }
    }
  });

  return (
    <>
      <div
        className={
          'text-xs ' +
          fileListStyles.fileList +
          ' ' +
          (props.orientation === 'horizontal' ? fileListStyles.fileListHorizontal : fileListStyles.fileListVertical)
        }>
        <div className={'border-b border-base-300 pt-1'}>
          <div className="join">
            <button
              className={'btn btn-xs join-item ' + fileListStyles.checkAllButton}
              onClick={() => dispatch(checkAllFiles())}
              title="Check all files"></button>
            <button
              className={`btn btn-xs join-item '+ ${fileListStyles.uncheckAllButton}`}
              onClick={() => dispatch(uncheckAllFiles())}
              title="Uncheck all files"></button>
            <button
              className={'btn btn-xs join-item ' + fileListStyles.flipButton}
              onClick={() => dispatch(switchAllFileSelection())}
              title="Switch file selection"></button>
            <button
              className={'btn btn-xs join-item '}
              onClick={() => {
                const dataPlugin = currentDataPlugins.filter((p: DatabaseSettingsDataPluginType) => p.id === filesDataPluginId)[0];
                refreshFileList(dataPlugin, dispatch);
              }}
              title="Switch file selection">
              REFRESH
            </button>
          </div>
        </div>
        <div>{fileCounts[filesDataPluginId ? filesDataPluginId : -1]} Files indexed</div>
        <div>
          {fileTrees[filesDataPluginId ? filesDataPluginId : -1] ? (
            <FileListFolder
              folder={filterFileTree(fileTrees[filesDataPluginId ? filesDataPluginId : -1], props.search)}
              foldedOut={true}></FileListFolder>
          ) : (
            <span className="loading loading-spinner loading-xs text-accent"></span>
          )}
        </div>
      </div>
    </>
  );
}

export default FileList;
