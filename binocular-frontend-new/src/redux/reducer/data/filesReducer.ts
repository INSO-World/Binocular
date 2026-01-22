import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { FileListElementType, FileTreeElementType } from '../../../types/data/fileListType.ts';

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

const opfsRoot = await navigator.storage.getDirectory();
const fileHandle = await opfsRoot.getFileHandle('files', { create: true });

export const filesSlice = createSlice({
  name: 'files',
  initialState: () => {
    fileHandle.getFile().then((files) => {
      if (files !== null) {
        files.text().then((list) => setFileList(JSON.parse(list)));
      }
    });
    return initialState;
  },
  reducers: {
    loadState: (state, action: PayloadAction<FilesInitialState>) => {
      state.fileCounts = action.payload.fileCounts;
      state.fileTrees = action.payload.fileTrees;
      state.fileLists = action.payload.fileLists;
    },
    setFileList: (state, action: PayloadAction<{ dataPluginId: number; fileTree: FileTreeElementType; files: FileListElementType[] }>) => {
      const fileCount: number = state.fileCounts[action.payload.dataPluginId];
      if (fileCount === undefined || fileCount !== action.payload.files.length) {
        state.fileTrees[action.payload.dataPluginId] = action.payload.fileTree;
        state.fileCounts[action.payload.dataPluginId] = action.payload.files.length;
        state.fileLists[action.payload.dataPluginId] = action.payload.files;
      }
      const data = JSON.stringify(state);
      console.log('writing fileList');
      fileHandle.createWritable().then((access) => access.write(data).then(() => access.close()));
    },
    setFilesDataPluginId: (state, action: PayloadAction<number>) => {
      state.dataPluginId = action.payload;
      //localStorage.setItem(`${filesSlice.name}StateV${Config.localStorageVersion}`, JSON.stringify(state));
    },
    updateFileListElement: (state, action: PayloadAction<FileTreeElementType & { update?: boolean }>) => {
      const updatedPaths: string[] = updateFileTreeRecursive(state.fileTrees[state.dataPluginId!], action.payload);
      if (action.payload.update) {
        state.fileLists[state.dataPluginId!] = state.fileLists[state.dataPluginId!].map((f: FileListElementType) => {
          if (updatedPaths.includes(f.element.path)) {
            f.checked = action.payload.checked;
          }
          return f;
        });
        const newState = JSON.stringify(state);
        fileHandle.createWritable().then((access) => access.write(newState).then(() => access.close()));
      }
    },
    showFileTreeElementInfo: (state, action: PayloadAction<FileTreeElementType>) => {
      (document.getElementById('fileTreeElementInfoDialog') as HTMLDialogElement).showModal();
      state.selectedFileTreeElement = action.payload;
    },
    clearFileStorage: () => {
      //localStorage.removeItem(`${filesSlice.name}StateV${Config.localStorageVersion}`);
    },
  },
});

export const { setFilesDataPluginId, setFileList, updateFileListElement, showFileTreeElementInfo, clearFileStorage, loadState } =
  filesSlice.actions;
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
