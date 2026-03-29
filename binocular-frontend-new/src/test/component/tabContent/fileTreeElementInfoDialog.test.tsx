import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';
import type { Middleware } from 'redux';

// Mock the fileTreeUtilities module that has top-level await
vi.mock('../../../components/tabs/fileTree/fileList/fileListUtilities/fileTreeUtilities.tsx', () => ({
  filterFileTree: vi.fn((element) => element),
  generateFileTree: vi.fn(),
  loadFileList: vi.fn(),
  refreshFileList: vi.fn(),
  writeFileListToStorage: vi.fn(),
  clearStorage: vi.fn(),
  formatName: (_: string | undefined, name: string) => [<span key="n">{name}</span>],
}));

// Mock contextMenuHelper
vi.mock('../../../components/contextMenu/contextMenuHelper.ts', () => ({ showContextMenu: vi.fn() }));

import FileTreeElementInfoDialog from '../../../components/tabs/fileTree/fileTreeElementInfoDialog/fileTreeElementInfoDialog.tsx';
import { FileTreeElementTypeType, type FileTreeElementType } from '../../../types/data/fileListType.ts';
import FilesReducer, { type FilesInitialState } from '../../../redux/reducer/data/filesReducer.ts';
import SettingsReducer from '../../../redux/reducer/settings/settingsReducer.ts';
import NotificationsReducer from '../../../redux/reducer/general/notificationsReducer.ts';
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

function createTestStore(filesState?: Partial<FilesInitialState>) {
  return configureStore({
    reducer: reducerMap,
    middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(actionsMiddleware() as Middleware),
    ...(filesState !== undefined
      ? {
          preloadedState: {
            files: {
              fileTrees: {},
              fileLists: {},
              fileCounts: {},
              dataPluginId: undefined,
              ...filesState,
            } as FilesInitialState,
          },
        }
      : {}),
  });
}

function makeFile(): FileTreeElementType {
  return {
    name: 'readme.md',
    type: FileTreeElementTypeType.File,
    checked: true,
    foldedOut: false,
    isRoot: false,
    element: { path: 'src/readme.md', webUrl: 'https://example.com/readme', maxLength: 120 },
  };
}

function makeFolder(): FileTreeElementType {
  return {
    name: 'src',
    type: FileTreeElementTypeType.Folder,
    checked: false,
    foldedOut: true,
    isRoot: false,
    children: [],
  };
}

describe('FileTreeElementInfoDialog', () => {
  beforeEach(() => {
    if (!document.getElementById('fileTreeElementInfoDialog')) {
      const d = document.createElement('dialog');
      d.id = 'fileTreeElementInfoDialog';
      d.showModal = vi.fn();
      document.body.appendChild(d);
    }
  });

  it('C42.1 when selectedFileTreeElement is undefined, no element name heading, type, or path content is shown (only Close button)', () => {
    const store = createTestStore({ selectedFileTreeElement: undefined });

    render(
      <Provider store={store}>
        <FileTreeElementInfoDialog />
      </Provider>,
    );

    // No heading with element name
    expect(document.getElementById('informationDialogHeadline')).toBeNull();
    // No type or path content rendered
    expect(screen.queryByText('File')).not.toBeInTheDocument();
    expect(screen.queryByText('Folder')).not.toBeInTheDocument();
    expect(screen.queryByText('src/readme.md')).not.toBeInTheDocument();

    // Two close buttons always present (styled + backdrop form)
    expect(screen.getAllByRole('button', { name: /close/i, hidden: true })).toHaveLength(2);
  });

  it('C42.2 File element: name "readme.md" appears as heading; "src/readme.md" appears as path; link to webUrl appears', () => {
    const store = createTestStore({ selectedFileTreeElement: makeFile() });

    render(
      <Provider store={store}>
        <FileTreeElementInfoDialog />
      </Provider>,
    );

    // Name in heading
    expect(screen.getByRole('heading', { name: 'readme.md', hidden: true })).toBeInTheDocument();
    // Path shown
    expect(screen.getByText('src/readme.md')).toBeInTheDocument();
    // Web URL as link
    const link = screen.getByRole('link', { hidden: true });
    expect(link).toHaveAttribute('href', 'https://example.com/readme');
  });

  it('C42.3 foldedOut: false → "folded in" badge; foldedOut: true → "folded out" badge', () => {
    // foldedOut: false → "folded in"
    const storeFoldedIn = createTestStore({ selectedFileTreeElement: { ...makeFile(), foldedOut: false } });
    const { unmount } = render(
      <Provider store={storeFoldedIn}>
        <FileTreeElementInfoDialog />
      </Provider>,
    );
    expect(screen.getByText('folded in')).toBeInTheDocument();
    expect(screen.queryByText('folded out')).not.toBeInTheDocument();
    unmount();

    // foldedOut: true → "folded out"
    const storeFoldedOut = createTestStore({ selectedFileTreeElement: { ...makeFile(), foldedOut: true } });
    render(
      <Provider store={storeFoldedOut}>
        <FileTreeElementInfoDialog />
      </Provider>,
    );
    expect(screen.getByText('folded out')).toBeInTheDocument();
    expect(screen.queryByText('folded in')).not.toBeInTheDocument();
  });

  it('C42.4 checked: false → "unchecked" badge; checked: true → "checked" badge', () => {
    // checked: false → "unchecked"
    const storeUnchecked = createTestStore({ selectedFileTreeElement: { ...makeFile(), checked: false } });
    const { unmount } = render(
      <Provider store={storeUnchecked}>
        <FileTreeElementInfoDialog />
      </Provider>,
    );
    expect(screen.getByText('unchecked')).toBeInTheDocument();
    expect(screen.queryByText('checked')).not.toBeInTheDocument();
    unmount();

    // checked: true → "checked"
    const storeChecked = createTestStore({ selectedFileTreeElement: { ...makeFile(), checked: true } });
    render(
      <Provider store={storeChecked}>
        <FileTreeElementInfoDialog />
      </Provider>,
    );
    expect(screen.getByText('checked')).toBeInTheDocument();
    expect(screen.queryByText('unchecked')).not.toBeInTheDocument();
  });

  it('C42.5 Folder element: "Folder Content" text appears; Path heading and webUrl link are absent', () => {
    const folderElement = makeFolder();
    const store = createTestStore({
      selectedFileTreeElement: folderElement,
      fileTrees: { 1: folderElement },
      dataPluginId: 1,
    });

    render(
      <Provider store={store}>
        <FileTreeElementInfoDialog />
      </Provider>,
    );

    // "Folder Content" section heading should be present
    expect(screen.getByText('Folder Content')).toBeInTheDocument();

    // File-specific sections (Path, webUrl link) should NOT be present
    expect(screen.queryByText('Path')).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { hidden: true })).not.toBeInTheDocument();
  });
});
