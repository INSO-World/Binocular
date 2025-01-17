import fileListStyles from './fileList.module.scss';
import { useSelector } from 'react-redux';
import { AppDispatch, RootState, store as globalStore, useAppDispatch } from '../../../../redux';
import { useEffect } from 'react';
import { FileListElementTypeType } from '../../../../types/data/fileListType.ts';
import { generateFileTree } from './fileListUtilities/fileTreeUtilities.ts';
import FileListFolder from './fileListElements/fileListFolder.tsx';
import { DatabaseSettingsDataPluginType } from '../../../../types/settings/databaseSettingsType.ts';
import DataPluginStorage from '../../../../utils/dataPluginStorage.ts';
import { setFileList, setFilesDataPluginId } from '../../../../redux/reducer/data/filesReducer.ts';

function FileList(props: { orientation?: string }) {
  const dispatch: AppDispatch = useAppDispatch();
  const currentDataPlugins = useSelector((state: RootState) => state.settings.database.dataPlugins);

  const fileLists = useSelector((state: RootState) => state.files.fileLists);
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
                    files: {
                      name: '/',
                      type: FileListElementTypeType.Folder,
                      children: generateFileTree(
                        files.map((file) => {
                          return {
                            file: file,
                            checked: true,
                          };
                        }),
                      ),
                      element: {
                        checked: true,
                      },
                    },
                    fileCount: files.length,
                  }),
                ),
              )
              .catch(() => console.log('Error loading Users from selected data source!'));
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
          {fileLists[filesDataPluginId] ? (
            <FileListFolder folder={fileLists[filesDataPluginId]} foldedOut={true}></FileListFolder>
          ) : (
            <span className="loading loading-spinner loading-xs text-accent"></span>
          )}
        </div>
      </div>
    </>
  );
}

export default FileList;
