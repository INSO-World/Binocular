import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';

// Mock the fileTreeUtilities module that has top-level await
vi.mock('../../../components/tabs/fileTree/fileList/fileListUtilities/fileTreeUtilities.tsx', () => ({
  formatName: (_searchTerm: string | undefined, name: string) => [<span key="name">{name}</span>],
  generateFileTree: vi.fn(),
  filterFileTree: vi.fn(),
  loadFileList: vi.fn(),
  refreshFileList: vi.fn(),
  writeFileListToStorage: vi.fn(),
  clearStorage: vi.fn(),
}));

// Mock contextMenuHelper
vi.mock('../../../components/contextMenu/contextMenuHelper.ts', () => ({
  showContextMenu: vi.fn(),
}));

import FileListFolder from '../../../components/tabs/fileTree/fileList/fileListElements/fileListFolder.tsx';
import { FileTreeElementTypeType, type FileTreeElementType } from '../../../types/data/fileListType.ts';
import FilesReducer from '../../../redux/reducer/data/filesReducer.ts';
import NotificationsReducer from '../../../redux/reducer/general/notificationsReducer.ts';
import SettingsReducer from '../../../redux/reducer/settings/settingsReducer.ts';
import DashboardReducer from '../../../redux/reducer/general/dashboardReducer.ts';
import AuthorsReducer from '../../../redux/reducer/data/authorsReducer.ts';
import AccountsReducer from '../../../redux/reducer/data/accountsReducer.ts';
import ParametersReducer from '../../../redux/reducer/parameters/parametersReducer.ts';
import SprintsReducer from '../../../redux/reducer/data/sprintsReducer.ts';
import ExportReducer from '../../../redux/reducer/export/exportReducer.ts';
import TabsReducer from '../../../redux/reducer/general/tabsReducer.ts';
import ActionsReducer from '../../../redux/reducer/general/actionsReducer.ts';
import LayoutReducer from '../../../redux/reducer/general/layoutReducer.ts';
import actionsMiddleware from '../../../redux/middleware/actions/actionsMiddleware.ts';
import type { Middleware } from 'redux';

const reducerMap = {
  dashboard: DashboardReducer,
  layout: LayoutReducer,
  authors: AuthorsReducer,
  accounts: AccountsReducer,
  files: FilesReducer,
  settings: SettingsReducer,
  export: ExportReducer,
  parameters: ParametersReducer,
  sprints: SprintsReducer,
  notifications: NotificationsReducer,
  tabs: TabsReducer,
  actions: ActionsReducer,
};

function createTestStore() {
  return configureStore({
    reducer: reducerMap,
    middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(actionsMiddleware() as Middleware),
  });
}

const childFile: FileTreeElementType = {
  name: 'index.ts',
  type: FileTreeElementTypeType.File,
  checked: true,
  foldedOut: false,
  isRoot: false,
  element: { path: 'components/index.ts', webUrl: '', maxLength: 0 },
};

const subFolder: FileTreeElementType = {
  name: 'subFolder',
  type: FileTreeElementTypeType.Folder,
  checked: true,
  foldedOut: false,
  isRoot: false,
  children: [childFile],
};

const folderElement: FileTreeElementType = {
  name: 'components',
  type: FileTreeElementTypeType.Folder,
  checked: true,
  foldedOut: false,
  isRoot: false,
  children: [childFile],
};

