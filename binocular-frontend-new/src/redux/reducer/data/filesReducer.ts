import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import Config from '../../../config.ts';
import { FileListElementType, FileTreeElementType } from '../../../types/data/fileListType.ts';

export interface FilesInitialState {
  fileTrees: { [id: number]: FileTreeElementType };
  fileLists: { [id: number]: FileListElementType[] };
  fileCounts: { [id: number]: number };
  dataPluginId: number | undefined;
  selectedFileTreeElement?: FileTreeElementType;
}

const initialState: FilesInitialState = {
  fileTrees: {},
  fileLists: {},
  fileCounts: {},
  dataPluginId: undefined,
  selectedFileTreeElement: undefined,
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
    setFileList: (state, action: PayloadAction<{ dataPluginId: number; fileTree: FileTreeElementType; files: FileListElementType[] }>) => {
      const fileCount: number = state.fileCounts[action.payload.dataPluginId];
      if (fileCount === undefined || fileCount !== action.payload.files.length) {
        state.fileTrees[action.payload.dataPluginId] = action.payload.fileTree;
        state.fileCounts[action.payload.dataPluginId] = action.payload.files.length;
        state.fileLists[action.payload.dataPluginId] = action.payload.files;
      }
      localStorage.setItem(`${filesSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    setFilesDataPluginId: (state, action: PayloadAction<number>) => {
      state.dataPluginId = action.payload;
      localStorage.setItem(`${filesSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    updateFileListElement: (state, action: PayloadAction<FileTreeElementType & { update?: boolean }>) => {
      const updatedPaths: string[] = updateFileTreeRecursive(state.fileTrees[state.dataPluginId], action.payload);
      if (action.payload.update) {
        state.fileLists[state.dataPluginId] = state.fileLists[state.dataPluginId].map((f: FileListElementType) => {
          if (updatedPaths.includes(f.element.path)) {
            f.checked = action.payload.checked;
          }
          return f;
        });
      }
      localStorage.setItem(`${filesSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    showFileTreeElementInfo: (state, action: PayloadAction<FileTreeElementType>) => {
      (document.getElementById('fileTreeElementInfoDialog') as HTMLDialogElement).showModal();
      state.selectedFileTreeElement = action.payload;
    },
  },
});

export const { setFilesDataPluginId, setFileList, updateFileListElement, showFileTreeElementInfo } = filesSlice.actions;
export default filesSlice.reducer;

function updateFileTreeRecursive(fileTree: FileTreeElementType, element: FileTreeElementType, checked?: boolean): string[] {
  const updatedPaths: string[] = [];
  if (fileTree.children) {
    fileTree.children = fileTree.children.map((f: FileTreeElementType) => {
      let elementChecked = checked;
      if (f.id === element.id) {
        if (f.element?.path && !updatedPaths.includes(f.element.path)) {
          updatedPaths.push(f.element.path);
        }
        elementChecked = element.checked;
        f.foldedOut = element.foldedOut;
      }
      if (elementChecked !== undefined) {
        if (f.element?.path && !updatedPaths.includes(f.element.path)) {
          updatedPaths.push(f.element.path);
        }
        f.checked = elementChecked;
      }
      updatedPaths.push(...updateFileTreeRecursive(f, element, elementChecked));
      return f;
    });
  }
  return updatedPaths;
}
