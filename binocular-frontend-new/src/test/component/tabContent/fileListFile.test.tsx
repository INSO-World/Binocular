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

import FileListFile from '../../../components/tabs/fileTree/fileList/fileListElements/fileListFile.tsx';
import { FileTreeElementTypeType } from '../../../types/data/fileListType.ts';
import FilesReducer from '../../../redux/reducer/data/filesReducer.ts';
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

describe('FileListFile', () => {
  let store: ReturnType<typeof createTestStore>;

  beforeEach(() => {
    store = createTestStore();
  });

  it('C7.1 renders the file name', () => {
    render(
      <Provider store={store}>
        <FileListFile file={fileElement} />
      </Provider>,
    );
    expect(screen.getByText('foo.ts')).toBeInTheDocument();
  });

  it('C7.2 checkbox is checked when file.checked is true', () => {
    render(
      <Provider store={store}>
        <FileListFile file={{ ...fileElement, checked: true }} />
      </Provider>,
    );
    expect(screen.getByRole('checkbox')).toBeChecked();
  });

  it('C7.3 checkbox is unchecked when file.checked is false', () => {
    render(
      <Provider store={store}>
        <FileListFile file={{ ...fileElement, checked: false }} />
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
        <FileListFile file={fileElement} />
      </Provider>,
    );
    const checkbox = screen.getByRole('checkbox');
    fireEvent.click(checkbox);
    // The dispatch happens; verify the file checked state was toggled in store
    expect(storeWithFileTree.getState().files.fileLists[1][0].checked).toBe(false);
  });
});
