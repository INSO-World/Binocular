import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import Config from '../../../config.ts';
import { FileListElementType } from '../../../types/data/fileListType.ts';

export interface FilesInitialState {
  fileLists: { [id: number]: FileListElementType };
  fileCounts: { [id: number]: number };
  dataPluginId: number | undefined;
}

const initialState: FilesInitialState = {
  fileLists: {},
  fileCounts: {},
  dataPluginId: undefined,
};

export const filesSlice = createSlice({
  name: 'files',
  initialState: () => {
    const storedState = localStorage.getItem(`${filesSlice.name}StateV${Config.localStorageVersion}`);
    if (storedState === null) {
      localStorage.setItem(`${filesSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(initialState));
      return initialState;
    } else {
      return JSON.parse(storedState);
    }
  },
  reducers: {
    setFileList: (state, action: PayloadAction<{ dataPluginId: number; files: FileListElementType; fileCount: number }>) => {
      const fileCount: number = state.fileCounts[action.payload.dataPluginId];
      if (fileCount === undefined || fileCount !== action.payload.fileCount) {
        state.fileLists[action.payload.dataPluginId] = action.payload.files;
        state.fileCounts[action.payload.dataPluginId] = action.payload.fileCount;
      }
      localStorage.setItem(`${filesSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    setFilesDataPluginId: (state, action: PayloadAction<number>) => {
      state.dataPluginId = action.payload;
      localStorage.setItem(`${filesSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    updateFileListElement: (state, action: PayloadAction<FileListElementType>) => {
      state.fileLists[state.dataPluginId] = updateFileListElementRecursive(state.fileLists[state.dataPluginId], action.payload);

      localStorage.setItem(`${filesSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
  },
});

export const { setFilesDataPluginId, setFileList, updateFileListElement } = filesSlice.actions;
export default filesSlice.reducer;

function updateFileListElementRecursive(
  fileList: FileListElementType,
  element: FileListElementType,
  checked?: boolean,
): FileListElementType {
  if (fileList.children) {
    fileList.children = fileList.children.map((f: FileListElementType) => {
      let elementChecked = checked;
      if (f.id === element.id) {
        elementChecked = element.checked;
        f.foldedOut = element.foldedOut;
      }
      if (elementChecked !== undefined) {
        f.checked = elementChecked;
      }
      updateFileListElementRecursive(f, element, elementChecked);
      return f;
    });
  }
  return fileList;
}
