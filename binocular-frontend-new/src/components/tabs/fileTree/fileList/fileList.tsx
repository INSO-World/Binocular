import fileListStyles from './fileList.module.scss';
import { useSelector } from 'react-redux';
import { type AppDispatch, type RootState, store as globalStore, useAppDispatch } from '../../../../redux';
import { useEffect } from 'react';
import type { DatabaseSettingsDataPluginType } from '../../../../types/settings/databaseSettingsType.ts';
import { setFilesDataPluginId, showFileTreeElementInfo, updateFileListElement } from '../../../../redux/reducer/data/filesReducer.ts';
import { type ContextMenuOption, showContextMenu } from '../../../contextMenu/contextMenuHelper';
import infoIcon from '../../../../assets/info_gray.svg';
import { FileTreeElementTypeType } from '../../../../types/data/fileListType';
import openInNewIcon from '../../../../assets/open_in_new_gray.svg';
import FileTreeFolder from '../../../fileTree/fileTreeElements/fileTreeFolder/fileTreeFolder';
import { refreshFileList } from '../utils/fileListUtilities';
import { filterFileTree } from '../../../fileTree/utils/fileTreeUtilities';

function FileList(props: { orientation?: string; search: string }) {
  const dispatch: AppDispatch = useAppDispatch();
  const currentDataPlugins = useSelector((state: RootState) => state.settings.database.dataPlugins);
  const fileTrees = useSelector((state: RootState) => state.files.fileTrees);
  const fileCounts = useSelector((state: RootState) => state.files.fileCounts);

  const filesDataPluginId = useSelector((state: RootState) => state.files.dataPluginId);

  function refreshFileTree(dP: DatabaseSettingsDataPluginType) {
    if (dP && dP.id !== undefined) {
      refreshFileList(dP, dispatch);
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
          {fileTrees[filesDataPluginId] ? (
            <FileTreeFolder
              folder={filterFileTree(fileTrees[filesDataPluginId], props.search)}
              foldedOut={true}
              showSelect={true}
              onElementClick={(element, foldOutState) => {
                if (element.type === FileTreeElementTypeType.Folder && foldOutState !== undefined) {
                  dispatch(updateFileListElement({ ...element, foldedOut: foldOutState }));
                }
                if (element.type === FileTreeElementTypeType.File) {
                  showFileTreeElementInfo(element);
                }
              }}
              onShowContextMenu={(e, element) => {
                e.preventDefault();
                e.stopPropagation();
                if (element.type === FileTreeElementTypeType.Folder) {
                  showContextMenu(e.clientX, e.clientY, [
                    {
                      label: 'info',
                      icon: infoIcon,
                      function: () => dispatch(showFileTreeElementInfo(element)),
                    },
                  ]);
                }
                if (element.type === FileTreeElementTypeType.File) {
                  const contextMenuOptions: ContextMenuOption[] = [
                    {
                      label: 'info',
                      icon: infoIcon,
                      function: () => dispatch(showFileTreeElementInfo(element)),
                    },
                  ];

                  if (element.element?.webUrl) {
                    contextMenuOptions.push({
                      label: 'open in browser',
                      icon: openInNewIcon,
                      function: () => window.open(element.element?.webUrl, '_blank'),
                    });
                  }

                  showContextMenu(e.clientX, e.clientY, contextMenuOptions);
                }
              }}
              onElementSelectionChange={(element, selectionState) => {
                dispatch(updateFileListElement({ ...element, checked: selectionState, update: true }));
              }}></FileTreeFolder>
          ) : (
            <span className="loading loading-spinner loading-xs text-accent"></span>
          )}
        </div>
      </div>
    </>
  );
}

export default FileList;
