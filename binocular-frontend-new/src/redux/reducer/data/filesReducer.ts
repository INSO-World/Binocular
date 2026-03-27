import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { FileListElementType, FileTreeElementType } from '../../../types/data/fileListType.ts';
import { updateFileTreeRecursive, writeFileListToStorage } from '../../../components/fileTree/utils/fileTreeUtilities';

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
    return initialState;
  },
  reducers: {
    loadState: (state, action: PayloadAction<FilesInitialState>) => {
      state.fileCounts = action.payload.fileCounts;
      state.fileTrees = action.payload.fileTrees;
      state.fileLists = action.payload.fileLists;
      state.dataPluginId = action.payload.dataPluginId;
    },
    setFileList: (state, action: PayloadAction<{ dataPluginId: number; fileTree: FileTreeElementType; files: FileListElementType[] }>) => {
      state.fileTrees[action.payload.dataPluginId] = action.payload.fileTree;
      state.fileCounts[action.payload.dataPluginId] = action.payload.files.length;
      state.fileLists[action.payload.dataPluginId] = action.payload.files;

      const newState = JSON.stringify(state);
      writeFileListToStorage(newState);
    },
    setFilesDataPluginId: (state, action: PayloadAction<number>) => {
      state.dataPluginId = action.payload;
      const newState = JSON.stringify(state);
      writeFileListToStorage(newState);
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
        writeFileListToStorage(newState);
      }
    },
    showFileTreeElementInfo: (state, action: PayloadAction<FileTreeElementType>) => {
      (document.getElementById('fileTreeElementInfoDialog') as HTMLDialogElement).showModal();
      state.selectedFileTreeElement = action.payload;
    },
    checkAllFiles: (state) => {
      state.fileLists[state.dataPluginId!] = state.fileLists[state.dataPluginId!].map((f: FileListElementType) => {
        f.checked = true;
        return f;
      });
      updateFileTreeRecursive(state.fileTrees[state.dataPluginId!], state.fileTrees[state.dataPluginId!], true);
      const newState = JSON.stringify(state);
      writeFileListToStorage(newState);
    },
    uncheckAllFiles: (state) => {
      updateFileTreeRecursive(state.fileTrees[state.dataPluginId!], state.fileTrees[state.dataPluginId!], false);
      state.fileLists[state.dataPluginId!] = state.fileLists[state.dataPluginId!].map((f: FileListElementType) => {
        f.checked = false;
        return f;
      });
      const newState = JSON.stringify(state);
      writeFileListToStorage(newState);
    },
    switchAllFileSelection: (state) => {
      state.fileLists[state.dataPluginId!] = state.fileLists[state.dataPluginId!].map((f: FileListElementType) => {
        f.checked = !f.checked;
        return f;
      });
      invertFileTreeSelection(state.fileTrees[state.dataPluginId!]);
      const newState = JSON.stringify(state);
      writeFileListToStorage(newState);
    },
    removeFileList: (state, action: PayloadAction<number>) => {
      delete state.fileLists[action.payload];
      delete state.fileTrees[action.payload];
      delete state.fileCounts[action.payload];
      const newState = JSON.stringify(state);
      writeFileListToStorage(newState);
    },
    clearFileStorage: () => {
      clearFileStorage();
    },
  },
});

export const {
  setFilesDataPluginId,
  setFileList,
  updateFileListElement,
  showFileTreeElementInfo,
  removeFileList,
  clearFileStorage,
  loadState,
  checkAllFiles,
  uncheckAllFiles,
  switchAllFileSelection,
} = filesSlice.actions;
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

function invertFileTreeSelection(fileTree: FileTreeElementType) {
  if (fileTree.children) {
    fileTree.children.map((f: FileTreeElementType) => {
      f.checked = !f.checked;
      invertFileTreeSelection(f);
    });
  }
}
