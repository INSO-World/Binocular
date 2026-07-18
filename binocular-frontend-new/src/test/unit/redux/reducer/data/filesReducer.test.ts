import { describe, it, expect, beforeEach, vi } from 'vitest';

vi.mock('../../../../../components/tabs/fileTree/utils/fileListUtilities', () => ({
  writeFileListToStorage: vi.fn(),
  loadFileList: vi.fn(),
  refreshFileList: vi.fn(),
  clearStorage: vi.fn(),
}));

import reducer, {
  loadState,
  setFileList,
  setFilesDataPluginId,
  updateFileListElement,
  checkAllFiles,
  uncheckAllFiles,
  removeFileList,
  switchAllFileSelection,
} from '../../../../../redux/reducer/data/filesReducer';
import type { FilesInitialState } from '../../../../../redux/reducer/data/filesReducer';
import { FileTreeElementTypeType } from '../../../../../types/data/fileListType';
import type { FileListElementType, FileTreeElementType } from '../../../../../types/data/fileListType';
import { writeFileListToStorage } from '../../../../../components/tabs/fileTree/utils/fileListUtilities';

const emptyState: FilesInitialState = {
  fileTrees: {},
  fileLists: {},
  fileCounts: {},
  fileListPluginNames: {},
  dataPluginId: undefined,
};

function makeFile(path: string): FileListElementType {
  return {
    element: { path, webUrl: '', maxLength: 0 },
    checked: true,
  };
}

function makeFileTree(id: number): FileTreeElementType {
  return {
    name: '/',
    id,
    type: FileTreeElementTypeType.Folder,
    children: [],
    checked: true,
    foldedOut: true,
    isRoot: true,
  };
}

beforeEach(() => {
  vi.mocked(writeFileListToStorage).mockClear();
});

describe('filesReducer – loadState', () => {
  it('U27.1 loads all fields from payload', () => {
    const payload: FilesInitialState = {
      fileTrees: { 1: makeFileTree(1) },
      fileLists: { 1: [makeFile('src/a.ts')] },
      fileCounts: { 1: 1 },
      dataPluginId: 1,
    };
    const state = reducer(emptyState, loadState(payload));
    expect(state.fileTrees[1]).toBeDefined();
    expect(state.fileLists[1]).toHaveLength(1);
    expect(state.fileCounts[1]).toBe(1);
    expect(state.dataPluginId).toBe(1);
  });
});

describe('filesReducer – setFileList', () => {
  it('U27.2 stores tree, list, count under dataPluginId', () => {
    const files = [makeFile('src/a.ts'), makeFile('src/b.ts')];
    const tree = makeFileTree(10);
    const state = reducer(emptyState, setFileList({ dataPluginId: 1, pluginName: 'Test', fileTree: tree, files }));
    expect(state.fileTrees[1]).toBe(tree);
    expect(state.fileLists[1]).toHaveLength(2);
    expect(state.fileCounts[1]).toBe(2);
  });

  it('U27.3 calls writeFileListToStorage', () => {
    reducer(emptyState, setFileList({ dataPluginId: 1, pluginName: 'Test', fileTree: makeFileTree(1), files: [] }));
    expect(writeFileListToStorage).toHaveBeenCalledOnce();
  });
});

describe('filesReducer – setFilesDataPluginId', () => {
  it('U27.4 updates dataPluginId', () => {
    const state = reducer(emptyState, setFilesDataPluginId(7));
    expect(state.dataPluginId).toBe(7);
  });

  it('U27.5 calls writeFileListToStorage', () => {
    reducer(emptyState, setFilesDataPluginId(7));
    expect(writeFileListToStorage).toHaveBeenCalledOnce();
  });
});

describe('filesReducer – updateFileListElement', () => {
  const stateWith1: FilesInitialState = {
    fileTrees: {
      1: {
        name: '/',
        id: 99,
        type: FileTreeElementTypeType.Folder,
        children: [
          {
            name: 'a.ts',
            id: 1,
            type: FileTreeElementTypeType.File,
            element: { path: 'src/a.ts', webUrl: '', maxLength: 0 },
            checked: true,
            foldedOut: false,
            isRoot: false,
          },
        ],
        checked: true,
        foldedOut: true,
        isRoot: true,
      },
    },
    fileLists: { 1: [{ element: { path: 'src/a.ts', webUrl: '', maxLength: 0 }, checked: true }] },
    fileCounts: { 1: 1 },
    dataPluginId: 1,
  };

  it('U27.6 update=true toggles checked on matching files', () => {
    const payload: FileTreeElementType & { update?: boolean } = {
      name: 'a.ts',
      id: 1,
      type: FileTreeElementTypeType.File,
      element: { path: 'src/a.ts', webUrl: '', maxLength: 0 },
      checked: false,
      foldedOut: false,
      isRoot: false,
      update: true,
    };
    const state = reducer(stateWith1, updateFileListElement(payload));
    expect(state.fileLists[1][0].checked).toBe(false);
  });

  it('U27.7 update=false does not touch fileList', () => {
    const payload: FileTreeElementType & { update?: boolean } = {
      name: 'a.ts',
      id: 1,
      type: FileTreeElementTypeType.File,
      element: { path: 'src/a.ts', webUrl: '', maxLength: 0 },
      checked: false,
      foldedOut: false,
      isRoot: false,
      update: false,
    };
    const state = reducer(stateWith1, updateFileListElement(payload));
    expect(state.fileLists[1][0].checked).toBe(true);
  });
});