describe('FileListFolder', () => {
  let store: ReturnType<typeof createTestStore>;

  beforeEach(() => {
    store = createTestStore();
    // showFileTreeElementInfo reducer calls document.getElementById('fileTreeElementInfoDialog').showModal()
    // Provide a stub dialog element to prevent errors in tests that trigger that action
    if (!document.getElementById('fileTreeElementInfoDialog')) {
      const dialog = document.createElement('dialog');
      dialog.id = 'fileTreeElementInfoDialog';
      dialog.showModal = vi.fn();
      document.body.appendChild(dialog);
    }
  });

  it('C8.1 renders the folder name', () => {
    render(
      <Provider store={store}>
        <FileListFolder folder={folderElement} foldedOut={false} />
      </Provider>,
    );
    expect(screen.getByText('components')).toBeInTheDocument();
  });

  it('C8.2 children are hidden when folder is collapsed (foldedOut: false)', () => {
    render(
      <Provider store={store}>
        <FileListFolder folder={folderElement} foldedOut={false} />
      </Provider>,
    );
    // When collapsed, the child file name should not be visible
    expect(screen.queryByText('index.ts')).not.toBeInTheDocument();
  });

  it('C8.3 children are visible when folder is expanded (foldedOut: true)', () => {
    render(
      <Provider store={store}>
        <FileListFolder folder={{ ...folderElement, foldedOut: true }} foldedOut={true} />
      </Provider>,
    );
    expect(screen.getByText('index.ts')).toBeInTheDocument();
  });

  it('C8.4 folder with foldedOut false shows collapsed state', () => {
    render(
      <Provider store={store}>
        <FileListFolder folder={{ ...folderElement, foldedOut: false }} foldedOut={false} />
      </Provider>,
    );
    // Collapsed folder shows its name but not children
    expect(screen.getByText('components')).toBeInTheDocument();
    expect(screen.queryByText('index.ts')).not.toBeInTheDocument();
  });

  it('C8.5 renders nested folder when expanded', () => {
    const nestedFolder: FileTreeElementType = {
      name: 'parent',
      type: FileTreeElementTypeType.Folder,
      checked: true,
      foldedOut: true,
      isRoot: false,
      children: [{ ...subFolder, foldedOut: false }],
    };

    render(
      <Provider store={store}>
        <FileListFolder folder={nestedFolder} foldedOut={true} />
      </Provider>,
    );
    // parent is expanded so we see its children (subFolder)
    expect(screen.getByText('subFolder')).toBeInTheDocument();
  });

  it('C8.6 foldedOut=false → children NOT rendered', () => {
    render(
      <Provider store={store}>
        <FileListFolder folder={{ ...folderElement, foldedOut: false }} foldedOut={false} />
      </Provider>,
    );
    expect(screen.queryByText('index.ts')).not.toBeInTheDocument();
  });

  it('C8.7 foldedOut=true → children ARE rendered', () => {
    render(
      <Provider store={store}>
        <FileListFolder folder={{ ...folderElement, foldedOut: true }} foldedOut={true} />
      </Provider>,
    );
    expect(screen.getByText('index.ts')).toBeInTheDocument();
  });

  it('C8.8 clicking collapsed folder dispatches updateFileListElement with foldedOut: true', () => {
    const dispatchSpy = vi.fn();
    const testStore = createTestStore();
    const originalDispatch = testStore.dispatch.bind(testStore);
    testStore.dispatch = ((action: Parameters<typeof originalDispatch>[0]) => {
      dispatchSpy(action);
      try {
        return originalDispatch(action);
      } catch {
        return action as ReturnType<typeof originalDispatch>;
      }
    }) as typeof testStore.dispatch;

    render(
      <Provider store={testStore}>
        <FileListFolder folder={{ ...folderElement, foldedOut: false }} foldedOut={false} />
      </Provider>,
    );

    // In collapsed state the folder name element is clickable
    const folderNameEl = screen.getByText('components');
    fireEvent.click(folderNameEl);

    const updateCalls = dispatchSpy.mock.calls
      .map((call) => call[0] as { type: string; payload: FileTreeElementType & { foldedOut: boolean } })
      .filter((action) => action.type === 'files/updateFileListElement');

    expect(updateCalls.length).toBeGreaterThan(0);
    expect(updateCalls[0].payload.foldedOut).toBe(true);
  });

  it('C8.9 clicking expanded folder dispatches updateFileListElement with foldedOut: false', () => {
    const dispatchSpy = vi.fn();
    const testStore = createTestStore();
    const originalDispatch = testStore.dispatch.bind(testStore);
    testStore.dispatch = ((action: Parameters<typeof originalDispatch>[0]) => {
      dispatchSpy(action);
      try {
        return originalDispatch(action);
      } catch {
        return action as ReturnType<typeof originalDispatch>;
      }
    }) as typeof testStore.dispatch;

    render(
      <Provider store={testStore}>
        <FileListFolder folder={{ ...folderElement, foldedOut: true }} foldedOut={true} />
      </Provider>,
    );

    // In expanded state, the folder name element is clickable
    const folderNameEl = screen.getByText('components');
    fireEvent.click(folderNameEl);

    const updateCalls = dispatchSpy.mock.calls
      .map((call) => call[0] as { type: string; payload: FileTreeElementType & { foldedOut: boolean } })
      .filter((action) => action.type === 'files/updateFileListElement');

    expect(updateCalls.length).toBeGreaterThan(0);
    expect(updateCalls[0].payload.foldedOut).toBe(false);
  });

  it('C8.10 listOnly=true always shows children regardless of foldedOut=false', () => {
    render(
      <Provider store={store}>
        <FileListFolder folder={{ ...folderElement, foldedOut: false }} foldedOut={false} listOnly={true} />
      </Provider>,
    );
    // listOnly forces the expanded branch, so children must be visible
    expect(screen.getByText('index.ts')).toBeInTheDocument();
  });

  it('C8.11 folder with id === undefined has no checkbox when expanded', () => {
    const folderWithoutId: FileTreeElementType = {
      name: 'noIdFolder',
      // id is intentionally omitted → undefined
      type: FileTreeElementTypeType.Folder,
      checked: true,
      foldedOut: true,
      isRoot: false,
      children: [],
    };

    render(
      <Provider store={store}>
        <FileListFolder folder={folderWithoutId} foldedOut={true} />
      </Provider>,
    );

    // In the expanded branch, checkbox is only rendered when !listOnly && id !== undefined
    expect(screen.queryByRole('checkbox')).not.toBeInTheDocument();
  });

  it('C8.12 checking folder checkbox dispatches updateFileListElement with updated checked', () => {
    const dispatchSpy = vi.fn();
    const testStore = createTestStore();
    const originalDispatch = testStore.dispatch.bind(testStore);
    testStore.dispatch = ((action: Parameters<typeof originalDispatch>[0]) => {
      dispatchSpy(action);
      try {
        return originalDispatch(action);
      } catch {
        return action as ReturnType<typeof originalDispatch>;
      }
    }) as typeof testStore.dispatch;

    // Use a collapsed folder (id present, not listOnly) — the collapsed branch always shows a checkbox
    const collapsedFolder: FileTreeElementType = {
      name: 'myFolder',
      id: 42,
      type: FileTreeElementTypeType.Folder,
      checked: false,
      foldedOut: false,
      isRoot: false,
      children: [],
    };

    render(
      <Provider store={testStore}>
        <FileListFolder folder={collapsedFolder} foldedOut={false} />
      </Provider>,
    );

    const checkbox = screen.getByRole('checkbox');
    fireEvent.click(checkbox);

    const updateCalls = dispatchSpy.mock.calls
      .map((call) => call[0] as { type: string; payload: FileTreeElementType & { checked: boolean; update: boolean } })
      .filter((action) => action.type === 'files/updateFileListElement');

    expect(updateCalls.length).toBeGreaterThan(0);
    // The onChange handler passes e.target.checked — after clicking an unchecked box it becomes true
    expect(updateCalls[0].payload.checked).toBe(true);
  });
});
