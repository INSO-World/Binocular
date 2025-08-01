import fileListStyles from './fileList.module.scss';
import { useSelector } from 'react-redux';
import {type AppDispatch, type RootState, store as globalStore, useAppDispatch } from '../../../../redux';
import { useEffect } from 'react';
import { FileTreeElementTypeType } from '../../../../types/data/fileListType.ts';
import { filterFileTree, generateFileTree } from './fileListUtilities/fileTreeUtilities.tsx';
import FileListFolder from './fileListElements/fileListFolder.tsx';
import type {DatabaseSettingsDataPluginType} from '../../../../types/settings/databaseSettingsType.ts';
import DataPluginStorage from '../../../../utils/dataPluginStorage.ts';
import { setFileList, setFilesDataPluginId } from '../../../../redux/reducer/data/filesReducer.ts';
import type {DataPluginFile} from '../../../../plugins/interfaces/dataPluginInterfaces/dataPluginFiles.ts';

function FileList(props: { orientation?: string; search: string }) {
  const dispatch: AppDispatch = useAppDispatch();
  const currentDataPlugins = useSelector((state: RootState) => state.settings.database.dataPlugins);
  const fileTrees = useSelector((state: RootState) => state.files.fileTrees);
  const fileCounts = useSelector((state: RootState) => state.files.fileCounts);

  const filesDataPluginId = useSelector((state: RootState) => state.files.dataPluginId);

  function refreshFileTree(dP: DatabaseSettingsDataPluginType) {
    if (dP && dP.id !== undefined) {
      console.log(`REFRESH FILES (${dP.name} #${dP.id})`);
      DataPluginStorage.getDataPlugin(dP)
        .then((dataPlugin) => {
          if (dataPlugin) {
            dataPlugin.files
              .getAll()
              .then((files) =>
                dispatch(
                  setFileList({
                    dataPluginId: dP.id !== undefined ? dP.id : -1,
                    fileTree: {
                      name: '/',
                      type: FileTreeElementTypeType.Folder,
                      children: generateFileTree(files),
                      checked: true,
                      foldedOut: true,
                      isRoot: true,
                    },
                    files: files.map((f: DataPluginFile) => {
                      return {
                        element: f,
                        checked: true,
                      };
                    }),
                  }),
                ),
              )
              .catch(() => console.log('Error loading Files from selected data source!'));
          }
        })
        .catch((e) => console.log(e));
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

  useEffect(() => {
    if (currentDataPlugins.length !== 0) {
      currentDataPlugins.forEach((dP: DatabaseSettingsDataPluginType) => {
        if (filesDataPluginId === undefined && dP.isDefault && dP.id !== undefined) {
          dispatch(setFilesDataPluginId(dP.id));
        }
        refreshFileTree(dP);
      });
    }
  }, [currentDataPlugins]);

  useEffect(() => {
    refreshFileTree(filesDataPluginId);
  }, [filesDataPluginId]);

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
        <div>{fileCounts[filesDataPluginId]} Files indexed</div>
        <div>
          {fileTrees[filesDataPluginId] ? (
            <FileListFolder folder={filterFileTree(fileTrees[filesDataPluginId], props.search)} foldedOut={true}></FileListFolder>
          ) : (
            <span className="loading loading-spinner loading-xs text-accent"></span>
          )}
        </div>
      </div>
    </>
  );
}

export default FileList;
