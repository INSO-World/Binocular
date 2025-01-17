import fileListStyles from './fileList.module.scss';
import { useSelector } from 'react-redux';
import { AppDispatch, RootState, store as globalStore, useAppDispatch } from '../../../../redux';
import { useEffect, useState } from 'react';
import { FileListElementType } from '../../../../types/data/fileListType.ts';
import { generateFileTree } from './fileListUtilities/fileTreeUtilities.ts';
import FileListFolder from './fileListElements/fileListFolder.tsx';
import { DatabaseSettingsDataPluginType } from '../../../../types/settings/databaseSettingsType.ts';
import DataPluginStorage from '../../../../utils/dataPluginStorage.ts';
import { setFilesDataPluginId } from '../../../../redux/reducer/data/filesReducer.ts';

function FileList(props: { orientation?: string }) {
  const dispatch: AppDispatch = useAppDispatch();
  const currentDataPlugins = useSelector((state: RootState) => state.settings.database.dataPlugins);

  const [fileList, setFileList] = useState<FileListElementType[]>();
  const filesDataPluginId = useSelector((state: RootState) => state.files.dataPluginId);

  function refreshFileTree() {
    const dataPlugin = currentDataPlugins.filter((p: DatabaseSettingsDataPluginType) => p.id === filesDataPluginId)[0];
    if (dataPlugin && dataPlugin.id !== undefined) {
      console.log(`REFRESH FILES (${dataPlugin.name} #${dataPlugin.id})`);
      DataPluginStorage.getDataPlugin(dataPlugin)
        .then((dataPlugin) => {
          if (dataPlugin) {
            dataPlugin.files
              .getAll()
              .then((files) => setFileList(generateFileTree(files)))
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
    refreshFileTree();
  }, [currentDataPlugins, filesDataPluginId]);

  globalStore.subscribe(() => {
    if (filesDataPluginId) {
      if (globalStore.getState().actions.lastAction === 'REFRESH_PLUGIN') {
        if ((globalStore.getState().actions.payload as { pluginId: number }).pluginId === filesDataPluginId) {
          refreshFileTree();
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
        <div>
          {fileList ? (
            <FileListFolder folder={fileList} name={'/'} foldedOut={true} checked={true}></FileListFolder>
          ) : (
            <span className="loading loading-spinner loading-xs text-accent"></span>
          )}
        </div>
      </div>
    </>
  );
}

export default FileList;
