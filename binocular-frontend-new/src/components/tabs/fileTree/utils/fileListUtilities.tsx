import type { DataPluginFile } from '../../../../plugins/interfaces/dataPluginInterfaces/dataPluginFiles';
import { FileTreeElementTypeType } from '../../../../types/data/fileListType';
import type { DatabaseSettingsDataPluginType } from '../../../../types/settings/databaseSettingsType';
import type { AppDispatch } from '../../../../redux';
import DataPluginStorage from '../../../../utils/dataPluginStorage';
import { setFileList } from '../../../../redux/reducer/data/filesReducer';
import { generateFileTree } from '../../../fileTree/utils/fileTreeUtilities';

export function refreshFileList(dP: DatabaseSettingsDataPluginType, dispatch: AppDispatch) {
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
            .catch((e) => console.log('Error loading Files from selected data source!', e));
        }
      })
      .catch((e) => console.log(e));
  }
}