describe('filesReducer – checkAllFiles / uncheckAllFiles', () => {
  const stateWith2: FilesInitialState = {
    fileTrees: { 1: makeFileTree(1) },
    fileLists: {
      1: [
        { element: { path: 'a.ts', webUrl: '', maxLength: 0 }, checked: false },
        { element: { path: 'b.ts', webUrl: '', maxLength: 0 }, checked: false },
      ],
    },
    fileCounts: { 1: 2 },
    dataPluginId: 1,
  };

  it('U27.8 checkAllFiles sets checked = true for every file', () => {
    const state = reducer(stateWith2, checkAllFiles());
    expect(state.fileLists[1].every((f) => f.checked === true)).toBe(true);
  });

  it('U27.9 uncheckAllFiles sets checked = false for every file', () => {
    const stateChecked: FilesInitialState = {
      ...stateWith2,
      fileLists: {
        1: [
          { element: { path: 'a.ts', webUrl: '', maxLength: 0 }, checked: true },
          { element: { path: 'b.ts', webUrl: '', maxLength: 0 }, checked: true },
        ],
      },
    };
    const state = reducer(stateChecked, uncheckAllFiles());
    expect(state.fileLists[1].every((f) => f.checked === false)).toBe(true);
  });
});

describe('filesReducer – removeFileList', () => {
  it('U27.10 deletes fileLists, fileTrees, fileCounts entries for the given id', () => {
    const stateWith: FilesInitialState = {
      fileTrees: { 1: makeFileTree(1), 2: makeFileTree(2) },
      fileLists: { 1: [], 2: [] },
      fileCounts: { 1: 0, 2: 0 },
      fileListPluginNames: { 1: 'A', 2: 'B' },
      dataPluginId: 1,
    };
    const state = reducer(stateWith, removeFileList(1));
    expect(state.fileTrees[1]).toBeUndefined();
    expect(state.fileLists[1]).toBeUndefined();
    expect(state.fileCounts[1]).toBeUndefined();
    // id 2 remains
    expect(state.fileTrees[2]).toBeDefined();
  });
});

describe('filesReducer – updateFileListElement (U61)', () => {
  const stateForU61: FilesInitialState = {
    fileTrees: {
      1: {
        name: '/',
        id: 99,
        type: FileTreeElementTypeType.Folder,
        children: [
          {
            name: 'a.ts',
            id: 1,
            type: FileTreeElementTypeType.File,
            element: { path: 'src/a.ts', webUrl: '', maxLength: 0 },
            checked: true,
            foldedOut: false,
            isRoot: false,
          },
        ],
        checked: true,
        foldedOut: true,
        isRoot: true,
      },
    },
    fileLists: { 1: [{ element: { path: 'src/a.ts', webUrl: '', maxLength: 0 }, checked: true }] },
    fileCounts: { 1: 1 },
    dataPluginId: 1,
  };

  it('U27.11 update=false → tree node changes but flat fileList remains unchanged', () => {
    const payload: FileTreeElementType & { update?: boolean } = {
      name: 'a.ts',
      id: 1,
      type: FileTreeElementTypeType.File,
      element: { path: 'src/a.ts', webUrl: '', maxLength: 0 },
      checked: false,
      foldedOut: false,
      isRoot: false,
      update: false,
    };
    const state = reducer(stateForU61, updateFileListElement(payload));
    // flat list is NOT updated
    expect(state.fileLists[1][0].checked).toBe(true);
    // tree node IS updated
    const treeChild = (state.fileTrees[1].children ?? [])[0];
    expect(treeChild.checked).toBe(false);
  });
});

describe('filesReducer – switchAllFileSelection (extended)', () => {
  it('U27.12 inverts all checked states: checked→unchecked, unchecked→checked', () => {
    const mixedState: FilesInitialState = {
      fileTrees: { 1: makeFileTree(1) },
      fileLists: {
        1: [
          { element: { path: 'a.ts', webUrl: '', maxLength: 0 }, checked: true },
          { element: { path: 'b.ts', webUrl: '', maxLength: 0 }, checked: false },
        ],
      },
      fileCounts: { 1: 2 },
      dataPluginId: 1,
    };
    const state = reducer(mixedState, switchAllFileSelection());
    const list = state.fileLists[1];
    expect(list.find((f) => f.element.path === 'a.ts')?.checked).toBe(false);
    expect(list.find((f) => f.element.path === 'b.ts')?.checked).toBe(true);
  });
});

describe('filesReducer – removeFileList (extended)', () => {
  it('U27.13 removes only the target plugin entry, leaving other plugins intact', () => {
    const stateWith2: FilesInitialState = {
      fileTrees: { 1: makeFileTree(1), 2: makeFileTree(2) },
      fileLists: {
        1: [{ element: { path: 'a.ts', webUrl: '', maxLength: 0 }, checked: true }],
        2: [{ element: { path: 'b.ts', webUrl: '', maxLength: 0 }, checked: true }],
      },
      fileCounts: { 1: 1, 2: 1 },
      fileListPluginNames: { 1: 'A', 2: 'B' },
      dataPluginId: 2,
    };
    const state = reducer(stateWith2, removeFileList(1));
    // plugin 1 entries are removed
    expect(state.fileLists[1]).toBeUndefined();
    expect(state.fileTrees[1]).toBeUndefined();
    expect(state.fileCounts[1]).toBeUndefined();
    // plugin 2 entries remain intact
    expect(state.fileLists[2]).toHaveLength(1);
    expect(state.fileTrees[2]).toBeDefined();
    expect(state.fileCounts[2]).toBe(1);
  });
});
