import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';

// Mock the fileTreeUtilities modules
vi.mock('../../../components/tabs/fileTree/utils/fileListUtilities.tsx', () => ({
  loadFileList: vi.fn(),
  refreshFileList: vi.fn(),
  writeFileListToStorage: vi.fn(),
  clearStorage: vi.fn(),
}));

// Mock contextMenuHelper
vi.mock('../../../components/contextMenu/contextMenuHelper.ts', () => ({
  showContextMenu: vi.fn(),
}));

import FileTreeFile from '../../../components/fileTree/fileTreeElements/fileTreeFile/fileTreeFile.tsx';
import { FileTreeElementTypeType } from '../../../types/data/fileListType.ts';
import FilesReducer, { updateFileListElement, showFileTreeElementInfo } from '../../../redux/reducer/data/filesReducer.ts';
import type { FilesInitialState } from '../../../redux/reducer/data/filesReducer.ts';
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

const fileElement = {
  name: 'foo.ts',
  id: 1,
  type: FileTreeElementTypeType.File,
  checked: true,
  foldedOut: false,
  isRoot: false,
  element: { path: 'src/utils/foo.ts', webUrl: '', maxLength: 0 },
};

describe('FileTreeFile', () => {
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

  it('C7.1 renders the file name', () => {
    render(
      <Provider store={store}>
        <FileTreeFile file={fileElement} showSelect={false} />
      </Provider>,
    );
    expect(screen.getByText('foo.ts')).toBeInTheDocument();
  });

  it('C7.2 checkbox is checked when file.checked is true', () => {
    render(
      <Provider store={store}>
        <FileTreeFile file={{ ...fileElement, checked: true }} showSelect={true} />
      </Provider>,
    );
    expect(screen.getByRole('checkbox')).toBeChecked();
  });

  it('C7.3 checkbox is unchecked when file.checked is false', () => {
    render(
      <Provider store={store}>
        <FileTreeFile file={{ ...fileElement, checked: false }} showSelect={true} />
      </Provider>,
    );
    expect(screen.getByRole('checkbox')).not.toBeChecked();
  });

  it('C7.4 clicking checkbox calls onToggle (dispatches updateFileListElement action)', () => {
    // Set up the store with a proper file tree so updateFileListElement doesn't throw
    const storeWithFileTree = configureStore({
      reducer: reducerMap,
      preloadedState: {
        files: {
          fileTrees: {
            1: {
              name: '/',
              id: 0,
              type: FileTreeElementTypeType.Folder,
              checked: true,
              foldedOut: true,
              isRoot: true,
              children: [fileElement],
            },
          },
          fileLists: {
            1: [{ element: { path: 'src/utils/foo.ts', webUrl: '', maxLength: 0 }, checked: true }],
          },
          fileCounts: { 1: 1 },
          dataPluginId: 1,
        } as FilesInitialState,
      },
    });

    render(
      <Provider store={storeWithFileTree}>
        <FileTreeFile
          file={fileElement}
          showSelect={true}
          onElementSelectionChange={(element, checked) => {
            storeWithFileTree.dispatch(updateFileListElement({ ...element, checked, update: true }));
          }}
        />
      </Provider>,
    );
    const checkbox = screen.getByRole('checkbox');
    fireEvent.click(checkbox);
    // The dispatch happens; verify the file checked state was toggled in store
    expect(storeWithFileTree.getState().files.fileLists[1][0].checked).toBe(false);
  });

  it('C7.5 without listOnly, checkbox is rendered', () => {
    render(
      <Provider store={store}>
        <FileTreeFile file={fileElement} showSelect={true} />
      </Provider>,
    );
    expect(screen.getByRole('checkbox')).toBeInTheDocument();
  });

  it('C7.6 with listOnly=true, checkbox is NOT rendered', () => {
    render(
      <Provider store={store}>
        <FileTreeFile file={fileElement} listOnly={true} showSelect={true} />
      </Provider>,
    );
    expect(screen.queryByRole('checkbox')).not.toBeInTheDocument();
  });

  it('C7.7 checking the checkbox dispatches updateFileListElement with checked: true, update: true', () => {
    const storeWithFileTree = configureStore({
      reducer: reducerMap,
      preloadedState: {
        files: {
          fileTrees: {
            1: {
              name: '/',
              id: 0,
              type: FileTreeElementTypeType.Folder,
              checked: true,
              foldedOut: true,
              isRoot: true,
              children: [{ ...fileElement, checked: false }],
            },
          },
          fileLists: {
            1: [{ element: { path: 'src/utils/foo.ts', webUrl: '', maxLength: 0 }, checked: false }],
          },
          fileCounts: { 1: 1 },
          dataPluginId: 1,
        } as FilesInitialState,
      },
    });

    render(
      <Provider store={storeWithFileTree}>
        <FileTreeFile
          file={{ ...fileElement, checked: false }}
          showSelect={true}
          onElementSelectionChange={(element, checked) => {
            storeWithFileTree.dispatch(updateFileListElement({ ...element, checked, update: true }));
          }}
        />
      </Provider>,
    );
    const checkbox = screen.getByRole('checkbox');
    // Fire change event with checked: true to simulate checking the box
    fireEvent.click(checkbox);
    // After toggling false → true, fileList entry should now be checked: true
    expect(storeWithFileTree.getState().files.fileLists[1][0].checked).toBe(true);
  });

  it('C7.8 clicking the element with listOnly=true dispatches showFileTreeElementInfo', () => {
    const dispatchSpy = vi.fn();
    // Patch store.dispatch to spy on dispatched actions
    const testStore = createTestStore();
    const originalDispatch = testStore.dispatch.bind(testStore);
    testStore.dispatch = ((action: Parameters<typeof originalDispatch>[0]) => {
      dispatchSpy(action);
      return originalDispatch(action);
    }) as typeof testStore.dispatch;

    render(
      <Provider store={testStore}>
        <FileTreeFile
          file={fileElement}
          listOnly={true}
          showSelect={false}
          onElementClick={(element) => {
            testStore.dispatch(showFileTreeElementInfo(element));
          }}
        />
      </Provider>,
    );

    // The clickable element wraps the file icon and name
    const fileNameEl = screen.getByText('foo.ts');
    fireEvent.click(fileNameEl);

    const actionTypes = dispatchSpy.mock.calls.map((call) => (call[0] as { type: string }).type);
    expect(actionTypes).toContain('files/showFileTreeElementInfo');
  });

  it('C7.9 clicking the element without listOnly does NOT dispatch showFileTreeElementInfo', () => {
    const dispatchSpy = vi.fn();
    const testStore = createTestStore();
    const originalDispatch = testStore.dispatch.bind(testStore);
    testStore.dispatch = ((action: Parameters<typeof originalDispatch>[0]) => {
      dispatchSpy(action);
      return originalDispatch(action);
    }) as typeof testStore.dispatch;

    render(
      <Provider store={testStore}>
        <FileTreeFile file={fileElement} showSelect={false} />
      </Provider>,
    );

    const fileNameEl = screen.getByText('foo.ts');
    fireEvent.click(fileNameEl);

    const actionTypes = dispatchSpy.mock.calls.map((call) => (call[0] as { type: string }).type);
    expect(actionTypes).not.toContain('files/showFileTreeElementInfo');
  });
});
